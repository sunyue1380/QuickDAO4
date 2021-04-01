package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.dao.DAO;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.time.LocalDateTime;

/**
 * 获取各类DAO接口对象
 */
public class DAOUtil {
    /**
     * 获取h2接口对象
     */
    public static HikariDataSource getH2DataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("org.h2.Driver");
        hikariDataSource.setJdbcUrl("jdbc:h2:" + new File("quickdao_h2.db").getAbsolutePath() + ";mode=MYSQL");
        return hikariDataSource;
    }

    /**
     * 获取h2接口对象
     */
    public static DAO getH2DAO() {
        return getH2DAO(null);
    }

    /**
     * 获取h2接口对象
     */
    public static DAO getH2DAO(HikariDataSource hikariDataSource) {
        if(null==hikariDataSource){
            hikariDataSource = getH2DataSource();
        }
        DAO dao = QuickDAO.newInstance()
                .dataSource(hikariDataSource)
                .packageName("cn.schoolwow.quickdao.h2.entity")
                .charset("utf8")
                .engine("InnoDB")
                .columnTypeMapping((property) -> {
                    if (property.column.equalsIgnoreCase("created_at") ||
                            property.column.equalsIgnoreCase("updated_at")) {
                        return LocalDateTime.class;
                    }
                    return null;
                })
                .build();
        return dao;
    }

    /**
     * 获取mysql接口对象
     */
    public static HikariDataSource getMySQLDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        hikariDataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
        hikariDataSource.setUsername("root");
        hikariDataSource.setPassword("123456");
        return hikariDataSource;
    }

    /**
     * 获取mysql接口对象
     */
    public static DAO getMySQLDAO() {
        return getMySQLDAO(null);
    }

    /**
     * 获取mysql接口对象
     */
    public static DAO getMySQLDAO(HikariDataSource hikariDataSource) {
        if(hikariDataSource==null){
            hikariDataSource = getMySQLDataSource();
        }
        DAO dao = QuickDAO.newInstance()
                .dataSource(hikariDataSource)
                .packageName("cn.schoolwow.quickdao.mysql.entity")
                .charset("utf8")
                .engine("InnoDB")
                .foreignKey(false)
                .columnTypeMapping((property) -> {
                    if (property.column.equalsIgnoreCase("created_at") ||
                            property.column.equalsIgnoreCase("updated_at")) {
                        return LocalDateTime.class;
                    }
                    return null;
                })
                .build();
        return dao;
    }

    /**
     * 获取postgre接口对象
     */
    public static HikariDataSource getPostgreDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("org.postgresql.Driver");
        hikariDataSource.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/quickdao");
        hikariDataSource.setUsername("postgres");
        hikariDataSource.setPassword("123456");
        return hikariDataSource;
    }

    /**
     * 获取postgre接口对象
     */
    public static DAO getPostgreDAO() {
        return getPostgreDAO(null);
    }

    /**
     * 获取postgre接口对象
     */
    public static DAO getPostgreDAO(HikariDataSource hikariDataSource) {
        if(null==hikariDataSource){
            hikariDataSource = getPostgreDataSource();
        }
        DAO dao = QuickDAO.newInstance()
                .dataSource(hikariDataSource)
                .packageName("cn.schoolwow.quickdao.postgre.entity")
                .charset("utf8")
                .engine("InnoDB")
                .columnTypeMapping((property) -> {
                    if (property.column.equalsIgnoreCase("created_at") ||
                            property.column.equalsIgnoreCase("updated_at")) {
                        return LocalDateTime.class;
                    }
                    return null;
                })
                .build();
        return dao;
    }

    /**
     * 获取SQLite接口对象
     */
    public static HikariDataSource getSQLiteDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setDriverClassName("org.sqlite.JDBC");
        hikariDataSource.setJdbcUrl("jdbc:sqlite:" + new File("quickdao_sqlite.db").getAbsolutePath());
        return hikariDataSource;
    }

    /**
     * 获取SQLite接口对象
     */
    public static DAO getSQLiteDAO() {
        return getSQLiteDAO(null);
    }

    /**
     * 获取SQLite接口对象
     */
    public static DAO getSQLiteDAO(HikariDataSource hikariDataSource) {
        if(null==hikariDataSource){
            hikariDataSource = getSQLiteDataSource();
        }
        DAO dao = QuickDAO.newInstance()
                .dataSource(hikariDataSource)
                .packageName("cn.schoolwow.quickdao.sqlite.entity")
                .autoCreateTable(true)
                .columnTypeMapping((property) -> {
                    if (property.column.equalsIgnoreCase("created_at") ||
                            property.column.equalsIgnoreCase("updated_at")) {
                        return LocalDateTime.class;
                    }
                    return null;
                })
                .build();
        return dao;
    }
}