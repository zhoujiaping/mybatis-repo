package org.sirenia.repo.plugin

import org.apache.ibatis.executor.Executor
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlSource
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Plugin
import org.apache.ibatis.plugin.Signature
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.sirenia.repo.conf.XMLMapperConf
import org.sirenia.repo.constant.AutoSql
import org.sirenia.repo.provider.AutoSqlProvider
import org.sirenia.repo.util.ReflectUtil

import java.lang.reflect.Method

@Intercepts([
    @Signature(type = Executor, method = "query", args = [MappedStatement, Object, RowBounds, ResultHandler]),
    @Signature(type = Executor, method = "update", args = [MappedStatement, Object ])
])
class AutoSqlExecutorPlugin implements Interceptor{

    final def providerMethodMap = new HashMap<String, Method>()

    AutoSqlExecutorPlugin(){
        AutoSqlProvider.methods.each {
            providerMethodMap[it.name] = it
        }
    }
    @Override
    Object intercept(Invocation invocation){
        def args = invocation.args
        MappedStatement statement = args[0]
        def parameterObject = args[1]
        String sql = statement.getBoundSql(parameterObject).sql
        if(AutoSql.SQL == sql){// 只有第一次会执行，因为执行一次后sql语句已经被改变。
            synchronized (this) {
                if(AutoSql.SQL == sql){
                    doIntercept(statement, parameterObject)
                }
            }
        }
        return invocation.proceed()
    }

    void doIntercept(MappedStatement statement, parameterObject) {
        def configuration = statement.configuration
        def parts = statement.id.split(/\./)
        def methodName = parts[-1]
        def mapperClazzName = parts[0..-2].join(".")
        def method = providerMethodMap[methodName]
        def conf = XMLMapperConf.of(configuration, mapperClazzName)
        def provider = new AutoSqlProvider(conf)
        String script = method.invoke(provider)
        // 不支持写<selectKey>，不支持<include>
        Class paramClazz = null
        if (parameterObject != null) {
            paramClazz = parameterObject.getClass()
        }
        SqlSource dynamicSqlSource = new XMLLanguageDriver().createSqlSource(configuration, script, paramClazz)
        ReflectUtil.setValueByFieldName(statement, "sqlSource", dynamicSqlSource)
    }

    @Override
    Object plugin(Object target) {
        return Plugin.wrap(target, this)
    }

    @Override
    void setProperties(Properties properties) {

    }
}
