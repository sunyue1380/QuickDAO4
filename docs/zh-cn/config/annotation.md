# 实体注解

QuickDAO提供了实体类注解用于用户自定义字段属性.

## @Id

标识Id字段,作用于类字段上

```java
public class User{
    @Id
    private long uid;
}
```

### Id生成策略

@Id注解带有strategy属性,strategy的值为以下值之一

* None(用户自己手动设置该Id属性)
* AutoIncrement(设置为数据库自增,默认值)
* IdGenerator(使用Id生成器,需要在配置DAO对象时手动指定Id生成器)

每个实体类可以单独设置自己的Id生成策略,同时也可以在配置DAO对象时指定全局Id策略.@Id注解优先于全局策略.

全局Id生成器策略请参阅[配置DAO](/zh-cn/config/configuration.md)

## @ColumnName

映射字段名,作用于类字段上

```java
public class User{
    @ColumnName("uid")
    private long id;
}
```

## @TableName

映射表名,作用于类上

```java
@TableName("myUser")
public class User{
}
```

## @ColumnType

自定义数据库类型,作用于类字段上

```java
public class User{
    @ColumnType("varchar(1024)")
    private String username;
}
```

## @Comment

添加数据库注释,作用于类和成员变量上

```java
@Comment("用户表")
public class User{
    @Comment("用户名")
    private String username;
}
```

## @Index

在该字段上建立索引,作用于类字段上

> 建议在有唯一性约束的字段上建立索引,以便加快检索速度

```java
public class User{
    @Index
    private String username;
}
```

> 从4.1.2版本开始新增以下属性

|属性|说明|默认值|
|---|---|---|
|indexType|索引类型|IndexType.NORMAL 一般索引|
|indexName|索引名称|为空则程序自动生成|
|using|索引方法|为空则使用数据库默认值|
|comment|索引注释|无|

同时支持重复注解,可在一个字段上添加多个Index注解

## @CompositeIndex

> 此注解从4.1.2开始提供

作用于类,在表上建立组合索引

```java
@CompositeIndex(columns={"username","password"})
public class User{
    private String username;
    private String password;
}
```

同时支持重复注解,可在一个类上添加多个CompositeIndex注解

## @UniqueField

> 此注解从4.1.2开始提供

作用于类,指定哪些字段作为判断该记录是否唯一的依据

此注解主要影响exist,save方法,用于判断记录是否存在于数据库中.

```java
@UniqueField(columns={"username"})
public class User{
    private String username;
}
```

## @Constraint

添加约束,包括是否非空,check约束以及默认值,作用于类字段上

* notNull 是否非空,默认为false
* check check约束,默认为空
* defaultValue 默认值,默认为为空

> 4.1.2版本移除了原来的unique和unionUnique属性,使用@UniqueField和@CompositeIndex注解代替.

```java
public class User{
    //userId属性id必须大于0
    @Constraint(check = "#{userId} > 0")
    private String userId;
 
    //设置username属性非空且唯一
    @Constraint(notNull=true, unique=true)
    private String username;

    //年龄默认设置为5岁
    @Constraint(defaultValue="5")
    private int age;
}
```

## @ForeignKey

添加外键约束,指定关联字段以及外键字段更新和删除时的策略,作用于类字段上

* table 关联到哪张表
* field 关联到表的哪个字段,默认为id
* foreignKeyOption 外键级联更新策略,默认为NOACTION

```java
public class User{ 
    @ForeignKey(table=Address.class,field="id",foreignKeyOption=ForeignKeyOption.RESTRICT)
    private long addressId;
}
```

## @Table

作用于类

* charset 指定表编码
* engine 指定表引擎

目前该注解只对Mysql数据库有效.

```java
@Table(charset="utf8",engine="innoDB")
public class User{
    @Ignore
    private int age;
}
```

## @TableField

作用于类字段

* createdAt 是否填充插入时间,默认为false
* updatedAt 是否填充更新时间,默认为false

```java
@TableField(createdAt = true)
private Date createdAt;

@TableField(updatedAt = true)
private Date updatedAt;
```

* function 指定字段函数属性

指定了function属性后,在查询,新增和更新操作时,会自动用指定的function值替换对应的SQL语句部分.

场景举例: 密码字段使用md5函数加密

```java
//以下注解会使得在查询,插入,更新此字段时都会自动执行md5函数
@TableField(function = "md5(concat('salt#',#{password}))")
private String password;
```
## @Ignore

忽略该类/忽略该字段,作用于类和类字段上

忽略age属性

```java
public class User{
    @Ignore
    private int age;
}
```

忽略Address类

```java
@Ignore
public class Address{
}
```

> 默认情况下QuickDAO会自动忽略实体类中集合类成员变量以及实体类成员变量,除非您调用了Condition类的compositField方法

```java
public class User{
    private Address address;
    private List<Long> userIds;
    //默认情况下上述属性均会自动被QuickDAO所忽略
}
```
