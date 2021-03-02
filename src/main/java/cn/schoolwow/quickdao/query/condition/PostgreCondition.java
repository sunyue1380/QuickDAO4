package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.domain.Query;

public class PostgreCondition<T> extends AbstractCondition<T>{

    public PostgreCondition(Query query) {
        super(query);
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        query.limit = "limit " + limit + " offset " + offset;
        return this;
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        query.limit = "limit " + pageSize + " offset " + (pageNum - 1) * pageSize;
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return this;
    }
}
