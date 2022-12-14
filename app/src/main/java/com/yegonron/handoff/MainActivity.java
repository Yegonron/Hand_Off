package com.yegonron.handoff;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference likesRef;
    private FirebaseAuth mAuth;
    // private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    Boolean likeChecker = false;
    private FirebaseRecyclerAdapter adapter;
    String currentUserID = null;

    private TextView postAdder;
    // FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inflate the tool bar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //initialize recyclerview
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //Reverse  the layout so as to display the most recent post at the top
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        //get the database reference where you will fetch posts
        // mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        //Initialize the database reference where you will store likes
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        //get an instance of firebase authentication
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {

            Intent loginIntent = new Intent(MainActivity.this, RegisterActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }

    }

    @Override
    protected void onStart() {
        //
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //if user is logged in populate the Ui With card views
            updateUI(currentUser);
            adapter.startListening();

        }

    }

    private void updateUI(final FirebaseUser currentUser) {
        //create and initialize an instance of Query that retrieves all posts uploaded
        Query query = FirebaseDatabase.getInstance().getReference().child("Posts");
        // Create and initialize and instance of Recycler Options passing in your model class and
        //Create a snap shot of your model
        FirebaseRecyclerOptions<Post> options = new FirebaseRecyclerOptions.Builder<Post>().setQuery(query, snapshot -> new Post(Objects.requireNonNull(snapshot.child("title").getValue()).toString(), Objects.requireNonNull(snapshot.child("desc").getValue()).toString(), Objects.requireNonNull(snapshot.child("postImage").getValue()).toString(), Objects.requireNonNull(snapshot.child("displayName").getValue()).toString(), Objects.requireNonNull(snapshot.child("profilePhoto").getValue()).toString(), Objects.requireNonNull(snapshot.child("time").getValue()).toString(), Objects.requireNonNull(snapshot.child("date").getValue()).toString())).build();
        // crate a fire base adapter passing in the model, an a View holder
        // Create a  new ViewHolder as a public inner class that extends RecyclerView.Holder, outside the create , start and update the Ui methods.
        //Then implement the methods onCreateViewHolder and onBindViewHolder
        //Complete all the steps in the AtticViewHolder before proceeding to  the methods onCreateViewHolder, and onBindViewHolder
        adapter = new FirebaseRecyclerAdapter<Post, AtticViewHolder>(options) {


            @NonNull
            @Override
            public AtticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //inflate the layout where you have the card view items
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_items, parent, false);
                return new AtticViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull AtticViewHolder holder, int position, @NonNull Post model) {
                // very important for you to get the post key since we will use this to set likes and delete a o particular post
                final String post_key = getRef(position).getKey();
                //populate the card views with data
                holder.setTitle(model.getTitle());
                holder.setDesc(model.getDesc());
                holder.setPostImage(getApplicationContext(), model.getPostImage());
                holder.setUserName(model.getDisplayName());
                holder.setProfilePhoto(getApplicationContext(), model.getProfilePhoto());
                //set a like on a particular post

                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setLikeButtonStatus(post_key);
                holder.commentPostButton.setOnClickListener(v -> {
                    Intent singleActivity = new Intent(MainActivity.this, SinglePostActivity.class);
                    singleActivity.putExtra("PostID", post_key);
                    startActivity(singleActivity);

                });
                //add  on click listener on the a particular post to  allow opening this post on a different screen
                holder.post_layout.setOnClickListener(view -> {
                    //launch the screen single post activity on clicking a particular cardview item
                    //create this activity using the empty activity template
                    Intent singleActivity = new Intent(MainActivity.this, SinglePostActivity.class);
                    singleActivity.putExtra("PostID", post_key);
                    startActivity(singleActivity);
                });
                // set the onclick listener on the button for liking a post
                holder.likePostButton.setOnClickListener(v -> {
                    // initialize the like checker to true, we are using this boolean variable to determine if a post has been liked or dislike
                    // we declared this variable on to of our activity class
                    likeChecker = true;
                    //check the currently logged in user using his/her ID
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        currentUserID = user.getUid();
                    } else {
                        Toast.makeText(MainActivity.this, "please login", Toast.LENGTH_SHORT).show();

                    }
                    //Listen to changes in the likes database reference
                    likesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (likeChecker.equals(true)) {
                                // if the current post has a like, associated to the current logged and the user clicks on it again, remove the like
                                //basically the user is disliking
                                if (dataSnapshot.child(post_key).hasChild(currentUserID)) {
                                    likesRef.child(post_key).child(currentUserID).removeValue();
                                    likeChecker = false;
                                } else {
                                    //here the user is liking, set value on the like
                                    likesRef.child(post_key).child(currentUserID).setValue(true);
                                    likeChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                });
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            adapter.stopListening();

        }

    }


    public class AtticViewHolder extends RecyclerView.ViewHolder {
        //Declare the view objects in the card view
        public final TextView post_title;
        public final TextView post_desc;
        public final ImageView post_image;
        public final TextView postUserName;
        public final ImageView user_image;
        public final TextView postTime;
        public final TextView postDate;
        public final LinearLayout post_layout;
        public final ImageButton likePostButton;
        public final ImageButton commentPostButton;
        public final TextView displayLikes;

        //Declare an int variable to hold the count  of likes
        int countLikes;
        //Declare a string variable to hold  the user ID of currently logged in user
        String currentUserID;
        //Declare an instance of firebase authentication
        FirebaseAuth mAuth;
        //Declare a database reference where you are saving  the likes
        final DatabaseReference likesRef;

        //create constructor matching super
        public AtticViewHolder(@NonNull View itemView) {
            super(itemView);
            //Initialize the card view item objects
            post_title = itemView.findViewById(R.id.post_title_txtview);
            post_desc = itemView.findViewById(R.id.post_desc_txtview);
            post_image = itemView.findViewById(R.id.post_image);
            postUserName = itemView.findViewById(R.id.post_user);
            user_image = itemView.findViewById(R.id.userImage);
            postTime = itemView.findViewById(R.id.time);
            postDate = itemView.findViewById(R.id.date);
            post_layout = itemView.findViewById(R.id.linear_layout_post);
            likePostButton = itemView.findViewById(R.id.like_button);
            commentPostButton = itemView.findViewById(R.id.comment);
            displayLikes = itemView.findViewById(R.id.likes_display);

            //Initialize a database reference where you will store  the likes
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        }

        // create yos setters, you will use this setter in you onBindViewHolder method
        public void setTitle(String title) {

            post_title.setText(title);
        }

        public void setDesc(String desc) {

            post_desc.setText(desc);
        }

        public void setPostImage(Context ctx, String postImage) {

            Picasso.get().load(postImage).into(post_image);
        }

        public void setUserName(String userName) {

            postUserName.setText(userName);
        }

        public void setProfilePhoto(Context context, String profilePhoto) {
            Picasso.get().load(profilePhoto).into(user_image);

        }

        public void setTime(String time) {
            postTime.setText(time);
        }

        public void setDate(String date) {
            postDate.setText(date);
        }

        public void setLikeButtonStatus(final String post_key) {
            //we want to know who has like a particular post, so let's get the user using their user_ID
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                currentUserID = user.getUid();
            } else {
                Toast.makeText(MainActivity.this, "please login", Toast.LENGTH_SHORT).show();
            }

            // Listen to changes in the database reference of Likes
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //define post_key in the in the onBindViewHolder method
                    //check if a particular post has been liked
                    if (dataSnapshot.child(post_key).hasChild(currentUserID)) {
                        //if liked get the number of likes
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        //check the image from initiali sislike to like
                        likePostButton.setImageResource(R.drawable.like);
                        // count the like and display them in the textView for likes
                        displayLikes.setText(Integer.toString(countLikes));
                    } else {
                        //If disliked, get the current number of likes
                        countLikes = (int) dataSnapshot.child(post_key).getChildrenCount();
                        // set the image resource as disliked
                        likePostButton.setImageResource(R.drawable.dislike);
                        //display the current number of likes
                        displayLikes.setText(Integer.toString(countLikes));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //implement the functionality of the add icon, so that the user on clicking it launches the post activity
        if (id == R.id.action_add) {
            Intent postIntent = new Intent(this, PostActivity.class);
            startActivity(postIntent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        } else if (id == R.id.attendance) {
            Intent intent1 = new Intent(MainActivity.this, AttendanceActivity.class);
            startActivity(intent1);
        } else if (id == R.id.gameFixtures) {
            Intent intent2 = new Intent(MainActivity.this, GameFixturesActivity.class);
            startActivity(intent2);
        } else if (id == R.id.leagueTable) {
            Intent intent3 = new Intent(MainActivity.this, LeagueTableActivity.class);
            startActivity(intent3);
        } else if (id == R.id.livestreamGames) {
            Intent intent4 = new Intent(MainActivity.this, LivestreamGamesActivity.class);
            startActivity(intent4);
        } else if (id == R.id.settings) {
            Intent intent5 = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent5);
        } else if (id == R.id.help) {
            Intent intent6 = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent6);
        } else if (id == R.id.About_Us) {
            Intent intent7 = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent7);
        } else if (id == R.id.logout) {
            mAuth.signOut();
            Intent logouIntent = new Intent(MainActivity.this, LoginActivity.class);
            logouIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(logouIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}