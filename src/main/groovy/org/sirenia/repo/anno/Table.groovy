package org.sirenia.repo.anno

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * 通过该注解，为Mapper配置表名、乐观锁字段名
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value=ElementType.TYPE)
@interface Table {
    /**表名*/
    String name()
    /**乐观锁字段名*/
    String optiLockColumn() default "version"
}
