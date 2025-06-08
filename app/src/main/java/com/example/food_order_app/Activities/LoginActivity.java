package com.example.food_order_app.Activities;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food_order_app.R;
import com.example.food_order_app.Models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private ImageButton btnBack, btnTogglePassword;
    private EditText etEmail, etPassword;
    private boolean isPasswordVisible = false;
    private DatabaseReference dbUsers;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private ImageButton btnGoogleSignIn;
    private ImageButton btnFacebookLogin;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());  // Đảm bảo dùng getApplicationContext() ở đây
        AppEventsLogger.activateApp((Application) getApplicationContext());  // Cast thành Application

        dbUsers = FirebaseDatabase.getInstance().getReference("Users");

        // Khởi tạo UI elements
        btnLogin = findViewById(R.id.btn_login);
        btnBack = findViewById(R.id.btn_back);
        etEmail = findViewById(R.id.txt_email);
        etPassword = findViewById(R.id.txt_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnGoogleSignIn = findViewById(R.id.btn_login_google);
        btnFacebookLogin = findViewById(R.id.btn_login_facebook);

        // Facebook Login
        callbackManager = CallbackManager.Factory.create(); // Tạo CallbackManager cho Facebook
        btnFacebookLogin.setOnClickListener(v -> {
            // Đăng nhập bằng Facebook
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile"));
        });

        // Đăng ký callback cho Facebook login
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Lấy token truy cập Facebook
                AccessToken accessToken = loginResult.getAccessToken();
                Log.d("FacebookLogin", "Access Token: " + accessToken.getToken());
                handleFacebookAccessToken(accessToken);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Login Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Client ID từ strings.xml
                .requestEmail()
                .build();
        Log.d("LoginActivity", "Starting Google Sign-In");
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Gán sự kiện click cho nút Google Sign-In
        btnGoogleSignIn.setOnClickListener(view -> {
            Log.d("LoginActivity", "Google Sign-In button clicked");
            signInWithGoogle();
        });

        // Các sự kiện cho nút khác
        btnBack.setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, MainActivity.class)));
        btnTogglePassword.setOnClickListener(view -> togglePasswordVisibility());
        btnLogin.setOnClickListener(view -> validateLogin());

        TextView txtForgotPassword = findViewById(R.id.txt_forgot_password);
        txtForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        Log.d("LoginActivity", "Starting Google Sign-In Intent");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Facebook Login
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("LoginActivity", "Google Sign-In successful");
                handleGoogleSignIn(account);
            } catch (ApiException e) {
                Log.e("LoginActivity", "Google Sign-In failed", e);
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                Log.d("Google Sign-In", "Error code: " + e.getStatusCode());
            }
        }
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        String userId = account.getId();
        String userName = account.getDisplayName();
        String userEmail = account.getEmail();
        Uri userPhotoUrl = account.getPhotoUrl();

        User user = new User(userId, userName, userEmail, userPhotoUrl != null ? userPhotoUrl.toString() : "");

        dbUsers.child(userId).setValue(user)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        prefs.edit().putString("current_user_id", userId).apply();

                        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error saving user data to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        }
        etPassword.setSelection(etPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void validateLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        } else if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
        } else {
            User user = new User(email, password);
            login(user);
        }
    }

    private void login(User user) {
        dbUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLoggedIn = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String emailInDb = dataSnapshot.child("userEmail").getValue(String.class);
                    String passwordInDb = dataSnapshot.child("userPassword").getValue(String.class);
                    if (user.getUserEmail().equals(emailInDb) && user.getUserPassword().equals(passwordInDb)) {
                        String userId = dataSnapshot.getKey();
                        boolean isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);

                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("current_user_id", userId)
                                .putBoolean("admin", isAdmin)
                                .apply();

                        redirectToAppropriateActivity(isAdmin);
                        isLoggedIn = true;
                        break;
                    }
                }

                if (!isLoggedIn) {
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToAppropriateActivity(boolean isAdmin) {
        Intent intent;
        if (isAdmin) {
            intent = new Intent(LoginActivity.this, AdminNavigationActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, NavigationActivity.class);
        }
        startActivity(intent);
        finish();
        Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
    }
}
