package com.mycompany.server.manager;

import com.mycompany.server.model.OnlineUser;
import com.mycompany.server.network.ClientHandler;
import com.mycompany.server.db.DatabaseManager;
import org.json.JSONArray;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUsersManager {
    private static OnlineUsersManager instance;
    private final ConcurrentHashMap<Integer, OnlineUser> onlineUsers;

    private OnlineUsersManager() {
        onlineUsers = new ConcurrentHashMap<>();
    }

    public static synchronized OnlineUsersManager getInstance() {
        if (instance == null) {
            instance = new OnlineUsersManager();
        }
        return instance;
    }

    public void addUser(OnlineUser user) {
        onlineUsers.put(user.getUserId(), user);
        System.out.println("[ONLINE] User added: " + user.getUsername() + " (Total: " + onlineUsers.size() + ")");
    }

    public void removeUser(int userId) {
        OnlineUser removed = onlineUsers.remove(userId);
        if (removed != null) {
            System.out.println(
                    "[ONLINE] User removed: " + removed.getUsername() + " (Total: " + onlineUsers.size() + ")");
        }
    }

    public OnlineUser getUser(int userId) {
        return onlineUsers.get(userId);
    }

    public ClientHandler getHandler(int userId) {
        OnlineUser user = onlineUsers.get(userId);
        return user != null ? user.getClientHandler() : null;
    }

    public boolean isUserOnline(int userId) {
        return onlineUsers.containsKey(userId);
    }

    public JSONArray getAvailablePlayersJSON() {
        JSONArray availablePlayers = new JSONArray();
        for (OnlineUser user : onlineUsers.values()) {
            if (!user.isInGame()) {
                availablePlayers.put(user.toJSON());
            }
        }
        return availablePlayers;
    }

    public JSONArray getAllOnlineUsersJSON() {
        JSONArray allUsers = new JSONArray();
        for (OnlineUser user : onlineUsers.values()) {
            allUsers.put(user.toJSON());
        }
        return allUsers;
    }

    public int getOnlineCount() {
        return onlineUsers.size();
    }

    public void setUserInGame(int userId, boolean inGame) {
        OnlineUser user = onlineUsers.get(userId);
        if (user != null) {
            user.setInGame(inGame);
            System.out.println("[ONLINE] User " + user.getUsername() + " inGame status: " + inGame);
        }
    }

    public void updateUserScore(int userId, int increment) {
        OnlineUser user = onlineUsers.get(userId);
        if (user != null) {
            int newScore = user.getScore() + increment;
            user.setScore(newScore);

            // Update DB
            DatabaseManager.getInstance().updateUserScore(userId, newScore);

            System.out.println("[ONLINE] User " + user.getUsername() + " score updated to " + newScore);
        }
    }
}
