package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 获取用户名
     * @return
     */
    @RequestMapping("name")
    public String name() {
        return  SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
