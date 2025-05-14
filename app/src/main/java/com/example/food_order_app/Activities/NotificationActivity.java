package com.example.food_order_app.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food_order_app.Adapters.NotificationAdapter;
import com.example.food_order_app.Models.Notification;
import com.example.food_order_app.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerNotifications;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private DatabaseReference dbNotifications;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerNotifications.setAdapter(notificationAdapter);

        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        String userId = getIntent().getStringExtra("userId");

        TextView textViewMarkAllAsRead = findViewById(R.id.textViewMarkAllAsRead);
        textViewMarkAllAsRead.setOnClickListener(v -> markAllNotificationsAsRead(userId));

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        dbNotifications = FirebaseDatabase.getInstance().getReference("Notifications");

        if (isAdmin) {
            dbNotifications.orderByChild("type").equalTo("order").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    notificationList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            notificationList.add(0, notification);
                            // Only send notification if the notification is newly added and unread
                            if (!notification.isSeen() && !snapshot.hasChild("isProcessingRead")) {
                                sendAdminNotification(notification.getMessage());
                                snapshot.getRef().child("isProcessingRead").setValue(true); // Mark as processed
                            }
                        }
                    }
                    notificationAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            dbNotifications.orderByChild("userId").equalTo(userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    notificationList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Notification notification = snapshot.getValue(Notification.class);
                        if (notification != null) {
                            notificationList.add(notification);
                        }
                    }
                    notificationAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void markAllNotificationsAsRead(String userId) {
        dbNotifications.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (notification != null && !notification.isSeen()) {
                        notification.setSeen(true);
                        updateNotificationSeenStatus(notification.getId());
                    }
                }
                Toast.makeText(NotificationActivity.this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(NotificationActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNotificationSeenStatus(String notificationId) {
        dbNotifications.child(notificationId).child("seen").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Optional: handle success feedback if needed
                })
                .addOnFailureListener(e -> {
                    // Optional: handle failure feedback if needed
                });
    }

    private void sendAdminNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "AdminChannel")
                .setSmallIcon(R.drawable.food_icon)
                .setContentTitle("Order Notification")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
