package com.example.food_order_app.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_order_app.Activities.AddNewFoodActivity;
import com.example.food_order_app.Adapters.DashBoardAdapter;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

// DashboardFragment.java
public class DashboardFragment extends Fragment {

    private RecyclerView rcv;
    private DashBoardAdapter adapter;
    private List<FoodItem> foodItemList;
    private Button btnAddNewFood;
    private Spinner spinnerCategory;
    private TextView tvItemCount;
    private DatabaseReference dbFoodItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems");
        foodItemList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        initializeViews(v);
        setupRecyclerView();
        setupButtonListeners();
        loadFoodItems();
        return v;
    }

    private void initializeViews(View v) {
        rcv = v.findViewById(R.id.recyclerView);
        btnAddNewFood = v.findViewById(R.id.btnAddNewFood);
        tvItemCount = v.findViewById(R.id.tvItemCount);
    }

    private void setupRecyclerView() {
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DashBoardAdapter(foodItemList, new DashBoardAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(FoodItem foodItem) {
                Toast.makeText(getContext(), "Edit", Toast.LENGTH_SHORT).show();
                updateFoodItem(foodItem);
            }

            @Override
            public void onDeleteClick(FoodItem foodItem) {
                showDeleteConfirmationDialog(foodItem);
            }
        });
        rcv.setAdapter(adapter);
    }

    private void updateItemCount() {
        tvItemCount.setText("Total Food Items: " + foodItemList.size()); // Update the TextView with the size of the list
    }

    private void setupButtonListeners() {
        btnAddNewFood.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddNewFoodActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void showDeleteConfirmationDialog(FoodItem foodItem) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Food Item")
                .setMessage("Are you sure you want to delete this food item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    deleteFoodItem(foodItem);
                    Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == AddNewFoodActivity.RESULT_OK) {
            loadFoodItems(); // Gọi load lại dữ liệu khi trở về từ AddNewFoodActivity
        }
    }

    public void loadFoodItems() {
        dbFoodItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodItemList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FoodItem foodItem = dataSnapshot.getValue(FoodItem.class);
                    foodItem.setFoodId(dataSnapshot.getKey());
                    foodItemList.add(foodItem);
                }
                adapter.setData(foodItemList);
                updateItemCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load food items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFoodItem(FoodItem foodItem) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_food_item, null);
        dialogBuilder.setView(dialogView);


        // Liên kết các trường giao diện với mã
        EditText etFoodName = dialogView.findViewById(R.id.etFoodName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        EditText etPrice = dialogView.findViewById(R.id.etPrice);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);

        Button btnUpdateFood = dialogView.findViewById(R.id.btnUpdateFood);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);


        // Tạo danh sách danh mục cho Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.food_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(adapter.getPosition(foodItem.getCategory()));

        // Hiển thị thông tin hiện tại của món ăn
        etFoodName.setText(foodItem.getName());
        etDescription.setText(foodItem.getDescription());
        etPrice.setText(String.valueOf(foodItem.getPrice()));
        etQuantity.setText(String.valueOf(foodItem.getQuantity()));

        AlertDialog dialog = dialogBuilder.create();
        dialog.setTitle("Updating " + foodItem.getName());
        dialog.show();


//        String category;
//        // Xử lý sự kiện chọn danh mục
//        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                category = parent.getItemAtPosition(position).toString();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                category = ""; // Hoặc thiết lập giá trị mặc định
//            }
//        });


        btnUpdateFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = etFoodName.getText().toString();
                String newDescription = etDescription.getText().toString();
                Long newPrice = Long.parseLong(etPrice.getText().toString());
                int newQuantity = Integer.parseInt(etQuantity.getText().toString());
                String newCategory = spinnerCategory.getSelectedItem().toString();
                // Cập nhật thông tin món ăn
                foodItem.setName(newName);
                foodItem.setDescription(newDescription);
                foodItem.setPrice(newPrice);
                foodItem.setQuantity(newQuantity);
                foodItem.setCategory(newCategory);

                // Cập nhật vào Firebase
                dbFoodItems.child(foodItem.getFoodId()).setValue(foodItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Tắt dialog sau khi cập nhật thành công
                        loadFoodItems(); // Tải lại danh sách sản phẩm
                    } else {
                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    public void deleteFoodItem(FoodItem foodItem) {
        dbFoodItems.child(foodItem.getFoodId()).removeValue();
        foodItemList.remove(foodItem);
        adapter.notifyDataSetChanged();
    }

}
