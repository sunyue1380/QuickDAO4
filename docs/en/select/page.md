# Pagination and Order By 

```java
Condition condition = dao.query(Person.class)
       .page(1,10)
       .limit(0,10)
       .orderBy("id")
       .orderByDesc("id")
       //this method provides since 4.1.4
       .order("id", "asc");
Response response = condition.execute();
PageVo<Person> personList = response.getPagingList();
```
## PageVo

```java
public class PageVo<T> implements Serializable {
    private List<T> list;
    private long totalSize;
    private int totalPage;
    private int pageSize;
    private int currentPage;
    private boolean hasMore;
}
```

## Oracle

Oracle Database pagination is supported from 4.1.8.

> Attention! You must invoke ``execute`` method immediately after you invoke ``page`` or ``limit`` method for Oracle's wired pagination rule.