package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**工具类*/
public class QuickDAOUtil {
    private static Logger logger = LoggerFactory.getLogger(QuickDAOUtil.class);

    /**
     * 将数据库结果集转化为JSONObject对象
     * @param entity 实体类信息
     * @param tableAliasName 表别名
     * @param resultSet 结果集
     */
    public static JSONObject getObject(Entity entity, String tableAliasName, ResultSet resultSet) throws SQLException {
        JSONObject subObject = new JSONObject(true);
        for (Property property : entity.properties) {
            String columnName = tableAliasName + "_" + property.column;
            String key = property.name==null?property.column:property.name;
            if(null==property.simpleTypeName){
                subObject.put(key, resultSet.getString(columnName));
                continue;
            }
            switch (property.simpleTypeName) {
                case "boolean": {
                    subObject.put(key, resultSet.getBoolean(columnName));
                }
                break;
                case "int":
                case "integer": {
                    subObject.put(key, resultSet.getInt(columnName));
                }
                break;
                case "float": {
                    subObject.put(key, resultSet.getFloat(columnName));
                }
                break;
                case "long": {
                    subObject.put(key, resultSet.getLong(columnName));
                }
                break;
                case "double": {
                    subObject.put(key, resultSet.getDouble(columnName));
                }
                break;
                case "string": {
                    subObject.put(key, resultSet.getString(columnName));
                }
                break;
                case "localdate": {
                    Date date = resultSet.getTimestamp(columnName);
                    if(null!=date){
                        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        subObject.put(key, localDate);
                    }
                }
                break;
                case "localdatetime": {
                    Date date = resultSet.getTimestamp(columnName);
                    if(null!=date){
                        LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        subObject.put(key, localDateTime);
                    }
                }
                break;
                default: {
                    subObject.put(key, resultSet.getObject(columnName));
                }
            }
        }
        return subObject;
    }
}
