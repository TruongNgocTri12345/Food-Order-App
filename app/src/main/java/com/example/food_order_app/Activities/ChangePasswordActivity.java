package com.example.food_order_app.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.food_order_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePasswordActivity extends DialogFragment {

    private EditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private DatabaseReference dbUsers;
    private String currentUserId;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_change_password, null);

        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = view.findViewById(R.id.confirmNewPasswordEditText);
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");
        currentUserId = requireActivity().getSharedPreferences("user_prefs", requireActivity().MODE_PRIVATE)
                .getString("current_user_id", null);

        changePasswordButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            if (validateInputs(currentPassword, newPassword, confirmNewPassword)) {
                updatePassword(currentPassword, newPassword);
            }
        });

        builder.setView(view);


//        return builder.create();

        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog_background);
        return dialog;
    }

    private boolean validateInputs(String currentPassword, String newPassword, String confirmNewPassword) {
        if (newPassword.equals(confirmNewPassword)) {
            return true;
        } else {
            Toast.makeText(getActivity(), "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void updatePassword(String currentPassword, String newPassword) {
        dbUsers.child(currentUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String storedPassword = task.getResult().child("userPassword").getValue(String.class);
                if (storedPassword != null && storedPassword.equals(currentPassword)) {
                    dbUsers.child(currentUserId).child("userPassword").setValue(newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getActivity(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
