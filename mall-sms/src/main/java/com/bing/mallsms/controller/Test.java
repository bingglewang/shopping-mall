package com.bing.mallsms.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class Test {

    @RequestMapping("/print")
    @ResponseBody
    public String  test(){
        return "success";
    }
}
