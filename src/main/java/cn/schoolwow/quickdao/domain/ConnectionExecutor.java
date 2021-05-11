package cn.schoolwow.quickdao.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionExecutor {
    private Logger logger = LoggerFactory.getLogger(ConnectionExecutor.class);

    /**
     * 数据记录总行数
     * */
    public int count = -1;

    /**
     * 数据库连接对象
     * */
    public Connection connection;

    /**
     * QuickDAO配置对象
     * */
    public QuickDAOConfig quickDAOConfig;

    public ConnectionExecutor(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    /**
     * 新建执行器子项
     * */
    public ConnectionExecutorItem newConnectionExecutorItem(String name, String sql) throws SQLException{
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            ConnectionExecutorItem connectionExecutorItem = new ConnectionExecutorItem();
            connectionExecutorItem.name = name;
            connectionExecutorItem.sql = sql;
            connectionExecutorItem.preparedStatement = preparedStatement;
            return connectionExecutorItem;
        }catch (SQLException e){
            logger.warn("[SQL语句执行失败]名称:{},原始SQL:{}", name, sql);
            throw e;
        }
    }

    /**
     * 执行查询操作
     * */
    public ResultSet executeQuery(String name, String sql) throws SQLException{
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = executeQuery(name, sql, preparedStatement);
            return resultSet;
        }catch (SQLException e){
            logger.warn("[SQL语句执行失败]名称:{},原始SQL:{}", name, sql);
            throw e;
        }
    }

    /**
     * 执行查询操作
     * */
    public ResultSet executeQuery(ConnectionExecutorItem connectionExecutorItem) throws SQLException{
        return executeQuery(connectionExecutorItem.name,connectionExecutorItem.sql,connectionExecutorItem.preparedStatement);
    }

    /**
     * 执行更新操作
     * @param name 名称
     * @param sql SQL语句
     * @return 影响行数
     * */
    public int executeUpdate(String name, String sql) throws SQLException {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            int effect = executeUpdate(name, sql, preparedStatement);
            preparedStatement.close();
            return effect;
        }catch (SQLException e){
            logger.warn("[SQL语句执行失败]名称:{},原始SQL:{}", name, sql);
            throw e;
        }
    }

    /**
     * 执行更新操作
     * @param connectionExecutorItem 执行子项
     * @return 影响行数
     * */
    public int executeUpdate(ConnectionExecutorItem connectionExecutorItem) throws SQLException{
        return executeUpdate(connectionExecutorItem.name,connectionExecutorItem.sql,connectionExecutorItem.preparedStatement);
    }

    /**
     * 执行更新操作
     * @param name 名称
     * @param sql SQL语句
     * @return 影响行数
     * */
    private ResultSet executeQuery(String name, String sql, PreparedStatement preparedStatement) throws SQLException {
        try {
            long startTime = System.currentTimeMillis();
            ResultSet resultSet = preparedStatement.executeQuery();
            long endTime = System.currentTimeMillis();
            if(!"获取行数".equals(name)){
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                if(stackTraceElements[2].getClassName().startsWith("cn.schoolwow.quickdao.builder.ddl")){
                    if(count>=0){
                        logger.trace("[{}]行数:{},耗时:{}ms,执行SQL:{}", name, count, endTime - startTime, sql);
                        count = -1;
                    }else{
                        logger.trace("[{}]耗时:{}ms,执行SQL:{}", name, endTime - startTime, sql);
                    }
                }else{
                    if(count>=0){
                        logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}", name, count, endTime - startTime, sql);
                        count = -1;
                    }else{
                        logger.debug("[{}]耗时:{}ms,执行SQL:{}", name, endTime - startTime, sql);
                    }
                }

                for(Interceptor interceptor:quickDAOConfig.interceptorList){
                    interceptor.afterExecuteConnection(SQLStatementType.SELECT, name, sql);
                }
            }
            return resultSet;
        }catch (SQLException e){
            logger.warn("[SQL语句执行失败]名称:{},原始SQL:{}", name, sql);
            throw e;
        }
    }

    /**
     * 执行更新操作
     * @param name 名称
     * @param sql SQL语句
     * @return 影响行数
     * */
    private int executeUpdate(String name, String sql, PreparedStatement preparedStatement) throws SQLException {
        try {
            long startTime = System.currentTimeMillis();
            int effect = preparedStatement.executeUpdate();
            long endTime = System.currentTimeMillis();
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            if(stackTraceElements[2].getClassName().startsWith("cn.schoolwow.quickdao.builder.ddl")){
                logger.trace("[{}]耗时:{}ms,执行SQL:{}", name, endTime - startTime, sql);
            }else{
                logger.debug("[{}]耗时:{}ms,执行SQL:{}", name, endTime - startTime, sql);
            }
            for(Interceptor interceptor:quickDAOConfig.interceptorList){
                interceptor.afterExecuteConnection(SQLStatementType.UPDATE, name, sql);
            }
            return effect;
        }catch (SQLException e){
            logger.warn("[SQL语句执行失败]名称:{},原始SQL:{}", name, sql);
            throw e;
        }
    }
}
