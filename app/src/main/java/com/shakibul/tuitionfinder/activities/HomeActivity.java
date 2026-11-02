package com.shakibul.tuitionfinder.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome, tvRole;
    private Button btnCreatePost, btnMyPosts, btnFindTuition, btnMyApplications, btnProfile, btnLogout, btnNotifications;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        btnMyPosts = findViewById(R.id.btnMyPosts);
        btnFindTuition = findViewById(R.id.btnFindTuition);
        btnMyApplications = findViewById(R.id.btnMyApplications);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnNotifications = findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, NotificationActivity.class)));

        btnCreatePost.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, CreatePostActivity.class)));

        btnMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TuitionListActivity.class);
            intent.putExtra(TuitionListActivity.EXTRA_MODE, "mine");
            startActivity(intent);
        });

        btnFindTuition.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TuitionListActivity.class);
            intent.putExtra(TuitionListActivity.EXTRA_MODE, "all");
            startActivity(intent);
        });

        btnMyApplications.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ApplicationListActivity.class);
            intent.putExtra(ApplicationListActivity.EXTRA_MODE, "mine");
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser();
        loadNotificationCount();
    }


    // না-পড়া নোটিফিকেশনের সংখ্যা বাটনে দেখানো
    private void loadNotificationCount() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        if (u == null) return;

        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("toUserId", u.getUid())
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int n = snapshot.size();
                    btnNotifications.setText(n > 0 ? "নোটিফিকেশন (" + n + ")" : "নোটিফিকেশন");
                });
    }

    private void loadUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            goToLogin();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        tvWelcome.setText("প্রোফাইল পাওয়া যায়নি");
                        return;
                    }

                    String name = doc.getString("name");
                    String role = doc.getString("role");
                    tvWelcome.setText("স্বাগতম, " + name);

                    if ("tutor".equals(role)) {
                        tvRole.setText("আপনি একজন শিক্ষক");
                        btnFindTuition.setVisibility(View.VISIBLE);
                        btnMyApplications.setVisibility(View.VISIBLE);
                        btnCreatePost.setVisibility(View.GONE);
                        btnMyPosts.setVisibility(View.GONE);
                    } else {
                        tvRole.setText("আপনি একজন অভিভাবক/ছাত্র");
                        btnCreatePost.setVisibility(View.VISIBLE);
                        btnMyPosts.setVisibility(View.VISIBLE);
                        btnFindTuition.setVisibility(View.GONE);
                        btnMyApplications.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> tvWelcome.setText("ডাটা আনতে সমস্যা হয়েছে"));
    }

    private void goToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}