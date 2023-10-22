package com.springBoot.SmartContactManager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class homeController {

    //Handler to show the Home page
    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("title", "Smart Contact Manager-Home");
        return "home";
    }
    //Handler to show the Login page
    @RequestMapping("/login")
    public String login(Model model){
        model.addAttribute("title", "Smart Contact Manager-Login");
        return "login";
    }

    //Handler to show deafult login page
    @GetMapping("/showMyLoginPage")
    public String showMyLoginPage(){
        return "login";
    }
}
