package com.campus.timebank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.timebank.common.exception.BizException;
import com.campus.timebank.domain.entity.User;
import com.campus.timebank.mapper.UserMapper;
import com.campus.timebank.service.UserService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User createUser(User user) {
        boolean exists = lambdaQuery().eq(User::getUsername, user.getUsername()).exists();
        if (exists) {
            throw new BizException("username already exists");
        }
        if (user.getTimeCoin() == null) {
            user.setTimeCoin(BigDecimal.ZERO);
        }
        if (user.getFrozenCoin() == null) {
            user.setFrozenCoin(BigDecimal.ZERO);
        }
        if (user.getCreditScore() == null) {
            user.setCreditScore(100);
        }
        save(user);
        return user;
    }
}
