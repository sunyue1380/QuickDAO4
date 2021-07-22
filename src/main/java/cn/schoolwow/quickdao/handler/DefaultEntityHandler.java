package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.annotation.*;
import cn.schoolwow.quickdao.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**负责扫描实体类信息*/
public class DefaultEntityHandler implements EntityHandler{
    private Logger logger = LoggerFactory.getLogger(DefaultEntityHandler.class);
    private QuickDAOConfig quickDAOConfig;

    public DefaultEntityHandler(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    /**
     * 获取实体类信息
     */
    @Override
    public synchronized void getEntityMap() throws Exception {
        List<Class> classList = new ArrayList<>();
        //扫描实体类包
        for (String packageName : quickDAOConfig.packageNameMap.keySet()) {
            List<Class> packageClassList = scanEntity(packageName);
            for (Class c : packageClassList) {
                Entity entity = new Entity();
                if(c.getDeclaredAnnotation(TableName.class)!=null){
                    entity.tableName = ((TableName) c.getDeclaredAnnotation(TableName.class)).value();
                }else if ((packageName.length() + c.getSimpleName().length() + 1) == c.getName().length()) {
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+camel2Underline(c.getSimpleName());
                } else {
                    String prefix = c.getName().substring(packageName.length() + 1, c.getName().lastIndexOf(".")).replace(".", "_");
                    entity.tableName = quickDAOConfig.packageNameMap.get(packageName)+prefix + "@" + camel2Underline(c.getSimpleName());
                }
                entity.clazz = c;
                quickDAOConfig.entityMap.put(c.getName(), entity);
            }
            classList.addAll(packageClassList);
        }
        //扫描指定实体类
        for(Class c:quickDAOConfig.entityClassMap.keySet()){
            Entity entity = new Entity();
            if(c.getDeclaredAnnotation(TableName.class)!=null){
                entity.tableName = ((TableName) c.getDeclaredAnnotation(TableName.class)).value();
            }else if(quickDAOConfig.entityClassMap.get(c).isEmpty()){
                entity.tableName = camel2Underline(c.getSimpleName());
            }else{
                entity.tableName = quickDAOConfig.entityClassMap.get(c)+"@"+camel2Underline(c.getSimpleName());
            }
            entity.clazz = c;
            quickDAOConfig.entityMap.put(c.getName(), entity);
            classList.add(c);
        }
        Map<String,String> typeFieldMapping = quickDAOConfig.database.getDDLBuilderInstance(quickDAOConfig).getTypeFieldMapping();
        for (Class c : classList) {
            Entity entity = quickDAOConfig.getEntityByClassName(c.getName());
            entity.escapeTableName = quickDAOConfig.database.escape(entity.tableName);

            //属性列表
            List<Property> propertyList = new ArrayList<>();
            //实体包类列表
            Field[] fields = getAllField(c);
            for(Field field:fields){
                //跳过实体包类
                if (isCompositProperty(field.getType())) {
                    if(!entity.compositFieldMap.containsKey(field.getType().getName())){
                        entity.compositFieldMap.put(field.getType().getName(),new ArrayList<>());
                    }
                    entity.compositFieldMap.get(field.getType().getName()).add(field.getName());
                    continue;
                }

                Property property = new Property();
                if (null!=field.getAnnotation(ColumnName.class)) {
                    property.column = field.getAnnotation(ColumnName.class).value();
                }else{
                    property.column = camel2Underline(field.getName());
                }
                property.name = field.getName();
                property.columnLabel = property.name;
                property.clazz = field.getType();
                property.className = field.getType().getName();
                if(null!=field.getAnnotation(ColumnType.class)){
                    property.columnType = field.getAnnotation(ColumnType.class).value();
                }else if(typeFieldMapping.containsKey(property.className)&&!typeFieldMapping.get(property.className).isEmpty()){
                    property.columnType = typeFieldMapping.get(property.className);
                }else{
                    throw new IllegalArgumentException("Java类型"+property.className+"无法自动匹配数据库类型,请使用@ColumnType注解手动指定数据库类型!");
                }
                Constraint constraint = field.getDeclaredAnnotation(Constraint.class);
                if(null!=constraint){
                    property.notNull = constraint.notNull();
                    if(null!=property.check){
                        if(!property.check.isEmpty()&&!property.check.contains("(")){
                            property.check = "(" + property.check + ")";
                        }
                        property.check = property.check.replace("#{" + property.name + "}", property.column);
                        property.escapeCheck = property.check.replace(property.column, quickDAOConfig.database.escape(property.column));
                    }
                    property.defaultValue = constraint.defaultValue();
                }
                if(property.name.equals("id")){
                    property.id = true;
                    property.strategy = IdStrategy.AutoIncrement;
                }
                Id id = field.getDeclaredAnnotation(Id.class);
                if(null!=id){
                    property.id = true;
                    property.strategy = id.strategy();
                }
                TableField tableField = field.getDeclaredAnnotation(TableField.class);
                if(null!=tableField){
                    if(!tableField.function().isEmpty()){
                        property.function = tableField.function().replace("#{"+property.name+"}","?");
                    }
                    property.createdAt = tableField.createdAt();
                    property.updateAt = tableField.updatedAt();
                }
                List<Index> indexList = new ArrayList<>();
                if(null!=field.getDeclaredAnnotation(Index.class)){
                    indexList.add(field.getDeclaredAnnotation(Index.class));
                }
                Indexes indexes = field.getDeclaredAnnotation(Indexes.class);
                if(null!=indexes&&indexes.value().length>0){
                    indexList.addAll(Arrays.asList(indexes.value()));
                }
                for(Index index:indexList){
                    IndexField indexField = new IndexField();
                    indexField.tableName = entity.tableName;
                    indexField.indexType = index.indexType();
                    if(!index.indexName().isEmpty()){
                        indexField.indexName = index.indexName();
                    }else{
                        indexField.indexName = entity.tableName+"_"+indexField.indexType.name().toLowerCase()+"_"+property.column;
                    }
                    indexField.using = index.using();
                    indexField.comment = index.comment();
                    indexField.columns.add(property.column);
                    entity.indexFieldList.add(indexField);
                }
                if(null!=field.getDeclaredAnnotation(Comment.class)){
                    property.comment = field.getDeclaredAnnotation(Comment.class).value();
                }
                property.foreignKey = field.getDeclaredAnnotation(ForeignKey.class);
                property.entity = entity;
                propertyList.add(property);
            }
            entity.properties = propertyList;
            Comment comment = getFirstAnnotation(c,Comment.class);
            if (null!=comment) {
                entity.comment = comment.value();
            }
            Table table = getFirstAnnotation(c,Table.class);
            if (null!=table) {
                entity.charset = table.charset();
                entity.engine = table.engine();
            }
            List<CompositeIndex> compositeIndexList = new ArrayList<>();
            CompositeIndex compositeIndexAnno = getFirstAnnotation(c,CompositeIndex.class);
            if(null!=compositeIndexAnno){
                compositeIndexList.add(compositeIndexAnno);
            }
            CompositeIndexes compositeIndexs = getFirstAnnotation(c,CompositeIndexes.class);
            if(null!=compositeIndexs) {
                compositeIndexList.addAll(Arrays.asList(compositeIndexs.value()));
            }
            if(compositeIndexList.size()>0){
                StringBuilder builder = new StringBuilder();
                for(CompositeIndex compositeIndex:compositeIndexList){
                    if(compositeIndex.columns().length==0){
                        continue;
                    }
                    IndexField indexField = new IndexField();
                    indexField.tableName = entity.tableName;
                    indexField.indexType = compositeIndex.indexType();
                    indexField.using = compositeIndex.using();
                    for(String column:compositeIndex.columns()){
                        indexField.columns.add(entity.getColumnNameByFieldName(column));
                    }
                    indexField.comment = compositeIndex.comment();
                    if(!compositeIndex.indexName().isEmpty()){
                        indexField.indexName = compositeIndex.indexName();
                    }else{
                        builder.setLength(0);
                        for(String column:indexField.columns){
                            builder.append(column+",");
                        }
                        builder.deleteCharAt(builder.length()-1);
                        indexField.indexName = entity.tableName+"_"+indexField.indexType.name().toLowerCase()+"_"+builder.toString();
                    }
                    entity.indexFieldList.add(indexField);
                }
            }
            UniqueField uniqueField = getFirstAnnotation(c,UniqueField.class);
            if(null!=uniqueField){
                for(String column:uniqueField.columns()){
                    Property property = entity.getPropertyByFieldName(column);
                    if(null==property){
                        throw new IllegalArgumentException("UniqueField注解参数无法匹配字段!类:"+entity.clazz.getName()+",字段:"+column);
                    }
                    entity.uniqueProperties.add(property);
                }
            }
        }

        //后处理实体类信息
        for(Entity entity: quickDAOConfig.entityMap.values()){
            if(quickDAOConfig.database.name().equals("H2")){
                entity.tableName = entity.tableName.toUpperCase();
                entity.escapeTableName = quickDAOConfig.database.escape(entity.tableName);
            }
            for(Property property : entity.properties){
                if(quickDAOConfig.database.name().equals("H2")){
                    property.column = property.column.toUpperCase();
                }
                if(property.id){
                    entity.id = property;
                    property.notNull = true;
                    property.comment = "自增id";
                    //@Id注解生成策略为默认值又在全局指定里Id生成策略则使用全局策略
                    if(property.strategy==IdStrategy.AutoIncrement&&null!=quickDAOConfig.idStrategy){
                        property.strategy = quickDAOConfig.idStrategy;
                    }
                }
                if(null!=property.foreignKey){
                    entity.foreignKeyProperties.add(property);
                }
            }
        }
    }

    @Override
    public void generateEntityFile(GenerateEntityFileOption generateEntityFileOption) {
        if(quickDAOConfig.packageNameMap.isEmpty()){
            throw new IllegalArgumentException("请先调用packageName方法指定包名");
        }
        quickDAOConfig.autoCreateTable = false;
        quickDAOConfig.autoCreateProperty = false;
        List<Entity> dbEntityList = quickDAOConfig.dbEntityList;
        if(null!=generateEntityFileOption.tableFilter){
            dbEntityList = dbEntityList.stream().filter(generateEntityFileOption.tableFilter).collect(Collectors.toList());
        }

        StringBuilder builder = new StringBuilder();
        String packageName = quickDAOConfig.packageNameMap.keySet().iterator().next();
        final Set<Map.Entry<String,String>> typeFieldMappingEntrySet = quickDAOConfig.database.getDDLBuilderInstance(quickDAOConfig).getTypeFieldMapping().entrySet();
        for(Entity dbEntity:dbEntityList){
            String entityClassName = underline2Camel(dbEntity.tableName);
            entityClassName = entityClassName.toUpperCase().charAt(0)+entityClassName.substring(1);
            if(null!=generateEntityFileOption.entityClassNameMapping){
                String newEntityClassName = generateEntityFileOption.entityClassNameMapping.apply(dbEntity,entityClassName);
                if(null!=newEntityClassName&&!newEntityClassName.isEmpty()){
                    entityClassName = newEntityClassName;
                }
            }

            Path target = Paths.get(generateEntityFileOption.sourceClassPath+"/"+ packageName.replace(".","/") + "/" + entityClassName.replace(".","/")+".java");
            try {
                Files.createDirectories(target.getParent());
            } catch (IOException e) {
                logger.warn("[创建文件夹失败]原因:{},文件夹路径:{}",e.getMessage(), target.getParent());
                continue;
            }
            if(Files.exists(target)){
                logger.warn("[实体类文件已经存在]{}",target);
                continue;
            }

            builder.setLength(0);
            //新建Java类
            builder.append("package " + packageName + (entityClassName.contains(".")?"."+entityClassName.substring(0,entityClassName.lastIndexOf(".")):"") +";\n");
            builder.append("import cn.schoolwow.quickdao.annotation.*;\n\n");
            if(null!=dbEntity.comment){
                builder.append("@Comment(\""+dbEntity.comment+"\")\n");
            }
            if(null!=dbEntity.tableName){
                builder.append("@TableName(\""+dbEntity.tableName+"\")\n");
            }
            builder.append("public class "+(entityClassName.contains(".")?entityClassName.substring(entityClassName.lastIndexOf(".")+1):entityClassName)+"{\n\n");
            for(Property property:dbEntity.properties){
                if(null!=property.comment&&!property.comment.isEmpty()){
                    builder.append("\t@Comment(\""+property.comment.replaceAll("\r\n","")+"\")\n");
                }
                if(property.id){
                    if(property.strategy.equals(IdStrategy.AutoIncrement)){
                        builder.append("\t@Id\n");
                    }else{
                        builder.append("\t@Id(strategy = IdStrategy.None)\n");
                    }
                }
                builder.append("\t@ColumnName(\""+property.column+"\")\n");
                builder.append("\t@ColumnType(\""+property.columnType+"\")\n");
                if(property.columnType.contains("(")){
                    property.columnType = property.columnType.substring(0,property.columnType.indexOf("("));
                }
                if(null!=generateEntityFileOption.columnFieldTypeMapping){
                    property.className = generateEntityFileOption.columnFieldTypeMapping.apply(property.columnType);
                }
                if(null==property.className){
                    for(Map.Entry<String,String> entry:typeFieldMappingEntrySet){
                        if(entry.getValue().contains(property.columnType.toUpperCase())){
                            property.className = entry.getKey().replace("java.lang.","");
                            break;
                        }
                    }
                }
                if(null==property.className){
                    logger.warn("[字段类型匹配失败]表名:{}字段名称:{},类型:{}",dbEntity.tableName,property.column,property.columnType);
                    property.className = "{{"+property.columnType+"}}";
                }
                property.name = underline2Camel(property.column);
                builder.append("\tprivate "+property.className+" "+property.name+";\n\n");
            }

            for(Property property:dbEntity.properties){
                builder.append("\tpublic "+ property.className +" get" +firstLetterUpper(property.name)+"(){\n\t\treturn this."+property.name+";\n\t}\n\n");
                builder.append("\tpublic void set" +firstLetterUpper(property.name)+"("+property.className+" "+property.name+"){\n\t\tthis."+property.name+" = "+property.name+";\n\t}\n\n");
            }

            builder.append("};");

            ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
            try {
                Files.createDirectories(target.getParent());
                Files.copy(bais, target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描实体包
     */
    private List<Class> scanEntity(String packageName) throws ClassNotFoundException, IOException {
        String packageNamePath = packageName.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(packageNamePath);
        if (url == null) {
            logger.warn("[实体类路径不存在]{}",packageNamePath);
            return new ArrayList<>();
        }
        logger.info("[扫描实体类]包名:{}", packageName);
        final List<Class> classList = new ArrayList<>();
        switch (url.getProtocol()) {
            case "file": {
                File file = new File(url.getFile());
                //TODO 对于有空格或者中文路径会无法识别
                if (!file.isDirectory()) {
                    throw new IllegalArgumentException("包名不是合法的文件夹!" + url.getFile());
                }
                String indexOfString = packageName.replace(".", "/");
                Files.walkFileTree(file.toPath(),new SimpleFileVisitor<Path>(){
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException
                    {
                        File f = file.toFile();
                        if(f.getName().endsWith(".class")){
                            String path = f.getAbsolutePath().replace("\\", "/");
                            int startIndex = path.indexOf(indexOfString);
                            String className = path.substring(startIndex, path.length() - 6).replace("/", ".");
                            try {
                                classList.add(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                logger.warn("[实体类不存在]{}",className);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
            break;
            case "jar": {
                JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                if (null != jarURLConnection) {
                    JarFile jarFile = jarURLConnection.getJarFile();
                    if (null != jarFile) {
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry jarEntry = jarEntries.nextElement();
                            String jarEntryName = jarEntry.getName();
                            if (jarEntryName.contains(packageNamePath) && jarEntryName.endsWith(".class")) { //是否是类,是类进行加载
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replaceAll("/", ".");
                                classList.add(classLoader.loadClass(className));
                            }
                        }
                    }
                }
            }
            break;
        }
        if (classList.size() == 0) {
            logger.warn("[扫描实体类信息为空]前缀:{},包名:{}", quickDAOConfig.packageNameMap.get(packageName), packageName);
            return classList;
        }
        Stream<Class> stream = classList.stream().filter((clazz) -> {
            return !needIgnoreClass(clazz);
        });
        return stream.collect(Collectors.toList());
    }

    /**
     * 自下而上查找注解
     * @param clazz 类
     * */
    private <T> T getFirstAnnotation(Class clazz, Class<T> annotation){
        T annotation1 = null;
        while(null!=clazz&&null==annotation1){
            annotation1 = (T) clazz.getDeclaredAnnotation(annotation);
            clazz = clazz.getSuperclass();
        }
        return annotation1;
    }

    /**
     * 获得该类所有字段(包括父类字段)
     * @param clazz 类
     * */
    private Field[] getAllField(Class clazz){
        List<Field> fieldList = new ArrayList<>();
        Class tempClass = clazz;
        while (null != tempClass) {
            Field[] fields = tempClass.getDeclaredFields();
            Field.setAccessible(fields, true);
            for (Field field : fields) {
                if(Modifier.isStatic(field.getModifiers())||Modifier.isFinal(field.getModifiers())|| Modifier.isTransient(field.getModifiers())){
                    logger.debug("[跳过常量或静态变量]{},该属性被static或者final修饰!", field.getName());
                    continue;
                }
                if (field.getDeclaredAnnotation(Ignore.class) != null) {
                    logger.debug("[跳过实体属性]{},该属性被Ignore注解修饰!", field.getName());
                    continue;
                }
                //跳过List类型和数组类型
                if(field.getType().isArray()||(!field.getType().isPrimitive()&&isCollection(field.getType()))){
                    continue;
                }
                if(needIgnoreClass(field.getType())){
                    continue;
                }
                field.setAccessible(true);
                fieldList.add(field);
            }
            tempClass = tempClass.getSuperclass();
            if (null!=tempClass&&"java.lang.Object".equals(tempClass.getName())) {
                break;
            }
        }
        return fieldList.toArray(new Field[0]);
    }

    /**
     * 判断是否是实体包类
     **/
    private boolean isCompositProperty(Class clazz) {
        Set<String> packageNameSet = quickDAOConfig.packageNameMap.keySet();
        for (String packageName : packageNameSet) {
            if (clazz.getName().contains(packageName)) {
                return true;
            }
        }
        Set<Class> classSet = quickDAOConfig.entityClassMap.keySet();
        for (Class c : classSet) {
            if(c.getName().equals(clazz.getName())){
                return true;
            }
        }
        return false;
    }

    /**是否集合*/
    private boolean isCollection(Class _class){
        Stack<Class[]> stack = new Stack<>();
        stack.push(_class.getInterfaces());
        while(!stack.isEmpty()){
            Class[] classes = stack.pop();
            for(Class clazz:classes){
                if(clazz.getName().equals(Collection.class.getName())){
                    return true;
                }
                Class[] subClasses = clazz.getInterfaces();
                if(null!=subClasses&&subClasses.length>0){
                    stack.push(subClasses);
                }
            }
        }
        return false;
    }

    /**是否需要忽略该类*/
    private boolean needIgnoreClass(Class clazz){
        if(clazz.isEnum()){
            return true;
        }
        if (clazz.getAnnotation(Ignore.class) != null) {
            return true;
        }
        //根据类过滤
        if(null!=quickDAOConfig.ignoreClassList){
            for(Class _clazz:quickDAOConfig.ignoreClassList){
                if(_clazz.getName().equals(clazz.getName())){
                    return true;
                }
            }
        }
        //根据包名过滤
        if (null!=quickDAOConfig.ignorePackageNameList) {
            for (String ignorePackageName : quickDAOConfig.ignorePackageNameList) {
                if (clazz.getName().contains(ignorePackageName)) {
                    return true;
                }
            }
            for(Class _clazz:quickDAOConfig.entityClassMap.keySet()){
                if(_clazz.getName().equals(clazz.getName())){
                    return true;
                }
            }
        }

        if(null!=quickDAOConfig.predicate){
            if(quickDAOConfig.predicate.test(clazz)){
                return true;
            }
        }
        return false;
    }

    /**
     * 首字母大写
     */
    private String firstLetterUpper(String s) {
        char firstLetter = s.charAt(0);
        if(firstLetter>=97&&firstLetter<=122){
            firstLetter -= 32;
        }
        return firstLetter+s.substring(1);
    }

    /**
     * 驼峰命名转下划线命名
     */
    private String camel2Underline(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) >= 65 && s.charAt(i) <= 90) {
                sb.append((char) (s.charAt(i) + 32));
                continue;
            }
            if (s.charAt(i) >= 65 && s.charAt(i) <= 90) {
                //如果它前面是小写字母
                if (s.charAt(i - 1) >= 97 && s.charAt(i - 1) <= 122) {
                    sb.append("_");
                }
                sb.append((char) (s.charAt(i) + 32));
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * 下划线命名转驼峰命名
     */
    private String underline2Camel(String s) {
        StringBuilder sb = new StringBuilder();
        //以下划线分割
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '_') {
                continue;
            }
            if (i > 0 && s.charAt(i - 1) == '_') {
                //如果当前是小写字母则转大写
                if (s.charAt(i) >= 97 && s.charAt(i) <= 122) {
                    sb.append((char) (s.charAt(i) - 32));
                } else {
                    sb.append(s.charAt(i));
                }
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
}
