package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.util.MigrateOption;
import cn.schoolwow.quickdao.domain.util.TableStructureSynchronizedOption;
import com.alibaba.fastjson.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO工具类
 * */
public class DAOUtils {
    private static Logger logger = LoggerFactory.getLogger(DAOUtils.class);

    /**
     * 数据库表结构同步
     * */
    public static void tableStructureSynchronized(TableStructureSynchronizedOption option){
        if(null==option.source){
            throw new IllegalArgumentException("请指定迁移源数据库!");
        }
        if(null==option.target){
            throw new IllegalArgumentException("请指定迁移目标数据库!");
        }
        List<Entity> sourceEntityList = option.source.getDbEntityList();
        for(Entity sourceEntity:sourceEntityList){
            Entity targetEntity = option.target.getDbEntity(sourceEntity.tableName);
            if(null==targetEntity){
                if(null!=option.createTablePredicate&&!option.createTablePredicate.test(sourceEntity)){
                    continue;
                }
                option.target.create(sourceEntity);
                continue;
            }
            //比对属性
            for(Property property:sourceEntity.properties){
                if(targetEntity.properties.stream().noneMatch(property1 -> property1.column.equalsIgnoreCase(property.column))){
                    if(null!=option.createPropertyPredicate&&!option.createPropertyPredicate.test(property)){
                        continue;
                    }
                    option.target.createColumn(sourceEntity.tableName,property);
                }
            }
        }
    }

    /**
     * 数据库迁移
     * @param migrateOption 迁移选项
     * */
    public static void migrate(MigrateOption migrateOption){
        if(null==migrateOption.source){
            throw new IllegalArgumentException("请指定迁移源数据库!");
        }
        if(null==migrateOption.target){
            throw new IllegalArgumentException("请指定迁移目标数据库!");
        }
        List<Entity> sourceEntityList = migrateOption.source.getDbEntityList();
        if(null!=migrateOption.tableFilter){
            sourceEntityList = sourceEntityList.stream().filter(migrateOption.tableFilter).collect(Collectors.toList());
        }
        if(null==sourceEntityList||sourceEntityList.isEmpty()){
            logger.warn("[数据库迁移]当前迁移源数据库表列表为空!");
            return;
        }
        //禁用外键约束
        migrateOption.target.enableForeignConstraintCheck(false);
        try{
            for(Entity sourceEntity:sourceEntityList){
                Entity targetEntity = sourceEntity.clone();
                if(null!=migrateOption.tableConsumer){
                    migrateOption.tableConsumer.accept(sourceEntity,targetEntity);
                }
                if(migrateOption.target.hasTable(targetEntity.tableName)){
                    logger.debug("[数据迁移]删除目标数据库表:{}",targetEntity.tableName);
                    migrateOption.target.dropTable(targetEntity.tableName);
                }
                logger.debug("[数据迁移]创建目标数据库表:{}",targetEntity.tableName);

                targetEntity.escapeTableName = migrateOption.target.getQuickDAOConfig().database.escape(targetEntity.tableName);
                migrateOption.target.create(targetEntity);

                long count = migrateOption.source.query(sourceEntity.tableName).execute().count();
                int effect = 0;
                if(count>0){
                    //传输数据
                    long totalPage = count/migrateOption.batchCount+1;
                    logger.info("[数据迁移]准备迁移数据库表,源表:{},总记录数:{},迁移目标表:{}",sourceEntity.tableName,count,targetEntity.tableName);
                    for(int i=1;i<=totalPage;i++){
                        logger.debug("[数据迁移]准备传输第{}/{}页数据,源数据库表:{},目标数据库表:{}",i,totalPage,sourceEntity.tableName,targetEntity.tableName);
                        JSONArray array = migrateOption.source.query(sourceEntity.tableName)
                                .page(i,migrateOption.batchCount)
                                .execute()
                                .getArray();
                        effect += migrateOption.target.query(targetEntity.tableName)
                                .addInsert(array)
                                .execute()
                                .insertBatch();
                        logger.debug("[数据迁移]第{}/{}页数据传输完毕,迁移完成记录数:{}/{},源数据库表:{},目标数据库表:{}",i,totalPage,effect,count,sourceEntity.tableName,targetEntity.tableName);
                    }
                }
                logger.info("[数据迁移]表数据迁移完毕,迁移完成记录数:{}/{},源数据库表:{},目标数据库表:{}",effect,count,sourceEntity.tableName,targetEntity.tableName);
            }
        } finally {
            migrateOption.target.enableForeignConstraintCheck(true);
        }
    }
}
