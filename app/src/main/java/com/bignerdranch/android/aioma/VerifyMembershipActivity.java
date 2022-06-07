package com.bignerdranch.android.aioma;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.jetbrains.annotations.NotNull;

public class VerifyMembershipActivity extends AppCompatActivity {

    String uid;

    TextView mPointsCollectionMessage;

    DatabaseReference pendingTransactionRef;
    Integer totalTransactions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_membership);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");


        mPointsCollectionMessage = (TextView) findViewById(R.id.points_collection_message);
        mPointsCollectionMessage.setVisibility(View.INVISIBLE);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //QR code
        ImageView mQRCode = findViewById(R.id.qr_code);

        //Initialise multi format writer
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        try {
            //Initialise bit matrix
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    "Verify Membership" + "\n" +
                            "User ID: " + uid
                    , BarcodeFormat.QR_CODE, 1000, 1000);

            //Initialise barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();

            //Initialise bitmap
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            //Set bitmap on imageView
            mQRCode.setImageBitmap(bitmap);


        } catch (
                WriterException e) {
            e.printStackTrace();
        }




        pendingTransactionRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(uid)
                .child("Pending Transaction");


        pendingTransactionRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull @NotNull DataSnapshot snapshot, String previousChildName) {
                String points = snapshot.child("points").getValue(String.class);
                String merchantName = snapshot.child("merchantName").getValue(String.class);
                String merchantID = snapshot.child("merchantID").getValue(String.class);


                Intent intent = new Intent(VerifyMembershipActivity.this, VerifyMembershipConfirmCollectionActivity.class);
                intent.putExtra("points", points);
                intent.putExtra("merchant_ID", merchantID);
                intent.putExtra("merchant_Name", merchantName);
                startActivity(intent);

            }

            @Override
            public void onChildChanged(@NonNull @NotNull DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull @NotNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull @NotNull DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


}