package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class OracleDDLBuilder extends PostgreDDLBuilder{

    public OracleDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        String hasTableExistsSQL = "select table_name from user_tables where table_name = '" + entity.tableName + "'";
        ResultSet resultSet = connectionExecutor.executeQuery("判断表是否存在",hasTableExistsSQL);
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        super.createTable(entity);
        createSequence(entity);
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String hasIndexExistsSQL = "select index_name from user_indexes where table_name = '" + tableName + "' and index_name = '" + indexName + "'";
        ResultSet resultSet = connectionExecutor.executeQuery("查看索引是否存在",hasIndexExistsSQL);
        boolean result = false;
        if (resultSet.next()) {
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {

    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","");
        fieldTypeMapping.put("java.lang.Byte","");
        fieldTypeMapping.put("[B","");
        fieldTypeMapping.put("boolean","");
        fieldTypeMapping.put("char","CHAR");
        fieldTypeMapping.put("java.lang.Character","CHAR");
        fieldTypeMapping.put("short","INTEGER");
        fieldTypeMapping.put("java.lang.Short","INTEGER");
        fieldTypeMapping.put("int","INTEGER");
        fieldTypeMapping.put("java.lang.Integer","INTEGER");
        fieldTypeMapping.put("float","BINARY_FLOAT");
        fieldTypeMapping.put("java.lang.Float","BINARY_FLOAT");
        fieldTypeMapping.put("long","INTEGER");
        fieldTypeMapping.put("java.lang.Long","INTEGER");
        fieldTypeMapping.put("double","BINARY_DOUBLE");
        fieldTypeMapping.put("java.lang.Double","BINARY_DOUBLE");
        fieldTypeMapping.put("java.lang.String","VARCHAR2(255)");
        fieldTypeMapping.put("java.util.Date","TIMESTAMP");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","TIMESTAMP");
        fieldTypeMapping.put("java.sql.Array","");
        fieldTypeMapping.put("java.math.BigDecimal","INTEGER");
        fieldTypeMapping.put("java.sql.Blob","BLOB");
        fieldTypeMapping.put("java.sql.Clob","CLOB");
        fieldTypeMapping.put("java.sql.NClob","NCLOB");
        fieldTypeMapping.put("java.sql.Ref","");
        fieldTypeMapping.put("java.net.URL","");
        fieldTypeMapping.put("java.sql.RowId","");
        fieldTypeMapping.put("java.sql.SQLXML","");
        fieldTypeMapping.put("java.io.InputStream","");
        fieldTypeMapping.put("java.io.Reader","");
        return fieldTypeMapping;
    }

    @Override
    protected List<Entity> getVirtualEntity(){
        Entity entity = new Entity();
        entity.tableName = "dual";
        entity.escapeTableName = "dual";
        entity.properties = new ArrayList<>();
        return Arrays.asList(entity);
    }

    @Override
    protected String getAutoIncrementSQL(Property property) {
        return quickDAOConfig.database.escape(property.column) + " number not null";
    }

    @Override
    protected void getIndex(List<Entity> entityList) throws SQLException {
        {
            String getIndexSQL = "select table_name, index_name,uniqueness from user_indexes";
            ResultSet resultSet = connectionExecutor.executeQuery("获取索引信息",getIndexSQL);
            while (resultSet.next()) {
                for(Entity entity:entityList) {
                    if (!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))) {
                        continue;
                    }
                    IndexField indexField = new IndexField();
                    if("UNIQUE".equalsIgnoreCase(resultSet.getString("uniqueness"))){
                        indexField.indexType = IndexType.UNIQUE;
                    }else{
                        indexField.indexType = IndexType.NORMAL;
                    }
                    indexField.indexName = resultSet.getString("index_name");
                    entity.indexFieldList.add(indexField);
                    break;
                }
            }
            resultSet.close();
        }
        {
            String getIndexSQL = "select table_name, index_name,column_name from user_ind_columns";
            ResultSet resultSet = connectionExecutor.executeQuery("获取索引字段信息",getIndexSQL);
            while (resultSet.next()) {
                String indexName = resultSet.getString("index_name");
                for(Entity entity:entityList) {
                    if (!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))) {
                        continue;
                    }
                    IndexField existIndexField = entity.indexFieldList.stream().filter(indexField1 -> indexField1.indexName.equals(indexName)).findFirst().orElse(null);
                    if(null==existIndexField){
                        continue;
                    }
                    existIndexField.columns.add(resultSet.getString("column_name"));
                    break;
                }
            }
            resultSet.close();
        }
    }

    @Override
    protected void getEntityPropertyList(List<Entity> entityList) throws SQLException {
        {
            String getEntityPropertyListSQL = "select table_name, column_name, data_type, nullable, data_length from user_tab_columns";
            ResultSet resultSet = connectionExecutor.executeQuery("获取表字段信息",getEntityPropertyListSQL);
            while (resultSet.next()) {
                for(Entity entity : entityList) {
                    if (!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))) {
                        continue;
                    }
                    Property property = new Property();
                    property.column = resultSet.getString("column_name");
                    property.columnType = resultSet.getString("data_type");
                    if(property.columnType.contains(" ")){
                        property.columnType = property.columnType.substring(0,property.columnType.indexOf(" "));
                    }
                    String dataLength = resultSet.getString("data_length");
                    if(null!=dataLength&&!dataLength.isEmpty()){
                        property.columnType += "(" + dataLength + ")";
                    }
                    property.notNull = "N".equals(resultSet.getString("nullable"));
                    entity.properties.add(property);
                    break;
                }
            }
            resultSet.close();
        }
        {
            //获取字段注释
            String getPropertyCommentList = "select table_name, column_name, comments from user_col_comments";
            ResultSet resultSet = connectionExecutor.executeQuery("获取字段注释", getPropertyCommentList);
            while(resultSet.next()){
                for(Entity entity : entityList) {
                    if (!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))) {
                        continue;
                    }
                    for(Property property : entity.properties){
                        if(property.column.equalsIgnoreCase(resultSet.getString("column_name"))){
                            property.comment = resultSet.getString("comments");
                            break;
                        }
                    }
                    break;
                }
            }
            resultSet.close();
        }
    }

    @Override
    protected List<Entity> getEntityList() throws SQLException {
        String getEntityListSQL = "select user_tables.table_name, user_tab_comments.comments from user_tables left join user_tab_comments on user_tables.table_name = user_tab_comments.table_name";
        ResultSet resultSet = connectionExecutor.executeQuery("获取表列表",getEntityListSQL);

        List<Entity> entityList = new ArrayList<>();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("table_name");
            entity.comment = resultSet.getString("comments");
            entityList.add(entity);
        }
        resultSet.close();
        return entityList;
    }

    /**创建序列和触发器*/
    private void createSequence(Entity entity) throws SQLException {
        if(null==entity.id){
            return;
        }
        //创建sequence
        String sequenceExistSQL = "select sequence_name from user_sequences where sequence_name= '" + entity.tableName.toUpperCase() + "_SEQ'";
        ResultSet resultSet = connectionExecutor.executeQuery("判断序列是否存在",sequenceExistSQL);
        if(resultSet.next()){
            //删除序列
            connectionExecutor.executeUpdate("删除序列","drop sequence " + entity.tableName.toUpperCase() + "_SEQ");
        }
        resultSet.close();
        //创建序列
        String createSequence = "create sequence " + entity.tableName + "_seq increment by 1 start with 1 minvalue 1 maxvalue 9999999999999 nocache order";
        connectionExecutor.executeUpdate("创建序列",createSequence);
        //创建触发器
        String createTrigger = "create or replace trigger " + entity.tableName + "_trigger " +
                "before insert on " + entity.escapeTableName + " " +
                "for each row when(new.\"" + entity.id.column + "\" is null) " +
                "begin select " + entity.tableName + "_seq.nextval into:new.\"" + entity.id.column + "\" from dual; end;";
        connectionExecutor.connection.createStatement().executeUpdate(createTrigger);
    }
}
