package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * H2数据库类型字段映射
 */
public class H2TypeFieldMapping extends AbstractTypeFieldMapping {
    public H2TypeFieldMapping() {
        super.typeFieldMappingList = Arrays.asList(
                //https://www.cnblogs.com/langtianya/p/4276368.html
                //Boolean类型
                new SingleTypeFieldMapping(Types.BOOLEAN, "BOOLEAN", true, boolean.class, Boolean.class),
                new SingleTypeFieldMapping(Types.BIT, "BIT", boolean.class, Boolean.class),
                new SingleTypeFieldMapping(Types.BOOLEAN, "BOOL", boolean.class, Boolean.class),
                //Integer类型
                new SingleTypeFieldMapping(Types.INTEGER, "INT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INT4", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SIGNED", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INTEGER", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.TINYINT, "TINYINT", byte.class, Byte.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "SMALLINT", short.class, Short.class),
                new SingleTypeFieldMapping(Types.INTEGER, "MEDIUMINT", int.class, Integer.class),
                //Long型
                new SingleTypeFieldMapping(Types.BIGINT, "BIGINT", true, long.class, Long.class),
                new SingleTypeFieldMapping(Types.BIGINT, "INT8", long.class, Long.class),
                new SingleTypeFieldMapping(Types.BIGINT, "IDENTITY", long.class, Long.class),
                //货币
                new SingleTypeFieldMapping(Types.DECIMAL, "DECIMAL", true, BigDecimal.class),
                new SingleTypeFieldMapping(Types.NUMERIC, "NUMBER", BigDecimal.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "DEC", BigDecimal.class),
                new SingleTypeFieldMapping(Types.NUMERIC, "NUMERIC", BigDecimal.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "IDENTITY", BigDecimal.class),
                //双精度实数
                new SingleTypeFieldMapping(Types.DOUBLE, "DOUBLE", true, double.class, Double.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "PRECISION", double.class, Double.class),
                new SingleTypeFieldMapping(Types.FLOAT, "FLOAT", double.class, Double.class),
                new SingleTypeFieldMapping(Types.FLOAT, "FLOAT4", double.class, Double.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "FLOAT8", double.class, Double.class),
                //实数
                new SingleTypeFieldMapping(Types.REAL, "REAL", float.class, Float.class),
                //日期类型
                new SingleTypeFieldMapping(Types.TIME, "TIME", Time.class),
                new SingleTypeFieldMapping(Types.DATE, "DATE", java.sql.Date.class, LocalDate.class),
                new SingleTypeFieldMapping(Types.TIMESTAMP, "TIMESTAMP", Date.class, Timestamp.class),
                new SingleTypeFieldMapping(Types.DATE, "DATETIME", true, Date.class, Timestamp.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.DATE, "SMALLDATETIME", Date.class, Timestamp.class, LocalDateTime.class),
                //二进制
                new SingleTypeFieldMapping(Types.BINARY, "BINARY", true, byte[].class),
                new SingleTypeFieldMapping(Types.VARBINARY, "VARBINARY", byte[].class),
                new SingleTypeFieldMapping(Types.LONGVARBINARY, "LONGVARBINARY", byte[].class),
                new SingleTypeFieldMapping(Types.OTHER, "RAW", byte[].class),
                new SingleTypeFieldMapping(Types.OTHER, "BYTEA", byte[].class),
                //字符串类型
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR", "VARCHAR(255)", true, String.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "LONGVARCHAR", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR2", String.class),
                new SingleTypeFieldMapping(Types.NVARCHAR, "NVARCHAR", String.class),
                new SingleTypeFieldMapping(Types.NVARCHAR, "NVARCHAR2", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR_CASESENSITIVE", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR_IGNORECASE", String.class),
                //字符型
                new SingleTypeFieldMapping(Types.CHAR, "CHAR", String.class),
                new SingleTypeFieldMapping(Types.CHAR, "CHARACTER", String.class),
                new SingleTypeFieldMapping(Types.NCHAR, "NCHAR", String.class),
                //二进制大对象
                new SingleTypeFieldMapping(Types.BLOB, "BLOB", true, Blob.class, InputStream.class),
                new SingleTypeFieldMapping(Types.BLOB, "TINYBLOB", Blob.class, InputStream.class),
                new SingleTypeFieldMapping(Types.BLOB, "MEDIUMBLOB", Blob.class, InputStream.class),
                new SingleTypeFieldMapping(Types.BLOB, "LONGBLOB", Blob.class, InputStream.class),
                new SingleTypeFieldMapping(Types.OTHER, "IMAGE", Blob.class, InputStream.class),
                new SingleTypeFieldMapping(Types.OTHER, "OID", Blob.class, InputStream.class),
                //文本大对象
                new SingleTypeFieldMapping(Types.CLOB, "CLOB", true, Clob.class, Reader.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "TINYTEXT", Clob.class, Reader.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "TEXT", Clob.class, Reader.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "MEDIUMTEXT", Clob.class, Reader.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "LONGTEXT", Clob.class, Reader.class),
                new SingleTypeFieldMapping(Types.NCLOB, "NTEXT", true, NClob.class, Reader.class),
                new SingleTypeFieldMapping(Types.NCLOB, "NCLOB", NClob.class, Reader.class),
                //通用唯一标识符
                new SingleTypeFieldMapping(Types.OTHER, "UUID", UUID.class),
                //数组类型
                new SingleTypeFieldMapping(Types.ARRAY, "ARRAY", Array.class, Object[].class)
        );
    }
}
