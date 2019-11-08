package org.sirenia.repo.conf

import org.sirenia.repo.anno.Table
import org.apache.ibatis.mapping.ResultMap
import org.apache.ibatis.mapping.ResultMapping
import org.apache.ibatis.session.Configuration


/**
 * 封装mapper.xml中的信息
 * 从Configuration中获取xml配置信息，提供给插件使用
 */
class XMLMapperConf {
    String tablename
    Set<String> mappedColumns
    List<ResultMapping> resultMappings
    ResultMapping idResultMapping
    ResultMapping optiLockResultMapping
    ResultMap resultMap
    String optiLockColumn

    static XMLMapperConf of(Configuration configuration, String mapperClazzName) {
        def conf = new XMLMapperConf()
        def table = Class.forName(mapperClazzName).getAnnotation(Table)
        if (!table) {
            throw new RuntimeException("BaseMapper需要和${Table.name}一起使用")
        }
        conf.tablename = table.name()
        conf.resultMap = configuration.getResultMap( "${mapperClazzName}.BaseResultMap")
        conf.idResultMapping = conf.resultMap.idResultMappings[0]// 只支持单列主键
        conf.mappedColumns = conf.resultMap.mappedColumns
        conf.resultMappings = conf.resultMap.resultMappings
        conf.optiLockColumn = table.optiLockColumn()
        conf.optiLockResultMapping = conf.resultMappings.find{
            it.column.toLowerCase() == conf.optiLockColumn.toLowerCase()
        }
        return conf
    }
}

