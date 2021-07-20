package cn.schoolwow.quickdao.sqlserver.test;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.GenerateEntityFileOption;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.query.condition.Condition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;
import cn.schoolwow.quickdao.sqlserver.SQLServerTest;
import cn.schoolwow.quickdao.sqlserver.entity.*;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**配置项测试*/
public class ConfigTest extends SQLServerTest {
    /**
     * 测试实体类扫描
     * */
    @Test
    public void scan(){
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .entity(Person.class)
                .entity(Order.class)
                .build();
        QuickDAOConfig quickDAOConfig = dao.getQuickDAOConfig();
        Assert.assertEquals(2,quickDAOConfig.entityMap.size());
        Entity entity = quickDAOConfig.getEntityByClassName(Person.class.getName());
        Assert.assertEquals("person",entity.tableName);
        Assert.assertEquals(quickDAOConfig.database.escape(entity.tableName),entity.escapeTableName);

        //正常执行操作
        dao.rebuild(Person.class);
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);
        }
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertEquals("Gates", person.getLastName());
        }
        {
            Person person = (Person) dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .execute()
                    .getOne();
            Assert.assertNotNull(person);
        }
    }

    /**
     * 测试define方法
     * */
    @Test
    public void define(){
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .entity(DownloadTask.class)
                .define(DownloadTask.class)
                .tableName("download_task")
                .comment("下载任务类")
                .property("filePath")
                .id(true)
                .comment("文件路径(主键)")
                .strategy(IdStrategy.None)
                .done()
                .property("fileSize")
                .notNull(true)
                .check("#{fileSize}>0")
                .comment("文件大小")
                .index(IndexType.NORMAL,"","","文件大小索引")
                .done()
                .done()
                .build();

        QuickDAOConfig quickDAOConfig = dao.getQuickDAOConfig();
        Assert.assertEquals(1,quickDAOConfig.entityMap.size());
        Entity entity = quickDAOConfig.getEntityByClassName(DownloadTask.class.getName());
        Assert.assertEquals("download_task",entity.tableName);
        Assert.assertEquals(quickDAOConfig.database.escape(entity.tableName),entity.escapeTableName);

        dao.rebuild(DownloadTask.class);
        {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.setFilePath("c:/quickdao.jar");
            downloadTask.setFileSize(10000);
            downloadTask.setRemark("quickdao jar file");
            int effect = dao.insert(downloadTask);
            Assert.assertEquals(1,effect);
        }
        {
            DownloadTask downloadTask = dao.fetch(DownloadTask.class,"filePath","c:/quickdao.jar");
            Assert.assertNotNull(downloadTask);
        }
    }

    @Test
    public void testGenerateEntityFile() throws IOException {
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .packageName("cn.schoolwow.quickdao.mysql.entity")
                .build();
        String directory = System.getProperty("user.dir")+"/mysql/";
        FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        };
        Path dir = Paths.get(directory);
        Files.walkFileTree(dir,fileVisitor);
        GenerateEntityFileOption generateEntityFileOption = new GenerateEntityFileOption();
        generateEntityFileOption.sourceClassPath = directory;
        generateEntityFileOption.tableFilter = (entity)->{return true;};
        generateEntityFileOption.columnFieldTypeMapping = (columnType)->{return "String";};
        dao.generateEntityFile(generateEntityFileOption);
        Files.walkFileTree(dir,fileVisitor);
    }

    /**测试类型转换*/
    @Test
    public void testTypeMapping(){
        dao.rebuild(TypeEntity.class);
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.setByteType((byte)0);
        typeEntity.setBytes(new byte[0]);
        typeEntity.setBooleanType(true);
        typeEntity.setShortType((short)0);
        typeEntity.setIntType(0);
        typeEntity.setFloatType(0.0f);
        typeEntity.setLongType(0l);
        typeEntity.setDoubleType(0.0d);
        typeEntity.setStringType("0");
        typeEntity.setDateType(new Date());
        typeEntity.setDateSQLType(new java.sql.Date(System.currentTimeMillis()));
        typeEntity.setTimeType(new Time(0,0,0));
        typeEntity.setLocalDate(LocalDate.now());
        typeEntity.setLocalDateTime(LocalDateTime.now());
        typeEntity.setBigDecimalType(new BigDecimal(0));
        typeEntity.setClobType(null);
        int effect = dao.insert(typeEntity);
        Assert.assertEquals(1,effect);

        JSONArray array = dao.query(TypeEntity.class)
                .execute()
                .getArray();
        Assert.assertEquals(1,array.size());
    }

    @Test
    public void testColumnValueFunction(){
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .packageName("cn.schoolwow.quickdao.sqlserver.entity")
                .autoCreateTable(false)
                .autoCreateProperty(false)
                .insertColumnValueFunction((property)->{
                    Object value = null;
                    switch (property.column){
                        case "insert_user_id":{value=1;}break;
                    }
                    return value;
                })
                .updateColumnValueFunction((property)->{
                    Object value = null;
                    switch (property.column){
                        case "update_user_id":{value=2;}break;
                    }
                    return value;
                })
                .build();
        dao.rebuild(Product.class);
        Product product = new Product();
        product.setName("笔记本电脑");
        product.setType("电器");
        product.setPrice(1000);
        product.setPublishTime(new Date());
        product.setPersonId(1);
        int effect = dao.insert(product);
        Assert.assertEquals(1,effect);
        product = dao.fetch(Product.class,product.getId());
        Assert.assertEquals(1,product.getInsertUserId());
        Assert.assertEquals(0,product.getUpdateUserId());
        product.setName("收音机");
        effect = dao.update(product);
        Assert.assertEquals(1,effect);
        product = dao.fetch(Product.class,product.getId());
        Assert.assertEquals(2,product.getUpdateUserId());
    }

    @Test
    public void testToString(){
        List<Entity> entityList = dao.getDbEntityList();
        for(Entity entity:entityList){
            System.out.println(entity);
        }
        Condition condition = dao.query(Person.class);
        System.out.println(condition);
        SubCondition subCondition = dao.query(Person.class)
                .joinTable(Order.class,"id","personId");
        System.out.println(subCondition);
    }
}
