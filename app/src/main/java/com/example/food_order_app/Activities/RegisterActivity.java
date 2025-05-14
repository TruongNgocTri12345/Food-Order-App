package com.example.food_order_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegister;
    private ImageButton btnBack, btnTogglePassword, btnToggleConfirmPassword;
    private EditText etUsername, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private boolean isPasswordVisible = false; // Biến toàn cục cho trạng thái hiển thị mật khẩu
    private boolean isConfirmPasswordVisible = false; // Biến toàn cục cho trạng thái hiển thị xác nhận mật khẩu
    private DatabaseReference dbUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");

        // Khởi tạo các thành phần UI
        btnBack = findViewById(R.id.btn_back);
        btnRegister = findViewById(R.id.btn_create_account);
        etUsername = findViewById(R.id.txt_username);
        etEmail = findViewById(R.id.txt_email);
        etPhone = findViewById(R.id.txt_phone);
        etAddress = findViewById(R.id.txt_address);
        etPassword = findViewById(R.id.txt_password);
        etConfirmPassword = findViewById(R.id.txt_confirm_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnToggleConfirmPassword = findViewById(R.id.btn_toggle_confirm_password);

        // Thiết lập sự kiện nhấn nút quay lại
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Thiết lập sự kiện nhấn nút ẩn hiện mật khẩu
        btnTogglePassword.setOnClickListener(view -> togglePasswordVisibility());

        // Thiết lập sự kiện nhấn nút ẩn hiện mật khẩu xác nhận
        btnToggleConfirmPassword.setOnClickListener(view -> toggleConfirmPasswordVisibility());

        // Thiết lập sự kiện nhấn nút đăng ký
        btnRegister.setOnClickListener(view -> {
            // Validate thông tin
            if (validateInput()) {
                User newUser = new User();
                newUser.setUserName(etUsername.getText().toString());
                newUser.setUserEmail(etEmail.getText().toString());
                newUser.setUserPhone(etPhone.getText().toString());
                newUser.setUserAddress(etAddress.getText().toString());
                newUser.setUserPassword(etPassword.getText().toString());
                newUser.setAdmin(false);
                createNewUser(newUser);
            }
        });
    }

    private void createNewUser(User newUser) {
        try {
            dbUsers.push().setValue(newUser);
            // Xử lý khi tạo mới thành công
            Toast.makeText(this, "Create new user successfully", Toast.LENGTH_SHORT).show();
            finish(); // Hoặc chuyển đến trang khác
        } catch (Exception ex) {
            // Xử lý khi tạo mới thất bại
            Toast.makeText(this, "Create new user failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm để ẩn hiện mật khẩu
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility); // Thay đổi icon
        }
        etPassword.setSelection(etPassword.length()); // Đưa con trỏ về cuối
        isPasswordVisible = !isPasswordVisible; // Đổi trạng thái
    }

    // Hàm để ẩn hiện mật khẩu xác nhận
    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility); // Thay đổi icon
        }
        etConfirmPassword.setSelection(etConfirmPassword.length()); // Đưa con trỏ về cuối
        isConfirmPasswordVisible = !isConfirmPasswordVisible; // Đổi trạng thái
    }

    // Hàm để validate thông tin đầu vào
    private boolean validateInput() {
        if (etUsername.getText().toString().isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        String email = etEmail.getText().toString();
        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        String phone = etPhone.getText().toString();
        if (phone.isEmpty() || !isValidPhone(phone)) {
            Toast.makeText(this, "Phone number must be 10-13 digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etAddress.getText().toString().isEmpty()) {
            Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        String password = etPassword.getText().toString();
        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Nếu có xác nhận mật khẩu, kiểm tra xem mật khẩu có khớp hay không
        String confirmPassword = etConfirmPassword.getText().toString();
        if (confirmPassword.isEmpty() || !password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidPhone(String phone) {
        String phonePattern = "^\\d{10,13}$"; // Số điện thoại từ 10 đến 13 chữ số
        return phone.matches(phonePattern);
    }

}
