package com.mycompany.server.model;

import org.json.JSONObject;

public class Challenge {
    private final int challengerId;
    private final String challengerUsername;
    private final int challengedId;
    private final String challengedUsername;
    private final long timestamp;

    public Challenge(int challengerId, String challengerUsername, int challengedId, String challengedUsername) {
        this.challengerId = challengerId;
        this.challengerUsername = challengerUsername;
        this.challengedId = challengedId;
        this.challengedUsername = challengedUsername;
        this.timestamp = System.currentTimeMillis();
    }

    public int getChallengerId() {
        return challengerId;
    }

    public String getChallengerUsername() {
        return challengerUsername;
    }

    public int getChallengedId() {
        return challengedId;
    }

    public String getChallengedUsername() {
        return challengedUsername;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("challengerId", challengerId);
        json.put("challengerUsername", challengerUsername);
        json.put("challengedId", challengedId);
        json.put("challengedUsername", challengedUsername);
        json.put("timestamp", timestamp);
        return json;
    }
}
