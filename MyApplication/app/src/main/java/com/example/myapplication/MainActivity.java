package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.myapplication.Controller.SettingsActivity;
import com.example.myapplication.Controller.UsersActivity;
import com.example.myapplication.Notifications.Token;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;

    private String mCurrentUserId;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;

    private TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
       mAuth= FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser!= null) {
            mCurrentUserId=mAuth.getCurrentUser().getUid();
            mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }

        //Tabs
        mViewPager=(ViewPager)findViewById(R.id.main_tabs_pager);
        mSectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        mToolbar=(Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Application");

    }

    public void checkUserStatus() {
        if(mCurrentUser==null){
            sendToStart();
        } else {
            //Save uid of current signed  in user in  shared preference
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mCurrentUserId);
            editor.apply();
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    public void onStart() {

        checkUserStatus();
        super.onStart();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCurrentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
    
    private void sendToStart() {
        Intent startIntent=new Intent(getApplicationContext(), StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mCurrentUserId).setValue(mToken);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             if(mCurrentUser!=null){
                 mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
             }
             sendToStart();
         }

         if(item.getItemId()==R.id.main_setting_btn){
             Intent settingIntent=new Intent(getApplicationContext(), SettingsActivity.class);
             startActivity(settingIntent);
             if(mCurrentUser!=null){
                 mUserRef.child("online").setValue("true");
             }
         }
        if(item.getItemId()==R.id.main_all_btn){
            Intent usersIntent=new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(usersIntent);
            if(mCurrentUser!=null){
                mUserRef.child("online").setValue("true");
            }
        }

         return true;
    }

}
