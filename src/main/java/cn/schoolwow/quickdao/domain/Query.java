package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.builder.dql.AbstractDQLBuilder;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询
 */
public class Query implements Serializable,Cloneable {
    /**
     * 是否开启事务
     * */
    public boolean transaction;
    /**
     * 关联Entity
     */
    public transient Entity entity;
    /**
     * from子查询
     */
    public Query fromQuery;
    /**
     * 关联表计数
     * */
    public int joinTableIndex = 1;
    /**
     * 主表别名
     */
    public String tableAliasName = "t";
    /**
     * distinct
     */
    public String distinct = "";
    /**
     * 列名
     */
    public StringBuilder columnBuilder = new StringBuilder();
    /**
     * 列名类型转换<返回列名,类型>
     */
    public Map<String,Class> columnTypeMap = new HashMap<>();
    /**
     * 字段插入
     */
    public StringBuilder insertBuilder = new StringBuilder();
    /**
     * 字段更新
     */
    public StringBuilder setBuilder = new StringBuilder();
    /**
     * 查询条件
     */
    public StringBuilder whereBuilder = new StringBuilder();
    /**
     * 分组查询
     */
    public StringBuilder groupByBuilder = new StringBuilder();
    /**
     * having查询
     */
    public StringBuilder havingBuilder = new StringBuilder();
    /**
     * 排序
     */
    public StringBuilder orderByBuilder = new StringBuilder();
    /**
     * 分页
     */
    public String limit = "";
    /**
     * 是否返回复杂属性
     */
    public boolean compositField;
    /**
     * union类型
     * */
    public transient UnionType unionType;
    /**
     * union语句列表
     * */
    public List<AbstractCondition> unionList = new ArrayList<>();
    /**
     * or查询语句列表
     * */
    public List<AbstractCondition> orList = new ArrayList<>();
    /**
     * 参数索引
     */
    public int parameterIndex = 1;
    /**
     * select子查询
     */
    public List<Query> selectQueryList = new ArrayList();
    /**
     * 查询参数
     */
    public List parameterList = new ArrayList();
    /**
     * 插入参数对象
     */
    public JSONObject insertValue;
    /**
     * 批量插入参数对象
     */
    public JSONArray insertArray;
    /**
     * 插入参数
     */
    public List insertParameterList = new ArrayList();
    /**
     * 更新参数
     */
    public List updateParameterList = new ArrayList();
    /**
     * having参数
     */
    public List havingParameterList = new ArrayList();
    /**
     * 分页对象
     * */
    public PageVo pageVo;
    /**
     * 关联子查询
     */
    public List<SubQuery> subQueryList = new ArrayList<>();
    /**
     * DQLBuilder实例
     * */
    public transient AbstractDQLBuilder dqlBuilder;
    /**
     * 关联QuickDAOConfig
     * */
    public transient QuickDAOConfig quickDAOConfig;

    @Override
    public Query clone(){
        ByteArrayInputStream bais = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Query query = (Query) ois.readObject();
            query.entity = this.entity;
            query.unionType = this.unionType;
            query.quickDAOConfig = this.quickDAOConfig;
            query.dqlBuilder = quickDAOConfig.database.getDQLBuilderInstance(quickDAOConfig);
            for(int i=0;i<query.orList.size();i++){
                query.orList.get(i).query = query;
            }
            for(int i=0;i<query.subQueryList.size();i++){
                query.subQueryList.get(i).entity = this.subQueryList.get(i).entity;
                query.subQueryList.get(i).query = query;
            }
            bais.close();
            return query;
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
