package com.springBoot.SmartContactManager.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.springBoot.SmartContactManager.Entity.User;

// Used to use the already implemented functions in JpaRepository
public interface UserRepository extends JpaRepository<User,Integer> {
    
    @Query("select u from User u where u.email = :email")
    public User getUserByUserName(@Param("email") String email);

    // @Query("Select c from Contact c where c.name like :name")
    
}
