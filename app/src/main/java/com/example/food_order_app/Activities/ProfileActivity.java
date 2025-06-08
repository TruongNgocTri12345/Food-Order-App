package com.example.food_order_app.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_REQUEST = 100; // Định nghĩa mã yêu cầu cho việc chọn hình ảnh
    private TextView userNameTextView;
    private TextView userPhoneTextView;
    private TextView userAddressTextView;
    private TextView userEmailTextView;
    private DatabaseReference dbUsers;
    private Uri imageUri; // Biến lưu URI của hình ảnh được chọn
    private String imageUrl;
    private ImageView imageAvatar;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userNameTextView = findViewById(R.id.userName);
        userPhoneTextView = findViewById(R.id.userPhone);
        userAddressTextView = findViewById(R.id.userAddress);
        userEmailTextView = findViewById(R.id.userEmail);
        imageAvatar = findViewById(R.id.userAvatar); // Khởi tạo imageAvatar
        ImageButton backButton = findViewById(R.id.backButton);
        Button editButton = findViewById(R.id.editButton);
        Button btnChooseImage = findViewById(R.id.btnChooseAvatar);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        fetchUserData();

        backButton.setOnClickListener(v -> finish());
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });

        btnChooseImage.setOnClickListener(view -> chooseImage());
    }

    private void fetchUserData() {
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_user_id", null);

        if (userId != null) {
            dbUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            userNameTextView.setText(currentUser.getUserName());
                            userPhoneTextView.setText(currentUser.getUserPhone());
                            userAddressTextView.setText(currentUser.getUserAddress());
                            userEmailTextView.setText(currentUser.getUserEmail());
                            if (currentUser.getAvatarUrl() != null) {
                                Glide.with(ProfileActivity.this).load(currentUser.getAvatarUrl()).error(R.drawable.ic_image_placeholder).into(imageAvatar);
                            }
                        } else {
                            showToast("User not found");
                        }
                    } else {
                        showToast("User not found");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    showToast("Failed to fetch user data");
                }
            });
        } else {
            showToast("No user found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String updatedName = data.getStringExtra("updatedName");
            String updatedPhone = data.getStringExtra("updatedPhone");
            String updatedAddress = data.getStringExtra("updatedAddress");

            // Cập nhật thông tin trên UI
            userNameTextView.setText(updatedName);
            userPhoneTextView.setText(updatedPhone);
            userAddressTextView.setText(updatedAddress);

            // Cập nhật thông tin trên Firebase
            String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .getString("current_user_id", null);
            if (userId != null) {
                dbUsers.child(userId).child("userName").setValue(updatedName);
                dbUsers.child(userId).child("userPhone").setValue(updatedPhone);
                dbUsers.child(userId).child("userAddress").setValue(updatedAddress);
            }
        }
    }

    private void uploadImage() {
        if (imageUri != null) {
            progressDialog = new ProgressDialog(ProfileActivity.this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            storageReference = FirebaseStorage.getInstance().getReference("avatars"); // Thay đổi đường dẫn đến thư mục "avatars"
            StorageReference fileReference = storageReference.child(System.currentTimeMillis() + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageUrl = uri.toString();
                                updateUserAvatarUrl(imageUrl); // Cập nhật URL của avatar trong cơ sở dữ liệu
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                showToast("Upload successful");
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        showToast(e.getMessage());
                    });
        } else {
            showToast("No image selected");
        }
    }

    private void updateUserAvatarUrl(String imageUrl) {
        String userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("current_user_id", null);
        if (userId != null) {
            dbUsers.child(userId).child("avatarUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> showToast("Avatar updated successfully"))
                    .addOnFailureListener(e -> showToast("Failed to update avatar"));
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
