package com.astrotech.transport.controllers;


import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Hidden
public class TemplateController {
    @GetMapping("/")
    public String index() {

        return "index";
    }
}
