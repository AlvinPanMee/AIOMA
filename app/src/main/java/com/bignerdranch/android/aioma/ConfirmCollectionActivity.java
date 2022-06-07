package com.bignerdranch.android.aioma;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ConfirmCollectionActivity extends AppCompatActivity {

    TextView mPoints;
    TextView mRecipientName;
    TextView mMerchantName;
    TextView mDate;
    TextView mTransactionID;

    Button mScanAgainBtn;
    Button mConfirmBtn;

    DatabaseReference mScannedMembershipRef;

    int newPoints;

    Integer currentPoints;

    String merchantID;
    String merchantName;
    String transactionID;
    String date;

    String uid;

    //QR scan result
    String QRPoints;
    String QRMerchantID;
    String QRMerchantName;
    String QRTransactionPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_collection);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        Intent intent = getIntent();
        QRTransactionPasscode = intent.getStringExtra("transaction_Passcode");
        String points = intent.getStringExtra("points");
        merchantID = intent.getStringExtra("merchant_ID");
        merchantName = intent.getStringExtra("merchant_Name");


        transactionID = UUID.randomUUID().toString();

        mScannedMembershipRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Memberships").child(merchantID);

        mPoints = (TextView) findViewById(R.id.points);
        mPoints.setText(points);
        newPoints = Integer.parseInt(mPoints.getText().toString());

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mRecipientName = (TextView) findViewById(R.id.recipient_name);
        DatabaseReference recipientName = FirebaseDatabase.getInstance().getReference("customerUsers");

        recipientName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String recipientName = snapshot.child(uid).child("username").getValue(String.class);
                mRecipientName.setText(recipientName);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantName.setText(merchantName);


        mDate = (TextView) findViewById(R.id.date);
        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy, HH:mm:ss");
        mDate.setText(dateFormat.format(currentTime));
        date = dateFormat.format(currentTime);

        mTransactionID = (TextView) findViewById(R.id.transaction_id);
        mTransactionID.setText(transactionID);

        mScanAgainBtn = (Button) findViewById(R.id.scan_again_btn);
        mScanAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanner();
            }
        });



        mScannedMembershipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                currentPoints = dataSnapshot.child("membershipPoints").getValue(int.class);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mConfirmBtn = (Button) findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transaction transaction = new Transaction(transactionID, merchantID, merchantName,
                        "+" + String.valueOf(newPoints) + " points", date, "Collection from");

                FirebaseDatabase.getInstance().getReference("customerUsers")
                        .child(uid)
                        .child("Transactions")
                        .child(transactionID)
                        .setValue(transaction)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Integer updatedPoints = currentPoints + newPoints;

                            mScannedMembershipRef.child("membershipPoints").setValue(updatedPoints);

                            Toast.makeText(ConfirmCollectionActivity.this,
                                    newPoints + " points has been added to " + merchantName
                                    , Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ConfirmCollectionActivity.this, MembershipActivity.class);
                            intent.putExtra("merchantID", merchantID);
                            startActivity(intent);

                            DatabaseReference pendingTransactionRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                                    .child(uid)
                                    .child("Pending Transaction");

                            pendingTransactionRef.removeValue();

                            FirebaseDatabase.getInstance().getReference("Transaction Passcode")
                                    .child(QRTransactionPasscode)
                                    .removeValue();
                        }
                    }
                });


            }
        });


    }

    public void QRScanner(){

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scanning QR Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {

                //QR code for points collection
                if(result.getContents().startsWith("Grant Membership Points")){
                    Intent intent = new Intent(getApplicationContext(), ConfirmCollectionActivity.class);
                    String[] tokens = result.getContents().split("\n");

                    for(int i = 0; i < tokens.length; i++){
                        System.out.println(" "+tokens[i]);

                        if(tokens[i].startsWith("Points:")) {
                            QRPoints = tokens[i].substring(8);
                            intent.putExtra("points", QRPoints);

                        } else if (tokens[i].startsWith("Merchant:")){
                            QRMerchantName = tokens[i].substring(10);
                            intent.putExtra("merchant_Name", QRMerchantName);

                        } else if(tokens[i].startsWith("Merchant ID:")){
                            QRMerchantID = tokens[i].substring(13);
                            intent.putExtra("merchant_ID", QRMerchantID);


                            DatabaseReference userMembershipList = FirebaseDatabase.getInstance()
                                    .getReference("customerUsers")
                                    .child(uid)
                                    .child("Memberships");

                            userMembershipList.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(QRMerchantID)){
                                        //do nothing and continue checking

                                    } else {
                                        Toast.makeText(ConfirmCollectionActivity.this,
                                                "This Membership Has Not Been Added",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent1 = new Intent(ConfirmCollectionActivity.this, HomeActivity.class);
                                        startActivity(intent1);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull  DatabaseError error) {

                                }
                            });

                        } else if(tokens[i].startsWith("Transaction Passcode:")) {
                            QRTransactionPasscode = tokens[i].substring(22);
                            intent.putExtra("transaction_Passcode", QRTransactionPasscode);

                            DatabaseReference pendingTransactionIDRef = FirebaseDatabase.getInstance()
                                    .getReference("Transaction Passcode");

                            pendingTransactionIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(QRTransactionPasscode)){
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(ConfirmCollectionActivity.this,
                                                "QR Code is no longer valid",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent1 = new Intent(ConfirmCollectionActivity.this, HomeActivity.class);
                                        startActivity(intent1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });


                        }
                    }
                } else {
                    Toast.makeText(ConfirmCollectionActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "No Result", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ConfirmCollectionActivity.this, HomeActivity.class));
        FirebaseDatabase.getInstance().getReference("Transaction Passcode")
                .child(QRTransactionPasscode)
                .removeValue();
        finish();

    }

}