package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.dao.DAOInvocationHandler;
import cn.schoolwow.quickdao.domain.ColumnTypeMapping;
import cn.schoolwow.quickdao.domain.Database;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.generator.IDGenerator;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.handler.DefaultEntityHandler;
import cn.schoolwow.quickdao.handler.DefaultTableDefiner;
import cn.schoolwow.quickdao.handler.TableDefiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.function.Predicate;

public class QuickDAO {
    private Logger logger = LoggerFactory.getLogger(QuickDAO.class);
    private QuickDAOConfig quickDAOConfig = new QuickDAOConfig();

    public static QuickDAO newInstance() {
        return new QuickDAO();
    }

    public QuickDAO(){
        DefaultEntityHandler entityHandler = new DefaultEntityHandler(quickDAOConfig);
        quickDAOConfig.entityHandler = entityHandler;
    }

    /**
     * 设置数据库连接池
     * @param dataSource 数据库连接池
     * */
    public QuickDAO dataSource(DataSource dataSource) {
        quickDAOConfig.dataSource = dataSource;
        try {
            Connection connection = quickDAOConfig.dataSource.getConnection();
            connection.setAutoCommit(false);
            String jdbcUrl = connection.getMetaData().getURL();
            quickDAOConfig.database = Database.getDatabaseByJdbcUrl(jdbcUrl);
            logger.info("[数据源]类型:{},地址:{}", quickDAOConfig.database.name(),jdbcUrl);
            connection.close();
        }catch (Exception e){
            throw new SQLRuntimeException(e);
        }
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param packageName 实体类所在包名
     * */
    public QuickDAO packageName(String packageName) {
        quickDAOConfig.packageNameMap.put(packageName, "");
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param packageName 实体类所在包名
     * @param prefix 表前缀
     * */
    public QuickDAO packageName(String packageName, String prefix) {
        quickDAOConfig.packageNameMap.put(packageName, prefix + "_");
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param entityClasses 实体类
     * */
    public QuickDAO entity(Class... entityClasses) {
        for(Class entityClass:entityClasses){
            quickDAOConfig.entityClassMap.put(entityClass,"");
        }
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param entityClass 实体类
     * @param prefix 表前缀
     * */
    public QuickDAO entity(Class entityClass, String prefix) {
        quickDAOConfig.entityClassMap.put(entityClass,prefix);
        return this;
    }

    /**
     * 忽略包名
     * @param ignorePackageName 扫描实体类时需要忽略的包
     * */
    public QuickDAO ignorePackageName(String ignorePackageName) {
        if (quickDAOConfig.ignorePackageNameList == null) {
            quickDAOConfig.ignorePackageNameList = new ArrayList<>();
        }
        quickDAOConfig.ignorePackageNameList.add(ignorePackageName);
        return this;
    }

    /**
     * 忽略该实体类
     * @param ignoreClass 需要忽略的实体类
     * */
    public QuickDAO ignoreClass(Class ignoreClass) {
        if (quickDAOConfig.ignoreClassList == null) {
            quickDAOConfig.ignoreClassList = new ArrayList<>();
        }
        quickDAOConfig.ignoreClassList.add(ignoreClass);
        return this;
    }

    /**
     * 过滤实体类
     * @param predicate 过滤实体类函数
     * */
    public QuickDAO filter(Predicate<Class> predicate) {
        quickDAOConfig.predicate = predicate;
        return this;
    }

    /**
     * 是否建立外键约束
     * @param openForeignKey 指定管是否建立外键约束
     * */
    public QuickDAO foreignKey(boolean openForeignKey) {
        quickDAOConfig.openForeignKey = openForeignKey;
        return this;
    }

    /**
     * 是否自动建表
     * @param autoCreateTable 指定是否自动建表
     * */
    public QuickDAO autoCreateTable(boolean autoCreateTable) {
        quickDAOConfig.autoCreateTable = autoCreateTable;
        return this;
    }

    /**
     * 是否自动新增属性
     * @param autoCreateProperty 指定是否自动新增字段
     * */
    public QuickDAO autoCreateProperty(boolean autoCreateProperty) {
        quickDAOConfig.autoCreateProperty = autoCreateProperty;
        return this;
    }

    /**
     * 指定全局Id生成策略
     * @param idStrategy 全局id生成策略
     * */
    public QuickDAO idStrategy(IdStrategy idStrategy) {
        quickDAOConfig.idStrategy = idStrategy;
        return this;
    }

    /**
     * 指定id生成器接口实例
     * <p><b>当id字段策略为IdGenerator起作用</b></p>
     * @param idGenerator id生成器实例
     * */
    public QuickDAO idGenerator(IDGenerator idGenerator) {
        quickDAOConfig.idGenerator = idGenerator;
        return this;
    }

    /**
     * 指定全局数据库表引擎
     * @param engine 数据库表引擎
     * */
    public QuickDAO engine(String engine) {
        quickDAOConfig.engine = engine;
        return this;
    }

    /**
     * 指定全局数据库表编码格式
     * @param charset charset
     * */
    public QuickDAO charset(String charset) {
        quickDAOConfig.charset = charset;
        return this;
    }

    /**
     * 指定全局类型转换
     * @param columnTypeMapping 全局类型转换函数
     * */
    public QuickDAO columnTypeMapping(ColumnTypeMapping columnTypeMapping) {
        quickDAOConfig.columnTypeMapping = columnTypeMapping;
        return this;
    }

    /**自定义表和列*/
    public TableDefiner define(Class clazz) {
        if(quickDAOConfig.entityMap.isEmpty()){
            try {
                quickDAOConfig.entityHandler.getEntityMap();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(null==quickDAOConfig.dataSource){
            throw new IllegalArgumentException("请先调用dataSource方法配置数据源!");
        }
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        if(null==entity){
            throw new IllegalArgumentException("未扫描指定类信息!类名:"+clazz.getName());
        }
        return new DefaultTableDefiner(entity,this);
    }

    public DAO build(){
        if(null==quickDAOConfig.database){
            throw new IllegalArgumentException("请先调用dataSource方法配置数据源!");
        }
        //获取实体类信息
        if(quickDAOConfig.entityMap.isEmpty()){
            try {
                quickDAOConfig.entityHandler.getEntityMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DAOInvocationHandler daoInvocationHandler = new DAOInvocationHandler(quickDAOConfig);
        DAO daoProxy = (DAO) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{DAO.class},daoInvocationHandler);
        quickDAOConfig.dao = daoProxy;
        //自动建表和新增字段
        daoProxy.refreshDbEntityList();
        daoProxy.automaticCreateTableAndColumn();
        return daoProxy;
    }
}
