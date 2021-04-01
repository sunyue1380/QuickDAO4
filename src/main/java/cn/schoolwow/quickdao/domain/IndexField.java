package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.IndexType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IndexField implements Serializable,Cloneable {
    /**
     * 表名
     * */
    public String tableName;

    /**
     * 索引类型
     * */
    public transient IndexType indexType;

    /**
     * 索引名称
     * */
    public String indexName;

    /**
     * 索引方法
     * */
    public String using;

    /**
     * 索引注释
     * */
    public String comment;

    /**
     * 索引字段
     * */
    public List<String> columns = new ArrayList<>();

    /**复制拷贝transient字段*/
    public void copyTransientField(IndexField target){
        this.indexType = target.indexType;
    }

    @Override
    public IndexField clone(){
        ByteArrayInputStream bais = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            IndexField indexField = (IndexField) ois.readObject();
            indexField.copyTransientField(this);
            bais.close();
            return indexField;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(null!=bais){
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
