# DAO Configuration

QuickDAO provides plenty of configurations to customize your own behavior.

## Build quickdao

```java
BasicDataSource mysqlDataSource = new BasicDataSource();
mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
mysqlDataSource.setUsername("root");
mysqlDataSource.setPassword("123456");
DAO dao = QuickDAO.newInstance()
        //specify DataSource
        .dataSource(mysqlDataSource)
        //specify entity pakcage name
        .packageName("cn.schoolwow.quickdao.entity")
        //specify entity pakcage name with table name prefix
        .packageName("cn.schoolwow.quickdao.entity","quickdao")
        //scan single entity class
        .entity(Person.class)
        //weather automatic create table, default value is true
        .autoCreateTable(false)
        //weather automatic create column, default value is true
        .autoCreateProperty(false)
        //specify global id strategy
        .idStrategy(IdStrategy.IdGenerator)
        //specify global id generator(currentlly only SnowflakeIdGenerator, but you can implement your own generator)
        .idGenerator(new SnowflakeIdGenerator())
        //weather create foreign key in database, default value is false
        .foreignKey(false)
        //ignore entity class
        .ignoreClass(Person.class)
        //ignore package name
        .ignorePackageName("cn.schoolwow.quickdao.entity.ignore")
        //initial dao before first time to access database
        .lazyLoad(true)
        //build dao object
        .build();
```

## Automatically creating column

QuickDAO will automatically mapping classes or fields to database if you has setting entity package name or entity class.

If you add new field in java, it will also aotomatically mapping new field to database column. 

You don't need to execute sql manually. 

## define entity annotation Dynamically

In some cases, you can't modify entity class directly. Then you can use this method to add entity annotation.

```java
DAO dao = QuickDAO.newInstance()
                .define(Person.class)
                .tableName("p")
                .property("lastName")
                .notNull(true)
                .unique(true)
                .defaultValue("quickdao")
                .done()
                .comment("Person表")
                .done()
                .build();
```

## Specify global id strategy

You can specify global id strategy and it works for all fields which use @Id.

```java
public class User{
  @Id
  private long id;
}

QuickDAO.newInstance()
              .idStrategy(IdStrategy.IdGenerator)
              .done();
```

> If @Id field has its own strategy, then global id strategy has no effects.

Currently QuickDAO has only one ID Generator(SnowflakeIdGenerator),but you can implement IdGenerator interface to create your own ID generator.

> Attention! ID Generator only works for fields using @Id. Please remember to add @Id annotatation.

## Global column mapping

> Since 4.1.3

> Attention! This feature only affects [Vitual Query](/en/select/virtual.md)

JDBC defined mapping rules that how the column type converts to java class.However you can specify your own mapping rules.

> Before 4.1.2, the result type of vitual query is always String.class.

```java
QuickDAO.newInstance()
        .columnTypeMapping((property) -> {
            return String.class;
        });
```

Recommending code for user using 4.1.1 or before.

```java
QuickDAO.newInstance()
        .columnTypeMapping((property) -> {
            return String.class;
        });
```

## Specify value on insert and update

This feature would be very nice for you if you want to know that a record is inserted or updated by who. 

> Since 4.1.8

```java
DAO dao = QuickDAO.newInstance()
        .dataSource(dataSource)
        .packageName("cn.schoolwow.quickdao.entity")
        .insertColumnValueFunction((property)->{
            Object value = null;
            switch (property.column){
                case "insert_user_id":{value=1;}break;
            }
            return value;
        })
        .updateColumnValueFunction((property)->{
            Object value = null;
            switch (property.column){
                case "update_user_id":{value=2;}break;
            }
            return value;
        })
        .build();
```