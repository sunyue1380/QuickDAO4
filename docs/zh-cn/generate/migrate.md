# 数据源迁移

> 此特性从v4.1.5版本开始提供

QuickDAO支持数据源迁移功能,可将数据从一个数据源迁移至另一个数据源,且支持跨数据库类型(例如从MySQL迁移到SQLite)

```java
//原始数据源
BasicDataSource sourceDataSource = new BasicDataSource();
dataSource.setDriverClassName("com.mysql.jdbc.Driver");
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
dataSource.setUsername("root");
dataSource.setPassword("123456");
DAO sourceDAO = QuickDAO.newInstance()
        //指定实体类的包名,该包名作为生成实体类的包名
        .packageName("cn.schoolwow")
        //指定DataSource        
        .dataSource(sourceDataSource)
        .autoCreateTable(false)
        .autoCreateProperty(false)
        .build();
//目标数据源
BasicDataSource targetDataSource = new BasicDataSource();
dataSource.setDriverClassName("org.sqlite.JDBC");
dataSource.setUrl("jdbc:sqlite:quickdao_sqlite.db");
DAO targetDAO = QuickDAO.newInstance()
        //指定实体类的包名,该包名作为生成实体类的包名
        .packageName("cn.schoolwow")
        //指定DataSource        
        .dataSource(targetDataSource)
        .autoCreateTable(false)
        .autoCreateProperty(false)
        .build();
//从源数据源迁移到目标数据源
sourceDAO.migrateTo(targetDAO);
```