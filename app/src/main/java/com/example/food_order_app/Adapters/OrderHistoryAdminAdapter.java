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
import com.example.food_order_app.Models.Order;
import com.example.food_order_app.R;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdminAdapter extends RecyclerView.Adapter<OrderHistoryAdminAdapter.ViewHolder> {
    private List<Order> orders;

    public OrderHistoryAdminAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        String orderId = order.getOrderId();


        holder.orderId.setText("Order ID: " + order.getOrderId().replace("-", ""));
        holder.orderTotal.setText("Total: " + formatTotal(order.getOrderTotal()) + " VNĐ");
        holder.orderStatus.setText("Status: " + order.getOrderStatus());
        String formattedDate = formatDate(order.getOrderDate());
        holder.orderTime.setText("Order Time: " + formattedDate);

        holder.itemView.setOnClickListener(v -> {
            openOrderDetail(v, order);
        });

        holder.btnDetail.setOnClickListener(v -> {
            openOrderDetail(v, order);
        });
    }

    private void openOrderDetail(View view, Order order) {
        Intent intent = new Intent(view.getContext(), OrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        intent.putExtra("orderAddress", order.getOrderAddress());
        intent.putExtra("userId", order.getUserId());
        intent.putExtra("orderPhone", order.getOrderPhone());
        intent.putExtra("orderStatus", order.getOrderStatus());
        intent.putExtra("orderTotal", order.getOrderTotal());
        intent.putExtra("orderNote", order.getOrderNote());
        intent.putExtra("orderDate", order.getOrderDate());
        intent.putExtra("orderedItems", (Serializable) order.getOrderedItems());
        view.getContext().startActivity(intent);
    }

    private String formatDate(String orderDate) {
        long timestamp = Long.parseLong(orderDate);
        Date date = new Date(timestamp);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        return outputFormat.format(date);
    }

    private String formatTotal(double amount) {

        NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
        return numberFormat.format(amount);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderTotal, orderStatus, orderTime, btnDetail;

        public ViewHolder(View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderTime = itemView.findViewById(R.id.orderTime);
            btnDetail = itemView.findViewById(R.id.btnDetail);
        }
    }
}
