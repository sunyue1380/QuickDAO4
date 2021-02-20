package cn.schoolwow.quickdao.postgre.entity;

import cn.schoolwow.quickdao.annotation.*;

@Comment("订单")
public class Order {
    @Id(strategy = IdStrategy.None)
    private String id;

    @Comment("订单id")
    @Constraint(notNull = true,check = "#{orderNo} > 0")
    private int orderNo;

    @Comment("所属人")
    @ForeignKey(table = Person.class)
    @Constraint(notNull = true,check = "#{personId} > 0")
    private long personId;

    @Comment("所属人姓名")
    @ForeignKey(table = Person.class,field = "last_name")
    private String lastName;

    private Person person;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public long getPersonId() {
        return personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
