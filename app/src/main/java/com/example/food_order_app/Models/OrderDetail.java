package com.example.food_order_app.Models;


public class OrderDetail {

    private int orderDetailId;

    private int orderId;

    private int foodId;

    private int quantity;

    private double price;

    public OrderDetail() {
    }

    public OrderDetail(int quantity, double price, int orderId, int foodId) {
        this.quantity = quantity;
        this.price = price;
        this.orderId = orderId;
        this.foodId = foodId;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
