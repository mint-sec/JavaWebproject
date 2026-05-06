package com.campus.timebank.web.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.timebank.common.result.ApiResponse;
import com.campus.timebank.domain.entity.SkillItem;
import com.campus.timebank.service.SkillItemService;
import com.campus.timebank.web.dto.SkillItemCreateRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
public class SkillItemController {

    private final SkillItemService skillItemService;

    public SkillItemController(SkillItemService skillItemService) {
        this.skillItemService = skillItemService;
    }

    @PostMapping
    public ApiResponse<SkillItem> createSkill(@Valid @RequestBody SkillItemCreateRequest request) {
        SkillItem skillItem = new SkillItem();
        BeanUtils.copyProperties(request, skillItem);
        return ApiResponse.success(skillItemService.createSkill(skillItem));
    }

    @GetMapping
    public ApiResponse<List<SkillItem>> listSkills(@RequestParam(required = false) String category,
                                                   @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SkillItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), SkillItem::getCategory, category);
        wrapper.eq(status != null, SkillItem::getStatus, status);
        wrapper.orderByDesc(SkillItem::getId);
        return ApiResponse.success(skillItemService.list(wrapper));
    }
}
