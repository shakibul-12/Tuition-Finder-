package com.shakibul.tuitionfinder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etClass, etSubjects, etArea, etSalary, etDays, etDetails;
    private RadioButton rbMale, rbFemale;
    private Button btnPost;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        db = FirebaseFirestore.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etClass = findViewById(R.id.etClass);
        etSubjects = findViewById(R.id.etSubjects);
        etArea = findViewById(R.id.etArea);
        etSalary = findViewById(R.id.etSalary);
        etDays = findViewById(R.id.etDays);
        etDetails = findViewById(R.id.etDetails);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnPost = findViewById(R.id.btnPost);
        progressBar = findViewById(R.id.progressBar);

        btnPost.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String title = etTitle.getText().toString().trim();
        String studentClass = etClass.getText().toString().trim();
        String subjects = etSubjects.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String salaryText = etSalary.getText().toString().trim();
        String daysText = etDays.getText().toString().trim();
        String details = etDetails.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("শিরোনাম দিন");
            return;
        }
        if (studentClass.isEmpty()) {
            etClass.setError("ক্লাস দিন");
            return;
        }
        if (subjects.isEmpty()) {
            etSubjects.setError("বিষয় দিন");
            return;
        }
        if (area.isEmpty()) {
            etArea.setError("এলাকা দিন");
            return;
        }
        if (salaryText.isEmpty()) {
            etSalary.setError("বেতন দিন");
            return;
        }

        long salary = Long.parseLong(salaryText);
        int days = daysText.isEmpty() ? 0 : Integer.parseInt(daysText);
        String gender = rbMale.isChecked() ? "male" : (rbFemale.isChecked() ? "female" : "any");

        progressBar.setVisibility(View.VISIBLE);
        btnPost.setEnabled(false);

        // অভিভাবকের নাম ও ফোন users থেকে এনে পোস্টে বসানো
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(userDoc -> {
                    DocumentReference ref = db.collection("posts").document();

                    Map<String, Object> post = new HashMap<>();
                    post.put("postId", ref.getId());
                    post.put("parentId", user.getUid());
                    post.put("parentName", userDoc.getString("name"));
                    post.put("parentPhone", userDoc.getString("phone"));
                    post.put("title", title);
                    post.put("studentClass", studentClass);
                    post.put("subjects", subjects);
                    post.put("area", area);
                    post.put("gender", gender);
                    post.put("salary", salary);
                    post.put("daysPerWeek", days);
                    post.put("details", details);
                    post.put("status", "open");
                    post.put("createdAt", System.currentTimeMillis());

                    ref.set(post)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "পোস্ট সফল হয়েছে!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnPost.setEnabled(true);
                                Toast.makeText(this, "পোস্ট ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPost.setEnabled(true);
                    Toast.makeText(this, "ইউজার ডাটা আনতে সমস্যা: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}