# Simple DML Operation

```java
//insert single person
Person person = new Person();
dao.insert(person);
//insert person array
Person[] persons = new Person[0];
dao.insert(persons);

/**
if person has unique constraint then updated by unique constraint;
else if person has id then updated by id,
else do nothing.
*/

//update single person
dao.update(person);
//update person array
dao.update(persons);

/**
if the person exists(judged by unique constraint and id) in record then update
else insert person
*/

//save single person
dao.save(person);
//save person array
dao.save(persons);
```

## Batch insert

> Since 4.1.8

```java
Product[] products = new Product[1000];
dao.insertBatch(products);
```

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

## RawUpdate

> Since 4.1.9

```java
effect = dao.rawUpdate("insert into DOWNLOAD_TASK(file_path,file_size,remark) values('filePath',0,'remark');");
```