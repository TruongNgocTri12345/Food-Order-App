package com.example.food_order_app.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.food_order_app.Adapters.OrderAdapter;
import com.example.food_order_app.Models.Cart;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrderDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrderDetails;
    private OrderAdapter orderAdapter;
    private TextView orderIdTextView, orderStatusTextView, orderTotalTextView, orderAddressTextView, orderPhoneTextview, orderNoteTextView, orderDateTextview;
    private TextView orderSubtotalTextView, orderDeliveryTextView, orderDiscountTextView;
    private TextView orderUserNameTextView, orderEmailTextView;
    private DatabaseReference dbCart, dbUser;
    private Button updateStatusButton, reorderButton;

    private ImageView icBack;
    private boolean isAdmin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        isAdmin = getSharedPreferences("user_prefs", MODE_PRIVATE).getBoolean("admin", false);

        String orderId = getIntent().getStringExtra("orderId");
        String orderStatus = getIntent().getStringExtra("orderStatus");
        double orderTotal = getIntent().getDoubleExtra("orderTotal", 0.0);
        String orderPhone = getIntent().getStringExtra("orderPhone");
        String orderAddress = getIntent().getStringExtra("orderAddress");
        String orderNote = getIntent().getStringExtra("orderNote");
        String orderDate = getIntent().getStringExtra("orderDate");
        List<Cart> orderedItems = (List<Cart>) getIntent().getSerializableExtra("orderedItems");
        String userId = getIntent().getStringExtra("userId");
        fetchUserInfo(userId);
        orderUserNameTextView = findViewById(R.id.orderUserName);
        orderEmailTextView = findViewById(R.id.orderEmail);
        icBack = findViewById(R.id.ic_back);

        reorderButton = findViewById(R.id.reorderButton);
        updateStatusButton = findViewById(R.id.updateStatusButton);
        if (isAdmin) {
            updateStatusButton.setVisibility(View.VISIBLE);
            reorderButton.setVisibility(View.GONE);
        } else {
            updateStatusButton.setVisibility(View.GONE);
            reorderButton.setVisibility(View.VISIBLE);
        }

        icBack.setOnClickListener(v -> {
            finish();
        });

        orderIdTextView = findViewById(R.id.orderId);
        orderStatusTextView = findViewById(R.id.orderStatus);
        orderTotalTextView = findViewById(R.id.orderTotal);
        orderAddressTextView = findViewById(R.id.orderAddress);
        orderPhoneTextview = findViewById(R.id.orderPhone);
        orderNoteTextView = findViewById(R.id.orderNote);
        orderDateTextview = findViewById(R.id.orderDate);
        orderSubtotalTextView = findViewById(R.id.orderSubtotal);
        orderDeliveryTextView = findViewById(R.id.orderDelivery);
        orderDiscountTextView = findViewById(R.id.orderDiscount);

        orderIdTextView.setText("Order ID: " + orderId.replace("-", ""));
        orderStatusTextView.setText("Status: " + orderStatus);
        orderAddressTextView.setText("Address: " + orderAddress);
        orderPhoneTextview.setText("Phone: " + orderPhone);
        orderDateTextview.setText("Date: " + formatDate(orderDate));
        if (orderNote == null || orderNote.trim().isEmpty()) {
            orderNoteTextView.setText("Note: No Note");
        } else {
            orderNoteTextView.setText("Note: " + orderNote);
        }

        Button reorderButton = findViewById(R.id.reorderButton);
        reorderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reorderItems(orderedItems);
            }
        });

        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateStatusDialog(orderId); // Pass the orderId to the dialog
            }
        });





        double subtotal = 0;
        double discount = 0;
        double deliveryFee = 10000;
        int totalQuantity = 0;
        Set<String> uniqueItems = new HashSet<>();

        for (Cart cartItem : orderedItems) {
            subtotal += cartItem.getTotal_price();
            totalQuantity += cartItem.getQuantity();
            uniqueItems.add(cartItem.getFoodId());
        }

        if (totalQuantity >= 3 || uniqueItems.size() >= 3) {
            discount = subtotal * 0.1;
        }

        double totalAmount = subtotal - discount + deliveryFee;

        orderTotalTextView.setText("Total: " + formatCurrency(totalAmount));
        orderSubtotalTextView.setText("Subtotal: " + formatCurrency(subtotal));
        orderDeliveryTextView.setText("Delivery Fee: " + formatCurrency(deliveryFee));
        orderDiscountTextView.setText("Discount: -" + formatCurrency(discount));

        recyclerViewOrderDetails = findViewById(R.id.recyclerView_order_details);
        recyclerViewOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(orderedItems);
        recyclerViewOrderDetails.setAdapter(orderAdapter);
    }

    private void fetchUserInfo(String userId) {
        dbUser = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        dbUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        orderUserNameTextView.setText("Username: " + user.getUserName());
                        orderEmailTextView.setText("Email: " + user.getUserEmail());
                    }
                } else {
                    showToast("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load user info: " + error.getMessage());
            }
        });
    }

    private String formatDate(String orderDate) {
        long timestamp = Long.parseLong(orderDate);
        Date date = new Date(timestamp);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return outputFormat.format(date);
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(amount);
        return formattedAmount + " VNĐ";
    }

    public void onUpdateOrderSuccess() {
        Intent intent = new Intent();
        intent.putExtra("orderUpdated", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void updateOrderStatus(String orderId, String status) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);
        orderRef.child("orderStatus").setValue(status)
                .addOnSuccessListener(aVoid -> {
                    orderStatusTextView.setText("Status: " + status);
                    showToast("Order status updated to: " + status);
                })
                .addOnFailureListener(e -> showToast("Failed to update order status: " + e.getMessage()));
    }


    private void showUpdateStatusDialog(String orderId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_update_status);

        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        Button buttonWaiting = dialog.findViewById(R.id.buttonWaiting);
        Button buttonSuccess = dialog.findViewById(R.id.buttonSuccess);

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(orderId, "Cancelled");
                dialog.dismiss();
            }
        });



        buttonWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(orderId, "Waiting");
                dialog.dismiss();
            }
        });

        buttonSuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(orderId, "Success");
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addToCart(Cart cartItem) {
        try {
            String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);
            if (userId == null) {
                showToast("Please login to add to cart");
                return;
            }
            dbCart = FirebaseDatabase.getInstance().getReference().child("Cart").child(userId);
            dbCart.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isExists = false;
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Cart existingCart = dataSnapshot.getValue(Cart.class);
                            existingCart.setCartId(dataSnapshot.getKey());
                            if (existingCart != null && existingCart.getFoodId().equals(cartItem.getFoodId())) {
                                int newQuantity = existingCart.getQuantity() + cartItem.getQuantity();
                                dataSnapshot.getRef().child("quantity").setValue(newQuantity);
                                dataSnapshot.getRef().child("total_price").setValue(cartItem.getTotal_price() * newQuantity);
                                isExists = true;
                                showToast("Increased quantity in cart");
                                break;
                            }
                        }
                    }

                    if (!isExists) {
                        Cart newCart = new Cart(userId, cartItem.getTotal_price(), cartItem.getQuantity(), cartItem.getFoodId());
                        dbCart.push().setValue(newCart).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Added to cart");
                            } else {
                                showToast("Failed to add to cart");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast(error.getMessage());
                }
            });
        } catch (Exception e) {
            showToast(e.getMessage());
        }
    }



    private void reorderItems(List<Cart> orderedItems) {
        for (Cart item : orderedItems) {
            addToCart(item);
        }

        Intent intent = new Intent(OrderDetailActivity.this, NavigationActivity.class);
        intent.putExtra("openCartFragment", true);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
