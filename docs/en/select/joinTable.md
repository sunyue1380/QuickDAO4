# Join Table 

The core method to achieve foreign key query feature is ``joinTable``.

* Entity Class

```java

/**Person*/
public class Person{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    
    private String lastName;
    //omit get/set
}

/**Order*/
public class Order{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    @ForeignKey(table = Person.class)
    private long personId;
    
    private String lastName;
    
    private Person person;
    //omit get/set
}

/**Address*/
public class Address{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;

    private String name;

    @ForeignKey(table = Order.class)
    private long orderId;
    
    private Order order;
    //omit get/set
}
```

## Main table, Child Table, Parent Table

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    .doneSubCondition()
    .done();
```

``joinTable`` is used for join table. The definition as following:

```java
<E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField);
```

* clazz: which table to join
* primaryField: **Main Table** field
* joinTableField: **Child Table** field

The meaning of Main Table and Child Table as following

* Main Table: dao.query(Class mainTable) mainTable is Main Table
* Child Table: joinTable(Class<E> childTable, String primaryField, String joinTableField) childTable is Child Table

Person is **Main Table** and Order is **Child Table** in above.

Furthermore, **Child Table** can join table again. At this time, origin child table called **Parent Table** and join table called **Child Table**.

Order is **Parent Table** and Address is **Child Table** in above.

Invoking ``done`` to return **Main Table** and invoking ``doneSubCondition`` to return **Parent Table**.

## Table Alias

The **Main Table** alias name is t and next join table is t1,t2,t3......

You can invoke ``tableAliasName`` method to specify your own table alias name.

```java
dao.query(Person.class).tableAliasName("p");
```

```java
dao.query(Person.class)
    .joinTable(Person.class,"motherId","id","mother")
    .tableAliasName("motherTable")
    .done()
    .joinTable(Person.class,"fatherId","id","father")
    .tableAliasName("fatherTable")
    .done()
```

## Join Table Query

You can still use addXXXQuery when you invoking ``joinTable``.

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //order.person_id > 0
    .addQuery("person_id",">",0)
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    //address.name = 'quickdao'
    .addQuery("name","quickdao")
    .doneSubCondition()
    .done();
```

## Multiple Field Join Query

> Since 4.1.3

You can join table with more than one condition by using ``on`` method.

```java
//from person
dao.query(Person.class)
        //join order on person.id = order.person_id
        .joinTable(Order.class,"id","personId")
        //and person.last_name = order.last_name
        .on("lastName","lastName")
        .done();
```

## CompositField

The composit member fields in entity class will not be filled by default if you don't invoke ``compositField`` method.

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //order.person_id > 0
    .addQuery("person_id",">",0)
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    //address.name = 'quickdao'
    .addQuery("name","quickdao")
    .doneSubCondition()
    .done()
    //invoke compositField to fill field person field in Order class and order in Address class
    .compositField();
```

> QuickDAO will search the only one entity type member field by default.

## Multiple Field Associate

You must specify associated member field name if there are more than one entity class memeber field.

```java
public class Person {
    private long id;
    private long motherId;
    private long fatherId;
    private Person mother;
    private Person father;
}
```

```java
List<Person> userList = dao.query(Person.class)
        //person1.mother_id = person2.id, fill to mother
        .joinTable(Person.class,"mother_id","id","mother")
        .done()
        //person1.father_id = person2.id, fill to father
        .joinTable(Person.class,"father_id","id","father")
        .done()
        .compositField()
        .execute()
        .getList();
```

## Join Condition

You can pass a Condition object as a query parameter.

```java
//select id,username,password,type from person as t join (select person_id,count(person_id) count from `order` group by person_id having count(person_id) > 0) t1 on t.id = t1.person_id where t.person_id = 1 order by t.id desc
Condition joinCondition = dao.query(Order.class)
                    .addColumn("person_id","count(person_id) count")
                    .groupBy("personId")
                    .having("count(person_id) > 0");
            Response response = dao.query(Person.class)
                    .joinTable(joinCondition,
                            "id","person_id")
                    .done()
                    .addQuery("person_id",1)
                    .orderByDesc("id")
                    .execute();
```

## cross join

```java
//select id,username,password,type from person as t cross join order as t1 where t.id =t1.person_id and t1.order_no > 0
List<Person> personList = dao.query(Person.class)
                    .crossJoinTable(Order.class)
                    .addQuery("orderNo",">",0)
                    .done()
                    .addRawQuery("t.id = t1.person_id")
                    .execute()
                    .getList();
```