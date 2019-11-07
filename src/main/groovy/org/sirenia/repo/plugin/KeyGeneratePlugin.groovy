package org.sirenia.repo.plugin

import com.sun.istack.internal.NotNull
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.NoKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Plugin
import org.apache.ibatis.plugin.Signature
import org.sirenia.repo.anno.IgnoreKeyGen
import org.sirenia.repo.anno.KeyGen
import org.sirenia.repo.keygen.MyJdbc3KeyGenerator
import org.sirenia.repo.util.BeanUtil
import org.sirenia.repo.util.ReflectUtil
import java.util.concurrent.ConcurrentHashMap

@Intercepts([@Signature(type = Executor, method = "update", args = [MappedStatement, Object])])
class KeyGeneratePlugin implements Interceptor {

/**
 *
 * mybatis插件实际上就是拦截器，需要实现Interceptor接口。
 * 可以拦截StatementHandler、Executor、ResultSetHandler、ParameterHandler
 *
 * 通过@Intercepts和@Signature指定被拦截的接口及其方法
 *
 * StatementHandler --BaseStatementHandler（abstract）
 * ----CallableStatementHandler ----PreparedStatementHandler
 * ----SimpleStatementHandler --RoutingStatementHandler *
 * configuration.xml中可以配置settings的useGeneratedKeys为true，
 * 但是需要mapper.xml中的insert标签有keyProperty属性才能有效。
 * 所以该插件的另外一个思路，是配置全局的useGeneratedKeys为true，
 * 然后获取@KeyGen注解，获取keyProperties和keyColumn， 设置到MappedStatement对象中。
 *
 */
    private def statementIdsDealedKeyGen = new ConcurrentHashMap<String, String>();

    Object intercept(Invocation ivk) {
        Object[] args = ivk.args
        MappedStatement mappedStatement = args[0]
        Object parameterObject = args[1]
        SqlCommandType commandType = mappedStatement.sqlCommandType
        if (commandType == SqlCommandType.INSERT) {
            if (!statementIdsDealedKeyGen.containsKey(mappedStatement.id)) {
                dealKeyGen(mappedStatement, parameterObject)
            }
        }
        return ivk.proceed()
    }

    private synchronized void dealKeyGen(MappedStatement mappedStatement, @NotNull Object parameterObject){
        def stmtId = mappedStatement.id
        if (statementIdsDealedKeyGen.containsKey(stmtId)) {
            return
        }
        if (parameterObject in Collection) {
            statementIdsDealedKeyGen[stmtId] = ""
            return
        }
        def lastDotIndex = stmtId.lastIndexOf('.')
        def methodName = stmtId.substring(lastDotIndex + 1)
        def mapperName = stmtId.substring(0, lastDotIndex)
        def mapperClass = Class.forName(mapperName)
        def keygenAnno = mapperClass.getAnnotation(KeyGen)
        if (keygenAnno == null) {
            statementIdsDealedKeyGen.put[stmtId] = ""
            return
        }
        def method = mapperClass.methods.find{
            it.name == methodName
        }
        def ignoreKeyGen = method.getAnnotation(IgnoreKeyGen)
        if (ignoreKeyGen != null) {
            statementIdsDealedKeyGen.put(stmtId, "")
            return;
        }
        doDealKeyGen(mappedStatement, stmtId, keygenAnno)
    }

    private void doDealKeyGen(MappedStatement mappedStatement, String stmtId, KeyGen keygenAnno) {
        String keyProperty = keygenAnno.keyProperty()
        String keyColumn = keygenAnno.keyColumn()
        if (!keyProperty && !keyColumn) {
            throw new RuntimeException("keyGen配置错误，keyPropertie和keyColumn不能都为空")
        } else if (keyColumn) {
            keyProperty = BeanUtil.underline2camel(keyColumn)
        } else {
            keyColumn = BeanUtil.camel2underline(keyProperty)
        }
        def kg = mappedStatement.getKeyGenerator()
        if (kg in NoKeyGenerator) {
            kg = new MyJdbc3KeyGenerator()
            ReflectUtil.setValueByFieldName(mappedStatement, "keyGenerator", kg)
            ReflectUtil.setValueByFieldName(mappedStatement, "keyProperties", [keyProperty])
            ReflectUtil.setValueByFieldName(mappedStatement, "keyColumns", [keyColumn])
        }
        statementIdsDealedKeyGen.put(stmtId, "")
    }

    Object plugin(Object target) {
        return Plugin.wrap(target, this)
    }

    void setProperties(Properties p) {
    }
}
