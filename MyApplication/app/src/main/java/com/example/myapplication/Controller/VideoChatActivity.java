package com.example.myapplication.Controller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import android.opengl.GLSurfaceView;


import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {
    //video
    private static String API_KEY = "46770284";
    private static String SESSION_ID = "1_MX40Njc3MDI4NH5-MTU5MDk0Mzg1NDUxNH5xdnB1K3k5bnFhOVJ4Y2Y4bzVoSnNSVDd-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00Njc3MDI4NCZzaWc9MWE5NTVkOWRkMWZiNDQxNTg4M2QyYTkyNzkyMzViM2IwZmMzODhmYTpzZXNzaW9uX2lkPTFfTVg0ME5qYzNNREk0Tkg1LU1UVTVNRGswTXpnMU5EVXhOSDV4ZG5CMUszazVibkZoT1ZKNFkyWTRielZvU25OU1ZEZC1mZyZjcmVhdGVfdGltZT0xNTkwOTQzOTQyJm5vbmNlPTAuMTIxMjA1NDA4NDUyNTM2MjUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MzUzNTk0MSZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private ImageView endVideo;
    private FrameLayout frameLayout;
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private String received_user_id;
    private long timeStart;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase, mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        //get
        Bundle bundle = getIntent().getExtras();
        received_user_id = bundle.getString("received_user_id");
        timeStart = bundle.getLong("timeStart");


        //video
        endVideo = (ImageView) findViewById(R.id.endCall_button);


            //endVideo
        endVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(mCurrentUserId).hasChild("CallFrom")) {
                            String current_user_ref = "Messages/" + mCurrentUserId + "/" + received_user_id;
                            String chat_user_ref = "Messages/" + received_user_id + "/" + mCurrentUserId;

                            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(received_user_id).push();
                            final String push_id = user_message_push.getKey();

                            Map messageCallTo = new HashMap();
                            messageCallTo.put("message", "Chat Video");
                            messageCallTo.put("seen", false);
                            messageCallTo.put("type", "ChatVideo");
                            messageCallTo.put("time", timeStart);
                            messageCallTo.put("videoEnd", System.currentTimeMillis());
                            messageCallTo.put("from",received_user_id);
                            messageCallTo.put("to", mCurrentUserId);


                            Map messageCallFrom = new HashMap();
                            messageCallFrom.put("message", "Chat Video");
                            messageCallFrom.put("seen", false);
                            messageCallFrom.put("type", "ChatVideo");
                            messageCallFrom.put("time", timeStart);
                            messageCallFrom.put("videoEnd", System.currentTimeMillis());
                            messageCallFrom.put("from",received_user_id);
                            messageCallFrom.put("to",  mCurrentUserId);

                            Map messageAdd = new HashMap();
                            messageAdd.put(current_user_ref + "/" + push_id, messageCallFrom);
                            messageAdd.put(chat_user_ref + "/" + push_id, messageCallTo);

                            messageAdd.put("Users/" + mCurrentUserId + "/CallFrom", null);
                            messageAdd.put("Users/" + received_user_id + "/CallTo", null);

                            mRootRef.updateChildren(messageAdd);

                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }
                        }

                        if (dataSnapshot.child(mCurrentUserId).hasChild("CallTo")){
                            String current_user_ref = "Messages/" + mCurrentUserId + "/" + received_user_id;
                            String chat_user_ref = "Messages/" + received_user_id + "/" + mCurrentUserId;

                            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserId).child(received_user_id).push();
                            final String push_id = user_message_push.getKey();

                            Map messageCallTo = new HashMap();
                            messageCallTo.put("message", "Chat Video");
                            messageCallTo.put("seen", false);
                            messageCallTo.put("type", "ChatVideo");
                            messageCallTo.put("time", timeStart);
                            messageCallTo.put("videoEnd", System.currentTimeMillis());
                            messageCallTo.put("from",mCurrentUserId);
                            messageCallTo.put("to", received_user_id);


                            Map messageCallFrom = new HashMap();
                            messageCallFrom.put("message", "Chat Video");
                            messageCallFrom.put("seen", false);
                            messageCallFrom.put("type", "ChatVideo");
                            messageCallFrom.put("time", timeStart);
                            messageCallFrom.put("videoEnd", System.currentTimeMillis());
                            messageCallFrom.put("from",mCurrentUserId);
                            messageCallFrom.put("to", received_user_id);

                            final Map messageAdd = new HashMap();
                            messageAdd.put(current_user_ref + "/" + push_id, messageCallTo);
                            messageAdd.put(chat_user_ref + "/" + push_id, messageCallFrom);

                            messageAdd.put("Users/" + mCurrentUserId + "/CallTo", null);
                            messageAdd.put("Users/" + received_user_id + "/CallFrom", null);

                            mRootRef.updateChildren(messageAdd);
                            if (mPublisher != null) {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null) {
                                mSubscriber.destroy();
                            }

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        requestPermissions();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            // initialize view objects from your layout
            mPublisherViewContainer = (FrameLayout) findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout) findViewById(R.id.subscriber_container);
 
            // initialize and connect to the session
            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
        finish();
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }
    //publisher a stream
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
      
    }
    //Subscriber
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onStart() {
        super.onStart();

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
    }

}
