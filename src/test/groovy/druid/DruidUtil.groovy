package druid

import com.alibaba.druid.pool.DruidDataSource
import org.apache.ibatis.datasource.DataSourceFactory

import javax.sql.DataSource

class DruidDataSourceFactory implements DataSourceFactory {
    private volatile DruidDataSource ds
    Properties props

    @Override
    void setProperties(Properties props) {
        this.props = props
    }

    @Override
    DataSource getDataSource() {
        if (ds == null) {
            synchronized (this) {
                if (ds == null) {
                    ds = initDruidDataSource()
                }
            }
        }
        return ds
    }

    private DruidDataSource initDruidDataSource() {
        DruidDataSource ds = new DruidDataSource()
        ds.connectProperties = props
        return ds
    }

}
