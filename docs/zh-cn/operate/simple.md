# 简单更新操作

```java
//插入单个Person实例
Person person = new Person();
dao.insert(person);
//插入Person数组
Person[] persons = new Person[0];
dao.insert(persons);

/*
更新Person实例,若存在唯一性约束,则根据唯一性约束更新
否则若存在id,则根据id更新,
若都不存在,则忽略更新操作
*/

//更新单个用户实例
dao.update(person);
//更新用户数组
dao.update(persons);

/**
保存Person实例,
判断Person实例是否已经存在于数据库中(根据唯一性约束和id判断)
若存在则执行更新操作
若不存在则执行插入操作
*/

//保存Person实例
dao.save(person);
//保存Perrson数组实例
dao.save(persons);
```

## 批量插入

为提高插入效率,QuickDAO支持批量插入(数据库batch)

> 此方法从4.1.8版本开始提供

```java
Product[] products = new Product[1000];
dao.insertBatch(products);
```

Condition接口的批量插入

```java
JSONArray array = new JSONArray();
IDGenerator idGenerator = new SnowflakeIdGenerator();
for(int i=0;i<1000;i++){
    JSONObject o = new JSONObject();
    o.put("name", "电冰箱");
    array.add(o);
}
dao.query("product")
    .addInsert(array)
    .execute()
    .insertBatch();
```