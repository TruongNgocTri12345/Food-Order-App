package com.example.food_order_app.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.food_order_app.Activities.ChangePasswordActivity;
import com.example.food_order_app.Activities.FavouriteActivity;
import com.example.food_order_app.Activities.NotificationActivity;
import com.example.food_order_app.Activities.OrderHistoryActivity;
import com.example.food_order_app.Activities.OrderHistoryAdminActivity;
import com.example.food_order_app.Activities.ProfileActivity;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView userNameTextView;
    private TextView userPhoneTextView;
    private DatabaseReference dbUsers;
    private ImageView imageAvatar;
    private Button btnFavourite, btnOrder, btnNotify;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        userNameTextView = view.findViewById(R.id.userName);
        userPhoneTextView = view.findViewById(R.id.userPhone);
        imageAvatar = view.findViewById(R.id.profileImage);
        Button btnChangePassword = view.findViewById(R.id.btnChangePassword);
        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnOrder = view.findViewById(R.id.btnOrder);
        btnFavourite = view.findViewById(R.id.btnFavourite);

        fetchUserData();

        btnOrder.setOnClickListener(v -> {
            String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                    .getString("current_user_id", null);

            if (userId != null) {
                dbUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User currentUser = dataSnapshot.getValue(User.class);
                            if (currentUser != null) {
                                Intent intent;
                                if (currentUser.isAdmin()) {
                                    intent = new Intent(getActivity(), OrderHistoryAdminActivity.class);
                                } else {
                                    intent = new Intent(getActivity(), OrderHistoryActivity.class);
                                }
                                startActivity(intent);
                            } else {
                                showToast("User not found");
                            }
                        } else {
                            showToast("User not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast("Failed to fetch user data");
                    }
                });
            } else {
                showToast("No user found");
            }
        });

        btnNotify = view.findViewById(R.id.btnNotification);

        btnNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotificationActivity.class);
                String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                        .getString("current_user_id", null);
                boolean isAdmin = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                        .getBoolean("admin", false);
                intent.putExtra("isAdmin", isAdmin);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });



        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            ChangePasswordActivity dialogFragment = new ChangePasswordActivity();
            dialogFragment.show(requireActivity().getSupportFragmentManager(), "ChangePasswordActivity");
        });

        btnFavourite.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FavouriteActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchUserData() {
        String userId = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE)
                .getString("current_user_id", null);

        if (userId != null) {
            dbUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isAdded() && dataSnapshot.exists()) {
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            userNameTextView.setText(currentUser.getUserName());
                            userPhoneTextView.setText(currentUser.getUserPhone());

                            if (currentUser.isAdmin()) {
                                btnNotify.setVisibility(View.VISIBLE); // Hiển thị nút Notify cho admin
                                btnFavourite.setVisibility(View.GONE); // Ẩn nút Favourite cho admin
                            } else {
                                btnNotify.setVisibility(View.GONE); // Ẩn nút Notify cho người dùng thường
                                btnFavourite.setVisibility(View.VISIBLE); // Hiển thị nút Favourite cho người dùng thường
                            }

                            Glide.with(ProfileFragment.this)
                                    .load(currentUser.getAvatarUrl())
                                    .apply(RequestOptions.circleCropTransform())
                                    .error(R.drawable.ic_image_placeholder)
                                    .into(imageAvatar);
                        } else {
                            showToast("User not found");
                        }
                    } else {
                        showToast("User not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showToast("Failed to fetch user data");
                }
            });
        } else {
            showToast("No user found");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserData();
    }

    private void showToast(String message) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
