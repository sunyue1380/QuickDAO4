# Complex Query

* Person

```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
    private int type;
    private Date createdTime;
}
```

## Condition

Use ``dao.query(Person.class);`` to get ``Condition`` object

```java
Condition condition = dao.query(Person.class)
    //add distinct
    .distinct()
    //username is null
    .addNullQuery("username")
    //username is not null
    .addNotNullQuery("username")
    //username is not null and username = ''
    .addEmptyQuery("username")
    //username is not null and username != ''
    .addNotEmptyQuery("username")
    //type in (1,2)
    .addInQuery("type","1","2")
    //type not in (3,4)
    .addNotInQuery("type","3","4")
    //type between 1 and 2
    .addBetweenQuery("type",1,2)
    //username like 'quickdao%'
    .addLikeQuery("username","quickdao%")
    //type >= 1
    .addQuery("type",">=","1")
```

> All query method names start with ``addXXXXQuery``

## OR query

```java
//select distinct id,username,password,type from person where username like 'a%' or username like 'b%' 
Condition condition = dao.query(Person.class)
           .distinct()
           .addLikeQuery("username","a%");
condition.or().addQuery("username","b%")
Response response = condition.execute();
```

## Union Query

> Make sure all union statements return same fields

```java
Condition unionCondition1 = dao.query(Person.class)
    .addQuery("username","a")
    .addColumn("username","password");

Condition unionCondition2 = dao.query(Person.class)
    .addQuery("username","b")
    .addColumn("username","password");

Response response = dao.query(Person.class)
                    .union(unionCondition1)
                    .union(unionCondition2,UnionType.UnionAll)
                    .addColumn("username","password")
                    .execute();
```

## AddRawQuery

> The parameters will append to sql statement directly without escape. Please pay attention to secure questions.

```java
Condition condition = dao.query(Person.class)
     .addRawQuery("username = (select max(username) from person where age = t.age) ")
```

## Response

``Response`` defined methods how to get results.

```java
Condition condition = dao.query(User.class);
Response response = condition.execute();
List<User> userList = response.getList();
```

## Specify Return Column Type

> Since 4.1.2

When you invoke [Virtual Query](/en/select/virtual.md), you can specify return column type.

```java
dao.query(Person.class)
        .setColumnTypeMapping(property -> property.columnType.equals("datetime")?String.class:null)
        .execute()
```

You can specify global column type function.Please refer to [Configuration](/en/config/configuration.md)

> Thr priority of setColumnTypeMapping is higner than global column type function.

## Clone

Condition implements Cloneable.