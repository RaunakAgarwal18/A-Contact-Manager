package com.springBoot.SmartContactManager.Dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springBoot.SmartContactManager.Entity.MyOrder;

public interface MyOrderRepository extends JpaRepository<MyOrder,Long> {
    
    //Searching the order via the order id
    public MyOrder findByOrderId(String id);

}
