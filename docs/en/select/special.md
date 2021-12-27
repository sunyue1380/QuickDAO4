# Special Query

## AddColumn

``addColumn`` is used for specifying which fields will be returned. 

* Person

```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
    private int type;
}
```

## Return Single Column

```java
//select id from person
String id = (String)dao.query(Person.class)
                .addColumn("id")
                .execute()
                .getSingleColumn(String.class);
```

> ``getSingleColumn`` only return the first row of result.

## Return Single Column List

```java
//select id from person
List<Long> ids = dao.query(Person.class)
                .addColumn("id")
                .execute()
                .getSingleColumnList(Long.class);
```

## Return Part Columns

```java
//select username,password from person
List<Person> personList = dao.query(Person.class)
                .addColumn("username","password")
                .execute()
                .getList();
```

## Group By

```java
//select COUNT(ID) as count,max(id) as \"M(ID)\" from person group by id having count(id) = 1 order by max(id)
JSONArray array = dao.query(Person.class)
        .addColumn("count(id) as count")
        .addColumn("max(id) as \"M(ID)\"")
        .groupBy("id")
        .having("count(id) = 1",null)
        .orderByDesc("max(id)")
        .execute()
        .getArray();
```