package cn.schoolwow.quickdao.h2.entity;

import cn.schoolwow.quickdao.annotation.Comment;
import cn.schoolwow.quickdao.annotation.Id;
import cn.schoolwow.quickdao.annotation.IdStrategy;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Comment("测试类型实体类")
public class TypeEntity {
    @Id(strategy = IdStrategy.IdGenerator)
    private long id;

    private byte byteType;

    private byte[] bytes;

    private boolean booleanType;

    private short shortType;

    private int intType;

    private float floatType;

    private long longType;

    private double doubleType;

    private String stringType;

    private Date dateType;

    private java.sql.Date dateSQLType;

    private Time timeType;

    private Timestamp timestampType;

    private LocalDate localDate;

    private LocalDateTime localDateTime;

    private Array arrayType;

    private BigDecimal bigDecimalType;

    private Blob blobType;

    private Clob clobType;

    private NClob nClobType;

    private InputStream inputStreamType;

    private Reader readerType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte getByteType() {
        return byteType;
    }

    public void setByteType(byte byteType) {
        this.byteType = byteType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public boolean isBooleanType() {
        return booleanType;
    }

    public void setBooleanType(boolean booleanType) {
        this.booleanType = booleanType;
    }

    public short getShortType() {
        return shortType;
    }

    public void setShortType(short shortType) {
        this.shortType = shortType;
    }

    public int getIntType() {
        return intType;
    }

    public void setIntType(int intType) {
        this.intType = intType;
    }

    public float getFloatType() {
        return floatType;
    }

    public void setFloatType(float floatType) {
        this.floatType = floatType;
    }

    public long getLongType() {
        return longType;
    }

    public void setLongType(long longType) {
        this.longType = longType;
    }

    public double getDoubleType() {
        return doubleType;
    }

    public void setDoubleType(double doubleType) {
        this.doubleType = doubleType;
    }

    public String getStringType() {
        return stringType;
    }

    public void setStringType(String stringType) {
        this.stringType = stringType;
    }

    public Date getDateType() {
        return dateType;
    }

    public void setDateType(Date dateType) {
        this.dateType = dateType;
    }

    public java.sql.Date getDateSQLType() {
        return dateSQLType;
    }

    public void setDateSQLType(java.sql.Date dateSQLType) {
        this.dateSQLType = dateSQLType;
    }

    public Time getTimeType() {
        return timeType;
    }

    public void setTimeType(Time timeType) {
        this.timeType = timeType;
    }

    public Timestamp getTimestampType() {
        return timestampType;
    }

    public void setTimestampType(Timestamp timestampType) {
        this.timestampType = timestampType;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public Array getArrayType() {
        return arrayType;
    }

    public void setArrayType(Array arrayType) {
        this.arrayType = arrayType;
    }

    public BigDecimal getBigDecimalType() {
        return bigDecimalType;
    }

    public void setBigDecimalType(BigDecimal bigDecimalType) {
        this.bigDecimalType = bigDecimalType;
    }

    public Blob getBlobType() {
        return blobType;
    }

    public void setBlobType(Blob blobType) {
        this.blobType = blobType;
    }

    public Clob getClobType() {
        return clobType;
    }

    public void setClobType(Clob clobType) {
        this.clobType = clobType;
    }

    public NClob getnClobType() {
        return nClobType;
    }

    public void setnClobType(NClob nClobType) {
        this.nClobType = nClobType;
    }

    public InputStream getInputStreamType() {
        return inputStreamType;
    }

    public void setInputStreamType(InputStream inputStreamType) {
        this.inputStreamType = inputStreamType;
    }

    public Reader getReaderType() {
        return readerType;
    }

    public void setReaderType(Reader readerType) {
        this.readerType = readerType;
    }
}
