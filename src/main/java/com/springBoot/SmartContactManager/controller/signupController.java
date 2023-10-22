package com.springBoot.SmartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springBoot.SmartContactManager.Dao.UserRepository;
import com.springBoot.SmartContactManager.Entity.User;
import com.springBoot.SmartContactManager.helper.Message;
import com.springBoot.SmartContactManager.services.emailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class signupController {
    
    @Autowired
    private HttpSession session;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private emailService eService;

    User userx;

    //Handler to show the signup page
    @RequestMapping("/signup")
    public String sign(Model model){
        model.addAttribute("title", "Smart Contact Manager-SignUp");
        model.addAttribute("user", new User());
        return "signup";
    }

    //Handler to process the entered form data
    @PostMapping("/signup-check")
    public String signupCheck(@ModelAttribute("user") User user,  //Gets the user entered from the form
                                @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, //checks if the user has accepted the terms or conditions or not
                                Model model,  // Using the model to add any new attributes required
                                HttpSession session, @RequestParam("profileImage") MultipartFile file){
        boolean check1 = false, check2 = false;
        try {
            if(!agreement){                                                                          //Cheking if the user checked the terms box
                check1 = true;                                                                       //check1 will check for the above
                throw new Exception("You have not accepted the terms and conditions.");
            }else if(userRepository.getUserByUserName(user.getEmail())!=null){                       //Checking if the email is already registered or not
                check2 = true;
                throw new Exception("Email already registered.");
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
                user.setImageURL("user.png");                                               //setting a default image if the image is not provided
            }
            userx = user;
            return sendOTP(user.getEmail());
        }catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            model.addAttribute("agreement", agreement);
            if(check1 == true)
                session.setAttribute("message", new Message("Check the terms and conditions", "alert-danger"));
            else if(check2 == true)
                session.setAttribute("message", new Message("Email Id Already Registered", "alert-danger"));
            return "signup";
        }
    }
    
    //Handler to send otp
    @PostMapping("/send-signup-otp")
    public String sendOTP(String email){
        try {
            int otp = (int)(Math.random()*1000000+Math.random()*100000+Math.random()*10000+Math.random()*1000+Math.random()*100+Math.random()*10);               //Generating random OTP (6 digits)
            String OTP = Integer.toString(otp);
            session.setAttribute("otp", OTP);                 //Storing the Otp to verify later
            eService.sendOTPEmail(email, OTP);                     //sending email containg otp
            return "signupOTP";                    //returning the page where user have to put the otp
        }catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! Try again later.", "alert-danger"));
            return "signup";             //Returning the email page if something went wrong
        }
    }

    //Handler to validate otp
    @RequestMapping("/signup-otp-check")
    public String checkOtp(@RequestParam("otp") String OTP, Model model){
        if(session.getAttribute("otp").equals(OTP)){          // Checking if the entered otp is same as send via email
            session.removeAttribute("message");        
            session.removeAttribute("otp");                   //Removing the stored otp as soon as the user entered the otp
            //if we do not remove the otp from the session then the user will get infinite number of tries to enter the otp -- Security Issue
            session.setAttribute("message",new Message("Registration successful!!", "alert-success"));
            model.addAttribute("user", new User());
            userRepository.save(userx);
            return "login";
        }else{
            session.setAttribute("message", new Message("Wrong OTP entered", "alert-danger"));
            session.removeAttribute("otp");
            model.addAttribute("user", new User());
            return "signup";             // If the otp entered is wrong then user have to re-enter the email and get another otp
        }
    }
}
