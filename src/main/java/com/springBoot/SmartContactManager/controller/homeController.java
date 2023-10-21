package com.springBoot.SmartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springBoot.SmartContactManager.Dao.UserRepository;
import com.springBoot.SmartContactManager.Entity.User;
import com.springBoot.SmartContactManager.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class homeController {

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private BCryptPasswordEncoder passwordEncoder;

    @RequestMapping("/")
    public String home(Model model){
        model.addAttribute("title", "Smart Contact Manager-Home");
        return "home";
    }
    
    @RequestMapping("/signup")
    public String sign(Model model){
        model.addAttribute("title", "Smart Contact Manager-SignUp");
        model.addAttribute("user", new User());
        return "signup";
    }
    
    @RequestMapping("/about")
    public String about(Model model){
        model.addAttribute("title", "Smart Contact Manager-About");
        return "about";
    }
    
    @RequestMapping("/login")
    public String login(Model model){
        model.addAttribute("title", "Smart Contact Manager-Login");
        return "login";
    }
    
    @PostMapping("/do_register")
    public String registerUser( @ModelAttribute("user") User user,  //Gets the user entered from the form
                                @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, //checks if the user has accepted the terms or conditions or not
                                Model model,  // Using the model to add any new attributes required
                                HttpSession session,
                                @RequestParam("profileImage") MultipartFile file){
        boolean check1 = false, check2 = false;
        try {
            if(!agreement){
                check1 = true;
                throw new Exception("You have not accepted the terms and conditions.");
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setPassword("{noop}"+user.getPassword());
            if(!file.isEmpty()){                                                                     //Checking if the user entered any photo or not
                // upload the file to a folder and set a unique name
                user.setImageURL(file.getOriginalFilename());                                        //Linking the image to the user
                File saveFile = new ClassPathResource("static/img").getFile();                  //saving the file in img folder
                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }else{
                user.setImageURL("user.png");                                 //setting a default image if the image is not provided
            }
            
            this.userRepository.save(user);
            check2 = true;
            model.addAttribute("user", new User());
            model.addAttribute("agreement", agreement);
            session.setAttribute("message",new Message("Registration successful!!", "alert-success"));
            return "signup";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            model.addAttribute("agreement", agreement);
            if(check1 == true)
                session.setAttribute("message", new Message("Check the terms and conditions", "alert-danger"));
            else if(check2 == false)
                session.setAttribute("message", new Message("Email Id Already Registered", "alert-danger"));
            return "signup";
        }
    }
    
    @RequestMapping("/admin")
    public String admin(){
        return "admin";
    }
    
    @GetMapping("/showMyLoginPage")
    public String showMyLoginPage(){
        return "login";
    }
}
