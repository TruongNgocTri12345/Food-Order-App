package com.example.food_order_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Models.Cart;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Cart> orderItems;
    private DatabaseReference dbFoodItems;
    private FoodItem foodItem;

    public OrderAdapter(List<Cart> orderItems) {
        this.orderItems = orderItems;
        foodItem = new FoodItem();
    }

    public void setData(List<Cart> list) {
        this.orderItems = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Cart orderItem = orderItems.get(position);
        if (orderItem == null) {
            return;
        }

        dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems").child(orderItem.getFoodId());
        dbFoodItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    foodItem = snapshot.getValue(FoodItem.class);
                    Glide.with(holder.itemView.getContext()).load(foodItem.getImage()).error(R.drawable.ic_image_placeholder).into(holder.foodImage);
                    holder.foodName.setText(foodItem.getName());
                    holder.foodCategory.setText(foodItem.getCategory());
                    holder.foodPrice.setText(String.format("%,d VNĐ", foodItem.getPrice()));
                    holder.foodQuantity.setText(String.valueOf(orderItem.getQuantity()));
                    holder.itemTotalPrice.setText(String.format("%,d VNĐ", orderItem.getTotal_price()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (orderItems != null) {
            return orderItems.size();
        } else {
            return 0;
        }
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        private ImageView foodImage;
        private TextView foodName, foodCategory, foodPrice, foodRating, foodQuantity, itemTotalPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.order_foodImage);
            foodName = itemView.findViewById(R.id.order_foodName);
            foodCategory = itemView.findViewById(R.id.order_foodCategory);
            foodPrice = itemView.findViewById(R.id.order_foodPrice);
            foodQuantity = itemView.findViewById(R.id.order_foodQuantity);
            itemTotalPrice = itemView.findViewById(R.id.order_itemPrice);

        }
    }
}
