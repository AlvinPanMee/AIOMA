package com.bignerdranch.android.aioma;

public class Merchants {

    public String merchantID;
    public String merchantName;
    public String type;
    public String merchantPFPUrl;

    public Merchants(){

    }

    public Merchants(String merchantID, String merchant_name, String merchant_type, String merchant_PFP_url) {
        this.merchantID = merchantID;
        this.merchantName = merchant_name;
        this.type = merchant_type;
        this.merchantPFPUrl = merchant_PFP_url;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String name) {
        this.merchantName = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMerchantPFPUrl() {
        return merchantPFPUrl;
    }

    public void setMerchantPFPUrl(String merchantPFPUrl) {
        this.merchantPFPUrl = merchantPFPUrl;
    }
}
