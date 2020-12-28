package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

/**
 * SQLServer数据库类型字段映射
 */
public class SQLServerTypeFieldMapping extends AbstractTypeFieldMapping {
    public SQLServerTypeFieldMapping() {
        super.typeFieldMappingList = Arrays.asList(
                //https://www.w3school.com.cn/sql/sql_datatypes.asp
                //Character字符串
                new SingleTypeFieldMapping(Types.CHAR, "char", String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "varchar", "varchar(255)", true, String.class),
                new SingleTypeFieldMapping(Types.VARCHAR, "varchar(max)", String.class),
                new SingleTypeFieldMapping(Types.LONGVARCHAR, "text", String.class),
                new SingleTypeFieldMapping(Types.NCHAR, "nchar", String.class),
                new SingleTypeFieldMapping(Types.NVARCHAR, "nvarchar", String.class),
                new SingleTypeFieldMapping(Types.NVARCHAR, "nvarchar(max)", String.class),
                new SingleTypeFieldMapping(Types.NVARCHAR, "ntext", String.class),
                //Binary类型
                new SingleTypeFieldMapping(Types.BIT, "bit", byte.class, Byte.class),
                new SingleTypeFieldMapping(Types.BINARY, "binary", "binary(1024)", true, byte[].class),
                new SingleTypeFieldMapping(Types.BINARY, "varbinary", byte[].class),
                new SingleTypeFieldMapping(Types.BINARY, "varbinary(max)", byte[].class),
                new SingleTypeFieldMapping(Types.OTHER, "image", byte[].class),
                //Number类型
                new SingleTypeFieldMapping(Types.TINYINT, "tinyint", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.SMALLINT, "smallint", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.INTEGER, "int", true, int.class, Integer.class),
                new SingleTypeFieldMapping(Types.BIGINT, "bigint", int.class, Integer.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "decimal", true, BigDecimal.class),
                new SingleTypeFieldMapping(Types.NUMERIC, "numeric", BigDecimal.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "smallmoney", BigDecimal.class),
                new SingleTypeFieldMapping(Types.DECIMAL, "money", BigDecimal.class),
                new SingleTypeFieldMapping(Types.FLOAT, "float", double.class, Double.class),
                new SingleTypeFieldMapping(Types.REAL, "real", float.class, Float.class),
                //Date类型
                new SingleTypeFieldMapping(Types.DATE, "datetime", true, Date.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.DATE, "datetime2", Date.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.DATE, "smalldatetime", Date.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.DATE, "date", java.sql.Date.class, LocalDate.class),
                new SingleTypeFieldMapping(Types.TIME, "time", Time.class),
                new SingleTypeFieldMapping(Types.DATE, "datetimeoffset", Date.class, LocalDateTime.class),
                new SingleTypeFieldMapping(Types.TIMESTAMP, "timestamp", Timestamp.class)
        );
    }
}
