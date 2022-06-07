package com.bignerdranch.android.aioma;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mUsername, mEmail, mPassword, mConfirmPassword;

    ImageView mShowPassword;

    private FirebaseAuth firebaseAuth;

    ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5F5DD8")));

        //EditText
        mUsername = findViewById(R.id.input_username);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);


        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);


        firebaseAuth = FirebaseAuth.getInstance();
        findViewById(R.id.sign_up_btn).setOnClickListener(this);
        findViewById(R.id.sign_in_btn).setOnClickListener(this);

    }

    @Override
    protected void onStart(){
        super.onStart();

        if(firebaseAuth.getCurrentUser() != null) {
            //handle the already logged in user
        }
    }

    private void registerUser() {
        mProgressBar.setVisibility(View.VISIBLE);

        final String username = mUsername.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String confirmPassword = mConfirmPassword.getText().toString().trim();
        final boolean customer = true;

        if(TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmPassword) ){
            Toast.makeText(SignUpActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //username
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(SignUpActivity.this, "Username is required", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //email
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(SignUpActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (!emailPattern.matcher(email).matches()) {
            Toast.makeText(SignUpActivity.this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //password
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(SignUpActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this, "Password should have at least 6 characters",
                    Toast.LENGTH_SHORT).show();

            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        //confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Confirm Password is required", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Confirm password does not match the password",
                    Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(!task.isSuccessful()) {
                        mProgressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(SignUpActivity.this,
                                "Error " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //Store additional field in firebase database
                        User cusUser = new User(username, email, customer);

                        FirebaseDatabase.getInstance().getReference("customerUsers")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(cusUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this,
                                            "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                }
                            }
                        });

                    }
                });

    }

    @Override
     public void onClick(View v){
        switch (v.getId()) {
            case R.id.sign_up_btn:
                registerUser();
                break;
            case R.id.sign_in_btn:
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
        }
    }

    public final Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    );


}





