package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.DAOUtils;
import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.util.MigrateOption;
import com.zaxxer.hikari.HikariDataSource;
import org.aeonbits.owner.ConfigCache;
import org.junit.Test;

public class DAOUtilsTest {
    DAOUtilProperties daoUtilProperties = ConfigCache.getOrCreate(DAOUtilProperties.class);

    @Test
    public void migrate() {
        HikariDataSource sorceHikariDataSource = new HikariDataSource();
        sorceHikariDataSource.setDriverClassName("org.postgresql.Driver");
        sorceHikariDataSource.setJdbcUrl(daoUtilProperties.sourcePostgreJdbc());
        sorceHikariDataSource.setUsername(daoUtilProperties.sourcePostgreUsername());
        sorceHikariDataSource.setPassword(daoUtilProperties.sourcePostgrePassword());
        DAO sourceDAO = QuickDAO.newInstance()
                .dataSource(sorceHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();
        HikariDataSource targetHikariDataSource = new HikariDataSource();
        targetHikariDataSource.setDriverClassName("org.postgresql.Driver");
        targetHikariDataSource.setJdbcUrl(daoUtilProperties.targetPostgreJdbc());
        targetHikariDataSource.setUsername(daoUtilProperties.targetPostgreUsername());
        targetHikariDataSource.setPassword(daoUtilProperties.targetPostgrePassword());
        DAO targetDAO = QuickDAO.newInstance()
                .dataSource(targetHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();

        MigrateOption migrateOption = new MigrateOption();
        migrateOption.source = sourceDAO;
        migrateOption.target = targetDAO;
        migrateOption.tableFilter = (entity)->{
            if(targetDAO.hasTable(entity.tableName)){
                return false;
            }
            return true;
        };
        DAOUtils.migrate(migrateOption);
    }
}