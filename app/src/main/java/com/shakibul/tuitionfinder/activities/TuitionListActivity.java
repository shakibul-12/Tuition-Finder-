package com.shakibul.tuitionfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.shakibul.tuitionfinder.R;
import com.shakibul.tuitionfinder.adapters.TuitionAdapter;
import com.shakibul.tuitionfinder.models.TuitionPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TuitionListActivity extends AppCompatActivity {

    // "all" = সব খোলা পোস্ট (শিক্ষকের জন্য), "mine" = নিজের পোস্ট (অভিভাবকের জন্য)
    public static final String EXTRA_MODE = "mode";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvListTitle, tvCount;
    private View layoutSearchArea, layoutFilter;
    private Button btnToggleFilter, btnClearFilter;
    private TextInputEditText etSearch, etFilterArea, etFilterClass, etMinSalary, etMaxSalary;

    // allPosts = Firebase থেকে আসা সব পোস্ট, posts = ছেঁকে নেওয়া পোস্ট (যা স্ক্রিনে দেখায়)
    private final List<TuitionPost> allPosts = new ArrayList<>();
    private final List<TuitionPost> posts = new ArrayList<>();
    private TuitionAdapter adapter;
    private String mode = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuition_list);

        String m = getIntent().getStringExtra(EXTRA_MODE);
        if (m != null) mode = m;

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvListTitle = findViewById(R.id.tvListTitle);
        tvCount = findViewById(R.id.tvCount);
        layoutSearchArea = findViewById(R.id.layoutSearchArea);
        layoutFilter = findViewById(R.id.layoutFilter);
        btnToggleFilter = findViewById(R.id.btnToggleFilter);
        btnClearFilter = findViewById(R.id.btnClearFilter);
        etSearch = findViewById(R.id.etSearch);
        etFilterArea = findViewById(R.id.etFilterArea);
        etFilterClass = findViewById(R.id.etFilterClass);
        etMinSalary = findViewById(R.id.etMinSalary);
        etMaxSalary = findViewById(R.id.etMaxSalary);

        boolean isMine = "mine".equals(mode);
        tvListTitle.setText(isMine ? "আমার পোস্ট" : "সব টিউশন");

        // অভিভাবকের "আমার পোস্ট" এ সার্চ দরকার নেই
        layoutSearchArea.setVisibility(isMine ? View.GONE : View.VISIBLE);

        adapter = new TuitionAdapter(posts, post -> {
            Intent intent = new Intent(TuitionListActivity.this, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getPostId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupSearch();
    }

    private void setupSearch() {
        // যেকোনো ঘরে লিখলেই applyFilter() চলবে
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        };

        etSearch.addTextChangedListener(watcher);
        etFilterArea.addTextChangedListener(watcher);
        etFilterClass.addTextChangedListener(watcher);
        etMinSalary.addTextChangedListener(watcher);
        etMaxSalary.addTextChangedListener(watcher);

        btnToggleFilter.setOnClickListener(v -> {
            boolean open = layoutFilter.getVisibility() == View.VISIBLE;
            layoutFilter.setVisibility(open ? View.GONE : View.VISIBLE);
            btnToggleFilter.setText(open ? "ফিল্টার" : "ফিল্টার বন্ধ করুন");
        });

        btnClearFilter.setOnClickListener(v -> {
            etSearch.setText("");
            etFilterArea.setText("");
            etFilterClass.setText("");
            etMinSalary.setText("");
            etMaxSalary.setText("");
        });
    }

    // বিস্তারিত পেজে পোস্ট মুছে ফিরলে লিস্ট নতুন করে আনার জন্য
    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        Query query;
        if ("mine".equals(mode)) {
            query = FirebaseFirestore.getInstance().collection("posts")
                    .whereEqualTo("parentId", user.getUid());
        } else {
            query = FirebaseFirestore.getInstance().collection("posts")
                    .whereEqualTo("status", "open");
        }

        query.get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    allPosts.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        allPosts.add(doc.toObject(TuitionPost.class));
                    }

                    // নতুন পোস্ট আগে দেখানো
                    Collections.sort(allPosts, new Comparator<TuitionPost>() {
                        @Override
                        public int compare(TuitionPost a, TuitionPost b) {
                            return Long.compare(b.getCreatedAt(), a.getCreatedAt());
                        }
                    });

                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "লোড ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // allPosts থেকে শর্ত মেলে এমন পোস্ট বেছে posts এ রাখা
    private void applyFilter() {
        String keyword = text(etSearch);
        String area = text(etFilterArea);
        String cls = text(etFilterClass);
        long minSalary = number(etMinSalary);
        long maxSalary = number(etMaxSalary);

        posts.clear();

        for (TuitionPost p : allPosts) {
            // ১) সার্চ বক্স: শিরোনাম, বিষয়, এলাকা বা ক্লাসের যেকোনো একটায় মিললেই হবে
            if (!keyword.isEmpty()) {
                String all = (safe(p.getTitle()) + " " + safe(p.getSubjects()) + " "
                        + safe(p.getArea()) + " " + safe(p.getStudentClass()))
                        .toLowerCase(Locale.ROOT);
                if (!all.contains(keyword)) continue;
            }

            // ২) এলাকা
            if (!area.isEmpty() && !safe(p.getArea()).toLowerCase(Locale.ROOT).contains(area)) continue;

            // ৩) ক্লাস
            if (!cls.isEmpty() && !safe(p.getStudentClass()).toLowerCase(Locale.ROOT).contains(cls)) continue;

            // ৪) বেতনের সীমা (ঘর খালি থাকলে সেই শর্ত ধরা হবে না)
            if (minSalary > 0 && p.getSalary() < minSalary) continue;
            if (maxSalary > 0 && p.getSalary() > maxSalary) continue;

            posts.add(p);
        }

        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(posts.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText("মোট " + posts.size() + " টি টিউশন পাওয়া গেছে");
    }

    // ঘরের লেখা ছোট হাতের ও স্পেস ছাড়া করে আনা
    private String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim().toLowerCase(Locale.ROOT);
    }

    private long number(TextInputEditText et) {
        try {
            return Long.parseLong(et.getText().toString().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}