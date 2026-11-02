package com.shakibul.tuitionfinder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shakibul.tuitionfinder.R;
import com.shakibul.tuitionfinder.models.TuitionPost;

import java.util.List;

public class TuitionAdapter extends RecyclerView.Adapter<TuitionAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(TuitionPost post);
    }

    private final List<TuitionPost> posts;
    private final OnItemClickListener listener;

    public TuitionAdapter(List<TuitionPost> posts, OnItemClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tuition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TuitionPost post = posts.get(position);

        holder.tvTitle.setText(post.getTitle());
        holder.tvClassSubjects.setText("ক্লাস: " + post.getStudentClass() + "  |  " + post.getSubjects());
        holder.tvArea.setText("এলাকা: " + post.getArea());
        holder.tvSalary.setText("বেতন: " + post.getSalary() + " টাকা/মাস");

        holder.itemView.setOnClickListener(v -> listener.onItemClick(post));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvClassSubjects, tvArea, tvSalary;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvClassSubjects = itemView.findViewById(R.id.tvClassSubjects);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvSalary = itemView.findViewById(R.id.tvSalary);
        }
    }
}