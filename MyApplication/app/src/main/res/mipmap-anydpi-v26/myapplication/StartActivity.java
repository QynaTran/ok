package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.Controller.LoginActivity;
import com.example.myapplication.Controller.RegisterActivity;

public class StartActivity extends AppCompatActivity {
    private Button mRegBtn, mLogBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn=(Button)findViewById(R.id.start_reg_btn);
        mRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent=new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(reg_intent);
            }
        });

        mLogBtn=(Button)findViewById(R.id.start_log_btn);
        mLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent log_intent=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(log_intent);
            }
        });
    }
}
