package com.bignerdranch.android.aioma;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class HomeMembershipListAdapterClass extends RecyclerView.Adapter<HomeMembershipListAdapterClass.AddedMembershipListViewHolder> {

    ArrayList<AddedMembership> list;

    public HomeMembershipListAdapterClass(ArrayList<AddedMembership> list){
        this.list = list;
    }

    @NonNull
    @Override
    public HomeMembershipListAdapterClass.AddedMembershipListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_membership_list_layout,parent,false);
        return new AddedMembershipListViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull AddedMembershipListViewHolder holder, int position) {
        String merchantID = list.get(position).getMerchantID();

        DatabaseReference merchantRef = FirebaseDatabase.getInstance().getReference("merchantUsers")
                .child(merchantID);

        merchantRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                String merchantName = snapshot.child("merchantName").getValue(String.class);
                holder.merchantName.setText(merchantName);

                String merchantPFPUrl = snapshot.child("merchantPFPUrl").getValue(String.class);
                Glide.with(holder.merchantPFP.getContext().getApplicationContext()).load(merchantPFPUrl)
                        .into(holder.merchantPFP);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent membershipIntent = new Intent(v.getContext(), MembershipActivity.class);
                membershipIntent.putExtra("merchantID", list.get(position).merchantID);

                v.getContext().startActivity(membershipIntent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class AddedMembershipListViewHolder extends RecyclerView.ViewHolder {
        AddedMembership mAddedMembership;
        ImageView merchantPFP;
        TextView merchantName;

        public AddedMembershipListViewHolder(@NonNull View itemView){
            super (itemView);
            merchantPFP = itemView.findViewById(R.id.merchantPFP);
            merchantName = itemView.findViewById(R.id.merchantName);
        }

//        public void bind(AddedMembership addedMembership){
//            merchantName.setText(addedMembership.getMerchantName());
//            Glide.with(merchantPFP.getContext()).load(addedMembership.merchantPFPUrl)
//                    .into(merchantPFP);
//
//        }


    }


}
