package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.FieldFragmentEntry;
import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.query.response.Response;

public class SQLServerCondition<T> extends AbstractCondition<T>{

    public SQLServerCondition(Query query) {
        super(query);
    }

    @Override
    public Condition<T> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        whereList.add(new FieldFragmentEntry(field,"charindex(?,{}) > 0"));
        query.parameterList.add(value.toString());
        return this;
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        query.limit = "offset " + offset + " rows fetch next " + limit + " rows only";
        return this;
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        query.limit = "offset "+(pageNum - 1) * pageSize+" rows " + " fetch next "+pageSize+" rows only";
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return this;
    }

    @Override
    public Response<T> execute() {
        //SQL Server的分页和排序必须一起用
        if((orderByList.size()>0&&query.limit.isEmpty())||(orderByList.size()==0&&!query.limit.isEmpty())){
            throw new IllegalArgumentException("SQL Server的排序和分页操作必须一起使用!");
        }
        return super.execute();
    }
}
