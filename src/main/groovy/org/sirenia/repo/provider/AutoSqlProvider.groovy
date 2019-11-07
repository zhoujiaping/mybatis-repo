package org.sirenia.repo.provider

import org.sirenia.repo.conf.XMLMapperConf

class AutoSqlProvider {
    XMLMapperConf conf
    AutoSqlProvider(XMLMapperConf conf){
        this.conf = conf
    }

    String selectByPrimaryKey() {
        def columns = conf.mappedColumns.join(",")
        return """<script>
select ${columns} from ${conf.tablename} 
where ${conf.idResultMapping.column} = #{id,jdbcType=${conf.idResultMapping.jdbcType}}
</script>
"""
    }

    String insert() {
        //getMappedColumns和getResultMappings的字段顺序不是一致的。
        def values = conf.resultMappings.collect{
            "#{${it.property},jdbcType=${it.jdbcType}}"
        }.join(",")
        return """<script>
insert into ${conf.tablename}(${conf.resultMappings*.column})values(${values});
</script>
"""
    }

    String insertSelective() {
        def columns = conf.resultMappings.collect{
            "<if test='${it.property}!=null'>${it.column},</if>"
        }
        def values = conf.resultMappings.collect{
            "<if test='${it.property}!=null'>#{${it.property},jdbcType=${it.jdbcType}},</if>"
        }.join(",")
        return """<script>
insert into ${conf.tablename}
<trim prefix='(' suffix=')' suffixOverrides=','>${columns}</trim>
values
<trim prefix='(' suffix=')' suffixOverrides=','>${values}</trim>
</script>
"""
    }

    String updateByPrimaryKeySelective() {
        def setters = conf.resultMappings.collect {
            "<if test='${it.property}!=null'>${it.column} = #{${it.property},jdbcType=${it.jdbcType}},</if>"
        }
        return """<script>
update ${conf.tablename}
<set>${setters}</set>
where ${conf.idResultMapping.column} = #{${conf.idResultMapping.property},jdbcType=${conf.idResultMapping.jdbcType}}
</script>
"""
    }

    String updateByPrimaryKeySelectiveAndVersion() {
        def setters = conf.resultMappings.findAll{
            it.column!=conf.optiLockColumn
        }.collect {
            "<if test='${it.property}!=null'>${it.column} = #{${it.property},jdbcType=${it.jdbcType}},</if>"
        }
        return """<script>
update ${conf.tablename}
<set>${setters} ${conf.optiLockColumn}=${conf.optiLockColumn}+1</set>
where ${conf.idResultMapping.column} = #{${conf.idResultMapping.property},jdbcType=${conf.idResultMapping.jdbcType}}
and ${conf.optiLockResultMapping.column} = #{${conf.optiLockResultMapping.property},jdbcType=${conf.optiLockResultMapping.jdbcType}}
</script>
"""
    }

    String updateByPrimaryKey() {
        def setters = conf.resultMappings.findAll{
            it.column!=conf.optiLockColumn
        }.collect {
            "${it.column} = #{${it.property},jdbcType=${it.jdbcType}}"
        }.join(",")
        return """<script>
update ${conf.tablename}
set ${setters},${conf.optiLockColumn}=${conf.optiLockColumn}+1
where ${conf.idResultMapping.column} = #{${conf.idResultMapping.property},jdbcType=${conf.idResultMapping.jdbcType}}
</script>
"""
    }

    String updateByPrimaryKeyAndVersion() {
        def setters = conf.resultMappings.findAll{
            it.column!=conf.optiLockColumn
        }.collect {
            "<if test='${it.property}!=null'>${it.column} = #{${it.property},jdbcType=${it.jdbcType}},</if>"
        }
        return """<script>
update ${conf.tablename}
set ${setters},${conf.optiLockColumn}=${conf.optiLockColumn}+1
where ${conf.idResultMapping.column} = #{${conf.idResultMapping.property},jdbcType=${conf.idResultMapping.jdbcType}}
and ${conf.optiLockResultMapping.column} = #{${conf.optiLockResultMapping.property},jdbcType=${conf.optiLockResultMapping.jdbcType}}
</script>
"""
    }

    String batchInsert() {
        def values = conf.resultMappings.collect{
            "#{${it.property},jdbcType=${it.jdbcType}}"
        }.join(",")
        return """<script>
insert into ${conf.tablename}(${conf.resultMappings*.column})values
<foreach collection='recordList' item='record' separator=','>(${values})</foreach>;
</script>
"""
    }

}
