package com.example.metadataui.customization.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("biz_customer_friend_tag")
public class BizCustomerFriendTag {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long customerPkId;
    private String tagName;
    private String color;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerPkId() { return customerPkId; }
    public void setCustomerPkId(Long customerPkId) { this.customerPkId = customerPkId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
