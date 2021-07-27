package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.SQLException;

public class OracleDQLBuilder extends AbstractDQLBuilder{

    public OracleDQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public int getResultSetRowCount(Query query) throws SQLException {
        return super.getResultSetRowCount(getResponseQuery(query));
    }

    @Override
    public ConnectionExecutorItem count(Query query) throws SQLException {
        return super.count(getResponseQuery(query));
    }

    /**
     * 获取返回结果Query
     * */
    private Query getResponseQuery(Query query){
        //oracle分页操作
        if("where rn >= ?".equals(query.where)){
            return query.fromQuery.fromQuery;
        }
        return query;
    }
}
