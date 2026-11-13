package com.shakibul.tuitionfinder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.shakibul.tuitionfinder.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        binding.tvForgot.setOnClickListener(v -> showForgotDialog());
    }

    private void loginUser() {
        String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            binding.etEmail.setError("ইমেইল দিন");
            binding.etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("সঠিক ইমেইল ঠিকানা দিন");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("পাসওয়ার্ড দিন");
            binding.etPassword.requestFocus();
            return;
        }

        setLoadingState(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "লগইন ব্যর্থ: ইমেইল বা পাসওয়ার্ড ভুল হতে পারে", Toast.LENGTH_LONG).show();
                });
    }

    private void setLoadingState(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.tvGoRegister.setEnabled(!isLoading);
        binding.tvForgot.setEnabled(!isLoading);
    }

    // পাসওয়ার্ড ভুলে গেলে: ইমেইল চেয়ে রিসেট লিংক পাঠানো
    private void showForgotDialog() {
        final EditText input = new EditText(this);
        input.setHint("আপনার ইমেইল");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // লগইন ঘরে ইমেইল লেখা থাকলে সেটাই বসিয়ে দেওয়া
        String typed = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
        input.setText(typed);

        FrameLayout container = new FrameLayout(this);
        int marginPx = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = marginPx;
        params.rightMargin = marginPx;
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("পাসওয়ার্ড রিসেট")
                .setMessage("আপনার অ্যাকাউন্টের ইমেইল দিন। সেখানে পাসওয়ার্ড বদলানোর লিংক পাঠানো হবে।")
                .setView(container)
                .setPositiveButton("লিংক পাঠান", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        Toast.makeText(this, "সঠিক ইমেইল দিন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendResetEmail(email);
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void sendResetEmail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "লিংক পাঠানো হয়েছে। ইনবক্স ও স্প্যাম ফোল্ডার দেখুন",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "পাঠানো যায়নি: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}