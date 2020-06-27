package com.example.myapplication.Controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.Model.GetTimeAgo;
import com.example.myapplication.Model.MessageAdapter;
import com.example.myapplication.Model.Messages;

import com.example.myapplication.Notifications.Data;
import com.example.myapplication.Notifications.Sender;
import com.example.myapplication.Notifications.Token;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {
    private String mChatUser_id;

    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView, mLastSeen, mTyping;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageView mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter  messageAdapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;

    //New Solution
    private int mCurrentPage = 1;
    private int itemPosition = 0;

    private String mLastKey = "";
    private String mPrevKey = "";

    private static final int GALLERY_PICK = 2;

    //Storage firebase
    private StorageReference mImageStorage;

    private ImageView video;

    private String calledBy;

    //for check  if user  has seen  message or not
    ValueEventListener seenListener;
    ValueEventListener seenChat;

    private RequestQueue requestQueue;
    private boolean notify = false;

    private String name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mChatUser_id = getIntent().getStringExtra("user_id");
        getSupportActionBar().setTitle("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        video=(ImageView) findViewById(R.id.custom_bar_video);

        //Custom action bar
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeen = (TextView) findViewById(R.id.custom_bar_seen);
        mTyping = (TextView) findViewById(R.id.chat_typing);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);

        mChatAddBtn = (ImageView) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageView) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_text);

        messageAdapter = new MessageAdapter(ChatActivity.this, messagesList);

        mMessagesList=(RecyclerView)findViewById(R.id.chat_message_list);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        //linearlayout  for RecycleView
        linearLayoutManager=new LinearLayoutManager(this);


        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayoutManager);
        mMessagesList.setAdapter(messageAdapter);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        //--Image Storage
        mImageStorage= FirebaseStorage.getInstance().getReference();
        
        loadMessage();
        //Loaf info
        mRootRef.child("Users").child(mChatUser_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    String image = dataSnapshot.child("thump_image").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(mProfileImage);

                    name = dataSnapshot.child("name").getValue().toString();
                    mTitleView.setText(name);
                    if (online.equals("true")) {
                        mLastSeen.setText("Online");
                    } else {
                        GetTimeAgo getTime = new GetTimeAgo();
                        long lastTime = Long.parseLong(online);
                        String lastSeenTime = (String) getTime.getTimeAgo(lastTime, getApplicationContext());
                        mLastSeen.setText(lastSeenTime);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map chatAddMap = new HashMap();
                chatAddMap.put("seen", true);
                chatAddMap.put("timestamp", System.currentTimeMillis());

                Map chatUserMap = new HashMap();
                chatUserMap.put("Chat/" + mCurrentUserId+ "/" + mChatUser_id, chatAddMap);
                // chatUserMap.put("Chat/" + mChatUser_id + "/" + mCurrentUserId, chatAddMap);

                mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.d("Chat_Log", databaseError.getMessage().toString());
                        }
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Video call
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRootRef.child("Users").child(mChatUser_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild("CallTo") && !dataSnapshot.hasChild("CallFrom")) {
                            HashMap<String, Object> info1 = new HashMap<>();
                            info1.put("toID", mChatUser_id);

                            HashMap<String, Object> info2 = new HashMap<>();
                            info2.put("fromID", mCurrentUserId);

                            HashMap add = new HashMap();
                            add.put("Users/" + mCurrentUserId + "/CallTo", info1);
                            add.put("Users/" + mChatUser_id + "/CallFrom", info2);

                            mRootRef.updateChildren(add, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        Toast.makeText(ChatActivity.this, "heloo", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(getApplicationContext(), CallRingActivity.class);
                                        i.putExtra("received_user_id", mChatUser_id);
                                        startActivity(i);
                                        finish();
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

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String message = mChatMessageView.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser_id;
                    String chat_user_ref = "Messages/" + mChatUser_id + "/" + mCurrentUserId;

                    DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser_id ).push();
                    String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", System.currentTimeMillis());
                    messageMap.put("from",mCurrentUserId);
                    messageMap.put("to", mChatUser_id);

                    Map chatAddMap1 = new HashMap();
                    chatAddMap1.put("seen", false);
                    chatAddMap1.put("timestamp", System.currentTimeMillis());

                    Map chatAddMap2 = new HashMap();
                    chatAddMap2.put("seen", true);
                    chatAddMap2.put("timestamp", System.currentTimeMillis());


                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put("Chat/" + mChatUser_id + "/" + mCurrentUserId, chatAddMap1);
                    messageUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser_id, chatAddMap2);

                    mChatMessageView.setText("");
                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("Chat_Log", databaseError.getMessage().toString());
                            }
                        }
                    });

                    String msg=message;
                    if (notify) {
                        sendNotification(mChatUser_id, name, msg);
                    }
                    notify = false;
                       
                }
            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });
         mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 mCurrentPage++;
                 itemPosition = 0;
                 loadMoreMessage();
             }
         });
         seenMessage();

        mChatMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() != 0) {
                    mRootRef.child("Users").child(mCurrentUserId).child("typing_to").setValue(mChatUser_id);

                }
                else {
                    mRootRef.child("Users").child(mCurrentUserId).child("typing_to").setValue("none");

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mRootRef.child("Users").child(mChatUser_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("typing_to")) {
                    String typ = dataSnapshot.child("typing_to").getValue().toString();
                    if (typ.equals(mCurrentUserId)) {
                        mTyping.setVisibility(View.VISIBLE);
                    }else {
                        mTyping.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                    Data data = new Data(mCurrentUserId, "New Message",name + ": " + message , mChatUser_id,"message", R.drawable.icon_profile);

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


    private void seenMessage() {
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Messages")) {
                    seenListener = mRootRef.child("Messages").child(mChatUser_id).child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                ds.getRef().child("seen").setValue(true);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();

            final String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser_id;
            final String chat_user_ref = "Messages/" + mChatUser_id + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser_id ).push();
            final String push_id = user_message_push.getKey();

            final StorageReference filepath = mImageStorage.child("message_image").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url=uri.toString();
                                Map messageMap = new HashMap();
                                messageMap.put("message", download_url);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", System.currentTimeMillis());
                                messageMap.put("from",mCurrentUserId);
                                messageMap.put("to", mChatUser_id);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                mChatMessageView.setText("");
                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("Chat_Log", databaseError.getMessage().toString());
                                        }
                                    }
                                });


                            }
                        });
                    }
                }
            });
        }
    }

    private void loadMoreMessage() {
        DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser_id);
        Query messageQuery = user_message_push.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
        messageQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    String messageKey=ds.getRef().getKey();
                    if (!mPrevKey.equals(messageKey)) {
                        messagesList.add(itemPosition++, messages);
                    }else{
                        mPrevKey=messageKey;
                    }

                    if (itemPosition == 1) {
                        mLastKey=messageKey;
                    }
                    Log.d("TOTALKEYS", "LASTKEY: " + mLastKey + "|PREVKEY:" + mPrevKey + "|MESSAGEKEY:" + messageKey);
                    messageAdapter.notifyDataSetChanged();
                    mRefreshLayout.setRefreshing(false);

                    linearLayoutManager.scrollToPositionWithOffset(10,0);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//        messageQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Messages messages=dataSnapshot.getValue(Messages.class);
//                String messageKey=dataSnapshot.getKey();
//                if (!mPrevKey.equals(messageKey)) {
//                    messagesList.add(itemPosition++, messages);
//                }else{
//                    mPrevKey=messageKey;
//                }
//
//                if (itemPosition == 1) {
//                    mLastKey=messageKey;
//                }
//
//                Log.d("TOTALKEYS", "LASTKEY: " + mLastKey + "|PREVKEY:" + mPrevKey + "|MESSAGEKEY:" + messageKey);
//                messageAdapter.notifyDataSetChanged();
//                mRefreshLayout.setRefreshing(false);
//
//                linearLayoutManager.scrollToPositionWithOffset(10,0);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
        
    }

    private void loadMessage() {
        DatabaseReference messageRef= mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser_id);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
//        messageRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//                Messages messages=dataSnapshot.getValue(Messages.class);
//
//                itemPosition++;
//                if (itemPosition == 1) {
//                    String messageKey=dataSnapshot.getKey();
//                    mLastKey = messageKey;
//                    mPrevKey = messageKey;
//                }
//
//                messagesList.add(messages);
//                messageAdapter.notifyDataSetChanged();
//                mMessagesList.scrollToPosition(messagesList.size()-1);
//                mRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messagesList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Messages messages = ds.getValue(Messages.class);
                    itemPosition++;
                    if (itemPosition == 1) {
                        String messageKey=ds.getRef().getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;
                    }
                    messagesList.add(messages);
                    messageAdapter.notifyDataSetChanged();
                    mMessagesList.scrollToPosition(messagesList.size()-1);
                    mRefreshLayout.setRefreshing(false);
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

        checkForReceivingCall();
    }



    private void checkForReceivingCall() {
         mRootRef.child("Users").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.hasChild("CallFrom")) {
                     if(dataSnapshot.child("CallFrom").hasChild("fromID")){
                         Toast.makeText(ChatActivity.this, "received", Toast.LENGTH_SHORT).show();
                         calledBy = dataSnapshot.child("CallFrom").child("fromID").getValue().toString();
                         Intent i = new Intent(getApplicationContext(), CallRingActivity.class);
                         i.putExtra("received_user_id", calledBy);
                         startActivity(i);
                         finish();
                     }
                 }

             }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }

    @Override
    protected void onStop() {
        super.onStop();
        mRootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Messages")) {
                    mRootRef.child("Messages").removeEventListener(seenListener);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
