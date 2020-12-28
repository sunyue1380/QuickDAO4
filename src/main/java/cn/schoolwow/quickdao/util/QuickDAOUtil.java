package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

/**
 * 工具类
 */
public class QuickDAOUtil {
    private static Logger logger = LoggerFactory.getLogger(QuickDAOUtil.class);

    /**
     * 将数据库结果集转化为JSONObject对象
     *
     * @param entity         实体类信息
     * @param tableAliasName 表别名
     * @param resultSet      结果集
     */
    public static JSONObject getObject(Entity entity, String tableAliasName, ResultSet resultSet) throws SQLException {
        JSONObject subObject = new JSONObject(true);
        for (Property property : entity.properties) {
            String columnName = tableAliasName + "_" + property.column;
            String columnLabel = property.name == null ? property.column : property.name;
            if (null == property.className) {
                subObject.put(columnLabel, resultSet.getString(columnName));
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
                    java.sql.Date date = resultSet.getDate(columnName);
                    value = new Date(date.getTime());
                }
                break;
                case "java.sql.Date": {
                    value = resultSet.getDate(columnName);
                }
                break;
                case "java.sql.Time": {
                    value = resultSet.getTime(columnName);
                }
                break;
                case "java.sql.Timestamp": {
                    value = resultSet.getTimestamp(columnName);
                }
                break;
                case "java.time.LocalDate": {
                    Date date = resultSet.getTimestamp(columnName);
                    value = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                }
                break;
                case "java.time.LocalDateTime": {
                    Date date = resultSet.getTimestamp(columnName);
                    value = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
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
                    switch (property.singleTypeFieldMapping.types) {
                        case Types.BLOB: {
                            value = resultSet.getBinaryStream(columnName);
                        }
                        break;
                        case Types.CLOB: {
                            value = resultSet.getAsciiStream(columnName);
                        }
                        break;
                    }
                }
                break;
                case "java.io.Reader": {
                    switch (property.singleTypeFieldMapping.types) {
                        case Types.CLOB: {
                            value = resultSet.getCharacterStream(columnName);
                        }
                        break;
                        case Types.NCLOB: {
                            value = resultSet.getNCharacterStream(columnName);
                        }
                        break;
                    }
                }
                break;
                default: {
                    value = resultSet.getString(columnName);
                }
            }
            subObject.put(columnLabel,value);
        }
        return subObject;
    }
}
