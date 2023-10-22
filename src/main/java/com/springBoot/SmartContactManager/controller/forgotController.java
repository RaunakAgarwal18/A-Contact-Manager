package com.springBoot.SmartContactManager.controller;

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
    private emailService eService;                                 //Required to send email to the User

    @RequestMapping("/forgotPassword")
    public String openEmailForm(){
        return "forgotPassword/forgot_email_form";
    }

    //Handler to send the otp via email
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email){
        try {
            int otp = (int)(Math.random()*1000000+Math.random()*100000+Math.random()*10000+Math.random()*1000+Math.random()*100+Math.random()*10);
            String OTP = Integer.toString(otp);
            session.setAttribute("otp", OTP);                 //Storing the Otp to verify later
            session.setAttribute("userEmail", email);
            eService.sendOTPEmail(email, OTP);                     //sending email containg otp
            return "forgotPassword/verify_otp";                    //returning the page where user have to put the otp
        }catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong! Try again later.", "alert-danger"));
            return "forgotPassword/forgot_email_form";             //Returning the email page if something went wrong
        }
    }

    //Handler to check the entered otp
    @RequestMapping("/otp-check")
    public String checkOtp(@RequestParam("otp") String OTP){
        if(session.getAttribute("otp").equals(OTP)){          // Checking if the entered otp is same as send via email
            session.removeAttribute("message");        
            session.removeAttribute("otp");                   //Removing the stored otp as soon as the user entered the otp

            //if we do not remove the otp from the session then the user will get infinite number of tries to enter the otp -- Security Issue

            return "forgotPassword/change_password";
        }else{
            session.setAttribute("message", new Message("Wrong OTP entered", "alert-danger"));
            session.removeAttribute("otp");
            return "forgotPassword/forgot_email_form";             // If the otp entered is wrong then user have to re-enter the email and get another otp
        }
    }

    //Handler to change the user password after the correct otp is entered
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword, @RequestParam("confirmNewPassword") String confirmNewPassword){
        if(newPassword.equals(confirmNewPassword)){                                                                     //Checking if both password are same
            User user = userRepository.getUserByUserName(session.getAttribute("userEmail").toString());
            user.setPassword("{noop}"+confirmNewPassword);
            userRepository.save(user);
            session.setAttribute("message", new Message("Password changed!!", "alert-success"));
            return "login";
        }else{
            session.setAttribute("message", new Message("Passwords did not match!!", "alert-danger"));
            return "forgotPassword/change_password";
        }
    }
}
