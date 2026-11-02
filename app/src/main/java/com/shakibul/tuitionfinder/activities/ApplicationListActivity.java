package com.shakibul.tuitionfinder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.shakibul.tuitionfinder.R;
import com.shakibul.tuitionfinder.adapters.ApplicationAdapter;
import com.shakibul.tuitionfinder.models.Application;
import com.shakibul.tuitionfinder.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationListActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";        // "post" বা "mine"
    public static final String EXTRA_POST_ID = "postId";   // "post" মোডে লাগবে

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvListTitle;

    private final List<Application> list = new ArrayList<>();
    private ApplicationAdapter adapter;
    private FirebaseFirestore db;
    private String mode = "mine";
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_list);

        db = FirebaseFirestore.getInstance();

        String m = getIntent().getStringExtra(EXTRA_MODE);
        if (m != null) mode = m;
        postId = getIntent().getStringExtra(EXTRA_POST_ID);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvListTitle = findViewById(R.id.tvListTitle);

        boolean parentView = "post".equals(mode);
        tvListTitle.setText(parentView ? "আবেদনকারী শিক্ষক" : "আমার আবেদনসমূহ");
        tvEmpty.setText(parentView ? "এখনো কেউ আবেদন করেনি" : "আপনি এখনো কোনো আবেদন করেননি");

        adapter = new ApplicationAdapter(list, parentView, new ApplicationAdapter.ActionListener() {
            @Override
            public void onAccept(Application app) {
                updateStatus(app, "accepted");
            }

            @Override
            public void onReject(Application app) {
                updateStatus(app, "rejected");
            }

            @Override
            public void onReview(Application app) {
                showReviewDialog(app);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApplications();
    }

    private void loadApplications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Query query;
        if ("post".equals(mode)) {
            if (postId == null) {
                finish();
                return;
            }
            query = db.collection("applications")
                    .whereEqualTo("postId", postId)
                    .whereEqualTo("parentId", user.getUid());
        } else {
            query = db.collection("applications").whereEqualTo("tutorId", user.getUid());
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    list.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        list.add(doc.toObject(Application.class));
                    }

                    Collections.sort(list, new Comparator<Application>() {
                        @Override
                        public int compare(Application a, Application b) {
                            return Long.compare(b.getCreatedAt(), a.getCreatedAt());
                        }
                    });

                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "লোড ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void updateStatus(Application app, String newStatus) {
        db.collection("applications").document(app.getApplicationId())
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {
                    boolean accepted = "accepted".equals(newStatus);

                    // শিক্ষককে জানানো
                    NotificationHelper.send(app.getTutorId(),
                            (accepted ? "আপনার আবেদন গ্রহণ করা হয়েছে ✔ : " : "আপনার আবেদন বাতিল হয়েছে : ")
                                    + app.getPostTitle());

                    Toast.makeText(this,
                            accepted ? "শিক্ষককে গ্রহণ করা হয়েছে" : "আবেদন বাতিল করা হয়েছে",
                            Toast.LENGTH_SHORT).show();
                    loadApplications();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ===== রিভিউ =====

    private void showReviewDialog(Application app) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(48, 32, 48, 16);

        RatingBar bar = new RatingBar(this);
        bar.setNumStars(5);
        bar.setStepSize(1f);
        bar.setRating(5f);
        box.addView(bar);

        EditText input = new EditText(this);
        input.setHint("মন্তব্য (ঐচ্ছিক)");
        box.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(app.getTutorName() + " কে রিভিউ দিন")
                .setView(box)
                .setPositiveButton("জমা দিন", (dialog, which) -> {
                    int rating = (int) bar.getRating();
                    if (rating < 1) rating = 1;
                    submitReview(app, rating, input.getText().toString().trim());
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void submitReview(Application app, int rating, String comment) {
        // তিনটা কাজ একসাথে: রিভিউ সেভ, শিক্ষকের মোট স্কোর বাড়ানো, আবেদনে "reviewed" চিহ্ন
        WriteBatch batch = db.batch();

        DocumentReference reviewRef = db.collection("reviews").document(app.getApplicationId());
        Map<String, Object> review = new HashMap<>();
        review.put("reviewId", app.getApplicationId());
        review.put("tutorId", app.getTutorId());
        review.put("parentId", app.getParentId());
        review.put("parentName", app.getParentName());
        review.put("rating", (long) rating);
        review.put("comment", comment);
        review.put("createdAt", System.currentTimeMillis());
        batch.set(reviewRef, review);

        batch.update(db.collection("users").document(app.getTutorId()),
                "ratingSum", FieldValue.increment(rating),
                "ratingCount", FieldValue.increment(1));

        batch.update(db.collection("applications").document(app.getApplicationId()),
                "reviewed", true);

        batch.commit()
                .addOnSuccessListener(unused -> {
                    NotificationHelper.send(app.getTutorId(),
                            app.getParentName() + " আপনাকে " + rating + " স্টার রেটিং দিয়েছেন ⭐");
                    Toast.makeText(this, "রিভিউ জমা হয়েছে, ধন্যবাদ!", Toast.LENGTH_SHORT).show();
                    loadApplications();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "রিভিউ ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}