# Transaction

```java
//start transaction
Transaction transaction = dao.startTransaction();
transaction.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
try {
    User user = new User();
    user.setUserName("xxx");
    user.setPassword("xxx");
    transaction.insert(user);
    //commit
    transaction.commit();
}catch (Exception e){
    //rollback
    transaction.rollback();
    e.printStackTrace();
}finally {
    //end transaction
    transaction.endTransaction();
}
```

> Attention! Please remeber to invoke ``commit`` or ``rollback`` manually.

## Convenient Method

> Since 4.1.8

```java
dao.startTransaction((transaction)->{
    User user = new User();
    user.setUserName("xxx");
    user.setPassword("xxx");
    transaction.insert(user);
});
```