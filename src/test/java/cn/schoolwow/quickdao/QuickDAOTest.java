package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import org.junit.Test;

import java.util.Arrays;

public class QuickDAOTest {

    @Test
    public void cloneTest() {
        Entity entity = new Entity();

        Property property = new Property();
        property.column = "a";
        property.columnType = "varchar(1024)";
        entity.properties = Arrays.asList(property);

        Entity cloneEntity = entity.clone();
        System.out.println(cloneEntity.properties.get(0).columnType);
    }
}