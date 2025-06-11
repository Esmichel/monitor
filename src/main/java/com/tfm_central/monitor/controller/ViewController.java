package com.tfm_central.monitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/*")
    public String showConfigPage() {
        return "config-view";  // Thymeleaf will look for templates/config-view.html
    }
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // Thymeleaf will look for templates/config-view.html
    }
}
