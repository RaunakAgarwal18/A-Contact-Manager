package com.springBoot.SmartContactManager.Dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.springBoot.SmartContactManager.Entity.Contact;
import com.springBoot.SmartContactManager.Entity.User;

public class ContactRepositoryImpl{
    
    @Autowired
    private UserRepository userRepository;
    public List<Contact> findContactsByNameContainingAndUser(String name,int id){
        Optional<User> userOptional = userRepository.findById(id);
        User user = userOptional.get();
        List<Contact> contacts = user.getContacts();
        List<Contact> list = new ArrayList<>();
        for(int i = 0 ; i<contacts.size(); i++){
            if(contacts.get(i).getName().toLowerCase().contains(name.toLowerCase())||contacts.get(i).getSecondName().toLowerCase().contains(name.toLowerCase())){
                list.add(contacts.get(i));
            }
        }
        return list;
    }
}
