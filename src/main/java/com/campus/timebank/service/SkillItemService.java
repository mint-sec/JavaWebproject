package com.campus.timebank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.timebank.domain.entity.SkillItem;

public interface SkillItemService extends IService<SkillItem> {

    SkillItem createSkill(SkillItem skillItem);
}
