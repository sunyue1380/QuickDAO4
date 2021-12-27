# Virtual Query(No Entity Class Query)

Normally QuickDAO will scan entity package and mapping entity class to database table.

However QuickDAO also can query database without any entity classes.
  
Assuming there is a table named **person** in database.

person table:

|id|username|password|type|
|:---:|:---:|:---:|:---:|
|1|tom|123456|1|
|2|jack|123456|2|

## Simple Query

```java
JSONObject person = dao.fetch("person","username","tom");
JSONArray person = dao.fetchList("person","password","123456");
```

## Complex Query
```java
//select id,username,password,type from person where username = 'tom'
JSONArray array = dao.query("person")
    .addQuery("username","tom")
    .addColumn("id","username","password","type")
    .execute()
    .getArray();
```

> Attention! You must specify return column by invoking ``addColumn`` method if you use Virual Query feature.

## Insert

```java
//insert into person(username,password) value('tony','123456')
int effect = dao.query("person")
    .addInsert("username","tony")
    .addInsert("password","123456")
    .execute()
    .insert();
```

## Update

```java
//update person set password = '654321' where username = 'tony'
int effect = dao.query("person")
    .addUpdate("password","654321")
    .addQuery("username","tony")
    .execute()
    .update();
```

## Delete

```java
//delete from person where username = 'tony'
int effect = dao.query("person")
    .addQuery("username","tony")
    .execute()
    .delete();
```

## Dual

```java
//select '1' id from dual
JSONArray array = dao.query("dual")
   .addColumn("'1' id")
   .execute()
   .getArray();
```