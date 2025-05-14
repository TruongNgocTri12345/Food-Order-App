package com.example.food_order_app.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.food_order_app.Fragments.DashboardFragment;
import com.example.food_order_app.Fragments.ProfileFragment;
import com.example.food_order_app.R;
import com.example.food_order_app.databinding.ActivityAdminNavigationBinding;

public class AdminNavigationActivity extends AppCompatActivity {

    ActivityAdminNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mặc định mở DashboardFragment khi Activity khởi động
        openFragment(new DashboardFragment());
        // Đặt item mặc định là Dashboard
        binding.adminBottomNavigationView.setSelectedItemId(R.id.dashboard);

        // Thiết lập sự kiện chọn item trong Bottom Navigation
        binding.adminBottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dashboard:
                    openFragment(new DashboardFragment());
                    return true;
                case R.id.profile:
                    openFragment(new ProfileFragment());
                    return true;
                case R.id.logout:
                    logout();
                    return true;
                default:
                    return false;
            }

        });
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Xóa thông tin đăng nhập từ SharedPreferences
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit()
                            .remove("current_user_id") // Xóa ID người dùng đã lưu
                            .remove("admin") // Xóa trạng thái admin
                            .apply();
                    // Chuyển hướng đến màn hình đăng nhập
                    Intent intent = new Intent(AdminNavigationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_bottom_navigation, fragment)
                .commit();
    }
}
