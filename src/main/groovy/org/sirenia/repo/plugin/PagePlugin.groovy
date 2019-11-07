package org.sirenia.repo.plugin

import org.apache.ibatis.executor.statement.BaseStatementHandler
import org.apache.ibatis.executor.statement.RoutingStatementHandler
import org.apache.ibatis.executor.statement.StatementHandler
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Plugin
import org.apache.ibatis.plugin.Signature
import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.SystemMetaObject
import org.sirenia.repo.util.ParamObjectUtil
import org.sirenia.repo.util.ReflectUtil
import java.sql.Connection

@Intercepts([
        @Signature(type = StatementHandler, method = "prepare", args = [Connection, Integer])])
class PagePlugin implements Interceptor {
    private String dialect = "" // 数据库方言

    private String pageSqlId = "" // mapper.xml中需要拦截的ID(正则匹配)

    Object intercept(Invocation ivk) {
        boolean targetIsRoutingStatementHandler = ivk.getTarget() in RoutingStatementHandler
        if (!targetIsRoutingStatementHandler) {
            return ivk.proceed()
        }
        RoutingStatementHandler statementHandler = ivk.getTarget()
        BaseStatementHandler delegate = ReflectUtil.getValueByFieldName(statementHandler, "delegate")
        MappedStatement mappedStatement = ReflectUtil.getValueByFieldName(delegate, "mappedStatement")
        // 拦截需要分页的SQL
        boolean needPagination = mappedStatement.getId().matches(pageSqlId)
        if (!needPagination) {
            return ivk.proceed()
        }
        return queryByPage(ivk, statementHandler, delegate)
    }

    private Object queryByPage(Invocation ivk, RoutingStatementHandler statementHandler, BaseStatementHandler delegate) {
        BoundSql boundSql = delegate.boundSql
        // 分页SQL<select>中parameterType属性对应的实体参数，即Mapper接口中执行分页方法的参数,该参数不得为空
        Object origParamObject = getParameterObject(boundSql)

        Connection connection = ivk.args[0]
        String sql = boundSql.sql
        //如果sql最后带有分号，则去掉分号
        String trimedSql = trimSql(sql)
        String countSql = "select count(0) from (${trimedSql}) as tmp_count" // 记录统计
        def countStmt = connection.prepareStatement(countSql)
        statementHandler.getParameterHandler().setParameters(countStmt)
        def rs = countStmt.executeQuery()
        int count = 0
        if (rs.next()) {
            count = rs.getInt(1)
        }
        rs.close()
        countStmt.close()
        /*这里通过duck类型，增强灵活性。比如web层的dto传到server层，
         * 如果不使用duck类型，就需要将dto转成该插件需要的Pageable类型。
         * 这样会增加大量的繁琐的重复代码。
         * 或者可以在web层依赖该模块（就为了用其中的一个类就需要依赖一个jar包么？）。
         * 所以，只要按照约定具有IndexPage或LimitPage相同的getter和setter，就可以实现分页。
         * */
        MetaObject page = findPageObject(origParamObject)
        page.setValue("total", count)
        int limit = 0
        int offset = 0
        if (page.hasGetter("limit") && page.hasGetter("offset")) {
            limit = (int) page.getValue("limit")
            offset = (int) page.getValue("offset")
        } else {
            int index = (int) page.getValue("index")
            int size = (int) page.getValue("size")
            limit = size
            offset = (index - 1) * size
        }
        String pageSql = createPageSql(trimedSql, dialect, limit, offset)
        ReflectUtil.setValueByFieldName(boundSql, "sql", pageSql) // 将分页sql语句反射回BoundSql.
        return ivk.proceed()
    }

    private Object getParameterObject(BoundSql boundSql) {
        Object parameterObject = boundSql.parameterObject
        if (parameterObject == null) {
            throw new IllegalArgumentException("parameterObject尚未实例化！")
        }
        //兼容加解密插件 开始
        Object origParamObject = ParamObjectUtil.getParamObject()
        if (origParamObject == null) {
            origParamObject = parameterObject
        }
        return origParamObject
    }

    private String trimSql(String sql) {
        String trimedSql = sql.trim()
        int len = trimedSql.length()
        if (trimedSql.charAt(len - 1) == ';') {
            trimedSql = trimedSql.substring(0, len - 1)
        }
        return trimedSql
    }

    private String createPageSql(String sql, String dialect, int limit, int offset) {
        StringBuilder pageSql = new StringBuilder()
        if (dialect.startsWith("mysql")) {
            pageSql.append(sql)
            pageSql.append(" limit " + offset + "," + limit)
        } else if (dialect.startsWith("oracle")) {
            pageSql.append("select * from (select tmp_tb.*,ROWNUM row_id from (")
            pageSql.append(sql)
            pageSql.append(") as tmp_tb where ROWNUM<=")
            pageSql.append(offset + limit)
            pageSql.append(") where row_id>")
            pageSql.append(offset)
        } else if (dialect.startsWith("postgre")) {
            pageSql.append(sql)
            pageSql.append(" limit " + limit + " offset  " + offset)
        }
        return pageSql.toString()
    }

    private MetaObject findPageObject(Object parameterObject) {
        if (parameterObject in Map) {
            Object obj = parameterObject.get("page")
            if (obj == null) {
                throw new IllegalArgumentException("map参数不存在名为page的键！")
            }
            return SystemMetaObject.forObject(obj)
        } else {
            throw new IllegalArgumentException("分页功能只支持map参数（在map中设置page）")
        }
    }

    Object plugin(Object arg0) {
        return Plugin.wrap(arg0, this)
    }

    void setProperties(Properties p) {
        String dialectValue = p.getProperty("dialect")
        if (!dialectValue) {
            throw new RuntimeException("dialect property is not found!")
        }
        this.dialect = dialectValue

        String pageSqlIdValue = p.getProperty("pageSqlId")
        if (!pageSqlIdValue) {
            throw new RuntimeException("pageSqlId property is not found!")
        }
        this.pageSqlId = pageSqlIdValue
    }
}
