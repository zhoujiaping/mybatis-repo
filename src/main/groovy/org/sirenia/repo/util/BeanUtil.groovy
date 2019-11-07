package org.sirenia.repo.util
/**
 * 驼峰命名和下划线命名风格转换的工具类
 */
class BeanUtil {
    /**
     * 驼峰命名转下划线命名
     */
    static String camel2underline(String prop) {
        return prop.replaceAll("(?<Uper>[A-Z])", '_${Uper}').toLowerCase()
    }

    static String underline2camel(String column) {
        if (!column) {
            return null
        }
        String[] array = column.toLowerCase().split("_(?=[a-z])")
        if (array.length == 1) {
            return array[0]
        }
        for (int i = 1; i < array.length; i++) {
            array[i] = array[i].substring(0, 1).toUpperCase() + array[i].substring(1)
        }
        return array.join("")
    }
}
