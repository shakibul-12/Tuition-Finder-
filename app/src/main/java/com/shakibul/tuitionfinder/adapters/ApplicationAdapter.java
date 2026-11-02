package com.shakibul.tuitionfinder.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.shakibul.tuitionfinder.R;
import com.shakibul.tuitionfinder.models.Application;

import java.util.List;
import java.util.Locale;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    public interface ActionListener {
        void onAccept(Application app);
        void onReject(Application app);
        void onReview(Application app);
    }

    private final List<Application> list;
    private final boolean parentView;   // true = অভিভাবক দেখছেন, false = শিক্ষক দেখছেন
    private final ActionListener listener;

    public ApplicationAdapter(List<Application> list, boolean parentView, ActionListener listener) {
        this.list = list;
        this.parentView = parentView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Application a = list.get(position);
        boolean accepted = "accepted".equals(a.getStatus());
        boolean pending = "pending".equals(a.getStatus());
        String phoneToCall;

        h.tvRating.setText("");
        h.tvRating.setVisibility(View.GONE);

        if (parentView) {
            // অভিভাবক শিক্ষকের তথ্য দেখছেন
            h.tvTitle.setText(a.getTutorName());
            h.tvInfo1.setText("যোগ্যতা: " + a.getTutorEducation()
                    + "  |  অভিজ্ঞতা: " + a.getTutorExperience() + " বছর");
            h.tvInfo2.setText("বিষয়: " + a.getTutorSubjects()
                    + "  |  প্রত্যাশিত বেতন: " + a.getTutorExpectedSalary() + " টাকা");
            h.tvInfo2.setVisibility(View.VISIBLE);

            h.tvContact.setText(accepted
                    ? "শিক্ষকের ফোন: " + a.getTutorPhone()
                    : "ফোন নম্বর গ্রহণ করার পর দেখা যাবে");

            h.layoutButtons.setVisibility(pending ? View.VISIBLE : View.GONE);
            phoneToCall = a.getTutorPhone();

            loadRating(h, a.getTutorId());
        } else {
            // শিক্ষক নিজের আবেদন দেখছেন
            h.tvTitle.setText(a.getPostTitle());
            h.tvInfo1.setText("অভিভাবক: " + a.getParentName());
            h.tvInfo2.setVisibility(View.GONE);

            h.tvContact.setText(accepted
                    ? "অভিভাবকের ফোন: " + a.getParentPhone()
                    : "অভিভাবক গ্রহণ করলে ফোন নম্বর দেখতে পাবেন");

            h.layoutButtons.setVisibility(View.GONE);
            phoneToCall = a.getParentPhone();
        }

        String msg = a.getMessage();
        if (msg == null || msg.isEmpty()) {
            h.tvMessage.setVisibility(View.GONE);
        } else {
            h.tvMessage.setVisibility(View.VISIBLE);
            h.tvMessage.setText("বার্তা: " + msg);
        }

        if (accepted) {
            h.tvStatus.setText("অবস্থা: গ্রহণ করা হয়েছে ✔");
            h.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("rejected".equals(a.getStatus())) {
            h.tvStatus.setText("অবস্থা: বাতিল করা হয়েছে");
            h.tvStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            h.tvStatus.setText("অবস্থা: অপেক্ষমাণ");
            h.tvStatus.setTextColor(Color.parseColor("#EF6C00"));
        }

        // কল ও SMS বাটন: শুধু গ্রহণের পর
        final String phone = phoneToCall;
        h.layoutContactButtons.setVisibility(accepted && phone != null ? View.VISIBLE : View.GONE);
        h.btnCall.setOnClickListener(v -> openDialer(v.getContext(), phone));
        h.btnSms.setOnClickListener(v -> openSms(v.getContext(), phone));

        // রিভিউ বাটন: অভিভাবক, গ্রহণের পর, একবারই
        if (parentView && accepted) {
            if (a.isReviewed()) {
                h.btnReview.setVisibility(View.GONE);
                h.tvStatus.setText("অবস্থা: গ্রহণ করা হয়েছে ✔  |  রিভিউ দেওয়া হয়েছে");
            } else {
                h.btnReview.setVisibility(View.VISIBLE);
            }
        } else {
            h.btnReview.setVisibility(View.GONE);
        }

        h.btnAccept.setOnClickListener(v -> listener.onAccept(a));
        h.btnReject.setOnClickListener(v -> listener.onReject(a));
        h.btnReview.setOnClickListener(v -> listener.onReview(a));
    }

    // শিক্ষকের গড় রেটিং এনে দেখানো
    private void loadRating(ViewHolder h, String tutorId) {
        if (tutorId == null) return;
        h.tvRating.setTag(tutorId);

        FirebaseFirestore.getInstance().collection("users").document(tutorId).get()
                .addOnSuccessListener(doc -> {
                    // কার্ড অন্য আইটেমে ব্যবহার হয়ে গেলে বাদ
                    if (!tutorId.equals(h.tvRating.getTag())) return;

                    Long count = doc.getLong("ratingCount");
                    Long sum = doc.getLong("ratingSum");
                    h.tvRating.setVisibility(View.VISIBLE);
                    if (count != null && count > 0 && sum != null) {
                        h.tvRating.setText(String.format(Locale.US,
                                "★ %.1f (%d টি রিভিউ)", sum / (double) count, count));
                    } else {
                        h.tvRating.setText("এখনো কোনো রিভিউ নেই");
                    }
                });
    }

    private void openDialer(Context ctx, String phone) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        } catch (Exception e) {
            Toast.makeText(ctx, "কল অ্যাপ খোলা যায়নি", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSms(Context ctx, String phone) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone)));
        } catch (Exception e) {
            Toast.makeText(ctx, "মেসেজ অ্যাপ খোলা যায়নি", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRating, tvInfo1, tvInfo2, tvMessage, tvStatus, tvContact;
        View layoutButtons, layoutContactButtons;
        Button btnAccept, btnReject, btnCall, btnSms, btnReview;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvInfo1 = itemView.findViewById(R.id.tvInfo1);
            tvInfo2 = itemView.findViewById(R.id.tvInfo2);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvContact = itemView.findViewById(R.id.tvContact);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
            layoutContactButtons = itemView.findViewById(R.id.layoutContactButtons);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnSms = itemView.findViewById(R.id.btnSms);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}