package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.Query;

public class OracleCondition<T> extends AbstractCondition<T>{

    public OracleCondition(Query query) {
        super(query);
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        throw new UnsupportedOperationException("Oracle数据库目前暂不支持分页操作!");
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        throw new UnsupportedOperationException("Oracle数据库目前暂不支持分页操作!");
    }
}
