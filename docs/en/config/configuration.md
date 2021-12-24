# DAO Configuration

QuickDAO provides plenty of configuration to customize your own behavior.

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
        //build dao object
        .build();
```

## Automatic create column

When you add new class entity class or new field, quickdao will automatically mapping these classes or fields to database.

You don't need to execute sql manually. 

## Dynamic define entity annotation

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

You can specify global id strategy and this will be works for all fields which use @Id.

```java
public class User{
  @Id
  private long id;
}

QuickDAO.newInstance()
              .idStrategy(IdStrategy.IdGenerator)
              .done();
```

> If @Id field has its own strategy, then global id strategy has no effect.

Currently QuickDAO only has one ID Generator(SnowflakeIdGenerator),but you can implement IdGenerator interface to create your own ID generator.

> Attention! ID Generator only works for fields using @Id. Please remember to add @Id annotatation.

## Global column mapping

> Since 4.1.3

> Attention! This feature only effect [Vitual Query](/en/select/virtual.md)

JDBC defined mapping rules that how the column type convert to java class.But you can specify your own mapping rules.

> Before 4.1.2, vitual query result type is String.class and can't be changed.

```java
QuickDAO.newInstance()
        .columnTypeMapping((property) -> {
            return String.class;
        });
```

Recommend code for user using 4.1.1 or before.

```java
QuickDAO.newInstance()
        .columnTypeMapping((property) -> {
            return String.class;
        });
```

## Specify value on insert and update

If you want to know a record inserted by who and when, this feature works for you.

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