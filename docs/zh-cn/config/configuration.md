# 配置DAO对象

QuickDAO通过配置信息生成DAO对象以便对数据库进行操作.用户可根据实际需求设置相应配置信息

## 配置DAO对象

```java
//QuickDAO需要传递DataSource实现对象,您可以自由选择市面上的任意DataSource实现,本例采用dbcp
BasicDataSource mysqlDataSource = new BasicDataSource();mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");mysqlDataSource.setUsername("root");mysqlDataSource.setPassword("123456");DAO dao = QuickDAO.newInstance()
        //指定DataSource
        .dataSource(mysqlDataSource)
        //指定要扫描的实体类包,支持多级目录
        .packageName("cn.schoolwow.quickdao.entity")
        //指定要扫描的实体类包,支持多级目录,同时添加表名前缀quickdao
        .packageName("cn.schoolwow.quickdao.entity","quickdao")
        //指定扫描单个实体类
        .entity(Person.class)
        //是否自动新增表,默认开启
        .autoCreateTable(false)
        //是否自动新增字段,默认开启
        .autoCreateProperty(false)
        //指定全局Id策略
        .idStrategy(IdStrategy.IdGenerator)
        //指定全局Id生成期实例(目前只支持雪花算法)
        .idGenerator(new SnowflakeIdGenerator())
        //是否建表时建立外键约束,默认关闭
        .foreignKey(false)
        //忽略指定实体类
        .ignoreClass(Person.class)
        //忽略指定实体类包
        .ignorePackageName("cn.schoolwow.quickdao.entity.ignore")
        //返回DAO接口对象
        .build();
```

## 自动建表和新增字段

QuickDAO支持自动建表和自动新增字段。QuickDAO启动后回自动扫描注册的实体类并创建对应的数据库表。

当您在实体类上添加了新字段时，重启应用程序，QuickDAO会自动的在数据库中也创建对应的数据库字段。

## 动态定义实体类注解

QuickDAO支持动态指定实体类注解,此功能适用于扫描第三方实体类包,即无法编辑实体类源码时.

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

## 指定全局Id策略

您可以指定全局Id生成策略,该全局策略对所有使用@Id注解的字段起作用

```java
public class User{
  @Id
  private long id;
}

QuickDAO.newInstance()
              //指定全局Id策略
              .idStrategy(IdStrategy.IdGenerator)
              .done();
```

> 若手动使用@Id注解设置了strategy属性,则使用@Id注解所设置的策略

QuickDAO内置了SnowflakeIdGenerator生成器,您也可以通过实现IdGenerator接口自定义Id生成器

> id生成器只对所有用@Id注解的属性起效果,请务必在id属性上添加@Id注解

## 全局字段类型转换

> 此特性从v4.1.3版本开始提供

QuickDAO允许指定返回结果的字段类型,默认情况下返回JDBC返回的数据类型

> 注意:此方法仅影响[虚拟查询](virtual.md)相关方法返回结果

> 在v4.1.2版本之前,所有虚拟查询返回结果都是String类型且无法改变.

```java
QuickDAO.newInstance()
        //指定全局列类型转换函数
        .columnTypeMapping((property) -> {
            //property属性包含了字段的列名,数据库类型等信息
            return String.class;
        });
```

为了兼容性考虑,建议从v4.1.1版本升级的用户使用以下配置兼容

```java
QuickDAO.newInstance()
        //指定全局列类型转换函数
        .columnTypeMapping((property) -> {
            //为了兼容考虑,v4.1.2版本之前默认全部返回String类型
            return String.class;
        });
```

## 插入和更新时设置指定值

QuickDAO支持在插入和更新时设置实例的某些字段值.

使用场景: 数据库表需要存储当前插入或者更新记录的用户是谁.

> 此方法从QuickDAO4.1.8版本开始提供

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
