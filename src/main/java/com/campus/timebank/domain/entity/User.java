package com.campus.timebank.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;
    private BigDecimal timeCoin;
    private BigDecimal frozenCoin;
    private Integer creditScore;
    private String campus;
    private String profile;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
