package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.myapplication.Controller.SettingsActivity;
import com.example.myapplication.Controller.UsersActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;


    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;

    private TabLayout mTabLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
       mAuth=FirebaseAuth.getInstance();
       mCurrentUser=mAuth.getCurrentUser();

        if (mCurrentUser!= null) {
            String userid=mCurrentUser.getUid();
            mUserRef=FirebaseDatabase.getInstance().getReference().child("Users").child(userid);
        }

        //Tabs
        mViewPager=(ViewPager)findViewById(R.id.main_tabs_pager);
        mSectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("App Chat");
    }



    @Override
    public void onStart() {
        super.onStart();

                if(mCurrentUser==null){
                    sendToStart();

                }
                else {
                    mUserRef.child("online").setValue("true");
                }


        // Check if user is signed in (non-null) and update UI accordingly.





    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    private void sendToStart() {
        Intent startIntent=new Intent(getApplicationContext(),StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             sendToStart();
         }

         if(item.getItemId()==R.id.main_setting_btn){
             Intent settingIntent=new Intent(getApplicationContext(), SettingsActivity.class);
             startActivity(settingIntent);
         }
        if(item.getItemId()==R.id.main_all_btn){
            Intent usersIntent=new Intent(getApplicationContext(), UsersActivity.class);
            startActivity(usersIntent);
        }

         return true;
    }
}
