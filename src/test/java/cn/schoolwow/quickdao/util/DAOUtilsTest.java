package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.DAOUtils;
import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.util.MigrateOption;
import cn.schoolwow.quickdao.domain.util.TableStructureSynchronizedOption;
import com.zaxxer.hikari.HikariDataSource;
import org.aeonbits.owner.ConfigCache;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Predicate;

@Ignore
public class DAOUtilsTest {
    DAOUtilProperties daoUtilProperties = ConfigCache.getOrCreate(DAOUtilProperties.class);

    @Test
    public void tableStructureSynchronized(){
        HikariDataSource sourceHikariDataSource = new HikariDataSource();
        sourceHikariDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        sourceHikariDataSource.setJdbcUrl(daoUtilProperties.sourceOracleJdbc());
        sourceHikariDataSource.setUsername(daoUtilProperties.sourceOracleUsername());
        sourceHikariDataSource.setPassword(daoUtilProperties.sourceOraclePassword());

        DAO sourceDAO = QuickDAO.newInstance()
                .dataSource(sourceHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();

        HikariDataSource targetHikariDataSource = new HikariDataSource();
        targetHikariDataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        targetHikariDataSource.setJdbcUrl(daoUtilProperties.targetOracleJdbc());
        targetHikariDataSource.setUsername(daoUtilProperties.targetOracleUsername());
        targetHikariDataSource.setPassword(daoUtilProperties.targetOraclePassword());

        DAO targetDAO = QuickDAO.newInstance()
                .dataSource(targetHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();

        TableStructureSynchronizedOption tableStructureSynchronizedOption = new TableStructureSynchronizedOption();
        tableStructureSynchronizedOption.source = sourceDAO;
        tableStructureSynchronizedOption.target = targetDAO;
        tableStructureSynchronizedOption.createTablePredicate = (entity)->{
            System.out.println(entity);
            return false;
        };
        tableStructureSynchronizedOption.createPropertyPredicate = (property)->{
            System.out.println(property);
            return false;
        };
        DAOUtils.tableStructureSynchronized(tableStructureSynchronizedOption);
    }

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
        migrateOption.tableConsumer = (sourceTable,targetTable)->{
            //若跨数据库类型迁移(比如mysql迁移到sqlite),可能存在数据库类型不兼容情况
            //此时需要手动调整列类型
        };
        DAOUtils.migrate(migrateOption);
    }
}