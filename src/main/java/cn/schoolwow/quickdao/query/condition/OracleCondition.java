package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.SubQuery;

public class OracleCondition<T> extends AbstractCondition<T>{

    public OracleCondition(Query query) {
        super(query);
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        Condition subCondition = query.quickDAOConfig.dao.query(this)
                .tableAliasName("a")
                .addColumn("a.*","rownum rn")
                .addQuery("rownum", "<=", limit+offset);
        Condition condition = query.quickDAOConfig.dao.query(subCondition)
                .tableAliasName("b")
                .addQuery("rn",">=",offset+1);
        if("JSONObject".equals(this.query.entity.clazz.getSimpleName())){
            condition.addColumn("b.*");
        }else{
            for(Property property:this.query.entity.properties){
                condition.addColumn("b." + this.query.tableAliasName + "_" + property.column);
            }
            if(this.query.compositField){
                for (SubQuery subQuery : this.query.subQueryList) {
                    for(Property property:subQuery.entity.properties){
                        condition.addColumn("b." + subQuery.tableAliasName + "_" + property.column);
                    }
                }
            }
        }
        return condition;
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        Condition condition = limit((pageNum - 1) * pageSize,pageSize);
        Query query = condition.getQuery();
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return condition;
    }
}
