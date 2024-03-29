package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.util.Collection;

/**
 * 数据库操作实例
 * */
public class AbstractSQLDAO implements SQLDAO {
    protected Logger logger = LoggerFactory.getLogger(SQLDAO.class);
    /**数据库配置对象*/
    protected QuickDAOConfig quickDAOConfig;
    /**数据库访问*/
    public AbstractSQLBuilder sqlBuilder;

    public AbstractSQLDAO(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public boolean exist(Object instance) {
        if(null==instance){
            return false;
        }
        boolean result = false;
        try {
            Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
            ConnectionExecutorItem connectionExecutorItem = null;
            if(!entity.uniqueProperties.isEmpty()){
                connectionExecutorItem = sqlBuilder.selectCountByUniqueKey(instance);
            }else if(null!=entity.id){
                connectionExecutorItem = sqlBuilder.selectCountById(instance);
            }else{
                throw new IllegalArgumentException("该实例无唯一性约束又无id值,无法判断!类名:"+instance.getClass().getName());
            }
            ResultSet resultSet = sqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            if (resultSet.next()) {
                result = resultSet.getLong(1)>0;
            }
            resultSet.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return result;
    }

    @Override
    public boolean existAny(Object... instances) {
        for(Object instance:instances){
            if(exist(instance)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existAll(Object... instances) {
        for(Object instance:instances){
            if(!exist(instance)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean existAny(Collection instances) {
        return existAny(instances.toArray());
    }

    @Override
    public boolean existAll(Collection instances) {
        return existAll(instances.toArray());
    }
}
