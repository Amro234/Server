package com.mycompany.server.model;

import com.mycompany.server.network.ClientHandler;
import org.json.JSONObject;

public class OnlineUser {
    private final int userId;
    private final String username;
    private final String email;
    private final int score;
    private final ClientHandler clientHandler;
    private boolean isInGame;

    public OnlineUser(int userId, String username, String email, int score, ClientHandler clientHandler) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.score = score;
        this.clientHandler = clientHandler;
        this.isInGame = false;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getScore() {
        return score;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public void setInGame(boolean inGame) {
        this.isInGame = inGame;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("username", username);
        json.put("email", email);
        json.put("score", score);
        json.put("isInGame", isInGame);
        return json;
    }
}
