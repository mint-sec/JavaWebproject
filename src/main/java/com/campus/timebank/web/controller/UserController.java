package com.campus.timebank.web.controller;

import com.campus.timebank.common.result.ApiResponse;
import com.campus.timebank.domain.entity.User;
import com.campus.timebank.service.UserService;
import com.campus.timebank.web.dto.UserCreateRequest;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return ApiResponse.success(userService.createUser(user));
    }

    @GetMapping
    public ApiResponse<List<User>> listUsers() {
        return ApiResponse.success(userService.list());
    }
}
