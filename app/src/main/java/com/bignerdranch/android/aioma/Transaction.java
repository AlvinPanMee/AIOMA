package com.bignerdranch.android.aioma;

import java.util.Comparator;

public class Transaction {

    public String transactionID;
    public String merchantID;
    public String merchantName;
    public String points;
    public String date;
    public String type;

    public Transaction() {
    }

    public Transaction(String transactionID, String merchantID, String merchantName, String points, String date, String type) {
        this.transactionID = transactionID;
        this.merchantID = merchantID;
        this.merchantName = merchantName;
        this.points = points;
        this.date = date;
        this.type = type;
    }

    public static Comparator<Transaction> sortByDate = new Comparator<Transaction>() {
        @Override
        public int compare(Transaction o1, Transaction o2) {
            return o2.getDate().compareTo(o1.getDate());
        }
    };

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
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

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
