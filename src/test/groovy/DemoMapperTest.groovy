import mybatis.MybatisUtil
import org.junit.Test

class DemoMapperTest {
    @Test
    void testInsert(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def demo = new Demo(
                creator:'john'
        )
        def res = mapper.insert(demo)
        println res
        session.commit()
    }
    @Test
    void testInsertSelective(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def demo = new Demo(
                creator:'john'
        )
        def res = mapper.insertSelective(demo)
        println res
        session.commit()
    }
    @Test
    void testBatchInsert(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def list = [new Demo(
                demoId:-1,
                creator:'john1'
        ),new Demo(
                demoId:-2,
                creator:'john2'
        )]
        def res = mapper.batchInsert(list)
        println res
        session.commit()
    }

    @Test
    void testSelectByPrimaryKey(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def res = mapper.selectByPrimaryKey(-1)
        println res
        session.commit()
    }

    @Test
    void testUpdateByPrimaryKeySelective(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def data = new Demo(
                demoId: -1,
                creator:'john0',
                version:1
        )
        def res = mapper.updateByPrimaryKeySelective(data)
        println res
        session.commit()
    }

    @Test
    void testUpdateByPrimaryKeySelectiveAndVersion(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def data = new Demo(
                demoId: -1,
                creator:'john',
                version:1
        )
        def res = mapper.updateByPrimaryKeySelectiveAndVersion(data)
        println res
        session.commit()
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
        session.commit()
    }

    @Test
    void updateByPrimaryKeyAndVersion(){
        def session = MybatisUtil.sqlSessionFactory.openSession()
        def mapper = session.getMapper(DemoMapper)
        def data = new Demo(
                demoId: 1,
                creator:'john',
                version:1
        )
        def res = mapper.updateByPrimaryKey(data)
        println res
        session.commit()
    }
}
