package com.vibe.pay.backend.member;

import java.time.LocalDateTime;

public class Member {

    private Long id;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private LocalDateTime createdAt;

    // Constructors
    public Member() {
    }

    public Member(String name, String shippingAddress, String phoneNumber) {
        this.name = name;
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now(); // Initialize creation date
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}