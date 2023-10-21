package com.springBoot.SmartContactManager.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// import com.springBoot.SmartContactManager.Dao.ContactRepository;
import com.springBoot.SmartContactManager.Dao.ContactRepositoryImpl;
import com.springBoot.SmartContactManager.Dao.UserRepository;
import com.springBoot.SmartContactManager.Entity.Contact;

@RestController
public class searchController {
    
    @Autowired
    private ContactRepositoryImpl contactRepositoryImpl;
    @Autowired
    private UserRepository userRepository;
    // @Autowired
    // private ContactRepository contactRepository;

    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
        
        try {
            List<Contact> contacts = contactRepositoryImpl.findContactsByNameContainingAndUser(query,userRepository.getUserByUserName(principal.getName()).getId());
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while searching for contacts.");
        }

    }
}
