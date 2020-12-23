package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.SQLDAO;
import cn.schoolwow.quickdao.dao.sql.ddl.DDLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.query.CompositQuery;

public interface DAO extends DAOOperation, CompositQuery, SQLDAO, DDLDAO, DQLDAO, DMLDAO {

}
