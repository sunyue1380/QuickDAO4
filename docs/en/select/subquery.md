# SubQuery

* Entity Class

```java
public class Product {
    @Id(strategy = IdStrategy.IdGenerator)
    private long id;

    private String name;

    private String type;

    private int price;

    @TableField(createdAt = true)
    private Date publishTime;
}
```

## Where SubQuery

```java
//select count(1) from product where price < (select avg(price) from product)
long count = dao.query(Product.class)
                .addSubQuery("price","<",dao.query(Product.class).addColumn("avg(price)"))
                .execute()
                .count();
```

## Having SubQuery

```java
//select count(type) count from product group by type having count(type) > (select 1 from dual)
Condition havingCondition = dao.query("dual")
                .addColumn("1");
long count = (long) dao.query(Product.class)
        .groupBy("type")
        .having("count(type)",">",havingCondition)
        .addColumn("count(type) count")
        .execute()
        .getSingleColumn(Long.class);
```

## From SubQuery

```java
//select type avgPrice from (select type, avg(price) avgPrice from product group by type) where avgPrice >= 2000
Condition<Product> fromCondition = dao.query(Product.class)
                .groupBy("type")
                .addColumn("type")
                .addColumn("avg(price) avgPrice");
JSONArray array = dao.query(fromCondition)
               .addQuery("avgPrice",">=",2000)
               .addColumn("type","avgPrice")
               .execute()
               .getArray();
```

## Select SubQuery

```java
//select (select name from dual) nameAlias from product 
Condition selectCondition = dao.query("dual")
                .addColumn("name");
List<String> productNameList = dao.query(Product.class)
                .addColumn(selectCondition,"nameAlias")
                .execute()
                .getSingleColumnList(String.class);
```

## Exist SubQuery

```java
//select name from product where exists (select id from product where price >= 5000)
List<String> productNameList = dao.query(Product.class)
                .addExistSubQuery(
                        dao.query(Product.class)
                                .addQuery("price",">=",5000)
                                .addColumn("id")
                )
                .addColumn("name")
                .execute()
                .getSingleColumnList(String.class);
```