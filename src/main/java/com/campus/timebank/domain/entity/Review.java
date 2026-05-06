package com.campus.timebank.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("review_record")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long skillItemId;
    private Long reviewerId;
    private Long revieweeId;
    private Integer rating;
    private String content;
    private LocalDateTime createTime;
}
