package com.example.myapplication.Controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class CallRingActivity extends AppCompatActivity {
    private ImageView avatar;
    private TextView name;
    private ImageView accept, cancelRing, cancelCall;

    private DatabaseReference mUserDatabase, mRootRef;
    private String received_user_id;
    private long timeStart;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_ring);

        name = (TextView) findViewById(R.id.call_name);
        avatar = (ImageView) findViewById(R.id.call_image);
        accept = (ImageView) findViewById(R.id.call_accept);
        cancelRing = (ImageView) findViewById(R.id.call_cancel_ring);
        cancelCall = (ImageView) findViewById(R.id.call_cancel_call);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        received_user_id = getIntent().getStringExtra("received_user_id");


        //get info user
        mUserDatabase.child(received_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String received_image = dataSnapshot.child("thump_image").getValue().toString();
                    Picasso.get().load(received_image).placeholder(R.drawable.icon_profile).into(avatar);

                    String received_user_name = dataSnapshot.child("name").getValue().toString();
                    name.setText(received_user_name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //cancelCall
        cancelCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String current_user_ref = "Messages/" + mCurrentUserId + "/" + received_user_id;
                String chat_user_ref = "Messages/" + received_user_id + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(received_user_id).push();
                final String push_id = user_message_push.getKey();

                Map messageCallTo = new HashMap();
                messageCallTo.put("message", "End Call");
                messageCallTo.put("seen", false);
                messageCallTo.put("type", "ChatVideo");
                messageCallTo.put("time", System.currentTimeMillis());
                messageCallTo.put("from",mCurrentUserId);
                messageCallTo.put("to", received_user_id);


                Map messageCallFrom = new HashMap();
                messageCallFrom.put("message", "Missed Call");
                messageCallFrom.put("seen", false);
                messageCallFrom.put("type", "ChatVideo");
                messageCallFrom.put("time", System.currentTimeMillis());
                messageCallFrom.put("from",mCurrentUserId);
                messageCallFrom.put("to", received_user_id);

                HashMap messageAdd = new HashMap();
                messageAdd.put(current_user_ref + "/" + push_id, messageCallTo);
                messageAdd.put(chat_user_ref + "/" + push_id, messageCallFrom);

                messageAdd.put("Users/" + mCurrentUserId + "/CallTo", null);
                messageAdd.put("Users/" + received_user_id + "/CallFrom", null);

                mRootRef.updateChildren(messageAdd);

            }
        });

        //callRing
        cancelRing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.stop();
                String current_user_ref = "Messages/" + mCurrentUserId + "/" + received_user_id;
                String chat_user_ref = "Messages/" + received_user_id + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(received_user_id).push();
                final String push_id = user_message_push.getKey();

                Map messageCallTo = new HashMap();
                messageCallTo.put("message", "End Call");
                messageCallTo.put("seen", false);
                messageCallTo.put("type", "ChatVideo");
                messageCallTo.put("time", System.currentTimeMillis());
                messageCallTo.put("from",received_user_id);
                messageCallTo.put("to", mCurrentUserId);


                Map messageCallFrom = new HashMap();
                messageCallFrom.put("message", "Missed Call");
                messageCallFrom.put("seen", false);
                messageCallFrom.put("type", "ChatVideo");
                messageCallFrom.put("time", System.currentTimeMillis());
                messageCallFrom.put("from",received_user_id);
                messageCallFrom.put("to",  mCurrentUserId);

                HashMap messageAdd = new HashMap();
                messageAdd.put(current_user_ref + "/" + push_id, messageCallFrom);
                messageAdd.put(chat_user_ref + "/" + push_id, messageCallTo);


                messageAdd.put("Users/" + mCurrentUserId + "/CallFrom", null);
                messageAdd.put("Users/" + received_user_id + "/CallTo", null);

                mRootRef.updateChildren(messageAdd);

            }
        });

        //accept
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mediaPlayer.stop();
                timeStart = System.currentTimeMillis();
                final HashMap<String, Object> pickUp = new HashMap<>();
                pickUp.put("pick", "picked");

                mUserDatabase.child(mCurrentUserId).child("CallFrom").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("fromID")) {
                            final String idA = dataSnapshot.child("fromID").getValue().toString();
                            mUserDatabase.child(idA).child("CallTo").updateChildren(pickUp).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent i = new Intent(getApplicationContext(), VideoChatActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putLong("timeStart", timeStart);
                                        bundle.putString("received_user_id", received_user_id);
                                        i.putExtras(bundle);
                                        startActivity(i);
                                        
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //imagebuton
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mCurrentUserId).hasChild("CallTo")) {
                    accept.setVisibility(View.INVISIBLE);
                    cancelRing.setVisibility(View.INVISIBLE);
                    //
                    if (dataSnapshot.child(mCurrentUserId).child("CallTo").hasChild("pick")) {
                        timeStart = System.currentTimeMillis();
                        Intent i = new Intent(getApplicationContext(), VideoChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putLong("timeStart", timeStart);
                        bundle.putString("received_user_id", received_user_id);
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                }
                if (dataSnapshot.child(mCurrentUserId).hasChild("CallFrom")) {
                    cancelCall.setVisibility(View.INVISIBLE);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mUserDatabase.child(mCurrentUserId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                i.putExtra("user_id", received_user_id);
                startActivity(i);
                finish();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //tat activity
//        mUserDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (!dataSnapshot.hasChild("CallTo")) {
//                    if (!dataSnapshot.hasChild("CallFrom")) {
//                        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
//                        i.putExtra("user_id", received_user_id);
//                        startActivity(i);
//                        finish();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }
}
