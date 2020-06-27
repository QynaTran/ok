package com.example.myapplication.Controller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    private CircleImageView mDisplayImage;
    private TextView mName, mStatus, mEmail;

    private ImageButton mImageBtn;

    private static final int GALLERY_PICK=13;

    private StorageReference mImageStorage;
    //Status
    private ProgressDialog loadingBar;
    private String current_uid;
    private ImageView statusUpdate;
    //name
    private ImageView changeName;
    //phone
    private ImageView updatePhone;
    private TextView mPhone;

    private Toolbar mToolbar;
    private String calledBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar=(Toolbar)findViewById(R.id.settings_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //settings
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mDisplayImage=(CircleImageView)findViewById(R.id.settings_image);
        mName=(TextView)findViewById(R.id.setting_display_name);
        mStatus=(TextView)findViewById(R.id.settings_status);
        mEmail = (TextView) findViewById(R.id.setings_email);
        mPhone = (TextView) findViewById(R.id.settings_phone);

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        current_uid=mCurrentUser.getUid();

        mEmail.setText(mCurrentUser.getEmail());

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString().trim();
                String status=dataSnapshot.child("status").getValue().toString();
                String number = dataSnapshot.child("phone").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                mPhone.setText(number);

                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.icon_profile).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {
                        }
                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.icon_profile).into(mDisplayImage);

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mImageBtn=(ImageButton)findViewById(R.id.settings_image_btn);
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SettingsActivity.this);
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        //status
        loadingBar=new ProgressDialog(this);
        statusUpdate = (ImageView) findViewById(R.id.settings_status_edit);
        statusUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateStatusDialog();
            }
        });

        //name
        changeName = (ImageView) findViewById(R.id.setting_display_name_edit);
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeNameDialog();
            }
        });

        //
        updatePhone = (ImageView) findViewById(R.id.settings_phone_edit);
        updatePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdatePhoneDialog();
            }
        });
    }

    private void showUpdatePhoneDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Phone");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText inputPhone = new EditText(this);
        inputPhone.setHint("Telephone number");
        inputPhone.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(inputPhone);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yPhone = inputPhone.getText().toString().trim();
                if (TextUtils.isEmpty(yPhone)) {
                    mStatus.setError("required");
                }
                if(!TextUtils.isEmpty(yPhone)) {
                    beginUpdatePhone(yPhone);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginUpdatePhone(String yPhone) {
        loadingBar.setMessage("Updating...");
        loadingBar.show();
        mUserDatabase.child("phone").setValue(yPhone).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Your phone updates successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error, Your phone doesn't update", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change name");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText inputName = new EditText(this);
        inputName.setHint("Name");
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(inputName);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yName = inputName.getText().toString().trim();
                if (TextUtils.isEmpty(yName)) {
                    mStatus.setError("required");
                }
                if(!TextUtils.isEmpty(yName)) {
                    beginChangeName(yName);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginChangeName(String yName) {
        loadingBar.setMessage("Updating...");
        loadingBar.show();
        mUserDatabase.child("name").setValue(yName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Your name changes successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error, Your name doesn't change", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUpdateStatusDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status");

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText inputStatus = new EditText(this);
        inputStatus.setHint("What's on your mind?");
        inputStatus.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        linearLayout.addView(inputStatus);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);

        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String yStatus = inputStatus.getText().toString().trim();
                if (TextUtils.isEmpty(yStatus)) {
                    mStatus.setError("required");
                }
                if(!TextUtils.isEmpty(yStatus)) {
                    beginUpdate(yStatus);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginUpdate(String yStatus) {
        loadingBar.setMessage("Updating...");
        loadingBar.show();
        mUserDatabase.child("status").setValue(yStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Status changes successful", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error, Status doesn't change", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();
            CropImage.activity(imageUri).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File thumb_filepath=new File(resultUri.getPath());

                String current_user_uid=mCurrentUser.getUid();

                Bitmap thumb_bitmap= null;
                try {
                    thumb_bitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumb_filepath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                    final byte[] thumb_byte=baos.toByteArray();

                final StorageReference filepath=mImageStorage.child("profile_images").child(current_user_uid+".jpg");
                final StorageReference thumb_file_path=mImageStorage.child("profile_images").child("thumbs").child(current_user_uid+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String download_url=uri.toString();
                                    final UploadTask uploadTask=thumb_file_path.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                thumb_file_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        String thumb_downloadUrl=uri.toString();
                                                        Map update=new HashMap();
                                                        update.put("image",download_url);
                                                        update.put("thump_image",thumb_downloadUrl);

                                                        mUserDatabase.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(SettingsActivity.this, "Avatar changes successful", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }else {
                                                Toast.makeText(SettingsActivity.this, "Error upload image", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }else{
                            Toast.makeText(SettingsActivity.this, "Avatar doesn't change successful", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onStart() {
        //checkUserStatus();
        //checkForReceivingCall();
        super.onStart();
    }    


    private void checkForReceivingCall() {
        mUserDatabase.addValueEventListener(new ValueEventListener() {
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
