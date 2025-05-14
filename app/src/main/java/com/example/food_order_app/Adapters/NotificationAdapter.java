package com.example.food_order_app.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.Activities.OrderDetailActivity;
import com.example.food_order_app.Activities.OrderHistoryAdminActivity;
import com.example.food_order_app.Models.Notification;
import com.example.food_order_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private DatabaseReference dbNotifications;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
        this.dbNotifications = FirebaseDatabase.getInstance().getReference("Notifications"); // Initialize Firebase reference
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvNotificationMessage.setText(notification.getMessage());
        holder.tvNotificationDate.setText(notification.getFormattedDate());

        if (notification.isSeen()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.tvNewLabel.setVisibility(View.GONE);
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.gray));
            holder.tvNewLabel.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {

            notification.setSeen(true);
            notifyItemChanged(position);
            updateNotificationSeenStatus(notification.getId());
            openOrderHistoryAdmin(v.getContext());
        });
    }


    private void updateNotificationSeenStatus(String notificationId) {
        dbNotifications.child(notificationId).child("seen").setValue(true)
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    private void openOrderHistoryAdmin(Context context) {
        Intent intent = new Intent(context, OrderHistoryAdminActivity.class);
        context.startActivity(intent);
    }



    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotificationMessage, tvNotificationDate, tvNewLabel;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationDate = itemView.findViewById(R.id.tvNotificationDate);
            tvNewLabel = itemView.findViewById(R.id.tvNewLabel);

        }
    }
}
