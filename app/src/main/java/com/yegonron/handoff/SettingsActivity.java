package com.yegonron.handoff;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingsActivity extends AppCompatActivity {

    private EditText passwordEt;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton backBtn = findViewById(R.id.backBtn);
        Button changePasswordBtn = findViewById(R.id.changePasswordBtn);
        Button deleteBtn = findViewById(R.id.deleteBtn);

        passwordEt = findViewById(R.id.passwordEt);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        backBtn.setOnClickListener(v -> onBackPressed());

        changePasswordBtn.setOnClickListener(v -> {
            //change user password
            changeUserPassword();
        });

        deleteBtn.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this);
            dialog.setTitle("Are you sure?");
            dialog.setMessage("Deleting this account will result in completely removing your" +
                    " account from the system and you won't be able to access the app.");
            dialog.setPositiveButton("Delete", (dialogInterface, i) -> deleteCurrentUser());

            dialog.setNegativeButton("Dismiss", (dialogInterface, i) -> dialogInterface.dismiss());
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        });

    }

    private void changeUserPassword() {
        //input data
        String password = passwordEt.getText().toString().trim();
//        String confirmPassword = cPasswordEt.getText().toString().trim();

        //validate data

        progressDialog.setMessage("Changing password...");
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        user.updatePassword(password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                    }
                });


    }

    public boolean isValidPassword(final String password) {
        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }


    private void deleteCurrentUser() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Account being deleted");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .setValue(null)
                .addOnSuccessListener(avoid -> FirebaseAuth.getInstance().getCurrentUser().delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                startActivity(intent);

                            }
                        }));
    }
}
