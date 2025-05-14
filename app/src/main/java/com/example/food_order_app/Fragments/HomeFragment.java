package com.example.food_order_app.Fragments;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.food_order_app.Activities.NavigationActivity;
import com.example.food_order_app.Adapters.FoodHomeAdapter;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodHomeAdapter foodHomeAdapter;
    private List<FoodItem> foodItems;
    private ProgressBar progressBar;
    private DatabaseReference dbUsers;
    private Handler handler;
    private int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA}; // Add your desired colors here
    private int currentColorIndex = 0;
    private boolean isBannerOneVisible = true;
private TextView headerText;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        foodItems = new ArrayList<>();
        handler = new Handler();
        dbUsers = FirebaseDatabase.getInstance().getReference("Users");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_popular);
fetchUserData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        TextView seeAll = view.findViewById(R.id.see_all);
        progressBar = view.findViewById(R.id.progress_bar);

        headerText = view.findViewById(R.id.header_text);

        TextView wishingText = view.findViewById(R.id.wishing_text);
        wishingText.setText("Wishing you a delicious meal");

        startWaveAnimation(wishingText);
        startColorChangeAnimation(wishingText);

        // Set click listeners for menu items
        view.findViewById(R.id.dishes).setOnClickListener(v -> openMenuFragment("Dishes"));
        view.findViewById(R.id.pizza).setOnClickListener(v -> openMenuFragment("Pizza"));
        view.findViewById(R.id.burger).setOnClickListener(v -> openMenuFragment("Burger"));
        view.findViewById(R.id.drinks).setOnClickListener(v -> openMenuFragment("Drinks"));
        view.findViewById(R.id.dessert).setOnClickListener(v -> openMenuFragment("Dessert"));

        seeAll.setOnClickListener(v -> openMenuFragment(null));

        fetchFoodItems();
        return view;
    }


    private void showToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserData() {
        String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                .getString("current_user_id", null);

        if (userId != null) {
            dbUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isAdded() && dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            String helloText = "Hello, ";
                            String userName = currentUser.getUserName() + "!";

                            SpannableString spannableString = new SpannableString(helloText + userName);
                            spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 0, helloText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            spannableString.setSpan(new StyleSpan(Typeface.BOLD), helloText.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            headerText.setText(spannableString);
                        } else {
                            showToast("User not found");
                        }
                    } else {
                        showToast("User not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Failed to fetch user data");
                }
            });
        } else {
            showToast("No user found");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchFoodItems();
    }

    private void startWaveAnimation(TextView textView) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationY", -10f, 10f);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.start();
    }

    private void startColorChangeAnimation(TextView wishingText) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wishingText.setTextColor(colors[currentColorIndex]);
                currentColorIndex = (currentColorIndex + 1) % colors.length;
                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void openMenuFragment(String category) {
        MenuFragment menuFragment = new MenuFragment();
        if (category != null) {
            Bundle args = new Bundle();
            args.putString("selected_category", category);
            menuFragment.setArguments(args);
            Log.d("MenuFragment", "Selected Category: " + category);
        }
        Log.d("HomeFragment", "Attempting to open MenuFragment");
        ((NavigationActivity) getActivity()).openFragment(menuFragment);
    }

    private void fetchFoodItems() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        DatabaseReference dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems");
        dbFoodItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                foodItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FoodItem foodItem = snapshot.getValue(FoodItem.class);
                    foodItems.add(foodItem);
                }
                Collections.sort(foodItems, new Comparator<FoodItem>() {
                    @Override
                    public int compare(FoodItem o1, FoodItem o2) {
                        return Double.compare(o2.getRating(), o1.getRating());
                    }
                });

                if (foodItems.size() > 8) {
                    foodItems = foodItems.subList(0, 8);
                }

                foodHomeAdapter = new FoodHomeAdapter(foodItems);
                recyclerView.setAdapter(foodHomeAdapter);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HomeFragment", "Database error: " + databaseError.getMessage());
            }
        });
    }
}
