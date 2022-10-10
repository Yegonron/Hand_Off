package com.yegonron.handoff;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText emailEt;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        ImageButton backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.emailEt);
        Button recoverBtn = findViewById(R.id.recoverBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(v -> onBackPressed());

        recoverBtn.setOnClickListener(view -> recoverPassword());
    }

    private void recoverPassword() {
        String email = emailEt.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Sending instructions to reset password");
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    // instructions sent
                    progressDialog.dismiss();
                    Toast.makeText(ForgetPasswordActivity.this, "Password reset instructions sent to your email", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // failed sending instructions
                    progressDialog.dismiss();
                    Toast.makeText(ForgetPasswordActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
