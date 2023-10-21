package com.springBoot.SmartContactManager.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springBoot.SmartContactManager.Dao.UserRepository;
import com.springBoot.SmartContactManager.Entity.User;
import com.springBoot.SmartContactManager.helper.Message;
import com.springBoot.SmartContactManager.services.emailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class forgotController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private emailService eService;
    Random random = new Random(100000);
    @RequestMapping("/forgotPassword")
    public String openEmailForm(){

        return "forgotPassword/forgot_email_form";
    }
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email){
        try {
            int otp = random.nextInt(1000000);
            String OTP = Integer.toString(otp);
            session.setAttribute("otp", OTP);
            session.setAttribute("userEmail", email);
            eService.sendOTPEmail(email, OTP);
            return "forgotPassword/verify_otp";
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! Try again later.", "alert-danger"));
            return "forgotPassword/forgot_email_form";
        }
        
    }

    @RequestMapping("/otp-check")
    public String checkOtp(@RequestParam("otp") String OTP){


        if(session.getAttribute("otp").equals(OTP)){
            session.removeAttribute("message");
            session.removeAttribute("otp");
            return "forgotPassword/change_password";
        }else{
            session.setAttribute("message", new Message("Wrong OTP entered", "alert-danger"));
            session.removeAttribute("otp");
            return "forgotPassword/forgot_email_form";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword, @RequestParam("confirmNewPassword") String confirmNewPassword){
        if(newPassword.equals(confirmNewPassword)){
            User user = userRepository.getUserByUserName(session.getAttribute("userEmail").toString());
            user.setPassword("{noop}"+confirmNewPassword);
            userRepository.save(user);
            session.setAttribute("message", new Message("Password changed!!", "alert-success"));
            return "forgotPassword/forgot_email_form";
        }else{
            session.setAttribute("message", new Message("Passwords did not match!!", "alert-danger"));
            return "forgotPassword/change_password";
        }
    }
}
