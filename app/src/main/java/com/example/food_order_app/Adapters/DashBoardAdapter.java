package com.example.food_order_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;

import java.util.List;

public class DashBoardAdapter extends RecyclerView.Adapter<DashBoardAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItemList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onEditClick(FoodItem foodItem);

        void onDeleteClick(FoodItem foodItem);
    }

    public DashBoardAdapter(List<FoodItem> foodItemList, OnItemClickListener onItemClickListener) {
        this.foodItemList = foodItemList;
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<FoodItem> list) {
        this.foodItemList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DashBoardAdapter.FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row_food_item, parent, false);
        return new FoodItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DashBoardAdapter.FoodItemViewHolder holder, int position) {
        FoodItem newFoodItem = foodItemList.get(position);
        if (newFoodItem == null) {
            return;
        }
        Glide.with(holder.itemView.getContext()).load(newFoodItem.getImage()).error(R.drawable.ic_image_placeholder).into(holder.foodImage);
        holder.foodName.setText(newFoodItem.getName());
        holder.foodDescription.setText(newFoodItem.getDescription());
        holder.foodPrice.setText(String.format("%,d VNĐ", newFoodItem.getPrice()));
        holder.foodRating.setText(String.format("★ %.1f", newFoodItem.getRating()));
        //Xu li edit
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onEditClick(newFoodItem);
            }
        });
        //xu li delete
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onDeleteClick(newFoodItem);
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
        private ImageButton btnEdit, btnDelete;

        public FoodItemViewHolder(View v) {
            super(v);
            foodName = v.findViewById(R.id.foodName);
            foodDescription = v.findViewById(R.id.foodDescription);
            foodPrice = v.findViewById(R.id.foodPrice);
            foodRating = v.findViewById(R.id.foodRating);
            foodImage = v.findViewById(R.id.foodImage);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelet);
        }
    }
}
