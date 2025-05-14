package com.example.food_order_app.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notification {
    private String id;
    private String userId;
    private String message;
    private String date;
    private String type;
    private String orderId;
    private boolean isSeen;
    public Notification() {}

    public Notification(String id, String userId, String message, String date,  String type, String orderId, boolean isSeen) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.date = date;
        this.type = type;
        this.orderId = orderId;
        this.isSeen = false;
        ;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFormattedDate() {
        long timestamp = Long.parseLong(date);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH-mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
