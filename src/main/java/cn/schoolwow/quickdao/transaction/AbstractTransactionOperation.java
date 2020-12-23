package cn.schoolwow.quickdao.transaction;

import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;

/**事务操作类*/
public class AbstractTransactionOperation implements TransactionOperation {
    private Connection connection;

    public AbstractTransactionOperation(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void setTransactionIsolation(int transactionIsolation) {
        try {
            connection.setTransactionIsolation(transactionIsolation);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public Savepoint setSavePoint(String name) {
        try {
            return connection.setSavepoint(name);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rollback(Savepoint savePoint) {
        try {
            connection.rollback(savePoint);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void endTransaction() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
