# DDL操作

QuickDAO支持DDL相关操作.

## 表是否存在

```java
dao.hasTable("person");
```

## 列是否存在

```java
dao.hasColumn("person","name");
```

## 建表

```java
dao.create(Person.class);
Entity entity = new Entity();
entity.tableName = "person";
entity.comment = "人";
entity.charset = "utf-8";
//设置字段信息
entity.properties = new ArrayList<Property>();
dao.create(entity);
```

## 删表

```java
dao.drop(Person.class);
dao.drop("person");
```

## 重建表

```java
dao.rebuild(Person.class);
dao.rebuild("person");
```

## 新增字段

```java
Property property = new Property();
property.column = "name";
property.columnType = "varchar(16)";
property.comment = "姓名";
dao.createColumn("person",property);
```

## 删除字段

```java
dao.dropColumn("person","name");
```

## 双向同步

让扫描实体类包信息和数据库保持一致,即新增实体类信息存在但数据库不存在的表和字段信息,删除数据库里多余的字段和表.

```java
dao.syncEntityList();
```

## 自动建表和新增字段

```java
dao.automaticCreateTableAndField();
```

## 刷新数据库字段信息

重新从数据库里获取表和字段信息

```java
dao.refreshDbEntityList();
```