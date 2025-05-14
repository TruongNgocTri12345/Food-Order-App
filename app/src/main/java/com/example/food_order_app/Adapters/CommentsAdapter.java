package com.example.food_order_app.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.food_order_app.Models.Review;
import com.example.food_order_app.Models.User;
import com.example.food_order_app.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Review> reviewList;
    private DatabaseReference dbUser;

    public CommentsAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
        dbUser = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void setData(List<Review> reviewList) {
        this.reviewList = reviewList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentsAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsAdapter.CommentViewHolder holder, int position) {
        Review review = reviewList.get(position);
        if (review == null) {
            return;
        }

        String userID = review.getUserId();
        dbUser.child(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                if (task.getResult().exists()) {
                    User u = task.getResult().getValue(User.class);
                    if (u != null) {
                        Glide.with(holder.itemView.getContext()).load(u.getAvatarUrl()).error(R.drawable.ic_image_placeholder).into(holder.imgUserAvatar);
                        holder.txtUserName.setText(u.getUserName());
                        holder.txtUserComment.setText(review.getComment());
                        String stars = "";
                        if (review.getRating() != null) {
                            for (int i = 0; i < review.getRating(); i++) {
                                stars += "★";
                            }
                            for (int i = 0; i < 5 - review.getRating(); i++) {
                                stars += "☆";
                            }
                        }
                        holder.txtUserRating.setText(stars + " " + String.valueOf(review.getRating()));
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        if (reviewList != null) {
            return reviewList.size();
        } else {
            return 0;
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgUserAvatar;
        private TextView txtUserName, txtUserComment, txtUserRating;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.img_user_avatar);
            txtUserName = itemView.findViewById(R.id.txt_user_name);
            txtUserComment = itemView.findViewById(R.id.txt_user_comment);
            txtUserRating = itemView.findViewById(R.id.user_rating);
        }
    }
}
