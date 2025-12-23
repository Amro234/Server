package com.mycompany.server.network;

import com.mycompany.server.manager.ChallengeManager;
import com.mycompany.server.manager.OnlineUsersManager;
import com.mycompany.server.model.OnlineUser;
import com.mycompany.server.service.AuthService;
import com.mycompany.server.service.ChallengeService;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

public class ClientHandler implements Runnable {

    public enum RequestType {
        REGISTER,
        LOGIN,
        LOGOUT,
        DISCONNECT,
        GET_ONLINE_USERS,
        GET_AVAILABLE_PLAYERS,
        SEND_CHALLENGE,
        ACCEPT_CHALLENGE,
        DECLINE_CHALLENGE,
        UNKNOWN
    }

    private final Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private Integer currentUserId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Keep connection alive for authenticated users
            while (!socket.isClosed()) {
                String requestStr = in.readUTF();
                JSONObject request = new JSONObject(requestStr);
                JSONObject response = handleRequest(request);

                out.writeUTF(response.toString());
                out.flush();

                // Break if logout or connection should close
                RequestType requestType = parseRequestType(request.optString("type", "UNKNOWN"));
                if (requestType == RequestType.LOGOUT || requestType == RequestType.DISCONNECT) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[CLIENT] IO Error: " + e.getMessage());
        } finally {
            // Remove user from online list when disconnecting
            if (currentUserId != null) {
                OnlineUsersManager.getInstance().removeUser(currentUserId);
                ChallengeManager.getInstance().clearUserChallenges(currentUserId);
            }
            close();
        }
    }

    public void sendNotification(JSONObject notification) {
        try {
            if (out != null && !socket.isClosed()) {
                out.writeUTF(notification.toString());
                out.flush();
            }
        } catch (IOException e) {
            System.err.println("[CLIENT] Error sending notification: " + e.getMessage());
        }
    }

    private RequestType parseRequestType(String type) {
        try {
            return RequestType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RequestType.UNKNOWN;
        }
    }

    private JSONObject handleRequest(JSONObject request) {
        RequestType type = parseRequestType(request.optString("type", "UNKNOWN"));

        switch (type) {
            case REGISTER:
                JSONObject registerResponse = AuthService.register(
                        request.getString("username"),
                        request.getString("email"),
                        request.getString("password"));

                // Add to online users if successful
                if (registerResponse.getBoolean("success")) {
                    JSONObject user = registerResponse.getJSONObject("user");
                    currentUserId = user.getInt("id");
                    OnlineUsersManager.getInstance().addUser(
                            new OnlineUser(
                                    user.getInt("id"),
                                    user.getString("username"),
                                    user.getString("email"),
                                    user.getInt("score"),
                                    this));
                }
                return registerResponse;

            case LOGIN:
                JSONObject loginResponse = AuthService.login(
                        request.getString("username"),
                        request.getString("password"));

                // Add to online users if successful
                if (loginResponse.getBoolean("success")) {
                    JSONObject user = loginResponse.getJSONObject("user");
                    currentUserId = user.getInt("id");
                    OnlineUsersManager.getInstance().addUser(
                            new OnlineUser(
                                    user.getInt("id"),
                                    user.getString("username"),
                                    user.getString("email"),
                                    user.getInt("score"),
                                    this));
                }
                return loginResponse;

            case LOGOUT:
            case DISCONNECT:
                if (currentUserId != null) {
                    OnlineUsersManager.getInstance().removeUser(currentUserId);
                    currentUserId = null;
                }
                JSONObject logoutResponse = new JSONObject();
                logoutResponse.put("success", true);
                return logoutResponse;

            case GET_ONLINE_USERS:
                JSONObject onlineResponse = new JSONObject();
                onlineResponse.put("success", true);
                onlineResponse.put("users", OnlineUsersManager.getInstance().getAllOnlineUsersJSON());
                onlineResponse.put("count", OnlineUsersManager.getInstance().getOnlineCount());
                return onlineResponse;

            case GET_AVAILABLE_PLAYERS:
                JSONObject availableResponse = new JSONObject();
                availableResponse.put("success", true);
                availableResponse.put("players", OnlineUsersManager.getInstance().getAvailablePlayersJSON());
                return availableResponse;

            case SEND_CHALLENGE:
                if (currentUserId == null) {
                    JSONObject authError = new JSONObject();
                    authError.put("success", false);
                    authError.put("message", "Not authenticated");
                    return authError;
                }

                int targetUserId = request.getInt("targetUserId");
                return ChallengeService.sendChallenge(currentUserId, targetUserId);

            case ACCEPT_CHALLENGE:
                if (currentUserId == null) {
                    JSONObject authError2 = new JSONObject();
                    authError2.put("success", false);
                    authError2.put("message", "Not authenticated");
                    return authError2;
                }

                return ChallengeService.acceptChallenge(currentUserId);

            case DECLINE_CHALLENGE:
                if (currentUserId == null) {
                    JSONObject authError3 = new JSONObject();
                    authError3.put("success", false);
                    authError3.put("message", "Not authenticated");
                    return authError3;
                }

                return ChallengeService.declineChallenge(currentUserId);

            default:
                JSONObject response = new JSONObject();
                response.put("success", false);
                response.put("message", "Request type not supported: " + type);
                return response;
        }
    }

    public void close() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
