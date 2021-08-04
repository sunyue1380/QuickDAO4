# 事务操作

QuickDAO封装了事务操作.事务操作只适用于更新操作,查询操作无事务操作.

```java
//开启事务
Transaction transaction = dao.startTransaction();
//设置事务隔离级别
transaction.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
try {
    User user = new User();
    user.setUserName("xxx");
    user.setPassword("xxx");
    transaction.insert(user);
    //提交
    transaction.commit();
}catch (Exception e){
    //回滚
    transaction.rollback();
    e.printStackTrace();
}finally {
    //结束事务
    transaction.endTransaction();
}
```

> 使用事务进行插入,更新和删除数据后,请务必手动调用commit方法或者rollback方法. 默认情况下QuickDAO不会自动提交事务

## 事务便捷方法

quickdao支持传入Consumer函数接口对象,只写业务逻辑即可

> 此方法从4.1.8开始提供

```java
dao.startTransaction((transaction)->{
    User user = new User();
    user.setUserName("xxx");
    user.setPassword("xxx");
    transaction.insert(user);
});
```