package com.example.food_order_app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Cart implements Parcelable {
    String cartId;
    String userId;
    String foodId;
    int quantity;
    Long total_price;

    public Cart() {
    }

    public Cart(String userId, Long total_price, int quantity, String foodId) {
        this.userId = userId;
        this.total_price = total_price;
        this.quantity = quantity;
        this.foodId = foodId;
    }

    // Phương thức tạo từ Parcel
    protected Cart(Parcel in) {
        cartId = in.readString();
        userId = in.readString();
        foodId = in.readString();
        quantity = in.readInt();
        // Lưu ý: Đối với Long, bạn cần sử dụng readLong()
        total_price = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cartId);
        dest.writeString(userId);
        dest.writeString(foodId);
        dest.writeInt(quantity);
        // Lưu ý: Đối với Long, bạn cần sử dụng writeLong()
        dest.writeLong(total_price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Tạo Creator cho Parcelable
    public static final Creator<Cart> CREATOR = new Creator<Cart>() {
        @Override
        public Cart createFromParcel(Parcel in) {
            return new Cart(in);
        }

        @Override
        public Cart[] newArray(int size) {
            return new Cart[size];
        }
    };

    // Getter và setter cho các thuộc tính
    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getTotal_price() {
        return total_price;
    }

    public void setTotal_price(Long total_price) {
        this.total_price = total_price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
