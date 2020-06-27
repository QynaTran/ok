package com.example.myapplication.Fragments;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Model.Users;
import com.example.myapplication.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private View mMainView;

    private RecyclerView mRequestList;

    private FirebaseAuth mAuth;
    private DatabaseReference mRequestDatabase, mUserDatabase, mFriendReqDatabase, mRootRef;

    private String mCurrent_user_id;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList=(RecyclerView)mMainView.findViewById(R.id.request_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mRequestDatabase.keepSynced(true);

        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mRootRef= FirebaseDatabase.getInstance().getReference();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(mRequestDatabase, Users.class).build();

        FirebaseRecyclerAdapter<Users, RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Users, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Users users) {
                final String list_user_id = getRef(i).getKey();

                final DatabaseReference get_type_ref=getRef(i).child("request_type").getRef();

                get_type_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String request_type=dataSnapshot.getValue().toString();
                            if (request_type.equals("received")) {
                                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String name=dataSnapshot.child("name").getValue().toString();
                                        String image=dataSnapshot.child("thump_image").getValue().toString();

                                        requestViewHolder.name.setText(name);
                                        Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(requestViewHolder.image);
                                        if(dataSnapshot.hasChild("online")){
                                            String online=dataSnapshot.child("online").getValue().toString();
                                            if(online.equals("true")){
                                                requestViewHolder.online.setVisibility(View.VISIBLE);
                                            }else {
                                                requestViewHolder.online.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                                        Map friendsMap=new HashMap();
                                        friendsMap.put("Friends/" + mCurrent_user_id + "/" + list_user_id + "/date", currentDate);
                                        friendsMap.put("Friends/" + list_user_id + "/" + mCurrent_user_id + "/date", currentDate);

                                        friendsMap.put("Friend_req/" + mCurrent_user_id + "/" + list_user_id, null);
                                        friendsMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user_id, null);

                                        mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                if(databaseError!=null){
                                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(getContext(), "Accept Request Successful", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });

                                requestViewHolder.cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mRequestDatabase.child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mFriendReqDatabase.child(list_user_id).child(mCurrent_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Cancel Request Successful", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                            } else if (request_type.equals("sent")) {
                                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String name=dataSnapshot.child("name").getValue().toString();
                                        String image=dataSnapshot.child("thump_image").getValue().toString();

                                        requestViewHolder.name.setText(name);
                                        Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(requestViewHolder.image);
                                        if(dataSnapshot.hasChild("online")){
                                            String online=dataSnapshot.child("online").getValue().toString();
                                            if(online.equals("true")){
                                                requestViewHolder.online.setVisibility(View.VISIBLE);
                                            }else {
                                                requestViewHolder.online.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                requestViewHolder.cancel.setVisibility(View.INVISIBLE);
                                requestViewHolder.accept.setText("Cancel");
                                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.register_bg);
                                requestViewHolder.accept.setBackground(drawable);
                                requestViewHolder.accept.setTextColor(Color.BLACK);
                                requestViewHolder.accept.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mRequestDatabase.child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mFriendReqDatabase.child(list_user_id).child(mCurrent_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Cancel Request Complete", Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_single_layout, parent, false);
                RequestViewHolder requestViewHolder=new RequestViewHolder(view);
                return requestViewHolder;
            }
        };
        mRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        TextView name;
        CircleImageView image;
        ImageView online;
        Button accept, cancel;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            name = mView.findViewById(R.id.request_single_name);
            image = mView.findViewById(R.id.request_single_image);
            online = mView.findViewById(R.id.request_single_icon);
            accept = mView.findViewById(R.id.request_single_accept);
            cancel = mView.findViewById(R.id.request_single_decline);

        }
    }
}
