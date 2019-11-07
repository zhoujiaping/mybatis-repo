package org.sirenia.repo.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.lang.reflect.Method
/**
 兼容加解密插件
 加解密插件由于遵循不修改方法入参的原则，会将入参拷贝一份，然后在拷贝的对象上转换明文为密文，
 这样会和一些插件（比如分页插件）不兼容。分页插件会通过入参传递数据。
 该类用于帮助解决这个问题。
 */
class ParamObjectUtil {

        private static final Logger logger = LoggerFactory.getLogger(ParamObjectUtil)

        private static final String PARAM_OBJECT_HOLDER_CLASS = "com.sfpay.module.mybatis.encrypt.util.ParamObjectHolder"
        private static Method paramObjHolderGetter
        static{
            try {
                def clazz = Class.forName(PARAM_OBJECT_HOLDER_CLASS)
                paramObjHolderGetter = clazz.getMethod("get")
            } catch (ClassNotFoundException e) {
                logger.warn("没有找到类【{}】，请确认项目中是否需要引入加解密插件", PARAM_OBJECT_HOLDER_CLASS)
            }catch(e){
                throw new RuntimeException("兼容加解密插件异常",e)
            }
        }
        static Object getParamObject(){
            Object origParameterObject = null
            if(paramObjHolderGetter){
                origParameterObject = paramObjHolderGetter.invoke(null)
            }
            return origParameterObject
        }
    }
