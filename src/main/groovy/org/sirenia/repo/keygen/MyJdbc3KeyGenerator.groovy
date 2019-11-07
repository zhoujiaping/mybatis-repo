package org.sirenia.repo.keygen

import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.reflection.MetaObject
import org.apache.ibatis.reflection.SystemMetaObject
import org.sirenia.repo.util.ParamObjectUtil

import java.sql.Statement
/**
 * 对于SelectKeyGenerator，只能用装饰器模式。
 * 对于Jdbc3KeyGenerator，只能用继承！
 * 别问：问就是试验出来的。
 *
 */
class MyJdbc3KeyGenerator extends Jdbc3KeyGenerator {
        @Override
        void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
            super.processBefore(executor, ms, stmt, parameter)
            fillPrimaryKeyBack(ms, parameter)
        }

        @Override
        void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
            super.processAfter(executor, ms, stmt, parameter)
            fillPrimaryKeyBack(ms, parameter)
        }

        private void fillPrimaryKeyBack(MappedStatement ms, Object parameter) {
            Object origParamObject = ParamObjectUtil.paramObject
            if (origParamObject) {
                def keyProperties = ms.keyProperties
                if (keyProperties) {
                    def metaParam = SystemMetaObject.forObject(parameter)
                    def metaOrigParam = SystemMetaObject.forObject(origParamObject)
                    for (def prop : keyProperties) {
                        Object value = metaParam.getValue(prop)
                        metaOrigParam.setValue(prop, value)
                    }
                }
            }
        }
    }
