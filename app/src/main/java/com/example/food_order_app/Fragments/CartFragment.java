package com.example.food_order_app.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food_order_app.Adapters.CartAdapter;
import com.example.food_order_app.Models.Cart;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.Activities.OrderActivity;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CartFragment extends Fragment {

    private RecyclerView rcv;
    private CartAdapter adapter;
    private List<Cart> cartItemsList;
    private List<FoodItem> foodItemList;
    private TextView tvSubtotal, tvDiscount, tvDeliveryFee, tvTotalAmount, tvEmptyCart;
    private LinearLayout paymentSummaryContainer, btnContainer;
    private Button btnAddItems, btnCheckout, btnDeleteAll;
    private DatabaseReference dbCartItems, dbFoodItems;
    private String userId;
    private double subtotal, discount, totalAmount;
    private double deliveryFee = 10000;
    private ProgressBar progressBar;
    private User currentUser;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                .getString("current_user_id", null);
        dbCartItems = FirebaseDatabase.getInstance().getReference("Cart").child(userId);
        dbFoodItems = FirebaseDatabase.getInstance().getReference("FoodItems");
        cartItemsList = new ArrayList<>();
        foodItemList = new ArrayList<>();
        loadCurrentUser();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cart, container, false);
        initializeViews(v);
        setupRecyclerView();
        setupButtonListeners();
        loadCartItems();
        return v;
    }

    private void loadCurrentUser() {
        String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                .getString("current_user_id", null);
        DatabaseReference dbUsers = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class); // Assuming User is your model class.
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load user data");
            }
        });
    }



    private void loadCartItems() {
        // Hiển thị ProgressBar và ẩn RecyclerView
        progressBar.setVisibility(View.VISIBLE);
        rcv.setVisibility(View.GONE);
        paymentSummaryContainer.setVisibility(View.GONE); // Ẩn summary ban đầu
        btnContainer.setVisibility(View.GONE);
        // Đọc dữ liệu từ Firebase
        dbCartItems.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemsList.clear();
                subtotal = 0;
                discount = 0;
                int totalQuantity = 0;
                Set<String> uniqueItems = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Cart cartItem = dataSnapshot.getValue(Cart.class);
                    if (cartItem != null) {
                        cartItem.setCartId(dataSnapshot.getKey());
                        cartItemsList.add(cartItem);
                        totalQuantity += cartItem.getQuantity();
                        uniqueItems.add(cartItem.getFoodId());
                        subtotal += cartItem.getTotal_price();
                    }
                }

                if (totalQuantity >= 3 || uniqueItems.size() >= 3) {
                    discount = subtotal * 0.1;
                }

                // Hiện/Ẩn thông báo giỏ hàng trống
                if (cartItemsList.isEmpty()) {
                    tvEmptyCart.setVisibility(View.VISIBLE);
                    rcv.setVisibility(View.GONE);
                    paymentSummaryContainer.setVisibility(View.GONE);
                    btnContainer.setVisibility(View.GONE);
                } else {
                    tvEmptyCart.setVisibility(View.GONE);
                    rcv.setVisibility(View.VISIBLE);
                    paymentSummaryContainer.setVisibility(View.VISIBLE);
                    btnContainer.setVisibility(View.VISIBLE);
                }

                // Tính tổng tiền và cập nhật
                totalAmount = subtotal - discount + deliveryFee;

                // Cập nhật các giá trị vào TextView với định dạng số
                NumberFormat numberFormat = NumberFormat.getInstance(new Locale("vi", "VN")); // Định dạng theo locale Việt Nam
                tvSubtotal.setText(numberFormat.format((long) subtotal) + " VNĐ"); // Định dạng subtotal
                tvDiscount.setText("- " + numberFormat.format((long) discount) + " VNĐ"); // Định dạng discount
                tvDeliveryFee.setText(numberFormat.format((long) deliveryFee) + " VNĐ"); // Định dạng delivery fee
                tvTotalAmount.setText(numberFormat.format((long) totalAmount) + " VNĐ"); // Định dạng total amount

                adapter.setData(cartItemsList);

                // Ẩn ProgressBar và hiển thị RecyclerView
                progressBar.setVisibility(View.GONE);
                rcv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load cart items");
                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar nếu có lỗi
            }
        });
    }


    private void initializeViews(View v) {
        rcv = v.findViewById(R.id.rcv_cartItems);
        tvSubtotal = v.findViewById(R.id.tvSubtotal);
        tvDiscount = v.findViewById(R.id.tvDiscount);
        tvDeliveryFee = v.findViewById(R.id.tvDeliveryFee);
        tvTotalAmount = v.findViewById(R.id.tvTotalAmount);
        tvEmptyCart = v.findViewById(R.id.tvEmptyCart);
        btnAddItems = v.findViewById(R.id.btnAddItems);
        btnCheckout = v.findViewById(R.id.btnCheckout);
        progressBar = v.findViewById(R.id.progress_bar);
        paymentSummaryContainer = v.findViewById(R.id.paymentSummaryContainer);
        btnContainer = v.findViewById(R.id.buttonContainer);
        btnDeleteAll = v.findViewById(R.id.btn_delete_all);
    }

    private void setupRecyclerView() {
        rcv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(cartItemsList, new CartAdapter.OnItemClickListener() {
            @Override
            public void onEncreaseClick(Cart cart) {
                increaseQuantity(cart);
            }

            @Override
            public void onDecreseClick(Cart cart) {
                decreseQuantity(cart);
            }

            @Override
            public void onDeleteClick(Cart cart) {
                deleteItem(cart);
            }
        });
        rcv.setAdapter(adapter);// Đặt adapter cho RecyclerView
    }

    private void deleteItem(Cart cart) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Delete Item");
        alertDialog.setMessage("Do you want to delete this item?");

        alertDialog.setPositiveButton("Yes", (dialog, which) -> {
            dbCartItems.child(cart.getCartId()).removeValue();
            loadCartItems();
        });
        alertDialog.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void decreseQuantity(Cart cart) {
        try {
            if (cart.getQuantity() > 1) {
                cart.setQuantity(cart.getQuantity() - 1);
                dbFoodItems.child(cart.getFoodId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long updatedTotalPrice = cart.getQuantity() * snapshot.getValue(FoodItem.class).getPrice();
                            cart.setTotal_price(updatedTotalPrice);
                            dbCartItems.child(cart.getCartId()).setValue(cart).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    loadCartItems(); // Cập nhật lại danh sách giỏ hàng
                                } else {
                                    showToast("Failed to increase quantity");
                                }

                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showToast("Failed to update total price");
                    }
                });
            } else {
                showToast("Quantity must be at least 1");
            }
        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }

    private void increaseQuantity(Cart cart) {
        try {
            cart.setQuantity(cart.getQuantity() + 1);
            dbFoodItems.child(cart.getFoodId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Long updatedTotalPrice = cart.getQuantity() * snapshot.getValue(FoodItem.class).getPrice();
                        cart.setTotal_price(updatedTotalPrice);
                        dbCartItems.child(cart.getCartId()).setValue(cart).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                loadCartItems(); // Cập nhật lại danh sách giỏ hàng
                            } else {
                                showToast("Failed to increase quantity");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Failed to update total price");
                }
            });

        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }

    private void setupButtonListeners() {
        btnAddItems.setOnClickListener(v -> {
            openFragment(new MenuFragment());
        });
        btnDeleteAll.setOnClickListener(v -> {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this.getContext());
            alertDialog.setTitle("Delete Item");
            alertDialog.setMessage("Do you want to delete all items?");

            alertDialog.setPositiveButton("Yes", (dialog, which) -> {
                dbCartItems.removeValue();
                loadCartItems();
            });
            alertDialog.setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            });
            alertDialog.show();
        });

        btnCheckout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderActivity.class);
            // Pass the list of cart items

            intent.putExtra("cartItems", (Serializable) cartItemsList);
            intent.putExtra("totalAmount", totalAmount);
            startActivity(intent);
            if (currentUser != null) {
                intent.putExtra("currentUser", currentUser);
            }

            startActivity(intent);
        });

    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void openFragment(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_bottom_navigation, fragment) // Thay R.id.fragment_container bằng ID của container trong layout
                .commit();
    }

}