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

/**
 * SQLite数据库类型字段映射
 */
public class SQLiteTypeFieldMapping extends AbstractTypeFieldMapping {
    public SQLiteTypeFieldMapping() {
        super.typeFieldMappingList = Arrays.asList(
                //https://www.runoob.com/sqlite/sqlite-data-types.html
                //Integer类型
                new SingleTypeFieldMapping(Types.INTEGER, "INT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INTEGER", true, int.class, Integer.class, long.class, Long.class),
                new SingleTypeFieldMapping(Types.TINYINT, "TINYINT", int.class, Integer.class, byte.class, Byte.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "SMALLINT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "MEDIUMINT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.BIGINT, "BIGINT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.BIGINT, "UNSIGNED BIG INT", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "INT2", short.class, Short.class),
                new SingleTypeFieldMapping(Types.BIGINT, "INT8", long.class, Long.class),
                //TEXT类型
                new SingleTypeFieldMapping(Types.CHAR, "CHARACTER", "CHARACTER(20)", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR", "VARCHAR(255)", true, String.class),
                new SingleTypeFieldMapping(Types.CHAR, "VARYING CHARACTER", "VARYING CHARACTER(255)", String.class),
                new SingleTypeFieldMapping(Types.NCHAR, "NCHAR", "NCHAR(55)", true, String.class, NClob.class),
                new SingleTypeFieldMapping(Types.CHAR, "NATIVE CHARACTER", "NATIVE CHARACTER(70)", String.class),
                new SingleTypeFieldMapping(Types.NCHAR, "NVARCHAR", "NVARCHAR(100)", String.class, NClob.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "TEXT", true, String.class, Clob.class, InputStream.class, Reader.class),
                new SingleTypeFieldMapping(Types.CLOB, "CLOB", Clob.class, InputStream.class, Reader.class),
                //NONE类型
                new SingleTypeFieldMapping(Types.BLOB, "BLOB", Blob.class, InputStream.class, Reader.class),
                //REAL类型
                new SingleTypeFieldMapping(Types.REAL, "REAL", true, float.class, Float.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "DOUBLE", true, double.class, Double.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "DOUBLE PRECISION", double.class, Double.class),
                new SingleTypeFieldMapping(Types.FLOAT, "FLOAT", float.class, Float.class),
                //NUMERIC类型
                new SingleTypeFieldMapping(Types.NUMERIC, "NUMERIC", BigDecimal.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "DECIMAL", true, BigDecimal.class),
                new SingleTypeFieldMapping(Types.BOOLEAN, "BOOLEAN", boolean.class, Boolean.class),
                new SingleTypeFieldMapping(Types.DATE, "DATE", java.sql.Date.class, LocalDate.class),
                new SingleTypeFieldMapping(Types.DATE, "DATETIME", Date.class, Timestamp.class, LocalDateTime.class)
        );
    }
}
