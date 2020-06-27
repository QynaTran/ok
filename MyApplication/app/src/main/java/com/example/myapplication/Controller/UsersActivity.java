package com.example.myapplication.Controller;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Users;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

    private String calledBy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar=(Toolbar)findViewById(R.id.users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth= FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();

        String id=mCurrentUser.getUid();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList=(RecyclerView)findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }



    @Override
    protected void onStart() {
        super.onStart();
        //checkForReceivingCall();
        
        FirebaseRecyclerOptions<Users> options=new FirebaseRecyclerOptions.Builder<Users>().setQuery(mUsersDatabase,Users.class).build();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> adapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.name.setText(users.getName());
                usersViewHolder.status.setText(users.getStatus());
                Picasso.get().load(users.getThump_image()).placeholder(R.drawable.icon_profile).into(usersViewHolder.image);

                final String user_id=getRef(i).getKey();
                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent=new Intent(getApplicationContext(),ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                UsersViewHolder viewHolder=new UsersViewHolder(view);
                return  viewHolder;
            }


        };
        mUsersList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class UsersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView name, status;
        CircleImageView image;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            name=itemView.findViewById(R.id.user_single_name);
            status=itemView.findViewById(R.id.user_single_status);
            image=itemView.findViewById(R.id.user_single_image);
        }
    }

    private void checkForReceivingCall() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("CallFrom")) {
                    if(dataSnapshot.child("CallFrom").hasChild("fromID")){
                        Toast.makeText(getApplicationContext(), "received", Toast.LENGTH_SHORT).show();
                        calledBy = dataSnapshot.child("CallFrom").child("fromID").getValue().toString();
                        Intent i = new Intent(getApplicationContext(), CallRingActivity.class);
                        i.putExtra("received_user_id", calledBy);
                        startActivity(i);
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
