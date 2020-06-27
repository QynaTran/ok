package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class ProfileActivity extends AppCompatActivity {
    private TextView mPName, mPStatus,mPFriendCount;
    private CircleImageView mPImage;
    private Button mPSendReqBtn;

    private DatabaseReference mUsersDatabase, mFriendReqDatabase, mFriendDatabase, mNotificationDatabase, mRootRef;

    private FirebaseUser mCurrent_user;
    private String mCurrent_state;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String user_id=getIntent().getStringExtra("user_id");

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef=FirebaseDatabase.getInstance().getReference();

        mPName=(TextView)findViewById(R.id.profile_display_name);
        mPStatus=(TextView)findViewById(R.id.profile_status);
        mPImage=(CircleImageView)findViewById(R.id.profile_image);
        mPFriendCount=(TextView)findViewById(R.id.profile_total_friends);
        mPSendReqBtn=(Button)findViewById(R.id.profile_send_req_btn);

        mCurrent_state="not_friends";


        if(mCurrent_state.equals("not_friends")){
            mPSendReqBtn.setText("ADD FRIEND");}
        String mCurrent_user_id=mCurrent_user.getUid();
        if(mCurrent_user_id.equals(user_id)){
            mPSendReqBtn.setEnabled(false);
            mPSendReqBtn.setVisibility(View.INVISIBLE);
        }

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("thump_image").getValue().toString();

                mPName.setText(name);
                mPStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(mPImage);

                mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            count=(int)dataSnapshot.getChildrenCount();
                            if(count==1){
                                mPFriendCount.setText("Total: "+Integer.toString(count)+" Friend");
                            }
                            else {
                                mPFriendCount.setText("Total: "+Integer.toString(count)+" Friends");
                            }


                        }else{
                            mPFriendCount.setText("Total: 0 Friend");
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

                    }
                });

                //--------Friend list/request feature

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state="req_received";
                                mPSendReqBtn.setText("Accept Request");
                                mPSendReqBtn.setTextColor(Color.WHITE);
                            }else if(req_type.equals("sent")){
                                mCurrent_state="req_sent";
                                mPSendReqBtn.setText("Cancel Request");
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                                mPSendReqBtn.setBackground(drawable);
                                mPSendReqBtn.setTextColor(Color.BLACK);
                            }
                        }else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state="friends";
                                        mPSendReqBtn.setText("UnFriend");
                                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                                        mPSendReqBtn.setBackground(drawable);
                                        mPSendReqBtn.setTextColor(Color.BLACK);
                                    }
                                }

                                @Override
                                public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });
        //---Not friends state
        mPSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPSendReqBtn.setEnabled(false);
                if(mCurrent_state.equals("not_friends")){
                    DatabaseReference newNotificationRef=mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId=newNotificationRef.getKey();

                    HashMap<String, String> notificationData=new HashMap<>();
                    notificationData.put("from",mCurrent_user.getUid());
                    notificationData.put("type","request");

                    Map requestMap=new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(com.example.myapplication.Controller.ProfileActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                            }
                            mPSendReqBtn.setEnabled(true);
                            mCurrent_state="req_sent";
                            mPSendReqBtn.setText("Cancel Request");
                            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                            mPSendReqBtn.setBackground(drawable);
                            mPSendReqBtn.setTextColor(Color.BLACK);
                        }
                    });
                }

                //----cancel request
                if(mCurrent_state.equals("req_sent")){
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mPSendReqBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mPSendReqBtn.setText("Add Friend");
                                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.login_bg);
                                    mPSendReqBtn.setBackground(drawable);
                                    mPSendReqBtn.setTextColor(Color.WHITE);

                                }
                            });
                        }
                    });
                }

                //----req received state
                if(mCurrent_state.equals("req_received")){
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap=new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mPSendReqBtn.setEnabled(true);
                                mCurrent_state="friends";
                                mPSendReqBtn.setText("UnFriend");
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                                mPSendReqBtn.setBackground(drawable);
                                mPSendReqBtn.setTextColor(Color.BLACK);
                            }else{
                                Toast.makeText(com.example.myapplication.Controller.ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //-----UNFRIENDS
                if(mCurrent_state.equals("friends")){
                    Map unfriendMap=new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);
                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mPSendReqBtn.setEnabled(true);
                                mCurrent_state="not_friends";
                                mPSendReqBtn.setText("Add Friend");
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.login_bg);
                                mPSendReqBtn.setBackground(drawable);
                                mPSendReqBtn.setTextColor(Color.WHITE);
                            }else{
                                Toast.makeText(com.example.myapplication.Controller.ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }
}
