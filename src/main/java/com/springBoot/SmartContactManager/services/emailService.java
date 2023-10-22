package com.springBoot.SmartContactManager.services;

// import java.net.Authenticator;
// import java.net.PasswordAuthentication;
// import java.util.Properties;

// import org.springframework.boot.autoconfigure.web.ServerProperties.Reactive.Session;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class emailService {

    //class to send Email to the user

    private final JavaMailSender javaMailSender;

    public emailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    public void sendOTPEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code for Smart Contact Manager");
        message.setText("Your OTP code for changing your password is: " + otp);

        javaMailSender.send(message);
    }
}
