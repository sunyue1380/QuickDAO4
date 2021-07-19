package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.domain.GenerateEntityFileOption;

public interface EntityHandler {
    /**
     * 获取实体类信息
     */
    void getEntityMap() throws Exception;
    /**
     * 生成实体类Java文件
     * @param generateEntityFileOption 生成实体类文件
     */
    void generateEntityFile(GenerateEntityFileOption generateEntityFileOption);
}
