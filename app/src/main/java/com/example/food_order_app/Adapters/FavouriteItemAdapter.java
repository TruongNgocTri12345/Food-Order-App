package com.example.food_order_app.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Activities.FavouriteActivity;
import com.example.food_order_app.Activities.FoodDetailActivity;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;

import java.util.List;

public class FavouriteItemAdapter extends RecyclerView.Adapter<FavouriteItemAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItems;


    public FavouriteItemAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_food_item, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);
        holder.foodName.setText(foodItem.getName());
        holder.foodPrice.setText(String.format("%,d VNĐ", foodItem.getPrice()));
        holder.foodRating.setText(String.format("★ %.1f", foodItem.getRating()));

        Glide.with(holder.itemView.getContext()).load(foodItem.getImage()).into(holder.foodImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), FoodDetailActivity.class);
                String foodId = foodItem.getFoodId();
                intent.putExtra("foodId", foodId);
                intent.putExtra("foodPrice", foodItem.getPrice());
                intent.putExtra("foodRating", foodItem.getRating());
                holder.itemView.getContext().startActivity(intent);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (holder.itemView.getContext() instanceof FavouriteActivity) {
                ((FavouriteActivity) holder.itemView.getContext()).removeFavourite(foodItem);
            }
        });
    }



    @Override
    public int getItemCount() {
        return foodItems.size();
    }



    public static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodPrice, foodRating;
        ImageView foodImage;
        ImageButton btnRemove;
        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.txt_foodName);
            foodPrice = itemView.findViewById(R.id.txt_foodPrice);
            foodImage = itemView.findViewById(R.id.img_foodImage);
            foodRating = itemView.findViewById(R.id.txt_foodRating);
            btnRemove = itemView.findViewById(R.id.btn_remove);

        }
    }
}
