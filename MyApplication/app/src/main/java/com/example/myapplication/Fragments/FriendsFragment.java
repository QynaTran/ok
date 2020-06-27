package com.example.myapplication.Fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Controller.ChatActivity;
import com.example.myapplication.Model.Friends;
import com.example.myapplication.Controller.ProfileActivity;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment{
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;
    private DatabaseReference mFriendsDatabase, mUsersDatabase, mRootRef;

    private RecyclerView mFriendsList;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth= FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();

        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }
    @Override
    public void onStart() {
        super.onStart();

        final FirebaseRecyclerOptions<Friends> options=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mFriendsDatabase, Friends.class).build();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> adapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {
                       friendsViewHolder.userStatusView.setText(friends.getDate());
                       final String list_user_id=getRef(i).getKey();

                       mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                               final String name=dataSnapshot.child("name").getValue().toString();
                               String thumb=dataSnapshot.child("thump_image").getValue().toString();


                               friendsViewHolder.userNameView.setText(name);
                               Picasso.get().load(thumb).placeholder(R.drawable.icon_profile).into(friendsViewHolder.userImageView);

                               if(dataSnapshot.hasChild("online")){
                                   String online=dataSnapshot.child("online").getValue().toString();
                                   if(online.equals("true")){
                                       friendsViewHolder.userOnlineView.setVisibility(View.VISIBLE);
                                   }else {
                                       friendsViewHolder.userOnlineView.setVisibility(View.INVISIBLE);
                                   }
                               }

                               friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       CharSequence options[] = new CharSequence[]{"Open Profile", "Send message"};
                                       AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                       builder.setTitle("Select Options");
                                       builder.setItems(options, new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialog, int which) {
                                               //Click event for each item
                                               if (which == 0) {
                                                   Intent profileIntent=new Intent(getContext(), ProfileActivity.class);
                                                   profileIntent.putExtra("user_id",list_user_id);
                                                   startActivity(profileIntent);
                                               }
                                               if (which == 1) {
                                                   Intent chatIntent=new Intent(getContext(), ChatActivity.class);
                                                   chatIntent.putExtra("user_id",list_user_id);
                                                   chatIntent.putExtra("user_name",name);
                                                   startActivity(chatIntent);
                                               }
                                           }
                                       });
                                       builder.show();
                                       
                                   }
                               });
                           }
                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout,parent,false);
                FriendsViewHolder viewHolder=new FriendsViewHolder(view);
                return  viewHolder;
            }
        };
        mFriendsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView userStatusView,userNameView;
        CircleImageView userImageView;
        ImageView userOnlineView;


        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            userOnlineView=(ImageView)mView.findViewById(R.id.user_single_icon);
        }
    }
}
