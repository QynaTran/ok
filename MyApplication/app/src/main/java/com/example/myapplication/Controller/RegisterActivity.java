package com.example.myapplication.Controller;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName, mEmail, mPassword;

    private Button mCreateBtn;

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private DatabaseReference mdatabaseReference;

    private ProgressDialog loadingBar;


    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=\\S+$)" + "(?=.*[A-Z])" + ".{8,}");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("CREATE ACCOUNT");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        loadingBar=new ProgressDialog(this);

        mDisplayName=(TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail=(TextInputLayout)findViewById(R.id.reg_email);
        mPassword=(TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn=(Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name=mDisplayName.getEditText().getText().toString().trim();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("required");
                }
                if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    mPassword.setError("A-Z, at least 8 characters, no white spaces");
                }
                if(TextUtils.isEmpty(email)){
                    mEmail.setError("required");
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Please enter a valid email address");
                }
                if (TextUtils.isEmpty(display_name)) {
                    mDisplayName.setError("required");
                }
                if (display_name.length() > 15) {
                    mDisplayName.setError("Username is too long");
                }

                if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(email) && PASSWORD_PATTERN.matcher(password).matches() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && display_name.length() <= 15 ){
                    register_user(display_name,email,password);
                    loadingBar.setTitle("Create New Account");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.show();
                }
            }
        });
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    final String mCurrent_user_id = current_user.getUid();


                    mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrent_user_id);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Online");
                    userMap.put("image", "default");
                    userMap.put("thump_image", "default");
                    userMap.put("typing_to", "none");
                    userMap.put("phone", "+84");

                    mdatabaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                sendEmailVerificationMessage();
                            }
                        }
                    });
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        loadingBar.dismiss();
    }

    private void sendEmailVerificationMessage(){
        FirebaseUser user= mAuth.getCurrentUser();
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful. We'll send you an email. Please check and verify your account.", Toast.LENGTH_SHORT).show();
                        sendToLoginActivity();
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void sendToLoginActivity(){
        Intent mainIntent = new Intent(getApplicationContext(), LoginActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser user= mAuth.getCurrentUser();
        mAuth.signOut();
    }
}
