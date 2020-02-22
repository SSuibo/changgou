package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PackageName: com.changgou.oauth.controller
 * @ClassName: LoginRedirectController
 * @Author: suibo
 * @Date: 2020/1/13 18:43
 * @Description: //TODO
 */
@Controller
@RequestMapping("/oauth")
public class LoginRedirectController {

    @RequestMapping("/toLogin")
    public String login(@RequestParam(value = "FROM",required = false,defaultValue = "")String from, Model model){
        model.addAttribute("from",from);
        return "login";
    }
}
