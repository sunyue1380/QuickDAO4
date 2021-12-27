# Entity Annotation

QuickDAO supports entity annotation.

## @Id

Specify database id, using in field

```java
public class User{
    @Id
    private long uid;
}
```

### Id generate strategy

@Id has a attribute named ``strategy`` whose value as following:

* None(handle by user)
* AutoIncrement(database autoincrement)
* IdGenerator(use IdGenerator to generate id)

Every entity class can set its own strategy and meanwhile you can set global id strategy(see [Configuration](/en/config/configuration.md))

The priority of @Id strategy is higher than global @Id strategy.

## @ColumnName

Specify column name, using in field

```java
public class User{
    @ColumnName("uid")
    private long id;
}
```

## @TableName

Specify table name, using in class

```java
@TableName("myUser")
public class User{
}
```

## @ColumnType

Specify column type, using in field

```java
public class User{
    @ColumnType("varchar(1024)")
    private String username;
}
```

## @Comment

Specify database comment, using in class or field

```java
@Comment("user table")
public class User{
    @Comment("username field")
    private String username;
}
```

## @Index

Creating database index, using in field

> Recommand creating index in a unique constraint column to accelerate searching speed

```java
public class User{
    @Index
    private String username;
}
```

> Since 4.1.2 @Index has new attributes as following:

|Field|Remark|
|---|---|
|indexType|default value is IndexType.NORMAL|
|indexName|if empty then automatically generate|
|using|btree or hash, if empty it will use database default value|
|comment|index comment|

you can add more than one @Index annotation in same field.

## @CompositeIndex

> Since 4.1.2

Create composit indexes, using in class

```java
@CompositeIndex(columns={"username","password"})
public class User{
    private String username;
    private String password;
}
```

You can add more than one @CompositeIndex annotation in same class.

## @UniqueField

> Since 4.1.2 

Specify fields weather a record is unique or not. This annotation effects ``exist`` and ``save`` methods. 

```java
@UniqueField(columns={"username"})
public class User{
    private String username;
}
```

## @Constraint

Specify database constraints includes notnull, check, defaultValue.

* notNull: default false
* check: default empty
* defaultValue: default empty

> After 4.1.2, unique and unionUnique attributes are removed from @Constraint. Please use @UniqueField and @CompositeIndex instead.

```java
public class User{
    //userId must greater than 0
    @Constraint(check = "#{userId} > 0")
    private String userId;
 
    //username must not null and should be unique 
    @Constraint(notNull=true, unique=true)
    private String username;

    //set age default value
    @Constraint(defaultValue="5")
    private int age;
}
```

## @ForeignKey

Specify foreign key constraint, using in class

* table: refer to join table
* field: refer to join table field, default value is ``id``
* foreignKeyOption: cascade strategy, default value is NOACTION

```java
public class User{ 
    @ForeignKey(table=Address.class,field="id",foreignKeyOption=ForeignKeyOption.RESTRICT)
    private long addressId;
}
```

## @Table

Using in class

* charset: Specify table charset
* engine: Specify db engine

> Currently this annotation only works for MySQL.

```java
@Table(charset="utf8",engine="innoDB")
public class User{
    @Ignore
    private int age;
}
```

## @TableField

Using in field

* createdAt: fill timestamp value before insert
* updatedAt: fill timestamp value before update

```java
@TableField(createdAt = true)
private Date createdAt;

@TableField(updatedAt = true)
private Date updatedAt;
```

* function: Specify field function

When setting function value, corresponding field sql will be replaced on select, insert and update. 

For example, encrypting password with md5.

```java
@TableField(function = "md5(concat('salt#',#{password}))")
private String password;
```

## @Ignore

Ignore this column, using in class and field.

QuickDAO won't mapping ignored column to database.

```java
public class User{
    @Ignore
    private int age;
}
```

```java
@Ignore
public class Address{
}
```

> QuickDAO will ignore entity type field and array by default unless you invoke ``compositField`` method

```java
public class User{
    private Address address;
    private List<Long> userIds;
    //foregoing fields will be ignored by default
}
```