package com.example.food_order_app.Models;

import java.util.List;

public class Order {

    private String orderId;

    private String userId;

    private String orderDate;

    private double orderTotal;

    private String orderStatus;

    private String orderAddress;

    private String orderPhone;

    private String orderNote;


    private List<Cart> orderedItems;
    public Order() {
    }

    public Order(String orderAddress, String orderDate, String orderNote, String orderPhone, String orderStatus, double orderTotal,  String userId) {
        this.orderAddress = orderAddress;
        this.orderDate = orderDate;
        this.orderNote = orderNote;
        this.orderPhone = orderPhone;
        this.orderStatus = orderStatus;
        this.orderTotal = orderTotal;
        this.userId = userId;
    }

    public List<Cart> getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(List<Cart> orderedItems) {
        this.orderedItems = orderedItems;
    }

    public String getOrderAddress() {
        return orderAddress;
    }

    public void setOrderAddress(String orderAddress) {
        this.orderAddress = orderAddress;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public String getOrderPhone() {
        return orderPhone;
    }

    public void setOrderPhone(String orderPhone) {
        this.orderPhone = orderPhone;
    }


    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
