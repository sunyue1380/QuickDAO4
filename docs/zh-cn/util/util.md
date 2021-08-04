# DAO工具类

QuickDAO提供了工具类用于在多个数据库之间同步数据结构或者迁移数据

> DAO工具类从4.1.8开始提供,同时移除了之前的DAO迁移接口

## 同步数据库结构

QuickDAO支持源数据库的数据库表结构(包括表,字段,索引等)同步到目标数据库

```java
//源数据库
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
//目标数据库
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
//数据结构同步选项
TableStructureSynchronizedOption tableStructureSynchronizedOption = new TableStructureSynchronizedOption();
//设置源数据库
tableStructureSynchronizedOption.source = sourceDAO;
//设置目标数据库
tableStructureSynchronizedOption.target = targetDAO;
//是否同步新增指定表
tableStructureSynchronizedOption.createTablePredicate = (entity)->{
    System.out.println(entity);
    return false;
};
//是否同步新增指定字段
tableStructureSynchronizedOption.createPropertyPredicate = (property)->{
    System.out.println(property);
    return false;
};
//同步表结构
DAOUtils.tableStructureSynchronized(tableStructureSynchronizedOption);
```

## 迁移数据库

QuickDAO支持迁移数据库,包括表结构和数据

```java
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
//过滤不需要迁移的表
migrateOption.tableFilter = (entity)->{
    if(targetDAO.hasTable(entity.tableName)){
        return false;
    }
    return true;
};
DAOUtils.migrate(migrateOption);
```

对于跨数据库类型迁移(例如mysql到sqlite),可能存在部分数据库列类型不兼容.
此时需要用户手动去设置数据库列类型

```java
//方法参数分别为源数据库表和对应的目标数据表信息
migrateOption.tableConsumer = (sourceTable,targetTable)->{
};
```