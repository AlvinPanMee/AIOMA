package com.bignerdranch.android.aioma;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class ForgetPasswordActivity extends AppCompatActivity {

    EditText mEmail;
    Button mSendEmailBtn;
    TextView mSentMessage;
    TextView mErrorMessage;
    ProgressBar mProgressBar;

    String email;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5F5DD8")));


        mEmail = (EditText) findViewById(R.id.input_email);

        mSentMessage = (TextView) findViewById(R.id.sent_message);
        mSentMessage.setVisibility(View.INVISIBLE);

        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mErrorMessage.setVisibility(View.INVISIBLE);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();

        mSendEmailBtn = (Button) findViewById(R.id.send_email_btn);
        mSendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);

                email = mEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)){
                    mEmail.setError("Email is required");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!emailPattern.matcher(email).matches()) {
                    mEmail.setError("Invalid Email Address");
                    mProgressBar.setVisibility(View.INVISIBLE);
                    return;
                }
                else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mSentMessage.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.INVISIBLE);
                            } else {
                                mErrorMessage.setText("Error: " + task.getException().getMessage());
                                mErrorMessage.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.INVISIBLE);

                            }
                        }
                    });

                }
            }
        });

    }

    public final Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    );
}