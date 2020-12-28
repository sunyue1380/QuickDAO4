package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.builder.ddl.*;
import cn.schoolwow.quickdao.builder.dql.AbstractDQLBuilder;
import cn.schoolwow.quickdao.builder.dql.PostgreDQLBuilder;
import cn.schoolwow.quickdao.builder.dql.SQLiteDQLBuilder;
import cn.schoolwow.quickdao.builder.typeFieldMapping.*;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import cn.schoolwow.quickdao.query.condition.Condition;
import cn.schoolwow.quickdao.query.condition.PostgreCondition;
import cn.schoolwow.quickdao.query.condition.SQLServerCondition;
import cn.schoolwow.quickdao.query.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SQLiteSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;

/**数据库类型*/
public enum Database {
    Mysql(new MySQLTypeFieldMapping()),
    H2(new H2TypeFieldMapping()),
    SQLite(new SQLiteTypeFieldMapping()),
    Postgre(new PostgreTypeFieldMapping()),
    SQLServer(new SQLServerTypeFieldMapping());

    public TypeFieldMapping typeFieldMapping;

    Database(TypeFieldMapping typeFieldMapping) {
        this.typeFieldMapping = typeFieldMapping;
    }

    /**返回注释语句*/
    public String comment(String comment){
        switch (this){
            case Mysql:{
                return "comment \"" + comment + "\"";
            }
            case SQLite:{
                return "/* "+comment+" */";
            }
            case H2:
            case Postgre:
            case SQLServer:{
                return "";
            }
        }
        return comment;
    }

    /**转义表,列等*/
    public String escape(String value){
        switch (this){
            case Mysql:
            case H2:
            case SQLite:{
                return "`"+value+"`";
            }
            case Postgre:
            case SQLServer:{
                return "\""+value +"\"";
            }
        }
        return value;
    }

    /**获取Condition实例*/
    public Condition getConditionInstance(Query query){
        switch(this){
            case Mysql:
            case SQLite:
            case H2:{return new AbstractCondition(query);}
            case Postgre:{return new PostgreCondition(query);}
            case SQLServer:{return new SQLServerCondition(query);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**获取SubCondition实例*/
    public SubCondition getSubConditionInstance(SubQuery subQuery){
        switch(this){
            case SQLite:{return new SQLiteSubCondition(subQuery);}
            case Mysql:
            case H2:
            case Postgre:
            case SQLServer:{return new AbstractSubCondition(subQuery);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**获取DDL实例*/
    public AbstractDDLBuilder getDDLBuilderInstance(QuickDAOConfig quickDAOConfig){
        switch(this){
            case Mysql:{return new MySQLDDLBuilder(quickDAOConfig);}
            case SQLite:{return new SQLiteDDLBuilder(quickDAOConfig);}
            case H2:{return new H2DDLBuilder(quickDAOConfig);}
            case Postgre:{return new PostgreDDLBuilder(quickDAOConfig);}
            case SQLServer:{return new SQLServerDDLBuilder(quickDAOConfig);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**获取DQL实例*/
    public AbstractDQLBuilder getDQLBuilderInstance(QuickDAOConfig quickDAOConfig){
        switch(this){
            case SQLite:{return new SQLiteDQLBuilder(quickDAOConfig);}
            case Mysql:
            case H2:
            case Postgre:{return new PostgreDQLBuilder(quickDAOConfig);}
            case SQLServer:{return new AbstractDQLBuilder(quickDAOConfig);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /***
     * 根据JDBCurl获取数据库类型
     */
    public static Database getDatabaseByJdbcUrl(String jdbcUrl){
        if (jdbcUrl.contains("jdbc:h2")) {
            return Database.H2;
        } else if (jdbcUrl.contains("jdbc:sqlite")) {
            return Database.SQLite;
        } else if (jdbcUrl.contains("jdbc:mysql")) {
            return Database.Mysql;
        } else if (jdbcUrl.contains("jdbc:postgresql")) {
            return Database.Postgre;
        } else if (jdbcUrl.contains("jdbc:sqlserver:")) {
            return Database.SQLServer;
        } else {
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
    }
}
