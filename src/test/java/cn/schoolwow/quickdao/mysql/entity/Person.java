package cn.schoolwow.quickdao.mysql.entity;

import cn.schoolwow.quickdao.annotation.*;

import java.time.LocalDateTime;

@Comment("人")
public class Person extends PersonDetail{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;

    @Comment("密码")
    @ColumnType("varchar(32)")
    @Constraint(notNull = true)
    @TableField(function = "md5(concat('salt#',#{password}))")
    private String password;

    @Comment("创建时间")
    @TableField(createdAt = true)
    private LocalDateTime createdAt;

    @Comment("更新时间")
    @TableField(updatedAt = true)
    private LocalDateTime updatedAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
