package com.campus.timebank.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("skill_item")
public class SkillItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long ownerId;
    private String title;
    private String description;
    private BigDecimal pricePerHour;
    private String category;
    private Integer capacity;
    private Integer status;
    private String tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
