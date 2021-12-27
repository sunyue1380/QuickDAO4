# DDL

## hasTable

```java
dao.hasTable("person");
```

## hasColumn

```java
dao.hasColumn("person","name");
```

## getPropertyList

```java
List<Property> propertyList = dao.getPropertyList("person");
```

## getProperty

```java
Property property = dao.getProperty("person","name");
```

## create Table

```java
dao.create(Person.class);
Entity entity = new Entity();
entity.tableName = "person";
entity.comment = "人";
entity.charset = "utf-8";
//column list
entity.properties = new ArrayList<Property>();
dao.create(entity);
```

## drop Table

```java
dao.drop(Person.class);
dao.drop("person");
```

## rebuild

```java
dao.rebuild(Person.class);
dao.rebuild("person");
```

## createColumn

```java
Property property = new Property();
property.column = "name";
property.columnType = "varchar(16)";
property.comment = "姓名";
dao.createColumn("person",property);
```

## dropColumn

```java
dao.dropColumn("person","name");
```

## syncEntityList

Make entity package class same with database table.

```java
dao.syncEntityList();
```

## automaticCreateTableAndField

```java
dao.automaticCreateTableAndField();
```

## refreshDbEntityList

```java
dao.refreshDbEntityList();
```