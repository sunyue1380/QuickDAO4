package cn.schoolwow.quickdao.transaction;

import java.sql.Savepoint;

/**
 * 事务相关操作
 */
public interface TransactionOperation {
    /**
     * 设置事务隔离级别
     *
     * @param transactionIsolation 指定事务级别,数值为Connection接口下的常量
     */
    void setTransactionIsolation(int transactionIsolation);

    /**
     * 设置保存点
     */
    Savepoint setSavePoint(String name);

    /**
     * 事务回滚
     */
    void rollback();

    /**
     * 事务回滚
     */
    void rollback(Savepoint savePoint);

    /**
     * 事务提交
     */
    void commit();

    /**
     * 结束事务
     */
    void endTransaction();
}
