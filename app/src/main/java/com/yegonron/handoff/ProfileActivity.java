package com.yegonron.handoff;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    // Declare instances if the views
    private EditText profUserName, profSurName, profFirstName, profLastName, profDateOfBirth, profPhoneNo, profTeamName, profPlayerPosition, profKitSize, profBootSize;
    private ImageButton imageButton;

    //Declare an Instance of the database reference  where we will be saving the profile photo and custom display name
    private DatabaseReference mDatabaseUser;
    //Declare an Instance of the Storage reference where we will upload the photo
    private StorageReference mStorageRef;
    // Declare an Instance of URI for getting the image from our phone, initialize it to null
    private Uri profileImageUri = null;
    //  since we want to get a result (getting and setting image) we will start the implicit intent using the method startActivityForResult()
    //startActivityForResult require two arguments the intent and the request code

    // Declare  and initialize  a private final static int  that will serve as our request code
    private final static int GALLERY_REQ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //inflate the tool bar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //Initialize the  instances of the views
        profUserName = findViewById(R.id.profUserName);
        profSurName = findViewById(R.id.profSurName);
        profFirstName = findViewById(R.id.profFirstName);
        profLastName = findViewById(R.id.profLastName);
        profDateOfBirth = findViewById(R.id.profDateOfBirth);
        profPhoneNo = findViewById(R.id.profPhoneNo);
        profTeamName = findViewById(R.id.profTeamName);
        profPlayerPosition = findViewById(R.id.profPlayerPosition);
        profKitSize = findViewById(R.id.profKitSize);
        profBootSize = findViewById(R.id.profBootSize);
        imageButton = findViewById(R.id.imagebutton);
        Button doneBtn = findViewById(R.id.doneBtn);
        //Initialize the instance of Firebase authentications
        // Declare an instance of firebase authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //We want to set the image and  on a specific user_ID we registered , hence get the user id of the current user and assign it to a string variable
        final String userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        //Initialize the database reference where you have your registered users
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        //Initialize the firebase storage reference where you will store the profile  photo images
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        //set on click listener on the image button so as to allow users to pick their  profile photo from their gallery
        imageButton.setOnClickListener(view -> {
            // create an implicit intent for getting the images
            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
            //set the type to images only
            galleryIntent.setType("image/*");
            //since we need results, use the method  startActivityForResult() and pass the intent and request code you initialized
            startActivityForResult(galleryIntent, GALLERY_REQ);
        });
        // on clicking the images we want to get the name and the profile photo, then later save this on a database reference for users
        doneBtn.setOnClickListener(view -> {
            //get the custom display name entered by the user
            final String userName = profUserName.getText().toString().trim();
            final String surName = profSurName.getText().toString().trim();
            final String firstName = profFirstName.getText().toString().trim();
            final String lastName = profLastName.getText().toString().trim();
            final String dateOfBirth = profDateOfBirth.getText().toString().trim();
            final String phoneNo = profPhoneNo.getText().toString().trim();
            final String teamName = profTeamName.getText().toString().trim();
            final String playerPosition = profPlayerPosition.getText().toString().trim();
            final String kitSize = profKitSize.getText().toString().trim();
            final String bootSize = profBootSize.getText().toString().trim();
            final String timeStamp = "" + System.currentTimeMillis();

            //validate to ensure that the name and profile image are not null
            if (!TextUtils.isEmpty(userName) && profileImageUri != null) {

                //create Storage reference node, inside profile_image storage reference where you will save the profile image
                StorageReference profileImagePath = mStorageRef.child("profile_images").child(profileImageUri.getLastPathSegment());
                //call the putFile() method passing the profile image the user set on the storage reference where you are uploading the image
                //further call addOnSuccessListener on the reference to listen if the upload task was successful,and get a snapshot of the task
                profileImagePath.putFile(profileImageUri).addOnSuccessListener(taskSnapshot -> {
                    //if the upload of the profile image was successful get the download url
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            //get the download url from your storage use the methods getStorage() and getDownloadUrl()
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            //call the method addOnSuccessListener to determine if we got the download url
                            result.addOnSuccessListener(uri -> {
                                //convert the uri to a string on success
                                final String profileImage = uri.toString();
                                // call the method push() to add values on the database reference of  a specif user
                                mDatabaseUser.push();
                                //call the method addValueEventListener to publish the additions in  the database reference of a specific user
                                mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //add data for the current user
                                        mDatabaseUser.child("displayName").setValue(userName);
                                        mDatabaseUser.child("surName").setValue(surName);
                                        mDatabaseUser.child("firstName").setValue(firstName);
                                        mDatabaseUser.child("lastName").setValue(lastName);
                                        mDatabaseUser.child("dateOfBirth").setValue(dateOfBirth);
                                        mDatabaseUser.child("phoneNo").setValue(phoneNo);
                                        mDatabaseUser.child("teamName").setValue(teamName);
                                        mDatabaseUser.child("playerPosition").setValue(playerPosition);
                                        mDatabaseUser.child("bootSize").setValue(bootSize);
                                        mDatabaseUser.child("kitSize").setValue(kitSize);
                                        mDatabaseUser.child("timeStamp").setValue(timeStamp);
                                        mDatabaseUser.child("online").setValue("true");
                                        mDatabaseUser.child("accountType").setValue("Player");
                                        mDatabaseUser.child("profilePhoto").setValue(profileImage).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                //show a toast to indicate the profile was updated
                                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                //launch the login activity
                                                Intent login = new Intent(ProfileActivity.this, LoginActivity.class);

                                                startActivity(login);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            });
                        }
                    }

                });

            }
        });

    }

    @Override
    //override this method to get the  profile image and  set it in the image button view
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK) {
            //get the image selected by the user
            profileImageUri = data.getData();
            //set in the image button view
            imageButton.setImageURI(profileImageUri);
        }
    }
}