package com.example.food_order_app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.Adapters.FavouriteItemAdapter;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavouriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavouriteItemAdapter adapter;
    private List<FoodItem> favouriteList;
    private DatabaseReference dbFavourites;
    private Map<FoodItem, String> foodItemKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        recyclerView = findViewById(R.id.rcv_favourites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        favouriteList = new ArrayList<>();
        foodItemKeys = new HashMap<>();
        adapter = new FavouriteItemAdapter(favouriteList);
        recyclerView.setAdapter(adapter);

        loadFavourites();

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadFavourites() {
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);
        if (userId == null) {
            Toast.makeText(this, "Please login to view favourites", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbFavourites = FirebaseDatabase.getInstance().getReference("Favourites").child(userId);
        dbFavourites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favouriteList.clear();
                foodItemKeys.clear();
                for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                    FoodItem foodItem = favSnapshot.getValue(FoodItem.class);
                    if (foodItem != null) {
                        favouriteList.add(foodItem);
                        foodItemKeys.put(foodItem, favSnapshot.getKey());
                    }
                }
                adapter.notifyDataSetChanged();

                LinearLayout layoutNoFavourites = findViewById(R.id.layout_no_favourites);
                if (favouriteList.isEmpty()) {
                    layoutNoFavourites.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    layoutNoFavourites.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavouriteActivity.this, "Failed to load favourites", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void removeFavourite(FoodItem foodItem) {
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);
        if (userId != null) {
            String key = foodItemKeys.get(foodItem);
            if (key != null) {
                dbFavourites.child(key).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FavouriteActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                            loadFavourites();
                        })
                        .addOnFailureListener(e -> Toast.makeText(FavouriteActivity.this, "Failed to remove from favourites", Toast.LENGTH_SHORT).show());
            }
        }
    }


}
