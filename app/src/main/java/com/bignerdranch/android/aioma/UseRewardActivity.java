package com.bignerdranch.android.aioma;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.jetbrains.annotations.NotNull;

public class UseRewardActivity extends AppCompatActivity {


    TextView mRewardTitle;
    ImageView mRewardIcon;

    ImageView QRCodeImageView;


    String merchantID;
    String merchantName;
    String merchantPFPUrl;

    String userID;
    String username;

    String rewardID;
    String rewardTitle;
    String rewardIconUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_use_reward);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Get info from MembershipActivity
        Intent rewardIntent = getIntent();
        merchantID = rewardIntent.getStringExtra("merchantID");
        merchantName = rewardIntent.getStringExtra("merchantName");
        merchantPFPUrl = rewardIntent.getStringExtra("merchantPFPUrl");

        userID = rewardIntent.getStringExtra("userID");
        username = rewardIntent.getStringExtra("username");

        rewardID = rewardIntent.getStringExtra("rewardID");
        rewardTitle = rewardIntent.getStringExtra("rewardTitle");
        rewardIconUrl = rewardIntent.getStringExtra("rewardIconUrl");

        mRewardTitle = (TextView) findViewById(R.id.reward_title);
        mRewardTitle.setText(rewardTitle);

        mRewardIcon = (ImageView) findViewById(R.id.reward_icon);
        Glide.with(mRewardIcon.getContext()).load(rewardIconUrl).into(mRewardIcon);



    }

    @Override
    protected void onStart() {
        super.onStart();

        //QR Code
        QRCodeImageView = (ImageView) findViewById(R.id.QR_code);

        //Initialise multi format writer
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //Initialise bit matrix
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    "User ID: " + userID + "\n" +
                            "Username: " + username + "\n" +
                            "Reward ID: " + rewardID + "\n" +
                            "Reward: " + rewardTitle + "\n" +
                            "Merchant: " + merchantName
                    , BarcodeFormat.QR_CODE, 900, 900);

            //Initialise barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();

            //Initialise bitmap
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            //Set bitmap on imageView
            QRCodeImageView.setImageBitmap(bitmap);


        } catch (
                WriterException e) {
            e.printStackTrace();
        }

        refreshActivity();

    }


    public void refreshActivity() {

        DatabaseReference redeemedUsers_ref = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID)
                .child("Rewards")
                .child(rewardID)
                .child("redeemedUsers");


        redeemedUsers_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if(snapshot.hasChild(userID)){

                    Toast.makeText(UseRewardActivity.this,
                            "Reward/Voucher redeemed successfully"
                            , Toast.LENGTH_SHORT).show();

                    Intent rewardIntent1 = new Intent( UseRewardActivity.this, RewardActivity.class);
                    rewardIntent1.putExtra("merchantID", merchantID);
                    rewardIntent1.putExtra("merchantName", merchantName);
                    rewardIntent1.putExtra("merchantPFPUrl", merchantPFPUrl);
                    rewardIntent1.putExtra("rewardID", rewardID);
                    rewardIntent1.putExtra("rewardTitle", rewardTitle);
                    rewardIntent1.putExtra("rewardIconUrl", rewardIconUrl);
                    startActivity(rewardIntent1);



                } else {
                    refresh(1000);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    public void refresh (int milliseconds){

        final Handler handler = new Handler();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refreshActivity();

            }
        };
        handler.postDelayed(runnable, milliseconds);

    }


    @Override
    public void onBackPressed() {

        Intent rewardIntent = new Intent( UseRewardActivity.this, RewardActivity.class);
        rewardIntent.putExtra("merchantID", merchantID);
        rewardIntent.putExtra("merchantName", merchantName);
        rewardIntent.putExtra("merchantPFPUrl", merchantPFPUrl);
        rewardIntent.putExtra("rewardID", rewardID);
        rewardIntent.putExtra("rewardTitle", rewardTitle);
        rewardIntent.putExtra("rewardIconUrl", rewardIconUrl);
        startActivity(rewardIntent);
        finish();

    }


}