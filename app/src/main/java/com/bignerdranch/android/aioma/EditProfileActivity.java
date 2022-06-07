package com.bignerdranch.android.aioma;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "PasswordCheck";

    ImageView mProfilePic;
    EditText mUsername;
    EditText mEmail;

    ProgressBar mProgressBar;

    DatabaseReference mCustomerUsersRef;

    Button mSaveChangeBtn;
    Button mDisableSaveChangeBtn;

    String username;
    String email;
    String userID;
    String profilePicUrl;
    String newProfilePicUrl;
    Uri profilePicUri;

    private StorageReference storageReference;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Get from xml layout
        mProfilePic = (ImageView) findViewById(R.id.profile_pic);
        mUsername = (EditText) findViewById(R.id.input_username);
        mEmail = (EditText) findViewById(R.id.input_email);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCustomerUsersRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(userID);


        mCustomerUsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                username = snapshot.child("username").getValue(String.class);
                email = snapshot.child("email").getValue(String.class);
                profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);

                mUsername.setText(username);
                mEmail.setText(email);

                if(profilePicUrl != null) {
                    Glide.with(mProfilePic).load(profilePicUrl).into(mProfilePic);
                }

                return;

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //Tap profile pic to open gallery
        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 3);
            }
        });

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mDisableSaveChangeBtn = (Button) findViewById(R.id.disable_save_change_btn);
        mDisableSaveChangeBtn.setVisibility(View.INVISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference();

        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            profilePicUri = data.getData();
            getPFPUrl(profilePicUri);
            mProfilePic.setImageURI(profilePicUri);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSaveChangeBtn = (Button) findViewById(R.id.save_change_btn);
        mSaveChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);

                final String newUsername =mUsername.getText().toString().trim();
                final String newEmail = mEmail.getText().toString().trim();

                if(TextUtils.isEmpty(newUsername) && TextUtils.isEmpty(newEmail)){
                    Toast.makeText(EditProfileActivity.this,
                            "All fields are required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                //username
                if (TextUtils.isEmpty(newUsername)) {
                    Toast.makeText(EditProfileActivity.this,
                            "Username is required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!newUsername.equals(username)){
                    mCustomerUsersRef.child("username").setValue(newUsername);
                }

                //profile pic
                if (newProfilePicUrl == null){
                    //Hide progress bar, do nothing and continue
                    mProgressBar.setVisibility(View.INVISIBLE);

                } else if (!newProfilePicUrl.equals(profilePicUrl)){
                    mCustomerUsersRef.child("profilePicUrl").setValue(newProfilePicUrl);
                }

                //email
                if (TextUtils.isEmpty(newEmail)) {
                    Toast.makeText(EditProfileActivity.this,
                            "Email is required", Toast.LENGTH_SHORT).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!emailPattern.matcher(newEmail).matches()) {
                    mEmail.setError("Invalid Email Address");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!newEmail.equals(email)){

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EditProfileActivity.this);
                    LayoutInflater layoutInflater = EditProfileActivity.this.getLayoutInflater();
                    View dialogView = layoutInflater.inflate(R.layout.dialog_confirm_password, null);
                    dialogBuilder.setView(dialogView);


                    ProgressBar dialogProgressBar = dialogView.findViewById(R.id.dialog_progress_bar);
                    dialogProgressBar.setVisibility(View.INVISIBLE);

                    TextView error_message = dialogView.findViewById(R.id.error_message);
                    error_message.setVisibility(View.INVISIBLE);

                    EditText mPassword = dialogView.findViewById(R.id.input_password);

                    dialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialogBuilder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    dialogBuilder.setView(dialogView);
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogProgressBar.setVisibility(View.VISIBLE);

                            final String password = mPassword.getText().toString().trim();

                            if (TextUtils.isEmpty(password)) {
                                dialogProgressBar.setVisibility(View.INVISIBLE);
                                mPassword.setError("Password is required");

                            } else if (password.length() < 6) {
                                dialogProgressBar.setVisibility(View.INVISIBLE);
                                mPassword.setError("Password should be at least 6 characters");
                            } else {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                                assert user != null;
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                                        Log.d(TAG, "User re-authenticated.");
                                        dialogProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialogProgressBar.setVisibility(View.INVISIBLE);

                                            user.updateEmail(newEmail).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull @NotNull Exception e) {
                                                    error_message.setText("Error: "+ e.getMessage());
                                                    error_message.setVisibility(View.VISIBLE);
                                                    return;
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    mCustomerUsersRef.child("email").setValue(newEmail);

                                                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                                                    startActivity(intent);
                                                }
                                            });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        dialogProgressBar.setVisibility(View.INVISIBLE);
                                        mPassword.setError("Incorrect Password");
                                    }
                                });

                            }
                        }
                    });
                    return;

                }

                //if nothing changed
                if (newUsername.equals(username) && newEmail.equals(email) && newProfilePicUrl == null) {

                    Toast.makeText(EditProfileActivity.this, "No field has been changed",
                            Toast.LENGTH_LONG).show();
                    return;

                }

                //if successful
                Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                startActivity(intent);

            }
        });


    }

    public final Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    );

    protected void getPFPUrl(Uri uri) {
        StorageReference storageRef = storageReference.child(userID + "_profile_pic."
                + getFileExtension(profilePicUri));
        storageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newProfilePicUrl = uri.toString();

                        mProgressBar.setVisibility(View.INVISIBLE);
                        mDisableSaveChangeBtn.setVisibility(View.INVISIBLE);

                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                mDisableSaveChangeBtn.setVisibility(View.VISIBLE);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                mProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(EditProfileActivity.this, "Uploading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


    public void onBackPressed() {

        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
    }
}