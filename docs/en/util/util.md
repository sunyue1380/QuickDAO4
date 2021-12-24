# DAO util

> Since 4.1.8

## Sync Table Structure

```java
//Source Database
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
//Target Database
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
//weather synchronized this table
tableStructureSynchronizedOption.createTablePredicate = (entity)->{
    System.out.println(entity);
    return false;
};
//weather synchronized this column
tableStructureSynchronizedOption.createPropertyPredicate = (property)->{
    System.out.println(property);
    return false;
};
DAOUtils.tableStructureSynchronized(tableStructureSynchronizedOption);
```

## Migrate Database

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
//weather migrate this table 
migrateOption.tableFilter = (entity)->{
    if(targetDAO.hasTable(entity.tableName)){
        return false;
    }
    return true;
};
DAOUtils.migrate(migrateOption);
```

It may appear column type compatible problem for cross database migration(eg from mysql to sqlite).

In this situation, user should customize column type mapping.

```java
migrateOption.tableConsumer = (sourceTable,targetTable)->{
//define how the source table migrate to target table
};
```