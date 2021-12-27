# Lambda

> Since 4.1.4

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

## lambdaCondition

```java
Condition condition = dao.query(Person.class)
    .lambdaCondition
    //username is null
    .addNullQuery(Person::getUsername)
    //username is not null
    .addNotNullQuery(Person::getUsername)
    //username is not null and username = ''
    .addEmptyQuery(Person::getUsername)
    //username is not null and username != ''
    .addNotEmptyQuery(Person::getUsername)
    //type in (1,2)
    .addInQuery(Person::getType,"1","2")
    //type not in (3,4)
    .addNotInQuery(Person::getType,"3","4")
    //type between 1 and 2
    .addBetweenQuery(Person::getType,1,2)
    //username like 'quickdao%'
    .addLikeQuery(Person::getUsername,"quickdao%")
    //type >= 1
    .addQuery(Person::getType,">=","1")
```

> Please refer to [Complex Query](/en/select/complex.md) for more infomation.

## lambdaSubCondition

Invoking ``lambdaSubCondition`` on SubCondition to get lambdaSubCondition object.

> Please refer to [Join Table Query](/en/select/joinTable.md) for more infomation.