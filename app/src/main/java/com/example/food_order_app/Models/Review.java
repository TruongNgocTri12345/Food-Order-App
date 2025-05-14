package com.example.food_order_app.Models;

public class Review {
    private String reviewId;

    private String userId;

    private String foodId;

    private Float rating;

    private String comment;

    public Review() {
    }

    public Review(String comment, Float rating, String reviewId, String userId) {
        this.comment = comment;
        this.rating = rating;
        this.reviewId = reviewId;
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
