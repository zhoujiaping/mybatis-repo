package org.sirenia.repo.anno

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * 通过该注解，配置自动生成主键的列名、属性名
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@interface KeyGen{
    String keyProperty() default ""
    String keyColumn() default ""
}
