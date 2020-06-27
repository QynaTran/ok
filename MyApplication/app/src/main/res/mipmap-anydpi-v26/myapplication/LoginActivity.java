package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail, mLoginPassword;

    Button mLoginBtn;

    private FirebaseAuth mAuth;
    private Boolean emailAddressChecked;

    private DatabaseReference mUserDatabase;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        mToolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("LOGIN");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword=(TextInputLayout)findViewById(R.id.login_password);
        loadingBar=new ProgressDialog(this);

        mLoginBtn=(Button)findViewById(R.id.login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mLoginEmail.getEditText().getText().toString().trim();
                String password=mLoginPassword.getEditText().getText().toString().trim();
                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    loadingBar.setTitle("Login Account");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.show();
                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@androidx.annotation.NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    verifyEmailAddress();
                } else {
                    Toast.makeText(com.example.myapplication.Controller.LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }

    private void verifyEmailAddress(){
        FirebaseUser user= mAuth.getCurrentUser();
        emailAddressChecked=user.isEmailVerified();
        if(emailAddressChecked==true){
            sendUserToMainActivity();
        }else{
            Toast.makeText(this, "Please verify your Account first...", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
