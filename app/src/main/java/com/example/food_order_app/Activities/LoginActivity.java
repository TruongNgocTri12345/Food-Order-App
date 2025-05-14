package com.example.food_order_app.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.food_order_app.Models.Notification;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private ImageButton btnBack, btnTogglePassword;
    private EditText etEmail, etPassword;
    private boolean isPasswordVisible = false;
    private DatabaseReference dbUsers, dbNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        dbNotifications = FirebaseDatabase.getInstance().getReference("Notifications");

        btnLogin = findViewById(R.id.btn_login);
        btnBack = findViewById(R.id.btn_back);
        etEmail = findViewById(R.id.txt_email);
        etPassword = findViewById(R.id.txt_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);

        btnBack.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, MainActivity.class)));
        btnTogglePassword.setOnClickListener(view -> togglePasswordVisibility());
        btnLogin.setOnClickListener(view -> validateLogin());

        TextView txtForgotPassword = findViewById(R.id.txt_forgot_password);
        txtForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("AdminChannel",
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void validateLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            User user = new User(email, password);
            login(user);
        }
    }

    private void login(User user) {
        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLoggedIn = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String emailInDb = dataSnapshot.child("userEmail").getValue(String.class);
                    String passwordInDb = dataSnapshot.child("userPassword").getValue(String.class);
                    if (user.getUserEmail().equals(emailInDb) && user.getUserPassword().equals(passwordInDb)) {
                        String userId = dataSnapshot.getKey();
                        boolean isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);

                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("current_user_id", userId)
                                .putBoolean("admin", isAdmin)
                                .apply();

                        if (isAdmin) {
                            fetchAndNotifyAdmin();
                        }
                        redirectToAppropriateActivity(isAdmin);
                        isLoggedIn = true;
                        break;
                    }
                }

                if (!isLoggedIn) {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndNotifyAdmin() {
        dbNotifications.orderByChild("type").equalTo("order").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null && !notification.isSeen()) {
                        sendAdminNotification(notification.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendAdminNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "AdminChannel")
                .setSmallIcon(R.drawable.food_icon)
                .setContentTitle("Admin Notification")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void redirectToAppropriateActivity(boolean isAdmin) {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(LoginActivity.this, AdminNavigationActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, NavigationActivity.class);
        }
        startActivity(intent);
        finish();
        Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        }
        etPassword.setSelection(etPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }
}
