package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * Postgre数据库类型字段映射
 */
public class PostgreTypeFieldMapping extends AbstractTypeFieldMapping {
    public PostgreTypeFieldMapping() {
        super.typeFieldMappingList = Arrays.asList(
                //https://www.runoob.com/postgresql/postgresql-data-type.html
                //数值类型
                new SingleTypeFieldMapping(Types.SMALLINT, "SMALLINT", true, short.class, Short.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "INT2",  short.class, Short.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INT", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INT4", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INTEGER", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.BIGINT, "BIGINT", true, long.class, Long.class),
                new SingleTypeFieldMapping(Types.BIGINT, "INT8",  long.class, Long.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "DECIMAL", true, float.class, Float.class),
                new SingleTypeFieldMapping(Types.NUMERIC, "NUMERIC", float.class, Float.class),
                new SingleTypeFieldMapping(Types.REAL, "REAL", float.class, Float.class),
                new SingleTypeFieldMapping(Types.REAL, "FLOAT4", float.class, Float.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "DOUBLE PRECISION", true, double.class, Double.class),
                new SingleTypeFieldMapping(Types.DOUBLE, "FLOAT8", double.class, Double.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SMALLSERIAL", short.class, Short.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SERIAL2", short.class, Short.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SERIAL", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SERIAL4", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "BIGSERIAL", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "SERIAL8", int.class, Integer.class),
                //货币类型
                new SingleTypeFieldMapping(Types.DECIMAL, "MONEY", BigDecimal.class),
                //字符串类型
                new SingleTypeFieldMapping(Types.CHAR, "CHARACTER VARYING", String.class, Clob.class, InputStream.class, Reader.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "VARCHAR", "varchar(255)", true, String.class),
                new SingleTypeFieldMapping(Types.CHAR, "CHARACTER", String.class),
                new SingleTypeFieldMapping(Types.CHAR, "CHAR", String.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "TEXT", true, String.class, Clob.class, InputStream.class, Reader.class),
                //日期类型
                new SingleTypeFieldMapping(Types.TIMESTAMP, "TIMESTAMP", Date.class, Timestamp.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.DATE, "DATE", java.sql.Date.class, LocalDate.class),
                new SingleTypeFieldMapping(Types.TIME, "TIME", Time.class),
                new SingleTypeFieldMapping(Types.INTEGER, "INTERVAL", Long.class),
                //Boolean类型
                new SingleTypeFieldMapping(Types.BOOLEAN, "BOOLEAN", true, boolean.class, Boolean.class),
                new SingleTypeFieldMapping(Types.BOOLEAN, "BOOL",  boolean.class, Boolean.class),
                //几何类型
                new SingleTypeFieldMapping(Types.OTHER, "POINT", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "LINE", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "LSEG", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "BOX", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "PATH", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "POLYGON", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "CIRCLE", String.class),
                //网络地址类型
                new SingleTypeFieldMapping(Types.OTHER, "CIDR", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "INET", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "MACADDR", String.class),
                //位串
                new SingleTypeFieldMapping(Types.BIT, "BIT", true, byte[].class, InputStream.class),
                new SingleTypeFieldMapping(Types.BIT, "BIT VARYING", byte[].class, InputStream.class),
                //文本搜索类型
                new SingleTypeFieldMapping(Types.OTHER, "TSVECTOR", String.class),
                new SingleTypeFieldMapping(Types.OTHER, "TSQUERY", String.class),
                //UUID
                new SingleTypeFieldMapping(Types.OTHER, "TXID_SNAPSHOT", String.class),
                //用户级别事务ID快照
                new SingleTypeFieldMapping(Types.OTHER, "UUID", UUID.class)
        );
    }
}
