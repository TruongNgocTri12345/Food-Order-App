package com.example.food_order_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editUserPhone;
    private EditText editUserAddress;
    private DatabaseReference dbUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        editUserName = findViewById(R.id.editUserName);
        editUserPhone = findViewById(R.id.editUserPhone);
        editUserAddress = findViewById(R.id.editUserAddress);
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        fetchUserData();

        saveButton.setOnClickListener(v -> {
            String updatedName = editUserName.getText().toString();
            String updatedPhone = editUserPhone.getText().toString();
            String updatedAddress = editUserAddress.getText().toString();
            String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("current_user_id", null);
            if (userId != null) {
                User updatedUser = new User(updatedName, updatedPhone, updatedAddress);
                updatedUser.setUserId(userId);
                dbUsers.child(userId).child("userName").setValue(updatedName);
                dbUsers.child(userId).child("userPhone").setValue(updatedPhone);
                dbUsers.child(userId).child("userAddress").setValue(updatedAddress);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedName", updatedName);
                resultIntent.putExtra("updatedPhone", updatedPhone);
                resultIntent.putExtra("updatedAddress", updatedAddress);
                setResult(RESULT_OK, resultIntent);
                showToast("Profile updated successfully");
                finish();
            } else {
                showToast("Error happened");
            }
        });


        cancelButton.setOnClickListener(v -> finish());
    }

    private void fetchUserData() {
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_user_id", null);

        if (userId != null) {
            dbUsers.child(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User currentUser = task.getResult().getValue(User.class);
                    if (currentUser != null) {
                        editUserName.setText(currentUser.getUserName());
                        editUserPhone.setText(currentUser.getUserPhone());
                        editUserAddress.setText(currentUser.getUserAddress());
                    } else {
                        showToast("User not found");
                    }
                } else {
                    showToast("Failed to fetch user data");
                }
            });
        } else {
            showToast("No user found");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
