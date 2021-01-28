package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLiteDQLBuilder extends AbstractDQLBuilder {

    public SQLiteDQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update " + query.entity.escapeTableName + " ");
        builder.append(query.setBuilder.toString());
        if(query.whereBuilder.length()>0){
            builder.append(" where " + query.whereBuilder.toString());
        }

        PreparedStatement ps = connection.prepareStatement(builder.toString().replace(query.tableAliasName+".",""));
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        addMainTableParameters(ps,query,query,builder);
        MDC.put("name","批量更新");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete from "+query.quickDAOConfig.database.escape(query.entity.tableName));
        if(query.whereBuilder.length()>0){
            builder.append(" where " + query.whereBuilder.toString().replace(query.tableAliasName+".",""));
        }

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,query,builder);
        MDC.put("name","批量删除");
        MDC.put("sql",builder.toString());
        return ps;
    }
}
