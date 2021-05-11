package cn.schoolwow.quickdao.domain;

public interface Interceptor {
    /**
     * 执行SQL语句之后
     * @param sqlStatementType SQL语句类型
     * @param name 执行功能名称
     * @param sql  执行SQL语句
     */
    void afterExecuteConnection(SQLStatementType sqlStatementType, String name, String sql);
}
