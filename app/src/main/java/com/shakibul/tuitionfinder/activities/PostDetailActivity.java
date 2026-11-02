package com.shakibul.tuitionfinder.activities;

import com.shakibul.tuitionfinder.utils.NotificationHelper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;
import com.shakibul.tuitionfinder.models.TuitionPost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "postId";

    private TextView tvTitle, tvClass, tvSubjects, tvArea, tvSalary, tvDays, tvGender, tvDetails, tvPostedBy;
    private Button btnDelete, btnApply, btnApplicants;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (postId == null || user == null) {
            finish();
            return;
        }
        db = FirebaseFirestore.getInstance();

        tvTitle = findViewById(R.id.tvTitle);
        tvClass = findViewById(R.id.tvClass);
        tvSubjects = findViewById(R.id.tvSubjects);
        tvArea = findViewById(R.id.tvArea);
        tvSalary = findViewById(R.id.tvSalary);
        tvDays = findViewById(R.id.tvDays);
        tvGender = findViewById(R.id.tvGender);
        tvDetails = findViewById(R.id.tvDetails);
        tvPostedBy = findViewById(R.id.tvPostedBy);
        btnDelete = findViewById(R.id.btnDelete);
        btnApply = findViewById(R.id.btnApply);
        btnApplicants = findViewById(R.id.btnApplicants);

        loadPost();
    }

    private void loadPost() {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(doc -> {
                    TuitionPost post = doc.toObject(TuitionPost.class);
                    if (post == null) {
                        Toast.makeText(this, "পোস্টটি পাওয়া যায়নি", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    showPost(post);
                    setupButtons(post);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "লোড ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void showPost(TuitionPost post) {
        tvTitle.setText(post.getTitle());
        tvClass.setText("ক্লাস: " + post.getStudentClass());
        tvSubjects.setText("বিষয়: " + post.getSubjects());
        tvArea.setText("এলাকা: " + post.getArea());
        tvSalary.setText("বেতন: " + post.getSalary() + " টাকা/মাস");
        tvDays.setText("সপ্তাহে: " + post.getDaysPerWeek() + " দিন");

        String g = post.getGender();
        String genderText = "male".equals(g) ? "পুরুষ শিক্ষক" : ("female".equals(g) ? "মহিলা শিক্ষক" : "যে কেউ");
        tvGender.setText("শিক্ষক: " + genderText);

        String details = post.getDetails();
        tvDetails.setText(details == null || details.isEmpty() ? "" : "বিস্তারিত: " + details);

        tvPostedBy.setText("পোস্ট করেছেন: " + post.getParentName());
    }

    // কে দেখছেন তার উপর নির্ভর করে বাটন ঠিক করা
    private void setupButtons(TuitionPost post) {
        if (user.getUid().equals(post.getParentId())) {
            // পোস্টের মালিক (অভিভাবক)
            btnApplicants.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);

            btnApplicants.setOnClickListener(v -> {
                Intent intent = new Intent(this, ApplicationListActivity.class);
                intent.putExtra(ApplicationListActivity.EXTRA_MODE, "post");
                intent.putExtra(ApplicationListActivity.EXTRA_POST_ID, postId);
                startActivity(intent);
            });
            btnDelete.setOnClickListener(v -> deletePost());
            return;
        }

        // অন্য কেউ: শিক্ষক হলে আবেদনের বাটন দেখাবে
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    if (!"tutor".equals(userDoc.getString("role"))) return;

                    btnApply.setVisibility(View.VISIBLE);

                    // আগেই আবেদন করা থাকলে বাটন বন্ধ
                    db.collection("applications").document(applicationId(post)).get()
                            .addOnSuccessListener(appDoc -> {
                                if (appDoc.exists()) {
                                    btnApply.setText("আবেদন করা হয়েছে ✔");
                                    btnApply.setEnabled(false);
                                } else {
                                    btnApply.setOnClickListener(v -> showApplyDialog(post));
                                }
                            });
                });
    }

    // একই শিক্ষক একই পোস্টে একবারই আবেদন করতে পারবে, তাই আইডি = postId_tutorId
    private String applicationId(TuitionPost post) {
        return post.getPostId() + "_" + user.getUid();
    }

    private void showApplyDialog(TuitionPost post) {
        EditText input = new EditText(this);
        input.setHint("যেমন: আমি গণিতে ৩ বছরের অভিজ্ঞ");
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("আবেদন করুন")
                .setMessage("অভিভাবকের জন্য একটি ছোট বার্তা লিখুন (ঐচ্ছিক)")
                .setView(input)
                .setPositiveButton("আবেদন পাঠান", (dialog, which) ->
                        submitApplication(post, input.getText().toString().trim()))
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void submitApplication(TuitionPost post, String message) {
        btnApply.setEnabled(false);

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(tutor -> {
                    if (!Boolean.TRUE.equals(tutor.getBoolean("profileComplete"))) {
                        btnApply.setEnabled(true);
                        Toast.makeText(this,
                                "আগে \"আমার প্রোফাইল\" থেকে প্রোফাইল সম্পূর্ণ করুন",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    String subjectsText = "";
                    List<String> subjects = (List<String>) tutor.get("subjects");
                    if (subjects != null) {
                        subjectsText = android.text.TextUtils.join(", ", subjects);
                    }

                    Long exp = tutor.getLong("experienceYears");
                    Long salary = tutor.getLong("expectedSalary");

                    String appId = applicationId(post);
                    Map<String, Object> app = new HashMap<>();
                    app.put("applicationId", appId);
                    app.put("postId", post.getPostId());
                    app.put("postTitle", post.getTitle());
                    app.put("parentId", post.getParentId());
                    app.put("parentName", post.getParentName());
                    app.put("parentPhone", post.getParentPhone());
                    app.put("tutorId", user.getUid());
                    app.put("tutorName", tutor.getString("name"));
                    app.put("tutorPhone", tutor.getString("phone"));
                    app.put("tutorEducation", tutor.getString("education"));
                    app.put("tutorSubjects", subjectsText);
                    app.put("tutorExperience", exp == null ? 0L : exp);
                    app.put("tutorExpectedSalary", salary == null ? 0L : salary);
                    app.put("message", message);
                    app.put("status", "pending");
                    app.put("createdAt", System.currentTimeMillis());

                    db.collection("applications").document(appId).set(app)
                            .addOnSuccessListener(unused -> {
                                NotificationHelper.send(post.getParentId(),
                                        tutor.getString("name") + " আপনার পোস্টে আবেদন করেছেন: " + post.getTitle());
                                Toast.makeText(this, "আবেদন পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show();
                                btnApply.setText("আবেদন করা হয়েছে ✔");
                            })
                            .addOnFailureListener(e -> {
                                btnApply.setEnabled(true);
                                Toast.makeText(this, "আবেদন ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnApply.setEnabled(true);
                    Toast.makeText(this, "ডাটা আনতে সমস্যা: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void deletePost() {
        db.collection("posts").document(postId).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "পোস্ট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "মুছতে ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}