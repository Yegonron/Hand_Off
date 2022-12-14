package com.yegonron.handoff;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPass;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //inflate the tool bar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //Initialize the views
        Button loginBtn = findViewById(R.id.loginBtn);
        loginEmail =findViewById(R.id.login_email);
        loginPass = findViewById(R.id.login_password);
        TextView signUp = findViewById(R.id.signUpTxtView);
        TextView forgotTV = findViewById(R.id.forgotTv);
        //Initialize the Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance();
        //Initialize the database reference where you have the child node Users
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        //if user is not registered , register him/her
        signUp.setOnClickListener(v -> {
            Intent register = new Intent(LoginActivity.this, RegisterActivity.class);

            startActivity(register);
        });

        //Set on click listener on the login button
        loginBtn.setOnClickListener(view -> {
            Toast.makeText(LoginActivity.this, "PROCESSING....", Toast.LENGTH_LONG).show();
            // get the email and password entered by the user
            String email = loginEmail.getText().toString().trim();
            String password = loginPass.getText().toString().trim();

            if (!TextUtils.isEmpty(email)&& !TextUtils.isEmpty(password)){
                // use firebase authentication instance you create and call the method signInWithEmailAndPassword method passing the email and password you got from the views
                //Further call the addOnCompleteListener() method to handle the Authentication result
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //create a method that will check if the user exists in our database reference
                        checkUserExistence();
                    } else {
                        //if the user does not exit in the database reference throw a toast
                        Toast.makeText(LoginActivity.this, "Couldn't login, User not found", Toast.LENGTH_SHORT).show();
                    }
                });
            }else {
                //if the fields for email and password where not completed show a toast
                Toast.makeText(LoginActivity.this, "Complete all fields", Toast.LENGTH_SHORT).show();
            }
        });

        forgotTV.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class)));

    }
    //check if the user exists
    public void checkUserExistence(){
        //check the user existence of the user using the user id in users database reference
        final String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        //call the method addValueEventListener on the database reference of the user to determine if the current userID supplied exists in our database reference
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get a dataSnapshot of the users database reference to determine if current user exists

                if (dataSnapshot.hasChild(user_id)){
                    //if the users exists direct the user to the Main Activity
                    Intent mainPage = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mainPage);
                }else {
                    //if the user id does not exist show a toast
                    Toast.makeText(LoginActivity.this, "User not registered!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}