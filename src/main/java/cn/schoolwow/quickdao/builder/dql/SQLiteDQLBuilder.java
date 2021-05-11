package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.SQLException;

public class SQLiteDQLBuilder extends AbstractDQLBuilder {

    public SQLiteDQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public ConnectionExecutorItem update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update " + query.entity.escapeTableName + " ");
        builder.append(query.setBuilder.toString() + " " + query.where.replace(query.tableAliasName + ".",""));

        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量更新",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,connectionExecutorItem.preparedStatement,query.parameterIndex++,builder);
        }
        addMainTableParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete from "+query.quickDAOConfig.database.escape(query.entity.tableName));
        builder.append(" " + query.where.replace(query.tableAliasName+".",""));

        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量删除",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addMainTableParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }
}
