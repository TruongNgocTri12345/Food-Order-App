package com.example.food_order_app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // Thêm dòng này


import com.example.food_order_app.Fragments.CartFragment;
import com.example.food_order_app.Fragments.HomeFragment;
import com.example.food_order_app.Fragments.MenuFragment;
import com.example.food_order_app.Fragments.ProfileFragment;
import com.example.food_order_app.R;
import com.example.food_order_app.databinding.ActivityNavigationBinding;

public class NavigationActivity extends AppCompatActivity {

    public ActivityNavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());//dùng để set layout trong activity bằng binding.getRoot()
        // Mặc định mở HomeFragment khi Activity khởi động
        openFragment(new HomeFragment());
        // Đặt item mặc định là Home
        binding.bottomNavigationView.setSelectedItemId(R.id.home);

        if (getIntent().getBooleanExtra("openCartFragment", false)) {
            openFragment(new CartFragment());
            binding.bottomNavigationView.setSelectedItemId(R.id.cart);
            getIntent().removeExtra("openCartFragment");
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    openFragment(new HomeFragment());
                    return true;
                case R.id.menu:
                    openFragment(new MenuFragment());
                    return true;
                case R.id.cart:
                    openFragment(new CartFragment());
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

    public void openFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_bottom_navigation, fragment)
                .commit();
        String fragmentName = fragment.getClass().getSimpleName();
        Log.d("NavigationActivity", "Opened Fragment: " + fragmentName);
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
                    Intent intent = new Intent(NavigationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
    }

}
