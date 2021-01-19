package cn.schoolwow.quickdao.transaction;

import cn.schoolwow.quickdao.dao.sql.ddl.DDLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.query.CompositQuery;

/**
 * 事务接口
 */
public interface Transaction extends TransactionOperation, CompositQuery, DDLDAO, DMLDAO {

}
