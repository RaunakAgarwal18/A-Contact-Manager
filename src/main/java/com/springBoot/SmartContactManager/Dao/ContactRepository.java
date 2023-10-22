package com.springBoot.SmartContactManager.Dao;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springBoot.SmartContactManager.Entity.Contact;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
    @Query("from Contact as c where c.user.id=:userId ORDER BY c.name")
    public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);

    //Pageable has 2 informations - 1. Contacts per page
    //                              2. Current Page
    //Pageable returns the appropriate page in form of sublist
    
    public List<Contact> findContactsByNameContaining(String name);

}
