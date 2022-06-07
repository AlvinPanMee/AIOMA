package com.bignerdranch.android.aioma;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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


public class SearchAdapterClass extends RecyclerView.Adapter<SearchAdapterClass.MerchantSearchListViewHolder> {

    ArrayList<Merchants> list;


    public SearchAdapterClass(ArrayList<Merchants> list){
        this.list = list;
    }

    @NonNull
    @Override
    public MerchantSearchListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_membership_list_layout,parent,false);
        return new MerchantSearchListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapterClass.MerchantSearchListViewHolder holder, int position) {

        holder.merchantName.setText(list.get(position).getMerchantName());
        holder.type.setText(list.get(position).getType());
        Glide.with(holder.merchantPFP.getContext()).load(list.get(position).merchantPFPUrl)
                .into(holder.merchantPFP);


        //add membership dialog
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getRootView().getContext());

                View dialogView = LayoutInflater.from(view.getRootView().getContext())
                        .inflate(R.layout.dialog_add_membership,null);


                //set merchant PFP in dialog
                ImageView merchant_PFP = dialogView.findViewById(R.id.merchantPFP);
                Glide.with(merchant_PFP.getContext()).load(list.get(position).merchantPFPUrl).
                        into(merchant_PFP);

                //set membership message in dialog

                TextView add_membership_message = dialogView.findViewById(R.id.confirm_add_membership_message);
                add_membership_message.setText(Html.fromHtml("Do you want to add "
                        + "<b>" +  list.get(position).getMerchantName() +"</b>" + " membership?"));

                //set added merchant message in dialog
                TextView dialog_message;

                dialog_message = dialogView.findViewById(R.id.dialog_message);
                dialog_message.setVisibility(View.INVISIBLE);

                //Yes and No buttons

                    //No
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                    //Yes
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.setView(dialogView);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference merchantRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                                .child(list.get(position).merchantID);

                        merchantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                if(!snapshot.hasChild("FreeMembership")){
                                    dialog_message.setText("This membership is not free. \n Join this membership at the merchant's store"  );
                                    dialog_message.setVisibility(View.VISIBLE);

                                } else {
                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                                            .getReference("customerUsers")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("Memberships");

                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(list.get(position).merchantID)) {
                                                dialog_message.setText("Membership has already been added");
                                                dialog_message.setVisibility(View.VISIBLE);

                                            } else {
                                                AddMerchant();
                                                Intent intent = new Intent(view.getContext(), HomeActivity.class);
                                                view.getContext().startActivity(intent);
                                                Toast.makeText(view.getContext(), list.get(position).merchantName +
                                                        " added", Toast.LENGTH_SHORT).show();
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
                });
            }

            private void AddMerchant(){

                DatabaseReference add_membership_ref = FirebaseDatabase.getInstance()
                        .getReference("customerUsers")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Memberships");

                final String merchantID = list.get(position).merchantID;
                final int membershipPoints = 0;

                AddedMembership addedMembership = new AddedMembership(merchantID, membershipPoints);

                add_membership_ref.child(list.get(position).merchantID).setValue(addedMembership);


            }
        });

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    //Get information to place in ViewHolder (CardView list)
    class MerchantSearchListViewHolder extends RecyclerView.ViewHolder{
        TextView merchantName;
        TextView type;
        ImageView merchantPFP;

        public MerchantSearchListViewHolder(@NonNull View itemView){
            super (itemView);

            merchantName = itemView.findViewById(R.id.merchantName);
            type = itemView.findViewById(R.id.merchantType);
            merchantPFP = itemView.findViewById(R.id.merchantPFP);
        }
    }



}
