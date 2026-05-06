package com.campus.timebank.web.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "username cannot be blank")
    private String username;

    @Email(message = "email format is invalid")
    private String email;

    private String campus;

    private String profile;
}
