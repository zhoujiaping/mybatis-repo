import mybatis.MybatisUtil
import org.junit.Test

class DemoMapperTest {
    @Test
    void testSelectByPrimaryKey(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def res = mapper.selectByPrimaryKey(1)
        println res
    }
    @Test
    void testUpdateByPrimaryKey(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def data = new Demo(
                demoId: 1,
                creator:'john'
        )
        def res = mapper.updateByPrimaryKey(data)
        println res
        session.rollback()
    }
}
