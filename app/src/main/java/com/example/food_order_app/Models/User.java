package com.example.food_order_app.Models;

import java.io.Serializable;

public class User implements Serializable  {

    private String userId;

    private String userName;

    private String userEmail;

    private String userPhone;
    private String userAddress;

    private String userPassword;

    private String avatarUrl;

    private boolean admin;

    private String userOtp;
    private long otpCreationTime;

    public User() {
    }

    public User(boolean admin, String userAddress, String userEmail, String userName, String userPassword, String userPhone, String userOtp) {
        this.admin = admin;
        this.userAddress = userAddress;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userPhone = userPhone;
        avatarUrl = "";
        this.userOtp = userOtp;
    }


    public User(String userEmail, String userPassword) {
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    public User(String userName, String userPhone, String userAddress) {
        this.userName = userName;
        this.userPhone = userPhone;
        this.userAddress = userAddress;
    }
    // Constructor với 4 tham số
    public User(String userId, String userName, String userEmail, String avatarUrl) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.avatarUrl = avatarUrl;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserOtp() {
        return userOtp;
    }

    public void setUserOtp(String userOtp) {
        this.userOtp = userOtp;
    }

    public long getOtpCreationTime() {
        return otpCreationTime;
    }

    public void setOtpCreationTime(long otpCreationTime) {
        this.otpCreationTime = otpCreationTime;
    }
}
