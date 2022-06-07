package com.bignerdranch.android.aioma;

public class RedeemedRewards {

    public String rewardTitle;
    public String rewardIconUrl;

    public RedeemedRewards() {

    }

    public RedeemedRewards(String rewardTitle, String rewardIconUrl) {
        this.rewardTitle = rewardTitle;
        this.rewardIconUrl = rewardIconUrl;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public String getRewardIconUrl() {
        return rewardIconUrl;
    }

    public void setRewardIconUrl(String rewardIconUrl) {
        this.rewardIconUrl = rewardIconUrl;
    }
}
