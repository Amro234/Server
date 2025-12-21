package com.mycompany.server.service;

import com.mycompany.server.manager.ChallengeManager;
import com.mycompany.server.manager.OnlineUsersManager;
import com.mycompany.server.model.Challenge;
import com.mycompany.server.model.OnlineUser;
import com.mycompany.server.network.ClientHandler;
import org.json.JSONObject;

public class ChallengeService {

    public static JSONObject sendChallenge(int challengerId, int targetUserId) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        OnlineUser challenger = OnlineUsersManager.getInstance().getUser(challengerId);
        OnlineUser challenged = OnlineUsersManager.getInstance().getUser(targetUserId);

        if (challenged == null) {
            response.put("message", "Player is not online");
            return response;
        }

        if (challenged.isInGame()) {
            response.put("message", "Player is currently in a game");
            return response;
        }

        Challenge challenge = new Challenge(
                challengerId,
                challenger.getUsername(),
                targetUserId,
                challenged.getUsername());

        if (!ChallengeManager.getInstance().createChallenge(challenge)) {
            response.put("message", "Player already has a pending challenge");
            return response;
        }

        // Send notification to challenged player
        ClientHandler targetHandler = OnlineUsersManager.getInstance().getHandler(targetUserId);
        if (targetHandler != null) {
            JSONObject notification = new JSONObject();
            notification.put("type", "CHALLENGE_RECEIVED");
            notification.put("challengerId", challengerId);
            notification.put("challengerUsername", challenger.getUsername());
            notification.put("message", challenger.getUsername() + " is challenging you!");
            targetHandler.sendNotification(notification);
        }

        response.put("success", true);
        response.put("message", "Challenge sent to " + challenged.getUsername());
        return response;
    }

    public static JSONObject acceptChallenge(int acceptingUserId) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        Challenge acceptedChallenge = ChallengeManager.getInstance()
                .removePendingChallenge(acceptingUserId);

        if (acceptedChallenge == null) {
            response.put("message", "No pending challenge");
            return response;
        }

        // Set both players as in game
        OnlineUsersManager.getInstance().setUserInGame(acceptingUserId, true);
        OnlineUsersManager.getInstance().setUserInGame(acceptedChallenge.getChallengerId(), true);

        // Notify the challenger that challenge was accepted
        ClientHandler challengerHandler = OnlineUsersManager.getInstance()
                .getHandler(acceptedChallenge.getChallengerId());
        if (challengerHandler != null) {
            JSONObject acceptNotification = new JSONObject();
            acceptNotification.put("type", "CHALLENGE_ACCEPTED");
            acceptNotification.put("acceptedBy", acceptingUserId);
            acceptNotification.put("acceptedByUsername",
                    OnlineUsersManager.getInstance().getUser(acceptingUserId).getUsername());
            acceptNotification.put("message", "Challenge accepted! Game starting...");
            challengerHandler.sendNotification(acceptNotification);
        }

        response.put("success", true);
        response.put("message", "Challenge accepted");
        response.put("opponent", acceptedChallenge.toJSON());
        return response;
    }

    public static JSONObject declineChallenge(int decliningUserId) {
        JSONObject response = new JSONObject();
        response.put("success", false);

        Challenge declinedChallenge = ChallengeManager.getInstance()
                .removePendingChallenge(decliningUserId);

        if (declinedChallenge == null) {
            response.put("message", "No pending challenge");
            return response;
        }

        // Notify the challenger that challenge was declined
        ClientHandler declinedChallengerHandler = OnlineUsersManager.getInstance()
                .getHandler(declinedChallenge.getChallengerId());
        if (declinedChallengerHandler != null) {
            OnlineUser decliningUser = OnlineUsersManager.getInstance().getUser(decliningUserId);
            JSONObject declineNotification = new JSONObject();
            declineNotification.put("type", "CHALLENGE_DECLINED");
            declineNotification.put("declinedBy", decliningUserId);
            declineNotification.put("declinedByUsername", decliningUser.getUsername());
            declineNotification.put("message", decliningUser.getUsername() + " declined your challenge");
            declinedChallengerHandler.sendNotification(declineNotification);
        }

        response.put("success", true);
        response.put("message", "Challenge declined");
        return response;
    }
}
