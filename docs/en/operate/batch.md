# Batch Operation

* Batch update

```java
int count = dao.query(Person.class)
     .addQuery("username","quickdao")
     .addUpdate("password","123456")
     .execute()
     .update();
```


* Batch delete

```java
int count = dao.query(Person.class)
     .addQuery("username","quickdao")
     .execute()
     .delete();
```

