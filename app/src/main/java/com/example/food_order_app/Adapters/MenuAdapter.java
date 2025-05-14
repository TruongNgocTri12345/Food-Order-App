package com.example.food_order_app.Adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Activities.FoodDetailActivity;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;
import com.example.food_order_app.Utils.RoundedCornersTransformation;

import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItemList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onAddClick(FoodItem foodItem);
    }

    public MenuAdapter(List<FoodItem> foodItemList, OnItemClickListener onItemClickListener) {
        this.foodItemList = foodItemList;
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<FoodItem> foodItemList) {
        this.foodItemList = foodItemList;
        notifyDataSetChanged();
        Log.d("MenuAdapter", "Menu adapter is: " + foodItemList.size());

    }


    @NonNull
    @Override
    public MenuAdapter.FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_food_item, parent, false);
        return new FoodItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuAdapter.FoodItemViewHolder holder, int position) {
        FoodItem foodItem = foodItemList.get(position);
        if (foodItem == null) {
            return;
        }

//        Glide.with(holder.itemView.getContext())
//                .load(foodItem.getImage())
//                .error(R.drawable.ic_image_placeholder)
//                .transform(new RoundedCornersTransformation(16, 0)) // Use your custom transformation here
//                .into(holder.foodImage);
        Glide.with(holder.itemView.getContext()).load(foodItem.getImage()).error(R.drawable.ic_image_placeholder).into(holder.foodImage);
        holder.foodName.setText(foodItem.getName());
        holder.foodDescription.setText(foodItem.getDescription());
        holder.foodPrice.setText(String.format("%,d VNĐ", foodItem.getPrice()));
        holder.foodRating.setText(String.format("★ %.1f", foodItem.getRating()));
        if (foodItem.getQuantity() > 0) {
            holder.btnAdd.setAlpha(1.0f);
            //Xử lý sự kiện nút "Add"
            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onAddClick(foodItem);
                }
            });
        } else {
            holder.btnAdd.setAlpha(0.5f);
            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(holder.itemView.getContext(), "Out of stock", Toast.LENGTH_SHORT).show();
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), FoodDetailActivity.class);
                String foodId = foodItem.getFoodId();
                intent.putExtra("foodId", foodId);
                intent.putExtra("foodPrice", foodItem.getPrice());

                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (foodItemList != null) {
            return foodItemList.size();
        } else {
            return 0;
        }
    }

    public class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private TextView foodName, foodDescription, foodPrice, foodRating;
        private ImageView foodImage;
        private ImageButton btnAdd;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);

            foodName = itemView.findViewById(R.id.menu_foodName);
            foodDescription = itemView.findViewById(R.id.menu_foodDescription);
            foodPrice = itemView.findViewById(R.id.menu_foodPrice);
            foodRating = itemView.findViewById(R.id.menu_foodRating);
            foodImage = itemView.findViewById(R.id.menu_foodImage);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}
