package com.example.myapplication.Controller;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Model.Users;
import com.example.myapplication.Notifications.Data;
import com.example.myapplication.Notifications.Sender;
import com.example.myapplication.Notifications.Token;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView mPName, mPStatus,mPFriendCount;
    private CircleImageView mPImage;
    private Button mPSendReqBtn;

    private DatabaseReference mUsersDatabase, mFriendReqDatabase, mFriendDatabase, mRootRef, mCurrentDatabase;

    private FirebaseUser mCurrent_user;
    private String mCurrent_state, mCurrent_user_id;
    private int count = 0;

    private RequestQueue requestQueue;
    private boolean notify = false;
    private Toolbar mToolbar;

    //video
    String calledBy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar=(Toolbar)findViewById(R.id.profile_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Information");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String user_id=getIntent().getStringExtra("user_id");
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();

        mCurrent_user_id = mCurrent_user.getUid();
        mCurrentDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user_id);

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends");
        
        mRootRef= FirebaseDatabase.getInstance().getReference();

        mPName=(TextView)findViewById(R.id.profile_display_name);
        mPStatus=(TextView)findViewById(R.id.profile_status);
        mPImage=(CircleImageView)findViewById(R.id.profile_image);
        mPFriendCount=(TextView)findViewById(R.id.profile_total_friends);
        mPSendReqBtn=(Button)findViewById(R.id.profile_send_req_btn);

        mCurrent_state="not_friends";

        if(mCurrent_user_id.equals(user_id)){
            mPSendReqBtn.setEnabled(false);
            mPSendReqBtn.setVisibility(View.INVISIBLE);
        }

        if(mCurrent_state.equals("not_friends")){
            mPSendReqBtn.setText("ADD FRIEND");}

        //info friend
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("thump_image").getValue().toString();

                mPName.setText(name);
                mPStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(mPImage);
                //Count friend
                mFriendDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            count=(int)dataSnapshot.getChildrenCount();
                            mPFriendCount.setText(count + "");
                        }else{
                            mPFriendCount.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                //--------Friend list/request feature

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){
                                        mCurrent_state="friends";
                                        mPSendReqBtn.setText("UnFriend");
                                        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                                        mPSendReqBtn.setBackground(drawable);
                                        mPSendReqBtn.setTextColor(Color.BLACK);
                                    }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //---Not friends state
        mPSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                mPSendReqBtn.setEnabled(false);
                if(mCurrent_state.equals("not_friends")){
                    Map requestMap=new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
                            }
                            mPSendReqBtn.setEnabled(true);
                            mCurrent_state="req_sent";
                            mPSendReqBtn.setText("Cancel Request");
                            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                            mPSendReqBtn.setBackground(drawable);
                            mPSendReqBtn.setTextColor(Color.BLACK);

                        }
                    });
                    mCurrentDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Users users = dataSnapshot.getValue(Users.class);
                            String msg="send you a friend request";
                            if (notify) {
                                sendNotification(user_id, users.getName(), msg);
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

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
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mPSendReqBtn.setEnabled(true);
                                mCurrent_state="friends";
                                mPSendReqBtn.setText("UnFriend");
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.register_bg);
                                mPSendReqBtn.setBackground(drawable);
                                mPSendReqBtn.setTextColor(Color.BLACK);
                            }else{
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mPSendReqBtn.setEnabled(true);
                                mCurrent_state="not_friends";
                                mPSendReqBtn.setText("Add Friend");
                                Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.login_bg);
                                mPSendReqBtn.setBackground(drawable);
                                mPSendReqBtn.setTextColor(Color.WHITE);
                            }else{
                                Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });

    }

    private void sendNotification(final String mChatUser_id, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(mChatUser_id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(mCurrent_user_id, "Friend Request",name + ": " + message , mChatUser_id,"request", R.drawable.icon_profile);

                    Sender sender = new Sender(data, token.getToken());
                    //fcm json object request
                    try {
                        JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPOND", "onRespond:" + response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPOND", "onRespond:" + error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAA_V8pm0s:APA91bHoKVewb77gsgPFyl8LkLqL6yY2uCVBJ4OaXMTQZS6f7IdfUd7_4yfLk5C-PGHF1-zPkFC1jn5rUDcxFOLL-RsJp8nM6GMCi2ROj-HlUZ_KtLeA3CS0glAXI2DQbdTloa0OD868");
                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrent_user_id != null) {
            mRootRef.child("Users").child(mCurrent_user_id).child("online").setValue("true");
        }
       // checkForReceivingCall();
    }



    private void checkForReceivingCall() {
        mCurrentDatabase.addValueEventListener(new ValueEventListener() {
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
