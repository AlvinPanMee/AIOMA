package com.bignerdranch.android.aioma;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MembershipActivity extends AppCompatActivity {

    TextView mMerchantName;
    ImageView mMerchantPFP;

    TextView mMembershipPoints;

    String merchantID;
    String merchantName;
    String merchantPFP;
    Integer membershipPoints;

    DatabaseReference mClickedMembershipRef;
    DatabaseReference mClickedMembershipCusSideRef;


    //Rewards
    RecyclerView mRewardsList;
    DatabaseReference mRewardsDatabaseRef;
    ArrayList<CreatedRewards> list;
    TextView rewardMessage;

    //QR dialog
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5F5DD8")));

        Intent membershipIntent = getIntent();
        merchantID = membershipIntent.getStringExtra("merchantID");


        //Set merchant PFP & name from merchantUsers (Firebase)
        mClickedMembershipRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID);

        mMerchantPFP = (ImageView) findViewById(R.id.merchant_PFP);
        mMerchantName = (TextView) findViewById(R.id.merchant_name);

        mClickedMembershipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                merchantName = snapshot.child("merchantName").getValue(String.class);
                mMerchantName.setText(merchantName);

                merchantPFP = snapshot.child("merchantPFPUrl").getValue(String.class);
                Glide.with(mMerchantPFP.getContext()).load(merchantPFP).into(mMerchantPFP);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        //Set MembershipPoints from customerUsers (Firebase)
        mMembershipPoints = (TextView) findViewById(R.id.membership_points);
        mClickedMembershipCusSideRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Memberships")
                .child(merchantID);

        mClickedMembershipCusSideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                membershipPoints = snapshot.child("membershipPoints").getValue(int.class);
                mMembershipPoints.setText(String.valueOf(membershipPoints));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });




        mRewardsList = (RecyclerView) findViewById(R.id.rewards_list);
        mRewardsList.setHasFixedSize(true);
        mRewardsList.setLayoutManager(new LinearLayoutManager(this));

        //Set recyclerview layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        mRewardsList.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView


        mRewardsDatabaseRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID)
                .child("Rewards");

        mRewardsDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    rewardMessage.setVisibility(View.INVISIBLE);

                    mRewardsDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                list = new ArrayList<>();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    list.add(dataSnapshot.getValue(CreatedRewards.class));
                                }
                                RewardAdapterClass rewardAdapterClass = new RewardAdapterClass(list);
                                mRewardsList.setAdapter(rewardAdapterClass);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Toast.makeText(MembershipActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    rewardMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


        rewardMessage = (TextView) findViewById(R.id.reward_message);
        rewardMessage.setVisibility(View.INVISIBLE);


        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.activity_membership, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.delete_membership:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MembershipActivity.this);
                LayoutInflater layoutInflater = MembershipActivity.this.getLayoutInflater();
                View dialogView = layoutInflater.inflate(R.layout.dialog_add_membership, null);
                dialogBuilder.setView(dialogView);

                TextView merchantMessage2 = dialogView.findViewById(R.id.dialog_message);
                merchantMessage2.setText("Note! All membership's information will be deleted");

                ImageView merchant_PFP = dialogView.findViewById(R.id.merchantPFP);
                Glide.with(merchant_PFP.getRootView()).load(merchantPFP).
                        into(merchant_PFP);

                TextView add_membership_message = dialogView.findViewById(R.id.confirm_add_membership_message);
                add_membership_message.setText(Html.fromHtml("Are you sure you want to delete "
                        + "<b>" + merchantName +"</b>" + " membership?"));

                dialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MembershipActivity.this, merchantName + " has been deleted",
                                Toast.LENGTH_LONG).show();

                        mClickedMembershipCusSideRef.removeValue();
                        Intent intent = new Intent(MembershipActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });

                dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();

                return true;

            case R.id.merchant_info:
                Intent intent = new Intent(MembershipActivity.this, MerchantInfoActivity.class);
                intent.putExtra("merchantID", merchantID);
                startActivity(intent);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public class RewardAdapterClass extends RecyclerView.Adapter<RewardAdapterClass.RewardListViewHolder>{

        ArrayList<CreatedRewards> list;

        public RewardAdapterClass(ArrayList<CreatedRewards> list){
            this.list = list;
        }

        @NonNull
        @Override
        public RewardAdapterClass.RewardListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reward_list_layout,parent,false);

            return new RewardListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RewardAdapterClass.RewardListViewHolder holder, int position) {
            holder.rewardTitle.setText(list.get(position).getRewardTitle());
            Glide.with(holder.rewardPic.getContext()).load(list.get(position).rewardIconUrl)
                    .into(holder.rewardPic);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent rewardIntent = new Intent(v.getContext(), RewardActivity.class);
                    rewardIntent.putExtra("merchantName", merchantName);
                    rewardIntent.putExtra("merchantPFPUrl", merchantPFP);
                    rewardIntent.putExtra("merchantID", merchantID);

                    rewardIntent.putExtra("rewardID", list.get(position).rewardID);
                    rewardIntent.putExtra("rewardTitle", list.get(position).rewardTitle);
                    rewardIntent.putExtra("rewardIconUrl", list.get(position).rewardIconUrl);

                    rewardIntent.putExtra("userMembershipPoints", membershipPoints);

                    v.getContext().startActivity(rewardIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        public class RewardListViewHolder extends RecyclerView.ViewHolder {
            ImageView rewardPic;
            TextView rewardTitle;

            public RewardListViewHolder (View itemView){
                super(itemView);
                rewardPic = itemView.findViewById(R.id.reward_pic);
                rewardTitle = itemView.findViewById(R.id.reward_title);
            }
        }


    }

    public void onBackPressed() {
        Intent homeActivityIntent = new Intent(MembershipActivity.this, HomeActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(homeActivityIntent);

        finish();
    }

}