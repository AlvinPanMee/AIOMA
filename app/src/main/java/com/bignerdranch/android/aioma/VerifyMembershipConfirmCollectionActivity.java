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

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class VerifyMembershipConfirmCollectionActivity extends AppCompatActivity {

    TextView mPoints;
    TextView mRecipientName;
    TextView mMerchantName;
    TextView mDate;
    TextView mTransactionID;

    Button mConfirmBtn;

    DatabaseReference mScannedMembershipRef;

    int newPoints;

    Integer currentPoints;

    String merchantID;
    String merchantName;
    String transactionID;
    String date;

    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_membership_confirm_collection);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        Intent intent = getIntent();
        String points = intent.getStringExtra("points");
        merchantID = intent.getStringExtra("merchant_ID");
        merchantName = intent.getStringExtra("merchant_Name");


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

        transactionID = UUID.randomUUID().toString();

        mTransactionID = (TextView) findViewById(R.id.transaction_id);
        mTransactionID.setText(transactionID);

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

                            Toast.makeText(VerifyMembershipConfirmCollectionActivity.this,
                                    newPoints + " points has been added to " + merchantName
                                    , Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(VerifyMembershipConfirmCollectionActivity.this, MembershipActivity.class);
                            intent.putExtra("merchantID", merchantID);
                            startActivity(intent);

                            FirebaseDatabase.getInstance().getReference("customerUsers")
                                    .child(uid)
                                    .child("Pending Transaction")
                                    .removeValue();

                        }
                    }
                });


            }
        });


    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(VerifyMembershipConfirmCollectionActivity.this, HomeActivity.class));
        finish();

    }

}