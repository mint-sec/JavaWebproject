package com.campus.timebank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.timebank.common.enums.SkillStatus;
import com.campus.timebank.common.exception.BizException;
import com.campus.timebank.domain.entity.SkillItem;
import com.campus.timebank.mapper.SkillItemMapper;
import com.campus.timebank.service.SkillItemService;
import com.campus.timebank.service.UserService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class SkillItemServiceImpl extends ServiceImpl<SkillItemMapper, SkillItem> implements SkillItemService {

    private final UserService userService;

    public SkillItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public SkillItem createSkill(SkillItem skillItem) {
        if (userService.getById(skillItem.getOwnerId()) == null) {
            throw new BizException("owner does not exist");
        }
        if (skillItem.getPricePerHour() == null || skillItem.getPricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("pricePerHour must be greater than 0");
        }
        if (skillItem.getCapacity() == null || skillItem.getCapacity() <= 0) {
            skillItem.setCapacity(1);
        }
        if (skillItem.getStatus() == null) {
            skillItem.setStatus(SkillStatus.PENDING_REVIEW.getCode());
        }
        save(skillItem);
        return skillItem;
    }
}
