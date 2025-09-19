package com.vibe.pay.backend.member;

import java.time.LocalDateTime;

public class Member {

    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
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
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}