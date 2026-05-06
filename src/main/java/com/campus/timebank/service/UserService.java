package com.campus.timebank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.timebank.domain.entity.User;

public interface UserService extends IService<User> {

    User createUser(User user);
}
