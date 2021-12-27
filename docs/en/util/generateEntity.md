# Entity Class Generate

QuickDAO has the ability to scan database and generate corresponding entity class.

## Before 4.1.8

```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName("com.mysql.jdbc.Driver");
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
dataSource.setUsername("root");
dataSource.setPassword("123456");
DAO dao = QuickDAO.newInstance()
        //specify entity package name
        .packageName("cn.schoolwow")
        //specify DataSource        
        .dataSource(dataSource)
        .autoCreateTable(false)
        .autoCreateProperty(false)
        .build();
dao.generateEntityFile("/path/to/src/main/java/",null);
dao.generateEntityFile("/path/to/src/main/java/",new String[]{"person"});
```

## After 4.1.8

```java
GenerateEntityFileOption generateEntityFileOption = new GenerateEntityFileOption();
//specify java source file path
generateEntityFileOption.sourceClassPath = System.getProperty("user.dir")+"/src/main/java/com/schoolwow/quickdao/entity/";
//specify tables to generate
generateEntityFileOption.tableFilter = (entity)->{
    return true;
};
//java class and database column type mapping rule
generateEntityFileOption.columnFieldTypeMapping = (columnType)->{
    switch (columnType.toUpperCase()){
        case "NVARCHAR2":return "String";
        case "NUMBER":return "int";
        case "NCLOB":return "String";
        case "TIMESTAMP":return "java.sql.Timestamp";
    }
    return null;
};
//specify java entity package name(eg:biz.User)
generateEntityFileOption.entityClassNameMapping = (dbEntity,defaultEntityClassName)->{
    String newEntityClassName = dbEntity.tableName.substring(2).replaceFirst("_",".");
    String className = underline2Camel(newEntityClassName.substring(newEntityClassName.lastIndexOf(".")+1)).toLowerCase();
    className = className.toUpperCase().charAt(0)+className.substring(1);
    String finalEntityName = newEntityClassName.substring(0,newEntityClassName.lastIndexOf(".")+1).toLowerCase()+className;
    System.out.println(finalEntityName);
    return finalEntityName;
};
dao.generateEntityFile(generateEntityFileOption);
```