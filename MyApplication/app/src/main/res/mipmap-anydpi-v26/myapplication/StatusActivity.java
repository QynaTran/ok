package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.Controller.SettingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    private DatabaseReference mStatusDatabsae;
    private FirebaseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        String status_value=getIntent().getStringExtra("status_value");

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mStatusDatabsae= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mToolbar=(findViewById(R.id.status_app_bar));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus=(TextInputLayout)findViewById(R.id.status_input);
        mStatus.getEditText().setText(status_value);
        mSaveBtn=(Button)findViewById(R.id.status_save_btn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status=mStatus.getEditText().getText().toString().trim();

                mStatusDatabsae.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(com.example.myapplication.Controller.StatusActivity.this, "Status changes successful", Toast.LENGTH_SHORT).show();
                            Intent setting_intent=new Intent(getApplicationContext(), SettingsActivity.class);
                            setting_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(setting_intent);

                        }else{
                            Toast.makeText(com.example.myapplication.Controller.StatusActivity.this, "Error, Status doesn't change", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
