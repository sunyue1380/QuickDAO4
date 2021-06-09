package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.builder.dcl.AbstractDCLBuilder;
import cn.schoolwow.quickdao.builder.dcl.MySQLDCLBuilder;
import cn.schoolwow.quickdao.builder.dcl.PostgreDCLBuilder;
import cn.schoolwow.quickdao.builder.ddl.*;
import cn.schoolwow.quickdao.builder.dql.AbstractDQLBuilder;
import cn.schoolwow.quickdao.builder.dql.SQLiteDQLBuilder;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import cn.schoolwow.quickdao.query.condition.Condition;
import cn.schoolwow.quickdao.query.condition.PostgreCondition;
import cn.schoolwow.quickdao.query.condition.SQLServerCondition;
import cn.schoolwow.quickdao.query.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SQLiteSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;
import com.alibaba.fastjson.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**数据库类型*/
public enum Database {
    /**MariaDB数据库*/
    MariaDB,
    /**MySQL数据库*/
    Mysql,
    /**H2数据库*/
    H2,
    /**SQLite数据库*/
    SQLite,
    /**Postgre数据库*/
    Postgre,
    /**SQLServer数据库*/
    SQLServer;

    /**返回注释语句*/
    public String comment(String comment){
        switch (this){
            case MariaDB:
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
            default:{
                return comment;
            }
        }
    }

    /**转义表,列等*/
    public String escape(String value){
        switch (this){
            case MariaDB:
            case Mysql:
            case H2:
            case SQLite:{
                return "`"+value+"`";
            }
            case Postgre:
            case SQLServer:{
                return "\""+value +"\"";
            }
            default:{
                return value;
            }
        }
    }

    /**获取Condition实例*/
    public Condition getConditionInstance(Query query){
        switch(this){
            case MariaDB:
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
            case MariaDB:
            case Mysql:
            case H2:
            case Postgre:
            case SQLServer:{return new AbstractSubCondition(subQuery);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**获取DCL实例*/
    public AbstractDCLBuilder getDCLBuilderInstance(QuickDAOConfig quickDAOConfig){
        switch(this){
            case H2:
            case MariaDB:
            case Mysql:{return new MySQLDCLBuilder(quickDAOConfig);}
            case Postgre:{return new PostgreDCLBuilder(quickDAOConfig);}
            case SQLite:{throw new IllegalArgumentException("SQLite不支持创建用户等操作!");}
            case SQLServer:{throw new UnsupportedOperationException("当前不支持SQLServer的DCL相关操作!");}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**获取DDL实例*/
    public AbstractDDLBuilder getDDLBuilderInstance(QuickDAOConfig quickDAOConfig){
        switch(this){
            case MariaDB:
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
            case SQLite:
            case SQLServer:
            case Postgre:{return new SQLiteDQLBuilder(quickDAOConfig);}
            case MariaDB:
            case Mysql:
            case H2:{return new AbstractDQLBuilder(quickDAOConfig);}
            default:{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
        }
    }

    /**
     * 将数据库结果集转化为JSONObject对象
     *
     * @param entity         实体类信息
     * @param tableAliasName 表别名
     * @param resultSet      结果集
     */
    public JSONObject getObject(Entity entity, String tableAliasName, ResultSet resultSet) throws SQLException {
        JSONObject subObject = new JSONObject(true);
        for (Property property : entity.properties) {
            String columnName = tableAliasName + "_" + property.column;
            String columnLabel = property.name == null ? property.column : property.name;
            if (null == property.className) {
                subObject.put(columnLabel, resultSet.getObject(columnName));
                continue;
            }
            Object value = null;
            switch (property.className) {
                case "byte": {
                    value = resultSet.getByte(columnName);
                }
                break;
                case "[B": {
                    value = resultSet.getBytes(columnName);
                }
                break;
                case "boolean": {
                    value = resultSet.getBoolean(columnName);
                }
                break;
                case "short": {
                    value = resultSet.getShort(columnName);
                }
                break;
                case "int": {
                    value = resultSet.getInt(columnName);
                }
                break;
                case "float": {
                    value = resultSet.getFloat(columnName);
                }
                break;
                case "long": {
                    value = resultSet.getLong(columnName);
                }
                break;
                case "double": {
                    value = resultSet.getDouble(columnName);
                }
                break;
                case "java.util.Date": {
                    switch(this){
                        case SQLite:{
                            value = resultSet.getString(columnName);
                        }break;
                        default:{
                            java.sql.Date date = resultSet.getDate(columnName);
                            if(null!=date){
                                value = new Date(date.getTime());
                            }
                        }break;
                    }
                }
                break;
                case "java.sql.Date": {
                    switch(this){
                        case SQLite:{
                            value = resultSet.getString(columnName);
                        }break;
                        default:{
                            value = resultSet.getDate(columnName);
                        }break;
                    }
                }
                break;
                case "java.sql.Time": {
                    switch(this){
                        case SQLite:{
                            value = resultSet.getString(columnName);
                        }break;
                        default:{
                            value = resultSet.getTime(columnName);
                        }break;
                    }
                }
                break;
                case "java.sql.Timestamp": {
                    switch(this){
                        case SQLite:{
                            value = resultSet.getString(columnName);
                        }break;
                        default:{
                            value = resultSet.getTimestamp(columnName);
                        }break;
                    }
                }
                break;
                case "java.time.LocalDate": {
                    switch(this){
                        case SQLite:{
                            String date = resultSet.getString(columnName);
                            if(null!=date){
                                value = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
                            }
                        }break;
                        default:{
                            Date date = resultSet.getTimestamp(columnName);
                            if(null!=date){
                                value = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                            }
                        }break;
                    }
                }
                break;
                case "java.time.LocalDateTime": {
                    switch(this){
                        case SQLite:{
                            String datetime = resultSet.getString(columnName);
                            if(null!=datetime){
                                value = LocalDateTime.parse(datetime, DateTimeFormatter.ISO_DATE_TIME);
                            }
                        }break;
                        default:{
                            Date date = resultSet.getTimestamp(columnName);
                            if(null!=date){
                                value = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                            }
                        }break;
                    }
                }
                break;
                case "java.sql.Array": {
                    value = resultSet.getArray(columnName);
                }
                break;
                case "java.math.BigDecimal": {
                    value = resultSet.getBigDecimal(columnName);
                }
                break;
                case "java.sql.Blob": {
                    value = resultSet.getBlob(columnName);
                }
                break;
                case "java.sql.Clob": {
                    value = resultSet.getClob(columnName);
                }
                break;
                case "java.sql.NClob": {
                    value = resultSet.getNClob(columnName);
                }
                break;
                case "java.sql.Ref": {
                    value = resultSet.getRef(columnName);
                }
                break;
                case "java.net.URL": {
                    value = resultSet.getURL(columnName);
                }
                break;
                case "java.sql.RowId": {
                    value = resultSet.getRowId(columnName);
                }
                break;
                case "java.sql.SQLXML": {
                    value = resultSet.getSQLXML(columnName);
                }
                break;
                case "java.io.InputStream": {
                    value = resultSet.getBinaryStream(columnName);
                }
                break;
                case "java.io.Reader": {
                    value = resultSet.getCharacterStream(columnName);
                }
                break;
                default: {
                    value = resultSet.getObject(columnName);
                }
            }
            subObject.put(columnLabel,value);
        }
        return subObject;
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
        } else if (jdbcUrl.contains("jdbc:mariadb")) {
            return Database.MariaDB;
        } else if (jdbcUrl.contains("jdbc:postgresql")) {
            return Database.Postgre;
        } else if (jdbcUrl.contains("jdbc:sqlserver:")) {
            return Database.SQLServer;
        } else {
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
    }
}
