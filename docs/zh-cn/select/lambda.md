# lambda查询

> 该特性从v4.1.4版本开始提供

QuickDAO支持lambda查询条件

## Person实体类
```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
    private int type;
}
```

## lambdaCondition对象

调用``dao.query(Person.class).lambdaCondition();``就得到了Person类的lambdaCondition对象

```java
Condition condition = dao.query(Person.class)
    //获取lambda查询条件对象
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

LambdaCondition接口所定义的查询方法同Condition接口定义的方法相似,您可以参阅[复杂查询](complex.md)章节获取具体使用方法.

## lambdaSubCondition对象

在SubCondition接口对象上调用lambdaSubCondition()即可获取该SubCondition对应的lambdaSubCondition对象.

lambdaSubCondition接口所定义的查询方法同SubCondition接口定义的方法相似,您可以参阅[关联查询](joinTable.md)章节获取具体使用方法.