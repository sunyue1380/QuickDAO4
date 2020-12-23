package cn.schoolwow.quickdao.sqlite.entity;

import cn.schoolwow.quickdao.annotation.*;

import java.util.Date;

@Comment("人")
public class Person extends PersonDetail {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;

    @Comment("密码")
    @ColumnType("varchar(32)")
    @Constraint(notNull = true)
    private String password;

    @Comment("创建时间")
    @TableField(createdAt = true)
    private Date createdAt;

    @Comment("更新时间")
    @TableField(updatedAt = true)
    private Date updatedAt;

    /**关联订单*/
    private Order order;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
