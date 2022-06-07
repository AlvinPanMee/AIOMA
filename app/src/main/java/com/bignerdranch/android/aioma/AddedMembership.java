package com.bignerdranch.android.aioma;

public class AddedMembership extends Merchants{

    public String merchantID;
    public int membershipPoints;

    public AddedMembership(){

    }

    public AddedMembership(String merchant_ID, int membership_points) {
        this.merchantID = merchant_ID;
        this.membershipPoints = membership_points;
    }

    @Override
    public String getMerchantID() {
        return merchantID;
    }

    @Override
    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }





}
