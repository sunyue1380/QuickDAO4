package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.generator.IDGenerator;
import cn.schoolwow.quickdao.domain.generator.SnowflakeIdGenerator;
import cn.schoolwow.quickdao.handler.EntityHandler;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**数据源访问配置选项*/
public class QuickDAOConfig {
    /**
     * 数据源
     */
    public DataSource dataSource;
    /**
     * 待扫描包名
     */
    public Map<String, String> packageNameMap = new HashMap<>();
    /**
     * 待扫描类
     */
    public Map<Class, String> entityClassMap = new HashMap<>();
    /**
     * 要忽略的类
     */
    public List<Class> ignoreClassList;
    /**
     * 要忽略的包名
     */
    public List<String> ignorePackageNameList;
    /**
     * 函数式接口过滤类
     */
    public Predicate<Class> predicate;
    /**
     * 是否开启外键约束
     */
    public boolean openForeignKey;
    /**
     * 是否启动时自动建表
     */
    public boolean autoCreateTable = true;
    /**
     * 是否自动新增属性
     */
    public boolean autoCreateProperty = true;
    /**
     * 全局Id生成策略
     */
    public IdStrategy idStrategy;
    /**
     * Id生成器实例
     * 默认生成器为雪花算法生成器
     */
    public IDGenerator idGenerator = new SnowflakeIdGenerator();
    /**
     * 当前数据库名称
     */
    public String databaseName;
    /**
     * 全局表编码格式
     * */
    public String charset;
    /**
     * 全局表引擎
     * */
    public String engine;
    /**
     * 全局类型转换
     */
    public ColumnTypeMapping columnTypeMapping;
    /**
     * 单次批量插入个数
     */
    public int perBatchCommit = 1000;
    /**
     * 扫描后的实体类信息
     * */
    public final Map<String, Entity> entityMap = new HashMap<>();
    /**
     * SQL语句缓存
     * */
    public final ConcurrentHashMap<String,String> sqlCache = new ConcurrentHashMap();
    /**
     * 数据库获取的表信息
     * */
    public volatile List<Entity> dbEntityList;
    /**
     * 虚拟表(dual等)
     * */
    public List<Entity> visualTableList;
    /**
     * 数据库信息
     * */
    public Database database;
    /**
     * dao对象,用于返回
     * */
    public DAO dao;
    /**
     * 实体类工具
     * */
    public EntityHandler entityHandler;
    /**
     * 拦截器
     * */
    public List<Interceptor> interceptorList = new ArrayList<>();

    /**SQLite数据库写锁*/
    public ReentrantLock sqliteLock = new ReentrantLock();

    /**根据类名获取实体类信息*/
    public Entity getEntityByClassName(String className){
        if(this.entityMap.containsKey(className)){
            return this.entityMap.get(className);
        }
        throw new IllegalArgumentException("扫描实体类列表中不包含该实体类!类名:"+className);
    }

    /**根据表名获取对应数据库实体类*/
    public Entity getDbEntityByTableName(String tableName){
        for(Entity entity:dbEntityList){
            if(entity.tableName.equals(tableName)){
                return entity;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "\n{\n" +
                "数据源:" + dataSource + "\n"
                + "待扫描包名:" + packageNameMap + "\n"
                + "待扫描类:" + entityClassMap + "\n"
                + "要忽略的类:" + ignoreClassList + "\n"
                + "要忽略的包名:" + ignorePackageNameList + "\n"
                + "函数式接口过滤类:" + predicate + "\n"
                + "是否开启外键约束:" + openForeignKey + "\n"
                + "是否自动建表:" + autoCreateTable + "\n"
                + "是否自动新增属性:" + autoCreateProperty + "\n"
                + "全局Id生成策略:" + idStrategy + "\n"
                + "全局Id生成器实例:" + idGenerator + "\n"
                + "当前数据库名称:" + databaseName + "\n"
                + "全局表编码格式:" + charset + "\n"
                + "全局表引擎:" + engine + "\n"
                + "全局类型转换:" + columnTypeMapping + "\n"
                + "扫描后的实体类信息个数:" + entityMap.size() + "\n"
                + "SQL语句缓存个数:" + sqlCache.size() + "\n"
                + "数据库获取的表个数:" + (null==dbEntityList?"无信息":dbEntityList.size()) + "\n"
                + "虚拟表(dual等):" + visualTableList + "\n"
                + "数据库信息:" + database + "\n"
                + "}\n";
    }
}
