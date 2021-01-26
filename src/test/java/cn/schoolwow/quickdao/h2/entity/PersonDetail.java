package cn.schoolwow.quickdao.h2.entity;

import cn.schoolwow.quickdao.annotation.*;

@Ignore
@Comment("人物表详细信息")
@CompositeIndex(columns = {"lastName","firstName"},comment = "人物姓名索引")
@CompositeIndex(columns = {"address"},comment = "地址索引")
@UniqueField(columns = "lastName")
public class PersonDetail {
    @Comment("姓")
    @ColumnType("varchar(64)")
    @Constraint(notNull = true)
    @Index(indexType = IndexType.NORMAL, indexName = "last_name_index", comment = "人物姓氏")
    private String lastName;

    @Comment("名")
    @ColumnType("varchar(255)")
    private String firstName;

    @Comment("地址")
    @ColumnType("varchar(255)")
    private String address;

    @Comment("城市")
    @ColumnType("varchar(255)")
    private String city;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
