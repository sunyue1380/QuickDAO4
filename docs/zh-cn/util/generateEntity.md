# 实体类生成

QuickDAO支持扫描数据库表信息并自动生成对应Entity类.
此方法在4.1.8版本后有重大改变.

## 4.1.8版本之前的实体类生成方法

```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName("com.mysql.jdbc.Driver");
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
dataSource.setUsername("root");
dataSource.setPassword("123456");
DAO dao = QuickDAO.newInstance()
        //指定实体类的包名,该包名作为生成实体类的包名
        .packageName("cn.schoolwow")
        //指定DataSource        
        .dataSource(dataSource)
        .autoCreateTable(false)
        .autoCreateProperty(false)
        .build();
//指定Java源码所在文件夹,生成所有数据库表对应的实体类
dao.generateEntityFile("/path/to/src/main/java/",null);
//生成指定表对应的实体类
dao.generateEntityFile("/path/to/src/main/java/",new String[]{"person"});
```

## 4.1.8版本之后的实体类生成方法

```java
GenerateEntityFileOption generateEntityFileOption = new GenerateEntityFileOption();
//设置java类路径
generateEntityFileOption.sourceClassPath = System.getProperty("user.dir")+"/entity/";;
//设置哪些表需要生成实体类
generateEntityFileOption.tableFilter = (entity)->{
    return true;
};
//Java类和数据库列类型转换,返回null时使用quickdao默认转换类型
generateEntityFileOption.columnFieldTypeMapping = (columnType)->{
    switch (columnType.toUpperCase()){
        case "NVARCHAR2":return "String";
        case "NUMBER":return "int";
        case "NCLOB":return "String";
        case "TIMESTAMP":return "java.sql.Timestamp";
    }
    return null;
};
//设置返回的java实体类包名,支持返回biz.User,则对应实体类包名为biz.User
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