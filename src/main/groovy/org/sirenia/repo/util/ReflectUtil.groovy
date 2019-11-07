package org.sirenia.repo.util

import com.sun.istack.internal.NotNull

import java.lang.reflect.Field

/**
 * 反射的帮助类
 */
class ReflectUtil {
    static Field getFieldByFieldName(@NotNull Object obj,@NotNull String fieldName) {
        for (Class superClass = obj.class; superClass != Object; superClass = superClass.superclass) {
            def field = superClass.declaredFields.find{
                it.name == fieldName
            }
            if(field){
                return field
            }
        }
        throw new RuntimeException("属性：" + fieldName + "未找到")
    }

    static def getValueByFieldName(@NotNull Object obj,@NotNull String fieldName) {
        def field = getFieldByFieldName(obj, fieldName)
        def value
        if (field.accessible) {
            value = field.get(obj)
        } else {
            field.accessible = true
            value = field.get(obj)
            field.accessible = false
        }
        return value
    }

    static void setValueByFieldName(@NotNull Object obj,@NotNull String fieldName, value) {
        Field field = obj.class.getDeclaredField(fieldName)
        if (field.accessible) {
            field.set(obj, value)
        } else {
            field.accessible = true
            field.set(obj, value)
            field.accessible = false
        }
    }

    /**
     * 处理有多个插件时的情况
     */
    static def unwrapProxy(@NotNull Object target) {
        def isProxy = true
        while (isProxy) {
            Class targetClazz = target.class
            isProxy = targetClazz.name.startsWith('com.sun.proxy.$Proxy')
            if (isProxy) {
                target = getValueByFieldName(target, "h")
                target = getValueByFieldName(target, "target")
            }
        }
        return target
    }
}

