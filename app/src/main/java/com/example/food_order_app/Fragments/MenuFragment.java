package com.example.food_order_app.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_order_app.Adapters.MenuAdapter;
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

public class MenuFragment extends Fragment {

    private RecyclerView rcv;
    private MenuAdapter adapter;
    private List<FoodItem> foodItemList;
    private DatabaseReference dbFoodItems;
    private Button btn_dishes, btn_pizza, btn_burger, btn_drinks, btn_dessert;
    private TextView txt_title;
    private ProgressBar progressBar;
    private EditText txt_search;
    private ImageView btn_sort;
    private boolean isAscending = true;
    private String currentCategory = null;
    private String currentSearchQuery = "";
    private TextView txt_empty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems");
        foodItemList = new ArrayList<>(); // Khởi tạo danh sách món ăn


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        initializeViews(v);
        setupRecyclerView();
        loadFoodItems();
        if (getArguments() != null) {
            String selectedCategory = getArguments().getString("selected_category");
            if (selectedCategory != null) {
                selectCategory(selectedCategory);
            }
        } else {
            resetButtonColors();
        }

        return v;
    }

    private void loadFoodItems() {
        progressBar.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);

        dbFoodItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodItemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FoodItem foodItem = dataSnapshot.getValue(FoodItem.class);
                    if (foodItem != null) {
                        foodItem.setFoodId(dataSnapshot.getKey());
                        dbFoodItems.child(dataSnapshot.getKey()).child("foodId").setValue(foodItem.getFoodId());
                        foodItemList.add(foodItem);
                    }
                }
                adapter.setData(foodItemList);
                progressBar.setVisibility(View.GONE);
                rcv.setVisibility(View.VISIBLE);

                if (getArguments() != null) {
                    String selectedCategory = getArguments().getString("selected_category");
                    if (selectedCategory != null) {
                        filterFoodItemsByCategory(selectedCategory);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load food items", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void setupRecyclerView() {
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                .getString("current_user_id", null);
        adapter = new MenuAdapter(foodItemList, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onAddClick(FoodItem foodItem) {
                String foodItemId = foodItem.getFoodId();
                DatabaseReference dbCart = FirebaseDatabase.getInstance().getReference("Cart").child(userId);

                dbCart.orderByChild("foodId").equalTo(foodItemId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                Cart existingCart = dataSnapshot.getValue(Cart.class);
                                if(existingCart != null){
                                    int newQuantity = existingCart.getQuantity() + 1;
                                    dataSnapshot.getRef().child("quantity").setValue(newQuantity);
                                    dataSnapshot.getRef().child("total_price").setValue(foodItem.getPrice() * newQuantity);
                                    Toast.makeText(getContext(), "Increased quantity in cart", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        } else {
                            Cart newCart = new Cart(userId, foodItem.getPrice(), 1, foodItemId);
                            dbCart.push().setValue(newCart);
                            Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        rcv.setAdapter(adapter);
    }

    private void initializeViews(View v) {
        rcv = v.findViewById(R.id.rcv_menu);
        btn_dishes = v.findViewById(R.id.btn_dishes);
        btn_pizza = v.findViewById(R.id.btn_pizza);
        btn_burger = v.findViewById(R.id.btn_burger);
        btn_drinks = v.findViewById(R.id.btn_drinks);
        btn_dessert = v.findViewById(R.id.btn_dessert);
        txt_title = v.findViewById(R.id.txt_title);
        progressBar = v.findViewById(R.id.progress_bar);
        txt_search = v.findViewById(R.id.txt_search);
        btn_sort = v.findViewById(R.id.btn_sort);
        txt_empty = v.findViewById(R.id.txt_empty);
        btn_sort.setOnClickListener(view -> {
            if (isAscending) {
                isAscending = false;
            } else {
                isAscending = true;
            }
            applyFilters();
        });

        txt_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentSearchQuery = charSequence.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        btn_dishes.setOnClickListener(v1 -> {
            currentCategory = "Dishes";
            selectCategory(currentCategory);
            applyFilters();
        });
        btn_pizza.setOnClickListener(v1 -> {
            currentCategory = "Pizza";
            selectCategory(currentCategory);
            applyFilters();
        });
        btn_burger.setOnClickListener(v1 -> {
            currentCategory = "Burger";
            selectCategory(currentCategory);
            applyFilters();
        });
        btn_drinks.setOnClickListener(v1 -> {
            currentCategory = "Drinks";
            selectCategory(currentCategory);
            applyFilters();
        });
        btn_dessert.setOnClickListener(v1 -> {
            currentCategory = "Dessert";
            selectCategory(currentCategory);
            applyFilters();
        });
    }

    private void applyFilters() {
        List<FoodItem> filteredList = new ArrayList<>(foodItemList);


        if (currentCategory != null) {
            filteredList.removeIf(foodItem -> !foodItem.getCategory().equalsIgnoreCase(currentCategory));
        }

        if (!currentSearchQuery.isEmpty()) {
            filteredList.removeIf(foodItem -> !foodItem.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()));
        }

        if (isAscending) {
            filteredList.sort((item1, item2) -> Double.compare(item1.getPrice(), item2.getPrice()));
        } else {
            filteredList.sort((item1, item2) -> Double.compare(item2.getPrice(), item1.getPrice()));
        }

        adapter.setData(filteredList);

        if (filteredList.isEmpty()) {
            rcv.setVisibility(View.GONE);
            txt_empty.setVisibility(View.VISIBLE);
        } else {
            rcv.setVisibility(View.VISIBLE);
            txt_empty.setVisibility(View.GONE);
        }
    }

    private void selectCategory(String category) {
        resetButtonColors();
        setSelectedButtonColor(category);
        filterFoodItemsByCategory(category);
    }

    private void setSelectedButtonColor(String category) {
        Button selectedButton;
        switch (category) {
            case "Dishes":
                selectedButton = btn_dishes;
                break;
            case "Pizza":
                selectedButton = btn_pizza;
                break;
            case "Burger":
                selectedButton = btn_burger;
                break;
            case "Drinks":
                selectedButton = btn_drinks;
                break;
            case "Dessert":
                selectedButton = btn_dessert;
                break;
            default:
                return;
        }
        selectedButton.setBackgroundResource(R.drawable.button_state);
        selectedButton.setTextColor(getResources().getColor(R.color.white));
        txt_title.setText(category);
    }

    private void resetButtonColors() {
        int defaultColor = getResources().getColor(R.color.white);
        btn_dishes.setBackgroundColor(defaultColor);
        btn_dishes.setTextColor(getContext().getColor(R.color.black));
        btn_dishes.setBackgroundResource(R.drawable.button_category_selector);

        btn_pizza.setBackgroundColor(defaultColor);
        btn_pizza.setTextColor(getContext().getColor(R.color.black));
        btn_pizza.setBackgroundResource(R.drawable.button_category_selector);

        btn_burger.setBackgroundColor(defaultColor);
        btn_burger.setTextColor(getContext().getColor(R.color.black));
        btn_burger.setBackgroundResource(R.drawable.button_category_selector);

        btn_drinks.setBackgroundColor(defaultColor);
        btn_drinks.setTextColor(getContext().getColor(R.color.black));
        btn_drinks.setBackgroundResource(R.drawable.button_category_selector);

        btn_dessert.setBackgroundColor(defaultColor);
        btn_dessert.setTextColor(getContext().getColor(R.color.black));
        btn_dessert.setBackgroundResource(R.drawable.button_category_selector);
    }


    private void filterFoodItemsByCategory(String category) {
        List<FoodItem> filteredList = new ArrayList<>();
        txt_title.setText(category);
        for (FoodItem foodItem : foodItemList) {
            if (foodItem.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(foodItem);
            }
        }
        adapter.setData(filteredList);
    }
}
