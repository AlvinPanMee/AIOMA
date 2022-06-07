package com.bignerdranch.android.aioma;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class RewardActivity extends AppCompatActivity {

    DatabaseReference mClickedRewardRef;

    TextView mMerchantName;
    ImageView mMerchantPFP;

    TextView mRewardTitle;
    ImageView mRewardIcon;

    TextView mRedeemed;

    TextView mPointsRequired, mRewardDesc, mRewardPolicy;

    //String and Integer values for relevant reward info
    Integer membershipPoints;

    String merchantID;
    String merchantName;
    String merchantPFPUrl;

    String username;

    String rewardID;
    String rewardTitle;
    Integer pointsRequired;
    String rewardIconUrl;
    String rewardDesc;
    String rewardPolicy;

    String transactionID;
    String date;

    Button mRedeemBtn;
    Button mUseNowBtn;
    Button mRedeemedAndUsedBtn;

    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        //Get info from MembershipActivity
        Intent membershipIntent = getIntent();
        merchantID = membershipIntent.getStringExtra("merchantID");
        merchantName = membershipIntent.getStringExtra("merchantName");
        merchantPFPUrl = membershipIntent.getStringExtra("merchantPFPUrl");

        rewardID = membershipIntent.getStringExtra("rewardID");
        rewardTitle = membershipIntent.getStringExtra("rewardTitle");
        rewardIconUrl = membershipIntent.getStringExtra("rewardIconUrl");
        membershipPoints = membershipIntent.getIntExtra("userMembershipPoints",0);


        //Set TextView and ImageView
        mMerchantName = (TextView) findViewById(R.id.merchant_name);
        mMerchantName.setText(merchantName);

        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        Glide.with(mMerchantPFP.getContext()).load(merchantPFPUrl).into(mMerchantPFP);

        mRewardTitle = (TextView) findViewById(R.id.reward_title);
        mRewardTitle.setText(rewardTitle);

        mRewardIcon = (ImageView) findViewById(R.id.reward_icon);
        Glide.with(mRewardIcon.getContext()).load(rewardIconUrl).into(mRewardIcon);

            //Set TextView onDataChange (snapshot)
        mPointsRequired = (TextView) findViewById(R.id.points_required);
        mRewardDesc = (TextView) findViewById(R.id.reward_desc);
        mRewardPolicy = (TextView) findViewById(R.id.reward_policy);


        DatabaseReference rewardInfoRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID)
                .child("Rewards")
                .child(rewardID);

        rewardInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                rewardDesc = snapshot.child("rewardDesc").getValue(String.class);
                pointsRequired = snapshot.child("pointsRequired").getValue(int.class);
                rewardPolicy = snapshot.child("rewardPolicy").getValue(String.class);

                mRewardDesc.setText(rewardDesc);
                mPointsRequired.setText(String.valueOf(pointsRequired));
                mRewardPolicy.setText(rewardPolicy);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.INVISIBLE);

        Date currentTime = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("d/MM/yyyy, HH:mm:ss");
        date = dateFormat.format(currentTime);

        transactionID = UUID.randomUUID().toString();

    }

    @Override
    protected void onStart() {
        super.onStart();

        mRedeemed = (TextView) findViewById(R.id.redeemed_message);
        mRedeemed.setVisibility(View.INVISIBLE);

        mUseNowBtn = (Button) findViewById(R.id.use_now_btn);
        mUseNowBtn.setVisibility(View.INVISIBLE);

        mRedeemedAndUsedBtn = (Button) findViewById(R.id.redeemed_and_used_btn);
        mRedeemedAndUsedBtn.setVisibility(View.INVISIBLE);


        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(userID)
                .child("username");

        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                username = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference redeemedUsers_ref = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID)
                .child("Rewards")
                .child(rewardID)
                .child("redeemedUsers");

        //User has redeemed and used reward
        redeemedUsers_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.hasChild(userID)){
                    //Redeemed TextView
                    mRedeemed.setVisibility(View.VISIBLE);

                    //Redeemed and Used Button
                    mRedeemedAndUsedBtn.setVisibility(View.VISIBLE);
                    mRedeemedAndUsedBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(RewardActivity.this, "Reward/Voucher has already been used", Toast.LENGTH_LONG).show();
                        }
                    });


                //User has redeemed reward, but has not used
                } else {

                    DatabaseReference membership_ref = FirebaseDatabase.getInstance()
                            .getReference("customerUsers")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Memberships")
                            .child(merchantID);

                    membership_ref.child("Rewards").child(rewardID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                mRedeemed.setVisibility(View.VISIBLE);
                                mUseNowBtn.setVisibility(View.VISIBLE);
                                mUseNowBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent useRewardIntent = new Intent(RewardActivity.this, UseRewardActivity.class);
                                        useRewardIntent.putExtra("merchantID", merchantID);
                                        useRewardIntent.putExtra("merchantName", merchantName);
                                        useRewardIntent.putExtra("merchantPFPUrl", merchantPFPUrl);

                                        useRewardIntent.putExtra("userID", userID);
                                        useRewardIntent.putExtra("username", username);

                                        useRewardIntent.putExtra("rewardID", rewardID);
                                        useRewardIntent.putExtra("rewardTitle", rewardTitle);
                                        useRewardIntent.putExtra("rewardIconUrl", rewardIconUrl);
                                        useRewardIntent.putExtra("rewardDesc", rewardDesc);

                                        startActivity(useRewardIntent);

                                    }
                                });

                            //User has not redeem reward
                            } else {
                                mRedeemBtn = (Button) findViewById(R.id.redeem_btn);
                                mRedeemBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RewardActivity.this);
                                        LayoutInflater layoutInflater = RewardActivity.this.getLayoutInflater();
                                        View dialogView = layoutInflater.inflate(R.layout.dialog_redeem_reward, null);
                                        dialogBuilder.setView(dialogView);

                                        ImageView reward_icon = dialogView.findViewById(R.id.reward_icon);
                                        Glide.with(reward_icon.getRootView()).load(rewardIconUrl).
                                                into(reward_icon);

                                        TextView reward_title = dialogView.findViewById(R.id.reward_title);
                                        reward_title.setText(rewardTitle);

                                        TextView insufficientPoints = dialogView.findViewById(R.id.insufficient_points);
                                        insufficientPoints.setVisibility(View.INVISIBLE);

                                        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {


                                                    }
                                                });
                                        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });

                                        dialogBuilder.setView(dialogView);
                                        AlertDialog dialog = dialogBuilder.create();
                                        dialog.show();
                                        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (membershipPoints >= pointsRequired) {

                                                    //Create transaction history
                                                    Transaction transaction = new Transaction(transactionID, merchantID, merchantName,
                                                            "-" + String.valueOf(pointsRequired) + " points", date, "Redeem from");

                                                    FirebaseDatabase.getInstance().getReference("customerUsers")
                                                            .child(userID)
                                                            .child("Transactions")
                                                            .child(transactionID)
                                                            .setValue(transaction)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                    //Deduct points after redemption and update database
                                                                    Integer updatedPoints = membershipPoints - pointsRequired;
                                                                    membership_ref.child("membershipPoints").setValue(updatedPoints);

                                                                    //construct in Rewards class
                                                                    RedeemedRewards redeemedRewards = new RedeemedRewards(rewardTitle, rewardIconUrl);

                                                                    //Add/set value in database
                                                                    membership_ref.child("Rewards").child(rewardID).setValue(redeemedRewards)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    finish();
                                                                                    overridePendingTransition(0, 0);
                                                                                    startActivity(getIntent());
                                                                                    overridePendingTransition(0, 0);
                                                                                }
                                                                            });
                                                                }
                                                            });

                                                } else {
                                                    insufficientPoints.setVisibility(View.VISIBLE);

                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });



    }

    public void onBackPressed() {
        Intent membershipIntent = new Intent(RewardActivity.this, MembershipActivity.class);
        membershipIntent.putExtra("merchantID", merchantID);
        startActivity(membershipIntent);
        finish();
    }

}