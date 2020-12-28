package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * MySQL数据库类型字段映射
 */
public class MySQLTypeFieldMapping extends AbstractTypeFieldMapping {
    public MySQLTypeFieldMapping() {
        super.typeFieldMappingList = Arrays.asList(
                //https://www.runoob.com/mysql/mysql-data-types.html
                //数值类型
                new SingleTypeFieldMapping(Types.TINYINT, "TINYINT", boolean.class, Boolean.class),
                new SingleTypeFieldMapping(Types.TINYINT, "TINYINT", byte.class, Byte.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "SMALLINT", short.class, Short.class),
                new SingleTypeFieldMapping(Types.INTEGER, "MEDIUMINT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INTEGER", "INTEGER(11)", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.BIGINT, "BIGINT", long.class, Long.class),
                new SingleTypeFieldMapping(Types.FLOAT, "FLOAT", "FLOAT(4,2)", float.class, Float.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "DOUBLE", "DOUBLE(5,2)", double.class, Double.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "DECIMAL", BigDecimal.class),
                //日期类型
                new SingleTypeFieldMapping(Types.DATE, "DATE", Date.class),
                new SingleTypeFieldMapping(Types.TIME, "TIME", Time.class),
                new SingleTypeFieldMapping(Types.CHAR, "YEAR", String.class),
                new SingleTypeFieldMapping(Types.DATE, "DATETIME", java.util.Date.class),
                new SingleTypeFieldMapping(Types.TIMESTAMP, "TIMESTAMP", Timestamp.class),
                new SingleTypeFieldMapping(Types.DATE, "DATE", LocalDate.class),
                new SingleTypeFieldMapping(Types.DATE, "DATETIME", LocalDateTime.class),
                //字符串类型
                new SingleTypeFieldMapping(Types.CHAR, "CHAR", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR", "VARCHAR(255)", true, String.class),
                new SingleTypeFieldMapping(Types.BLOB, "TINYBLOB", Blob.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "TINYTEXT", String.class),
                new SingleTypeFieldMapping(Types.BLOB, "BLOB", true, Blob.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "TEXT", true, String.class, Clob.class, NClob.class, InputStream.class, Reader.class),
                new SingleTypeFieldMapping(Types.BLOB, "MEDIUMBLOB", Blob.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "MEDIUMTEXT", String.class, Clob.class, NClob.class, InputStream.class, Reader.class),
                new SingleTypeFieldMapping(Types.BLOB, "LONGBLOB", Blob.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "LONGTEXT", String.class, Clob.class, NClob.class, InputStream.class, Reader.class)
        );
    }
}
