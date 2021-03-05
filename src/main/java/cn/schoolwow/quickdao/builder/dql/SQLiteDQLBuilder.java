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
        builder.append(query.setBuilder.toString() + " " + query.where);
        MDC.put("name","批量更新");
        String sql = builder.toString().replace(query.tableAliasName+".","");
        MDC.put("sql",sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        addMainTableParameters(ps,query,query,builder);
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete from "+query.quickDAOConfig.database.escape(query.entity.tableName));
        builder.append(" " + query.where.replace(query.tableAliasName+".",""));
        MDC.put("name","批量删除");
        String sql = builder.toString();
        MDC.put("sql",sql);

        PreparedStatement ps = connection.prepareStatement(sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,query,builder);
        MDC.put("sql",builder.toString());
        return ps;
    }
}
