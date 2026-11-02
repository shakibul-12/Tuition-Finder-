package com.shakibul.tuitionfinder.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    private ListView listView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("notifications").whereEqualTo("toUserId", user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);

                    List<DocumentSnapshot> docs = new ArrayList<>(snapshot.getDocuments());

                    // নতুন আগে
                    Collections.sort(docs, new Comparator<DocumentSnapshot>() {
                        @Override
                        public int compare(DocumentSnapshot a, DocumentSnapshot b) {
                            Long x = a.getLong("createdAt");
                            Long y = b.getLong("createdAt");
                            return Long.compare(y == null ? 0 : y, x == null ? 0 : x);
                        }
                    });

                    SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
                    List<String> lines = new ArrayList<>();

                    for (DocumentSnapshot d : docs) {
                        boolean unread = Boolean.FALSE.equals(d.getBoolean("read"));
                        Long time = d.getLong("createdAt");
                        String when = time == null ? "" : fmt.format(new Date(time));

                        lines.add((unread ? "🔵 " : "") + d.getString("message") + "\n" + when);

                        // দেখা হয়ে গেলে "পড়া হয়েছে" চিহ্ন
                        if (unread) {
                            d.getReference().update("read", true);
                        }
                    }

                    listView.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1, lines));
                    tvEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "লোড ব্যর্থ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}