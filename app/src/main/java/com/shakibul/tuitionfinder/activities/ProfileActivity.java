package com.shakibul.tuitionfinder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etArea;
    private TextInputEditText etEducation, etSubjects, etClasses, etExperience, etSalary, etBio;
    private RadioButton rbMale, rbFemale;
    private View layoutTutor;
    private TextView tvRoleInfo, tvRating;    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String uid;
    private boolean isTutor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etArea = findViewById(R.id.etArea);
        etEducation = findViewById(R.id.etEducation);
        etSubjects = findViewById(R.id.etSubjects);
        etClasses = findViewById(R.id.etClasses);
        etExperience = findViewById(R.id.etExperience);
        etSalary = findViewById(R.id.etSalary);
        etBio = findViewById(R.id.etBio);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        layoutTutor = findViewById(R.id.layoutTutor);
        tvRoleInfo = findViewById(R.id.tvRoleInfo);
        tvRating = findViewById(R.id.tvRating);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        btnSave.setEnabled(false);
        loadProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    // Firebase থেকে আগের তথ্য এনে ঘরে বসানো
    private void loadProfile() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);

                    if (!doc.exists()) {
                        Toast.makeText(this, "প্রোফাইল পাওয়া যায়নি", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String role = doc.getString("role");
                    isTutor = "tutor".equals(role);

                    if (isTutor) {
                        layoutTutor.setVisibility(View.VISIBLE);
                        tvRoleInfo.setText("আপনি একজন শিক্ষক");
                    } else {
                        layoutTutor.setVisibility(View.GONE);
                        tvRoleInfo.setText("আপনি একজন অভিভাবক/ছাত্র");
                    }

                    etName.setText(doc.getString("name"));
                    etPhone.setText(doc.getString("phone"));
                    etArea.setText(doc.getString("area"));

                    if (isTutor) {
                        Long rc = doc.getLong("ratingCount");
                        Long rs = doc.getLong("ratingSum");
                        if (rc != null && rc > 0 && rs != null) {
                            tvRating.setText(String.format(java.util.Locale.US,
                                    "★ %.1f (%d টি রিভিউ)", rs / (double) rc, rc));
                            tvRating.setVisibility(View.VISIBLE);
                        }
                        etEducation.setText(doc.getString("education"));
                        etClasses.setText(doc.getString("classes"));
                        etBio.setText(doc.getString("bio"));

                        List<String> subjects = (List<String>) doc.get("subjects");
                        if (subjects != null) {
                            etSubjects.setText(android.text.TextUtils.join(", ", subjects));
                        }

                        Long exp = doc.getLong("experienceYears");
                        if (exp != null) etExperience.setText(String.valueOf(exp));

                        Long salary = doc.getLong("expectedSalary");
                        if (salary != null) etSalary.setText(String.valueOf(salary));

                        if ("female".equals(doc.getString("gender"))) {
                            rbFemale.setChecked(true);
                        } else {
                            rbMale.setChecked(true);
                        }
                    }

                    btnSave.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "ডাটা আনতে সমস্যা: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // নতুন তথ্য Firebase এ সেভ করা
    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String area = etArea.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("নাম দিন");
            return;
        }
        if (phone.length() < 11) {
            etPhone.setError("সঠিক ফোন নম্বর দিন");
            return;
        }
        if (area.isEmpty()) {
            etArea.setError("এলাকা দিন");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("area", area);

        if (isTutor) {
            String education = etEducation.getText().toString().trim();
            String subjectsText = etSubjects.getText().toString().trim();
            String classes = etClasses.getText().toString().trim();
            String bio = etBio.getText().toString().trim();

            if (education.isEmpty()) {
                etEducation.setError("শিক্ষাগত যোগ্যতা দিন");
                return;
            }
            if (subjectsText.isEmpty()) {
                etSubjects.setError("অন্তত একটি বিষয় দিন");
                return;
            }

            // "গণিত, ইংরেজি" -> ["গণিত", "ইংরেজি"]
            List<String> subjects = new ArrayList<>();
            for (String s : subjectsText.split(",")) {
                String t = s.trim();
                if (!t.isEmpty()) subjects.add(t);
            }

            long experience = parseLong(etExperience.getText().toString());
            long salary = parseLong(etSalary.getText().toString());

            data.put("gender", rbFemale.isChecked() ? "female" : "male");
            data.put("education", education);
            data.put("subjects", subjects);
            data.put("classes", classes);
            data.put("experienceYears", experience);
            data.put("expectedSalary", salary);
            data.put("bio", bio);
            data.put("profileComplete", true);
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        db.collection("users").document(uid).update(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "প্রোফাইল সেভ হয়েছে", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "সেভ ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private long parseLong(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}