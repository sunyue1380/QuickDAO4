package cn.schoolwow.quickdao.query;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import cn.schoolwow.quickdao.query.condition.Condition;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

public class AbstractCompositQuery implements CompositQuery{
    private QuickDAOConfig quickDAOConfig;

    public AbstractCompositQuery(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public <T> Condition<T> query(Class<T> clazz) {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        if(null==entity){
            throw new IllegalArgumentException("不存在的实体类:"+clazz.getName()+"!");
        }
        return query(entity);
    }

    @Override
    public Condition query(String tableName) {
        for(Entity entity:quickDAOConfig.dbEntityList){
            if(entity.tableName.equals(tableName)){
                return query(entity);
            }
        }
        for(Entity entity:quickDAOConfig.visualTableList){
            if(entity.tableName.equals(tableName)){
                return query(entity);
            }
        }
        throw new IllegalArgumentException("不存在的表名:"+tableName+"!");
    }

    @Override
    public Condition query(Condition condition) {
        Query fromQuery = ((AbstractCondition) condition).query;
        if(null==fromQuery.tableAliasName){
            fromQuery.tableAliasName = "from_table";
        }
        condition.execute();

        Entity entity = new Entity();
        entity.clazz = JSONObject.class;
        entity.properties = new ArrayList<>();
        AbstractCondition condition1 = (AbstractCondition) query(entity);
        condition1.query.fromQuery = fromQuery;
        entity.tableName = "( " + condition1.query.dqlBuilder.getArraySQL(fromQuery).toString() +" )";
        entity.escapeTableName = entity.tableName;
        return condition1;
    }

    private Condition query(Entity entity){
        Query query = new Query();
        query.entity = entity;
        query.dqlBuilder = quickDAOConfig.database.getDQLBuilderInstance(quickDAOConfig);
        query.quickDAOConfig = quickDAOConfig;
        return quickDAOConfig.database.getConditionInstance(query);
    }
}
