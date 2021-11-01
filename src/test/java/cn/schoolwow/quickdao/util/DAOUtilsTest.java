package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.DAOUtils;
import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.util.DiffTableStructureOption;
import cn.schoolwow.quickdao.domain.util.MigrateOption;
import cn.schoolwow.quickdao.domain.util.TableStructureSynchronizedOption;
import com.zaxxer.hikari.HikariDataSource;
import org.aeonbits.owner.ConfigCache;
import org.junit.Test;

public class DAOUtilsTest {
    DAOUtilProperties daoUtilProperties = ConfigCache.getOrCreate(DAOUtilProperties.class);
    private DAO sourceDAO;
    private DAO targetDAO;
    {
        HikariDataSource sourceHikariDataSource = new HikariDataSource();
        sourceHikariDataSource.setDriverClassName(daoUtilProperties.sourceJdbcDriver());
        sourceHikariDataSource.setJdbcUrl(daoUtilProperties.sourceJdbcUrl());
        sourceHikariDataSource.setUsername(daoUtilProperties.sourceUsername());
        sourceHikariDataSource.setPassword(daoUtilProperties.sourcePassword());
        sourceDAO = QuickDAO.newInstance()
                .dataSource(sourceHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();

        HikariDataSource targetHikariDataSource = new HikariDataSource();
        targetHikariDataSource.setDriverClassName(daoUtilProperties.targetJdbcDriver());
        targetHikariDataSource.setJdbcUrl(daoUtilProperties.targetJdbcUrl());
        targetHikariDataSource.setUsername(daoUtilProperties.targetUsername());
        targetHikariDataSource.setPassword(daoUtilProperties.targetPassword());
        targetDAO = QuickDAO.newInstance()
                .dataSource(targetHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();
    }

    @Test
    public void diffTableStructureOption(){
        DiffTableStructureOption diffTableStructureOption = new DiffTableStructureOption();
        diffTableStructureOption.source = sourceDAO;
        diffTableStructureOption.target = targetDAO;
        diffTableStructureOption.executeSQL = true;
        DAOUtils.diffTableStructure(diffTableStructureOption);
    }

    @Test
    public void tableStructureSynchronized(){
        HikariDataSource sourceHikariDataSource = new HikariDataSource();
        sourceHikariDataSource.setDriverClassName(daoUtilProperties.sourceJdbcDriver());
        sourceHikariDataSource.setJdbcUrl(daoUtilProperties.sourceJdbcUrl());
        sourceHikariDataSource.setUsername(daoUtilProperties.sourceUsername());
        sourceHikariDataSource.setPassword(daoUtilProperties.sourcePassword());

        DAO sourceDAO = QuickDAO.newInstance()
                .dataSource(sourceHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();

        HikariDataSource targetHikariDataSource = new HikariDataSource();
        targetHikariDataSource.setDriverClassName(daoUtilProperties.targetJdbcDriver());
        targetHikariDataSource.setJdbcUrl(daoUtilProperties.targetJdbcUrl());
        targetHikariDataSource.setUsername(daoUtilProperties.targetUsername());
        targetHikariDataSource.setPassword(daoUtilProperties.targetPassword());

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
        HikariDataSource sourceHikariDataSource = new HikariDataSource();
        sourceHikariDataSource.setDriverClassName(daoUtilProperties.sourceJdbcDriver());
        sourceHikariDataSource.setJdbcUrl(daoUtilProperties.sourceJdbcUrl());
        sourceHikariDataSource.setUsername(daoUtilProperties.sourceUsername());
        sourceHikariDataSource.setPassword(daoUtilProperties.sourcePassword());
        DAO sourceDAO = QuickDAO.newInstance()
                .dataSource(sourceHikariDataSource)
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .build();
        HikariDataSource targetHikariDataSource = new HikariDataSource();
        targetHikariDataSource.setDriverClassName(daoUtilProperties.targetJdbcDriver());
        targetHikariDataSource.setJdbcUrl(daoUtilProperties.targetJdbcUrl());
        targetHikariDataSource.setUsername(daoUtilProperties.targetUsername());
        targetHikariDataSource.setPassword(daoUtilProperties.targetPassword());
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