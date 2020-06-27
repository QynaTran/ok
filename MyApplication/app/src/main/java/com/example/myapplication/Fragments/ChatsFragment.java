package com.example.myapplication.Fragments;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
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
import com.example.myapplication.Model.Conversation;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView nConvList;

    private DatabaseReference nConvDatabase, mMessageDatabase, mUsersDatabase, mRootRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        nConvList = (RecyclerView) mMainView.findViewById(R.id.chat_list);
        mAuth= FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        nConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        nConvDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        nConvList.setHasFixedSize(true);
        nConvList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = nConvDatabase.orderByChild("timestamp");

        final FirebaseRecyclerOptions<Conversation> options=new FirebaseRecyclerOptions.Builder<Conversation>().setQuery(conversationQuery, Conversation.class).build();

        FirebaseRecyclerAdapter<Conversation, ConvViewHolder> adapter=new FirebaseRecyclerAdapter<Conversation, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, int i, @NonNull final Conversation conversation) {
                final String list_user_id=getRef(i).getKey();
                mMessageDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Messages")) {
                            Query lastMessageQuery=mMessageDatabase.child("Messages").child(mCurrent_user_id).child(list_user_id).limitToLast(1);
                            lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    String data = dataSnapshot.child("message").getValue().toString();

                                    convViewHolder.setMessage(data, conversation.isSeen());


                                    convViewHolder.userTime.setVisibility(View.VISIBLE);
                                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                                    calendar.setTimeInMillis(Long.parseLong(dataSnapshot.child("time").getValue().toString()));
                                    String datetime = DateFormat.format("hh:mm aa", calendar).toString();
                                    convViewHolder.userTime.setText(datetime);

                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name=dataSnapshot.child("name").getValue().toString();
                        String image=dataSnapshot.child("thump_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online=dataSnapshot.child("online").getValue().toString();
                            if(online.equals("true")) {
                                convViewHolder.useronline.setVisibility(View.VISIBLE);
                            }
                        }
                        convViewHolder.userName.setText(name);
                        Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(convViewHolder.userImage);

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("user_id", list_user_id);
                                startActivity(intent);
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
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);
                ConvViewHolder viewHolder = new ConvViewHolder(view);
                return  viewHolder;
            }
        };

        nConvList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView userName, userStatus, userTime;
        CircleImageView userImage;
        ImageView useronline;
        public ConvViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            userName = (TextView) mView.findViewById(R.id.user_single_name);
            userStatus = (TextView) mView.findViewById(R.id.user_single_status);
            userImage = (CircleImageView) mView.findViewById(R.id.user_single_image);
            useronline = (ImageView) mView.findViewById(R.id.user_single_icon);
            userTime = (TextView) mView.findViewById(R.id.user_single_time);

        }

        public void setMessage(String message, boolean isSeen) {
            userStatus.setText(message);
            if (!isSeen) {
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.BOLD);
            }else{
                userStatus.setTypeface(userStatus.getTypeface(), Typeface.NORMAL);
            }
        }
    }
}
