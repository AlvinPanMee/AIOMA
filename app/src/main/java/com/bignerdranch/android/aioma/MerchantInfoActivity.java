package com.bignerdranch.android.aioma;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class MerchantInfoActivity extends AppCompatActivity {

    ImageView mMerchantPFP, mCopyBtn;
    TextView mMerchantName, mMerchantEmail, mMerchantType;

    String merchantID;
    String merchantName;
    String merchantPFP;
    String merchantEmail;
    String merchantType;

    DatabaseReference mMerchantRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_info);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        Intent intent = getIntent();
        merchantID = intent.getStringExtra("merchantID");

        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantEmail = (TextView) findViewById(R.id.merchant_email);
        mMerchantType = (TextView) findViewById(R.id.merchant_type);

        mMerchantRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID);

        mMerchantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantPFP = snapshot.child("merchantPFPUrl").getValue(String.class);
                merchantName = snapshot.child("merchantName").getValue(String.class);
                merchantEmail = snapshot.child("email").getValue(String.class);
                merchantType = snapshot.child("type").getValue(String.class);

                Glide.with(mMerchantPFP).load(merchantPFP).into(mMerchantPFP);
                mMerchantName.setText(merchantName);
                mMerchantEmail.setText(merchantEmail);
                mMerchantType.setText(merchantType);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mCopyBtn = (ImageView) findViewById(R.id.copy_btn);
        mCopyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("merchant_email", merchantEmail);
                clipboardManager.setPrimaryClip(data);

                Toast.makeText(MerchantInfoActivity.this, "Merchant email copied", Toast.LENGTH_SHORT).show();
            }
        });
    }
}