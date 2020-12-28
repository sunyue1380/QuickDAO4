package cn.schoolwow.quickdao.handler;

public interface EntityHandler {
    /**
     * 获取实体类信息
     */
    void getEntityMap() throws Exception;
    /**
     * 生成entity的java文件
     * @param sourcePath 生成文件夹路径
     * @param tableNames 指定需要生成实体类的对应的表名
     */
    void generateEntityFile(String sourcePath, String[] tableNames);
}
