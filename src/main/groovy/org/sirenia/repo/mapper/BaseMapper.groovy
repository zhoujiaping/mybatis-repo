package org.sirenia.repo.mapper

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.ResultType
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.sirenia.repo.anno.IgnoreKeyGen
import org.sirenia.repo.constant.AutoSql


/**
 * 通用mapper接口，子接口只要继承该接口并添加一个说明表名的注解， 即可获得单表增、删、查、改、批量增删改、分页查、按example查等多种方法。
 *
 * 1、继承BaseMapper，就拥有了 基本的CURD功能。
 * 由于加解密需要加注解，所以如果需要重载BaseMapper的方法，并加上对应的注解。
 * 2、Table注解用于支持BaseMapper。optiLockColumn属性为乐观锁字段名，默认为version。
 * 3、KeyGen注解，用于支持新增并返回主键。目前只支持mysql，userGeneratorKeys。
 */
interface BaseMapper<ENTITY, PK> {

    @ResultType(Integer)
    @Insert(AutoSql.SQL)
    int insert(ENTITY record)

    @ResultType(Integer)
    @Insert(AutoSql.SQL)
    int insertSelective(ENTITY record)

    @ResultType(Integer)
    @Insert(AutoSql.SQL)
    @IgnoreKeyGen
    int batchInsert(@Param("recordList") Collection<ENTITY> recordList)

    @ResultMap(AutoSql.BASE_RESULT_MAP)
    @Select(AutoSql.SQL)
    ENTITY selectByPrimaryKey(@Param(AutoSql.ID) PK id)// @Param("id")不要删除，约定名称为传参名为id，这样简单方便。

    @ResultType(Integer)
    @Update(AutoSql.SQL)
    int updateByPrimaryKeySelective(ENTITY record)

    @ResultType(Integer)
    @Update(AutoSql.SQL)
    int updateByPrimaryKeySelectiveAndVersion(ENTITY record)

    @ResultType(Integer)
    @Update(AutoSql.SQL)
    int updateByPrimaryKey(ENTITY record)

    @ResultType(Integer)
    @Update(AutoSql.SQL)
    int updateByPrimaryKeyAndVersion(ENTITY record)
}
