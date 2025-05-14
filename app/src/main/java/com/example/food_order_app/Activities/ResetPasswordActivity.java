package com.example.food_order_app.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.os.CountDownTimer;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText txtOtp, txtNewPassword, txtConfirmPassword;
    private Button btnUpdatePassword;
    private String receivedOtp;
    private DatabaseReference dbUsers;
    private TextView tvTimer;
    private long otpExpiryDuration = 5 * 60 * 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        txtOtp = findViewById(R.id.txt_otp);
        txtNewPassword = findViewById(R.id.txt_new_password);
        txtConfirmPassword = findViewById(R.id.txt_confirm_password);
        btnUpdatePassword = findViewById(R.id.btn_update_password);
        tvTimer = findViewById(R.id.tv_timer);
        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        receivedOtp = getIntent().getStringExtra("otp");
        SharedPreferences sharedPreferences = getSharedPreferences("otp_prefs", MODE_PRIVATE);
        long savedOtpCreationTime = sharedPreferences.getLong("otpCreationTime", 0);
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - savedOtpCreationTime;
        long remainingTime = otpExpiryDuration - elapsedTime;

        if (remainingTime > 0) {
            startOtpCountdown(remainingTime);
        } else {
            tvTimer.setText("OTP expired");
            Toast.makeText(ResetPasswordActivity.this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show();
            btnUpdatePassword.setEnabled(false);
            btnUpdatePassword.setBackgroundTintList(ContextCompat.getColorStateList(ResetPasswordActivity.this, R.color.dark_gray));
        }

        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = txtOtp.getText().toString();
                String newPassword = txtNewPassword.getText().toString();
                String confirmPassword = txtConfirmPassword.getText().toString();

                if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ResetPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if (!otp.equals(receivedOtp)) {
                    Toast.makeText(ResetPasswordActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ResetPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    checkOtpExpiryAndUpdatePassword(newPassword);
                }
            }
        });
    }

    private void startOtpCountdown(long remainingTime) {
        new CountDownTimer(remainingTime, 1000) {

            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                tvTimer.setText("OTP expired");
                Toast.makeText(ResetPasswordActivity.this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show();
                btnUpdatePassword.setEnabled(false);
                btnUpdatePassword.setBackgroundTintList(ContextCompat.getColorStateList(ResetPasswordActivity.this, R.color.dark_gray));
                SharedPreferences sharedPreferences = getSharedPreferences("otp_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("otpCreationTime");
                editor.apply();
            }
        }.start();
    }

    private void checkOtpExpiryAndUpdatePassword(String newPassword) {
        String email = getIntent().getStringExtra("email");
        dbUsers.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            if (user.getUserOtp() != null && user.getUserOtp().equals(receivedOtp)) {
                                if (isOtpExpired(user)) {
                                    Toast.makeText(ResetPasswordActivity.this, "OTP has expired. Please request a new one.", Toast.LENGTH_SHORT).show();
                                } else {
                                    user.setUserPassword(newPassword);
                                    user.setUserOtp(null);
                                    user.setOtpCreationTime(0);
                                    userSnapshot.getRef().setValue(user).addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ResetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            redirectToLogin();
                                        } else {
                                            Toast.makeText(ResetPasswordActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResetPasswordActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOtpExpired(User user) {
        long currentTime = System.currentTimeMillis();
        long otpCreationTime = user.getOtpCreationTime();
        long expiryDuration = 5 * 60 * 1000;
        return (currentTime - otpCreationTime) > expiryDuration;
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
