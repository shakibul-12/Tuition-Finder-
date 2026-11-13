package com.shakibul.tuitionfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;

public class HomeActivity extends AppCompatActivity {

    private View layoutHeader, card1, card2, card3, card4, tab2, tab3, tab4;
    private TextView tvAvatar, tvWelcome, tvRole, btnLogout;
    private TextView tvIcon1, tvLabel1, tvIcon2, tvLabel2, tvLabel3, tvTabIcon2, tvTabLabel2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        layoutHeader = findViewById(R.id.layoutHeader);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvRole = findViewById(R.id.tvRole);
        btnLogout = findViewById(R.id.btnLogout);

        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        tvIcon1 = findViewById(R.id.tvIcon1);
        tvLabel1 = findViewById(R.id.tvLabel1);
        tvIcon2 = findViewById(R.id.tvIcon2);
        tvLabel2 = findViewById(R.id.tvLabel2);
        tvLabel3 = findViewById(R.id.tvLabel3);

        tab2 = findViewById(R.id.tab2);
        tab3 = findViewById(R.id.tab3);
        tab4 = findViewById(R.id.tab4);
        tvTabIcon2 = findViewById(R.id.tvTabIcon2);
        tvTabLabel2 = findViewById(R.id.tvTabLabel2);

        // যেগুলো সব ধরনের ইউজারের জন্য একই
        Runnable openNotifications = () ->
                startActivity(new Intent(this, NotificationActivity.class));
        Runnable openProfile = () ->
                startActivity(new Intent(this, ProfileActivity.class));

        card3.setOnClickListener(v -> openNotifications.run());
        tab3.setOnClickListener(v -> openNotifications.run());
        card4.setOnClickListener(v -> openProfile.run());
        tab4.setOnClickListener(v -> openProfile.run());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            goToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser();
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
                    if (name == null) name = "";

                    tvWelcome.setText("স্বাগতম, " + name);
                    tvAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());

                    if ("tutor".equals(role)) {
                        setupTutor();
                    } else {
                        setupParent();
                    }
                    loadNotificationCount(currentUser.getUid());
                })
                .addOnFailureListener(e -> tvWelcome.setText("ডাটা আনতে সমস্যা হয়েছে"));
    }

    // শিক্ষকের হোম
    private void setupTutor() {
        layoutHeader.setBackgroundResource(R.drawable.bg_header_teacher);
        tvRole.setText("শিক্ষক প্যানেল");

        tvIcon1.setText("🔍");
        tvLabel1.setText("টিউশন খুঁজুন");
        tvIcon2.setText("📨");
        tvLabel2.setText("আমার আবেদনসমূহ");

        Runnable find = () -> {
            Intent i = new Intent(this, TuitionListActivity.class);
            i.putExtra(TuitionListActivity.EXTRA_MODE, "all");
            startActivity(i);
        };
        Runnable myApplications = () -> {
            Intent i = new Intent(this, ApplicationListActivity.class);
            i.putExtra(ApplicationListActivity.EXTRA_MODE, "mine");
            startActivity(i);
        };

        card1.setOnClickListener(v -> find.run());
        card2.setOnClickListener(v -> myApplications.run());

        tvTabIcon2.setText("🔍");
        tvTabLabel2.setText("খুঁজুন");
        tab2.setOnClickListener(v -> find.run());
    }

    // অভিভাবকের হোম
    private void setupParent() {
        layoutHeader.setBackgroundResource(R.drawable.bg_header_parent);
        tvRole.setText("অভিভাবক / ছাত্র প্যানেল");

        tvIcon1.setText("➕");
        tvLabel1.setText("টিউশন পোস্ট করুন");
        tvIcon2.setText("📋");
        tvLabel2.setText("আমার পোস্ট");

        Runnable create = () ->
                startActivity(new Intent(this, CreatePostActivity.class));
        Runnable myPosts = () -> {
            Intent i = new Intent(this, TuitionListActivity.class);
            i.putExtra(TuitionListActivity.EXTRA_MODE, "mine");
            startActivity(i);
        };

        card1.setOnClickListener(v -> create.run());
        card2.setOnClickListener(v -> myPosts.run());

        tvTabIcon2.setText("📋");
        tvTabLabel2.setText("আমার পোস্ট");
        tab2.setOnClickListener(v -> myPosts.run());
    }

    // না-পড়া নোটিফিকেশনের সংখ্যা কার্ডে দেখানো
    private void loadNotificationCount(String uid) {
        FirebaseFirestore.getInstance().collection("notifications")
                .whereEqualTo("toUserId", uid)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int n = snapshot.size();
                    tvLabel3.setText(n > 0 ? "নোটিফিকেশন (" + n + ")" : "নোটিফিকেশন");
                });
    }

    private void goToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}