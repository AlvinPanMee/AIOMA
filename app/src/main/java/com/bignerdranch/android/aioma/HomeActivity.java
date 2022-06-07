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
import android.widget.Button;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity {
    String uid;

    FirebaseUser user;

    ArrayList<AddedMembership> list;

    TextView mAddMembershipMessage;
    RecyclerView mAddedMerchantsList;

    FirebaseDatabase mCustomerUsersDatabase;
    DatabaseReference mAddedMerchantDatabaseRef;


    ImageView mQRScannerBtn;
    ImageView mProfileBtn;

    //QR scan result
    String QRPoints;
    String QRMerchantID;
    String QRMerchantName;
    String QRMerchantPFPUrl;
    String QRTransactionPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5F5DD8")));


        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();


        mAddMembershipMessage = (TextView)findViewById(R.id.add_membership_message);
        mAddMembershipMessage.setVisibility(View.INVISIBLE);

        mAddedMerchantsList = (RecyclerView) findViewById(R.id.added_merchants_list);;
        mAddedMerchantsList.setHasFixedSize(true);
        mAddedMerchantsList.setLayoutManager(new LinearLayoutManager(this));

        //Set recyclerview layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),3);
        mAddedMerchantsList.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mCustomerUsersRef = databaseReference.child("customerUsers");


        Button buttonSearch = findViewById(R.id.add_membership);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });


        //Navbar
        mQRScannerBtn = (ImageView) findViewById(R.id.scan_QR_code_btn);
        mQRScannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRScanner();
            }
        });

        mProfileBtn = (ImageView) findViewById(R.id.profile_nav);
        mProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(profileIntent);
            }
        });


        //reference added merchants from database
        mCustomerUsersDatabase = FirebaseDatabase.getInstance();

        mAddedMerchantDatabaseRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Memberships");

        Button verifyMembershipBtn = findViewById(R.id.verify_membership_btn);
        verifyMembershipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, VerifyMembershipActivity.class);
                startActivity(intent);
            }
        });



    }


    @Override
    protected void onStart() {
        super.onStart();

        //check added membership in database
        mAddedMerchantDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //hide add membership message
                    mAddMembershipMessage.setVisibility(View.GONE);

                    //get added merchants' information
                    mAddedMerchantDatabaseRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                list = new ArrayList<>();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    list.add(ds.getValue(AddedMembership.class));
                                }
                                HomeMembershipListAdapterClass homeMembershipListAdapterClass = new HomeMembershipListAdapterClass(list);
                                mAddedMerchantsList.setAdapter(homeMembershipListAdapterClass);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {
                            Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    //show add membership message
                    mAddMembershipMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }



    public void search(){
        startActivity(new Intent(getApplicationContext(),SearchActivity.class));
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
                                        Toast.makeText(HomeActivity.this,
                                                "This Membership Has Not Been Added",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent1 = new Intent(HomeActivity.this, HomeActivity.class);
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
                                        Toast.makeText(HomeActivity.this,
                                                "QR Code is no longer valid",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent1 = new Intent(HomeActivity.this, HomeActivity.class);
                                        startActivity(intent1);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });


                        }
                    }

                //QR code to add membership
                } else if (result.getContents().startsWith("Merchant Name:")){

                    String[] tokens = result.getContents().split("\n");

                    for(int i = 0; i < tokens.length; i++){
                        System.out.println(" "+tokens[i]);

                        if(tokens[i].startsWith("Merchant Name:")) {
                            QRMerchantName = tokens[i].substring(15);


                        } else if(tokens[i].startsWith("Merchant ID:")){
                            QRMerchantID = tokens[i].substring(13);


                        } else if (tokens[i].startsWith("merchantPFPUrl:")) {
                            QRMerchantPFPUrl = tokens[i].substring(15);
                        }
                    }

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                    LayoutInflater layoutInflater = HomeActivity.this.getLayoutInflater();
                    View dialogView = layoutInflater.inflate(R.layout.dialog_add_membership, null);
                    dialogBuilder.setView(dialogView);

                    //set merchant PFP in dialog

                    ImageView merchant_PFP = dialogView.findViewById(R.id.merchantPFP);
                    Glide.with(merchant_PFP.getContext()).load(QRMerchantPFPUrl).
                            into(merchant_PFP);

                    //set membership message in dialog

                    TextView add_membership_message = dialogView.findViewById(R.id.confirm_add_membership_message);
                    add_membership_message.setText(Html.fromHtml("Do you want to add "
                            + "<b>" +  QRMerchantName +"</b>" + " membership?"));

                    //set added merchant message in dialog
                    TextView dialog_message;

                    dialog_message = dialogView.findViewById(R.id.dialog_message);
                    dialog_message.setVisibility(View.INVISIBLE);

                    //Yes and No buttons

                    //No
                    dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    //Yes
                    dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    });

                    dialogBuilder.setView(dialogView);
                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                    .getReference("customerUsers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("Memberships");

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(QRMerchantID)) {
                                        dialog_message.setText("Membership has already been added");
                                        dialog_message.setVisibility(View.VISIBLE);

                                    } else {
                                        AddMerchant();
                                        Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(HomeActivity.this, QRMerchantName +
                                                " added", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                        }
                    });

                } else {
                    Toast.makeText(HomeActivity.this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "No Result", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void AddMerchant(){

        DatabaseReference add_membership_ref = FirebaseDatabase.getInstance()
                .getReference("customerUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Memberships");

        final String merchantID = QRMerchantID;
        final int membershipPoints = 0;

        AddedMembership addedMembership = new AddedMembership(merchantID, membershipPoints);

        add_membership_ref.child(QRMerchantID).setValue(addedMembership);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transaction_history_btn:
                Intent intent = new Intent(HomeActivity.this, TransactionHistoryActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }









    public void onBackPressed(){
       //no activity to go back
    }
}