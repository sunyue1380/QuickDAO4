# ID generate strategy

Currently there are three strategies:

```java
public enum IdStrategy {
    /**handle by user */
    None,
    /**autoincrement(default value)*/
    AutoIncrement,
    /**use id generator*/
    IdGenerator;
}
```

There are two ways to specify id strategy.

```java
public class User{
    @Id(strategy=IdStrategy.AutoIncrement)
    private long uid;
}
```

or

```java
DAO dao = QuickDAO.newInstance()
        //......
        //specify global id strategy
        .idStrategy(IdStrategy.IdGenerator)
        //specify global id generator(currentlly only SnowflakeIdGenerator, but you can implement your own generator)
        .idGenerator(new SnowflakeIdGenerator())
        .build();
```

> The priority of @Id is greater than ``idStrategy`` method.

# Builtin ID Generator

Currently there only one Id Generator named SnowflakeIdGenerator. However you can customize you own Generator by implementing IdGenerator. 