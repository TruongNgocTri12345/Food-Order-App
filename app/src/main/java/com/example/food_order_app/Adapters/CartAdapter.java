package com.example.food_order_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Fragments.CartFragment;
import com.example.food_order_app.Models.Cart;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<Cart> cartItems;
    private OnItemClickListener onItemClickListener;
    private DatabaseReference dbFoodItems;
    FoodItem foodItem;

    public interface OnItemClickListener {
        void onEncreaseClick(Cart cart);

        void onDecreseClick(Cart cart);

        void onDeleteClick(Cart cart);
    }

    public CartAdapter(List<Cart> cartItems, OnItemClickListener onItemClickListener) {
        this.cartItems = cartItems;
        this.onItemClickListener = onItemClickListener;
        foodItem = new FoodItem();
    }


    public void setData(List<Cart> list) {
        this.cartItems = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_food_item, parent, false);
        return new CartViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.CartViewHolder holder, int position) {
        Cart cartItem = cartItems.get(position);
        if (cartItem == null) {
            return;
        }

        dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems").child(cartItem.getFoodId());
        dbFoodItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    foodItem = snapshot.getValue(FoodItem.class);
                    Glide.with(holder.itemView.getContext()).load(foodItem.getImage()).error(R.drawable.ic_image_placeholder).into(holder.foodImage);
                    holder.foodName.setText(foodItem.getName());
                    holder.foodCategory.setText(foodItem.getCategory());
                    holder.foodPrice.setText(String.format("%,d VNĐ", cartItem.getTotal_price()));
                    holder.foodRating.setText(String.format("★ %.1f", foodItem.getRating()));
                    holder.foodQuantity.setText(String.valueOf(cartItem.getQuantity()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });


        holder.btnEncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onEncreaseClick(cartItem);
            }
        });

        holder.btnDecrese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onDecreseClick(cartItem);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onDeleteClick(cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cartItems != null) {
            return cartItems.size();
        } else {
            return 0;
        }
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView foodImage;
        private TextView foodName, foodCategory, foodPrice, foodRating, foodQuantity;
        private ImageButton btnEncrease, btnDecrese, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImage = itemView.findViewById(R.id.cart_foodImage);
            foodName = itemView.findViewById(R.id.cart_foodName);
            foodCategory = itemView.findViewById(R.id.cart_foodCategory);
            foodPrice = itemView.findViewById(R.id.cart_foodPrice);
            foodRating = itemView.findViewById(R.id.cart_foodRating);
            foodQuantity = itemView.findViewById(R.id.cart_foodQuantity);
            btnEncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrese = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete_cart_item);
        }
    }
}
