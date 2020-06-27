package com.example.myapplication;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.Model.GetTimeAgo;
import com.example.myapplication.Model.MessageAdapter;
import com.example.myapplication.Model.Messages;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mChatUser_id, mChatUser_name;

    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView, mLastSeen;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    private ImageButton mChatAddBtn, mChatSendBtn;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefeshLayout;

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





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();


        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mChatUser_id = getIntent().getStringExtra("user_id");
        mChatUser_name = getIntent().getStringExtra("user_name");
        getSupportActionBar().setTitle("");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //Custom action bar
        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeen = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mTitleView.setText(mChatUser_name);

        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageButton) findViewById(R.id.chat_send_btn);
        mChatMessageView = (EditText) findViewById(R.id.chat_text);

        messageAdapter=new MessageAdapter(messagesList);

        mMessagesList=(RecyclerView)findViewById(R.id.chat_message_list);
        mRefeshLayout=(SwipeRefreshLayout)findViewById(R.id.message_swipe_layout);
        linearLayoutManager=new LinearLayoutManager(this);
        

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(linearLayoutManager);
        mMessagesList.setAdapter(messageAdapter);

        //--Image Storage
        mImageStorage= FirebaseStorage.getInstance().getReference();
        
        loadMessage();

        mRootRef.child("Users").child(mChatUser_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("thump_image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(mProfileImage);
                if (online.equals("true")) {
                    mLastSeen.setText("Online");
                } else {
                    GetTimeAgo getTime = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTime.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeen.setText(lastSeenTime);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser_id)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser_id, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser_id + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("Chat_Log", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mChatMessageView.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser_id;
                    String chat_user_ref = "messages/" + mChatUser_id + "/" + mCurrentUserId;

                    DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser_id).push();
                    String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from",mCurrentUserId);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                    
                    mChatMessageView.setText("");
                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("Chat_Log", databaseError.getMessage().toString());
                            }
                        }
                    });
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
         mRefeshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 mCurrentPage++;
                 itemPosition = 0;
                 loadMoreMessage();
             }
         });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();

            final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser_id;
            final String chat_user_ref = "messages/" + mChatUser_id + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser_id).push();
            final String push_id = user_message_push.getKey();

            final StorageReference filepath = mImageStorage.child("message_image").child(push_id + ".jpg");
            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@androidx.annotation.NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String download_url=uri.toString();
                                Map messageMap = new HashMap();
                                messageMap.put("message", download_url);
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from",mCurrentUserId);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                mChatMessageView.setText("");
                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@androidx.annotation.Nullable DatabaseError databaseError, @androidx.annotation.NonNull DatabaseReference databaseReference) {
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
        DatabaseReference messageRef= mRootRef.child("messages").child(mCurrentUserId).child(mChatUser_id);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);
                String messageKey=dataSnapshot.getKey();
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
                mRefeshLayout.setRefreshing(false);

                linearLayoutManager.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessage() {
        DatabaseReference messageRef= mRootRef.child("messages").child(mCurrentUserId).child(mChatUser_id);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        mRootRef.child("messages").child(mCurrentUserId).child(mChatUser_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {
                Messages messages=dataSnapshot.getValue(Messages.class);

                itemPosition++;
                if (itemPosition == 1) {
                    String messageKey=dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                    
                }

                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                mRefeshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot dataSnapshot, @androidx.annotation.Nullable String s) {

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {

            }
        });
    }

}
