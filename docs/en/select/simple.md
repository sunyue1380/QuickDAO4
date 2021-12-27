# Simple Query

QuickDAO provides convenient methods to get result.

* Person

```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
}
```

## Simple Query Example

```java
//sql: select id,username,password from person where id = 1
User user1 = dao.fetch(Person.class,1);

//sql: select id,username,password from person where username = 'quickdao'
User user2 = dao.fetch(User.class,"username","quickdao");

//sql: select id,username,password from person where username = 'quickdao'
List<User> userList = dao.fetchList(User.class,"username","quickdao");
```

> ``fetch`` method will only return first row of record even there are more than one rows in result.

## RawSelect

> Since 4.1.9

```java
JSONArray array = dao.rawSelect("select * from person where password = ?","123456");
```