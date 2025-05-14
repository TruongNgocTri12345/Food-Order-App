package com.example.food_order_app.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.example.food_order_app.Utils.MailSender;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText txtEmail;
    private Button btnResetPassword;
    private ImageButton btnBack;
    private String generatedOtp;
    private DatabaseReference dbUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmail = findViewById(R.id.txt_email);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        btnBack = findViewById(R.id.btn_back);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = txtEmail.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                } else {
                    checkEmailExists(email);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void checkEmailExists(String email) {
        dbUsers.orderByChild("userEmail").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            if (isOtpExpired(user)) {
                                generatedOtp = generateOtp();
                                user.setUserOtp(generatedOtp);
                                user.setOtpCreationTime(System.currentTimeMillis());
                                userSnapshot.getRef().setValue(user);
                                saveOtpCreationTime();  
                                sendOtpToEmail(email, generatedOtp);
                            } else {
                                navigateToResetPassword(email, user.getUserOtp());
                                Toast.makeText(ForgotPasswordActivity.this, "OTP has not expired yet. Please check your email.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ForgotPasswordActivity.this, "Error checking email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToResetPassword(String email, String otp) {
        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("otp", otp);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private boolean isOtpExpired(User user) {
        long currentTime = System.currentTimeMillis();
        long otpCreationTime = user.getOtpCreationTime();
        long expiryDuration = 5 * 60 * 1000;
        return (currentTime - otpCreationTime) > expiryDuration;
    }

    private void sendOtpToEmail(String email, String otp) {
        String subject = "Your OTP Code";
        String message = "Your OTP code for password reset is: " + otp;

        MailSender mailSender = new MailSender(email, subject, message);
        mailSender.execute();

        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
        intent.putExtra("otp", otp);
        intent.putExtra("email", email);
        intent.putExtra("otpCreationTime", System.currentTimeMillis());
        startActivity(intent);
    }

    private void saveOtpCreationTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("otp_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("otpCreationTime", System.currentTimeMillis());
        editor.apply();
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}
