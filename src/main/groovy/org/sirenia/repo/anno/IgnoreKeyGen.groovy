package org.sirenia.repo.anno

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * 如果表不需要自动生成主键，需要配置该注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@interface IgnoreKeyGen{
}