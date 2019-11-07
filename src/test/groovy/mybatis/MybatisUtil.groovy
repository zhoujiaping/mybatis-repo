package mybatis

import org.apache.ibatis.builder.xml.XMLConfigBuilder
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MybatisUtil {
    private static final Logger logger = LoggerFactory.getLogger(MybatisUtil)

    private static String resource = "configuration-mysql.xml"
    private static volatile boolean inited
    private static SqlSessionFactory sqlSessionFactory
    private static void initMybatisContext() {
        if(!inited){
            synchronized (MybatisUtil) {
                if(!inited){
                    //读取jar包里面的资源，不能用获得file的方式，应该用获取的流的方式
                    Reader reader = Resources.getResourceAsReader(resource)
                    XMLConfigBuilder xMConfigBuilder = new XMLConfigBuilder(reader)
                    Configuration configuration = xMConfigBuilder.parse()
                    sqlSessionFactory = new DefaultSqlSessionFactory(configuration)
                    logger.info("mybatis启动完毕")
                }
            }
        }
    }
    static SqlSessionFactory getSqlSessionFactory(){
        initMybatisContext()
        return sqlSessionFactory
    }
}

