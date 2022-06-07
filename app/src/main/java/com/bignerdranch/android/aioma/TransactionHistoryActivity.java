package com.bignerdranch.android.aioma;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class TransactionHistoryActivity extends AppCompatActivity {

    ArrayList<Transaction> list;

    RecyclerView mTransactionHistoryList;

    DatabaseReference mTransactionHistoryRef;

    TextView mTransactionMessage1;
    TextView mTransactionMessage2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setElevation(0);
        actionBar.setTitle("");

        mTransactionMessage1 = (TextView) findViewById(R.id.transaction_history_message);
        mTransactionMessage1.setVisibility(View.INVISIBLE);

        mTransactionMessage2 = (TextView) findViewById(R.id.transaction_history_message_2);
        mTransactionMessage2.setVisibility(View.INVISIBLE);

        mTransactionHistoryList = findViewById(R.id.transaction_history_list);
        mTransactionHistoryList.setHasFixedSize(true);
        mTransactionHistoryList.setLayoutManager(new LinearLayoutManager(this));

        mTransactionHistoryRef = FirebaseDatabase.getInstance().getReference("customerUsers")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("Transactions");


        mTransactionHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    list = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        list.add(ds.getValue(Transaction.class));
                        Collections.sort(list, Transaction.sortByDate);
                    }
                    TransactionHistoryAdapterClass transactionHistoryAdapterClass = new TransactionHistoryAdapterClass(list);
                    mTransactionHistoryList.setAdapter(transactionHistoryAdapterClass);
                } else {
                    mTransactionMessage1.setVisibility(View.VISIBLE);
                    mTransactionMessage2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public class TransactionHistoryAdapterClass extends RecyclerView.Adapter<TransactionHistoryAdapterClass.TransactionHistoryListViewHolder>{

        ArrayList<Transaction> list;

        public TransactionHistoryAdapterClass(ArrayList<Transaction> list){
            this.list = list;
        }

        @NonNull
        @Override
        public TransactionHistoryAdapterClass.TransactionHistoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.transaction_history_list_layout,parent,false);

            return new TransactionHistoryListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionHistoryListViewHolder holder, int position) {
            holder.mTransactionType.setText(list.get(position).getType());
            holder.mMerchantName.setText(" " + list.get(position).getMerchantName());
            holder.mPoints.setText(list.get(position).getPoints());
            holder.mDate.setText(list.get(position).getDate());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        public class TransactionHistoryListViewHolder extends RecyclerView.ViewHolder {

            TextView mTransactionType, mMerchantName, mPoints, mDate;

            public TransactionHistoryListViewHolder(View itemView) {
                super(itemView);

                mTransactionType = itemView.findViewById(R.id.transaction_type);
                mMerchantName = itemView.findViewById(R.id.merchant_name);
                mPoints = itemView.findViewById(R.id.points);
                mDate = itemView.findViewById(R.id.date);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater ();
        inflater.inflate(R.menu.activity_transaction_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history_btn:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TransactionHistoryActivity.this);
                dialogBuilder.setTitle("Are you sure you want to clear history?");
                dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTransactionHistoryRef.removeValue();
                        Intent intent = new Intent(TransactionHistoryActivity.this, HomeActivity.class);
                        startActivity(intent);
                    }
                });
                dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogBuilder.show();


            default:
                return super.onOptionsItemSelected(item);
        }
    }

}