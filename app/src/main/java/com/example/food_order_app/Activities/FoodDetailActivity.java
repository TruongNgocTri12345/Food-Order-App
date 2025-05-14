package com.example.food_order_app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.food_order_app.Adapters.CommentsAdapter;
import com.example.food_order_app.Models.Cart;
import com.example.food_order_app.Models.FoodItem;
import com.example.food_order_app.Models.Review;
import com.example.food_order_app.R;
import com.example.food_order_app.Utils.RoundedCornersTransformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FoodDetailActivity extends AppCompatActivity {

    private TextView foodName, foodDescription, foodPrice, foodRating, foodCategory, txt_no_reviews;
    private ImageView foodImage1, favoriteIcon;
    private DatabaseReference dbReview, dbFoodItem, dbCart, dbFavorites;
    private RecyclerView recyclerView;
    private String strFoodId;
    private Long strFoodPrice;
    private AlertDialog dialog;
    private CommentsAdapter adapter;
    private List<Review> reviewList;
    private FoodItem foodItem;
    private boolean isFavorite = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_detail);

        initUI();
        strFoodId = getIntent().getStringExtra("foodId");
        strFoodPrice = getIntent().getLongExtra("foodPrice", 0);
        if (strFoodId == null) {
            showToast("Invalid food ID");
            finish();
            return;
        }
        setupRecyclerView();
        loadContent();

        favoriteIcon.setOnClickListener(v -> {
            if (foodItem != null) {
                toggleFavorite(foodItem);
            } else {
                showToast("Food item is not available.");
            }
        });
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btn_add_comment).setOnClickListener(v -> addComment());
        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> addToCart());
    }

    private void toggleFavorite(FoodItem foodItem) {
        dbFavorites = FirebaseDatabase.getInstance().getReference().child("Favourites").child(getCurrentUserId());
        dbFavorites.child(strFoodId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    dbFavorites.child(snapshot.getKey()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Removed from favorites");
                            favoriteIcon.setImageResource(R.drawable.ic_favourite_default);
                            isFavorite = false;
                        } else {
                            showToast("Failed to remove from favorites");
                        }
                    });
                } else {
                    dbFavorites.child(strFoodId).setValue(foodItem).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Added to favorites");
                            favoriteIcon.setImageResource(R.drawable.ic_favourite_full);
                            isFavorite = true;
                        } else {
                            showToast("Failed to add to favorites");
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to check favorites");
            }
        });
    }


    private void addToCart() {
        try {
            String userId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);
            if (userId == null) {
                showToast("Please login to add to cart");
                return;
            }
            dbCart = FirebaseDatabase.getInstance().getReference().child("Cart").child(userId);
            dbCart.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isExists = false;
                    if (snapshot.exists()) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Cart cart = dataSnapshot.getValue(Cart.class);
                            cart.setCartId(dataSnapshot.getKey());
                            if (cart != null && cart.getFoodId().equals(strFoodId)) {
                                int newQuantity = cart.getQuantity() + 1;
                                dataSnapshot.getRef().child("quantity").setValue(newQuantity);
                                dataSnapshot.getRef().child("total_price").setValue(strFoodPrice * newQuantity);
                                isExists = true;
                                showToast("Increased quantity in cart");
                                break;
                            }
                        }
                    }

                    if (!isExists) {
                        Cart newCart = new Cart(userId, strFoodPrice, 1, strFoodId);
                        dbCart.push().setValue(newCart).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                showToast("Added to cart");
                            } else {
                                showToast("Failed to add to cart");
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast(error.getMessage());
                }
            });
        } catch (Exception e) {
            showToast(e.getMessage());
        }

    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentsAdapter(reviewList);
        recyclerView.setAdapter(adapter);


        // Kiểm tra số lượng bình luận
        if (reviewList.isEmpty()) {
            recyclerView.setVisibility(View.GONE); // Ẩn RecyclerView
            findViewById(R.id.txt_no_reviews).setVisibility(View.VISIBLE); // Hiện thông báo chưa có bình luận
        } else {
            recyclerView.setVisibility(View.VISIBLE); // Hiện RecyclerView
        }
    }

    private void initUI() {
        foodName = findViewById(R.id.txt_foodName);
        foodDescription = findViewById(R.id.txt_foodDescription);
        foodPrice = findViewById(R.id.txt_foodPrice);
        foodRating = findViewById(R.id.txt_foodRating);
        foodCategory = findViewById(R.id.txt_foodCategory);
        foodImage1 = findViewById(R.id.img_foodImage);
        recyclerView = findViewById(R.id.rcv_reviews);
        txt_no_reviews = findViewById(R.id.txt_no_reviews);
        favoriteIcon = findViewById(R.id.favoriteIcon);


        reviewList = new ArrayList<>();
    }

    private void loadContent() {
        dbFoodItem = FirebaseDatabase.getInstance().getReference().child("FoodItems").child(strFoodId);
        dbFoodItem.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    foodItem = snapshot.getValue(FoodItem.class);
                    displayFoodDetails(snapshot);
                    loadReview();
                    setupRecyclerView();
                    checkIfFavorite(strFoodId);
                    if (foodItem != null && foodItem.getFoodId() != null) {
                        checkIfFavorite(snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load food details");
            }
        });
    }

    private void checkIfFavorite(String foodId) {
        dbFavorites = FirebaseDatabase.getInstance().getReference().child("Favourites").child(getCurrentUserId());

        dbFavorites.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFoodFound = false;

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    if (childSnapshot.getKey().equals(foodId)) {
                        isFavorite = true;
                        favoriteIcon.setImageResource(R.drawable.ic_favourite_full);
                        isFoodFound = true;
                        break;
                    }
                }

                if (!isFoodFound) {
                    isFavorite = false;
                    favoriteIcon.setImageResource(R.drawable.ic_favourite_default);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to check favorites");
            }
        });
    }

    private String getCurrentUserId() {
        return getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);
    }

    private void loadReview() {
        dbReview = FirebaseDatabase.getInstance().getReference().child("Reviews").child(strFoodId);
        dbReview.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                adapter.setData(reviewList);
                setupRecyclerView();
                txt_no_reviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load reviews");
            }
        });

    }

    private void displayFoodDetails(DataSnapshot snapshot) {
        foodName.setText(snapshot.child("name").getValue(String.class));
        foodDescription.setText(snapshot.child("description").getValue(String.class));
        foodPrice.setText(String.format("%,d VNĐ", snapshot.child("price").getValue(Long.class)));
        foodRating.setText(String.format("★ %.1f", snapshot.child("rating").getValue(Double.class)));
        foodCategory.setText(snapshot.child("category").getValue(String.class));
        Glide.with(this)
                .load(snapshot.child("image").getValue(String.class))
                .error(R.drawable.ic_image_placeholder)
                .transform(new CenterCrop(), new RoundedCornersTransformation(30, 0)) // Adjust radius (30) as needed
                .into(foodImage1);
//        Glide.with(this).load(snapshot.child("image").getValue(String.class)).error(R.drawable.ic_image_placeholder).into(foodImage1);
    }

    private void addComment() {
        dbReview = FirebaseDatabase.getInstance().getReference().child("Reviews").child(strFoodId);
        dialog = createCommentDialog();
        dialog.show();
    }

    private AlertDialog createCommentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.comment_dialog, null);
        builder.setView(view);

        EditText etComment = view.findViewById(R.id.edtComment);
        RatingBar ratingBar = view.findViewById(R.id.foodRating);
        Button btnSubmit = view.findViewById(R.id.btnSubmitRating);

        String currentUserId = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("current_user_id", null);

        btnSubmit.setOnClickListener(v -> submitComment(etComment, ratingBar, currentUserId));
        return builder.create();
    }

    private void submitComment(EditText etComment, RatingBar ratingBar, String userId) {
        String comment = etComment.getText().toString();
        float rating = ratingBar.getRating();

        if (comment.isEmpty() || rating == 0.0f) {
            etComment.setError("Please provide a comment");
            return;
        }

        Review newReview = new Review(comment, rating, strFoodId, userId);
        dbReview.push().setValue(newReview).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                updateTotalRating(rating);
                dialog.dismiss();
            } else {
                showToast("Failed to add review");
            }
        });
    }

    private void updateTotalRating(float newRating) {
        dbReview.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalRating = 0.0f;
                int count = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    totalRating += dataSnapshot.getValue(Review.class).getRating();
                    count++;
                }
                float averageRating = (count > 0) ? totalRating / count : newRating;
                dbFoodItem.child("rating").setValue(averageRating);
                loadContent();
                setupRecyclerView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to update rating");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
