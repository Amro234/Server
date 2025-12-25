package com.mycompany.server.manager;

import com.mycompany.server.model.GameSession;
import com.mycompany.server.network.ClientHandler;
import org.json.JSONObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameSessionManager {

    private static GameSessionManager instance;
    private final Map<String, GameSession> sessions;
    private final Map<Integer, String> userSessionMap; // UserId -> SessionId

    private GameSessionManager() {
        sessions = new ConcurrentHashMap<>();
        userSessionMap = new ConcurrentHashMap<>();
    }

    public static synchronized GameSessionManager getInstance() {
        if (instance == null) {
            instance = new GameSessionManager();
        }
        return instance;
    }

    public void createSession(int player1Id, int player2Id) {
        String sessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession(sessionId, player1Id, player2Id);

        sessions.put(sessionId, session);
        userSessionMap.put(player1Id, sessionId);
        userSessionMap.put(player2Id, sessionId);

        // Notify players game started asynchronously to allow the AcceptChallenge
        // response to be sent first
        new Thread(() -> {
            try {
                // Formatting delay to ensure response goes out first
                Thread.sleep(50);
                notifyGameStart(sessionId, player1Id, player2Id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void notifyGameStart(String sessionId, int player1Id, int player2Id) {
        ClientHandler p1 = OnlineUsersManager.getInstance().getHandler(player1Id);
        ClientHandler p2 = OnlineUsersManager.getInstance().getHandler(player2Id);

        String p1Name = OnlineUsersManager.getInstance().getUser(player1Id).getUsername();
        String p2Name = OnlineUsersManager.getInstance().getUser(player2Id).getUsername();

        if (p1 != null) {
            JSONObject msg = new JSONObject();
            msg.put("type", "GAME_STARTED");
            msg.put("sessionId", sessionId);
            msg.put("opponent", p2Name);
            msg.put("yourSymbol", "X");
            p1.sendNotification(msg);
        }

        if (p2 != null) {
            JSONObject msg = new JSONObject();
            msg.put("type", "GAME_STARTED");
            msg.put("sessionId", sessionId);
            msg.put("opponent", p1Name);
            msg.put("yourSymbol", "O");
            p2.sendNotification(msg);
        }
    }

    public JSONObject handleMove(int userId, int row, int col) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId == null) {
            return error("User not in a game");
        }

        GameSession session = sessions.get(sessionId);
        if (session == null) {
            return error("Session not found");
        }

        JSONObject result = session.processMove(userId, row, col);

        if (result.getBoolean("valid")) {
            broadcastMove(session, result);

            String status = result.getString("status");
            if ("WIN".equals(status) || "DRAW".equals(status)) {
                endSession(sessionId, status, result.optInt("winnerId", -1), result);
            }
        }

        return result; // Return to sender (ack)
    }

    private void broadcastMove(GameSession session, JSONObject moveData) {
        ClientHandler p1 = OnlineUsersManager.getInstance().getHandler(session.getPlayer1Id());
        ClientHandler p2 = OnlineUsersManager.getInstance().getHandler(session.getPlayer2Id());

        JSONObject msg = new JSONObject();
        msg.put("type", "GAME_MOVE");
        msg.put("row", moveData.getInt("row"));
        msg.put("col", moveData.getInt("col"));
        msg.put("symbol", moveData.getString("symbol"));
        msg.put("nextTurn", moveData.getInt("nextTurn")); // userId of next player

        if (p1 != null)
            p1.sendNotification(msg);
        if (p2 != null)
            p2.sendNotification(msg);
    }

    private void endSession(String sessionId, String status, int winnerId, JSONObject finalState) {
        GameSession session = sessions.get(sessionId);
        if (session != null) {

            // Update scores in DB and Memory
            if ("WIN".equals(status)) {
                if (winnerId == session.getPlayer1Id()) {
                    session.incrementP1Wins();
                    OnlineUsersManager.getInstance().updateUserScore(session.getPlayer1Id(), 10);
                } else if (winnerId == session.getPlayer2Id()) { // Should be else, but safer
                    session.incrementP2Wins();
                    OnlineUsersManager.getInstance().updateUserScore(session.getPlayer2Id(), 10);
                }
            } else if ("DRAW".equals(status)) {
                session.incrementDraws();
            }

            // Notify Game Over with updated scores and session stats
            ClientHandler p1 = OnlineUsersManager.getInstance().getHandler(session.getPlayer1Id());
            ClientHandler p2 = OnlineUsersManager.getInstance().getHandler(session.getPlayer2Id());

            int p1Score = 0;
            if (OnlineUsersManager.getInstance().getUser(session.getPlayer1Id()) != null)
                p1Score = OnlineUsersManager.getInstance().getUser(session.getPlayer1Id()).getScore();

            int p2Score = 0;
            if (OnlineUsersManager.getInstance().getUser(session.getPlayer2Id()) != null)
                p2Score = OnlineUsersManager.getInstance().getUser(session.getPlayer2Id()).getScore();

            JSONObject msg = new JSONObject();
            msg.put("type", "GAME_OVER");
            msg.put("status", status); // WIN or DRAW
            msg.put("winnerId", winnerId);
            msg.put("p1Score", p1Score);
            msg.put("p2Score", p2Score);

            // Session stats
            msg.put("sessionP1Wins", session.getP1Wins());
            msg.put("sessionP2Wins", session.getP2Wins());
            msg.put("sessionDraws", session.getDraws());

            if (p1 != null)
                p1.sendNotification(msg);
            if (p2 != null)
                p2.sendNotification(msg);
        }
    }

    public void removeSession(String sessionId) {
        GameSession session = sessions.remove(sessionId);
        if (session != null) {
            userSessionMap.remove(session.getPlayer1Id());
            userSessionMap.remove(session.getPlayer2Id());

            OnlineUsersManager.getInstance().setUserInGame(session.getPlayer1Id(), false);
            OnlineUsersManager.getInstance().setUserInGame(session.getPlayer2Id(), false);
        }
    }

    public JSONObject requestRematch(int requesterId) {
        String sessionId = userSessionMap.get(requesterId);
        if (sessionId == null)
            return error("No active session");

        GameSession session = sessions.get(sessionId);
        if (session == null)
            return error("Session not found");

        int opponentId = (requesterId == session.getPlayer1Id()) ? session.getPlayer2Id() : session.getPlayer1Id();
        ClientHandler opponent = OnlineUsersManager.getInstance().getHandler(opponentId);

        if (opponent != null) {
            JSONObject msg = new JSONObject();
            msg.put("type", "REMATCH_REQUESTED");
            opponent.sendNotification(msg);
        }

        JSONObject empty = new JSONObject();
        empty.put("success", true);
        return empty;
    }

    public JSONObject handleRematchResponse(int responderId, boolean accepted) {
        String sessionId = userSessionMap.get(responderId);
        if (sessionId == null)
            return error("No active session");

        GameSession session = sessions.get(sessionId);
        if (session == null)
            return error("Session not found");

        int requesterId = (responderId == session.getPlayer1Id()) ? session.getPlayer2Id() : session.getPlayer1Id();
        ClientHandler requester = OnlineUsersManager.getInstance().getHandler(requesterId);

        if (accepted) {
            // Reset Session
            session.reset();
            notifyGameStart(sessionId, session.getPlayer1Id(), session.getPlayer2Id());
            // This re-sends GAME_STARTED
        } else {
            // Notify rejection
            if (requester != null) {
                JSONObject msg = new JSONObject();
                msg.put("type", "REMATCH_DECLINED");
                requester.sendNotification(msg);
            }
            // Terminate session
            removeSession(sessionId);
        }

        JSONObject res = new JSONObject();
        res.put("success", true);
        return res;
    }

    public void handlePlayerDisconnect(int userId) {
        handlePlayerLeave(userId, "OPPONENT_DISCONNECTED");
    }

    public void handlePlayerLeave(int userId, String reason) {
        String sessionId = userSessionMap.get(userId);
        if (sessionId != null) {
            GameSession session = sessions.get(sessionId);
            if (session != null) {
                int opponentId = (userId == session.getPlayer1Id()) ? session.getPlayer2Id() : session.getPlayer1Id();
                ClientHandler opponent = OnlineUsersManager.getInstance().getHandler(opponentId);

                if (opponent != null) {
                    JSONObject msg = new JSONObject();
                    msg.put("type", "GAME_OVER");
                    msg.put("status", reason);
                    msg.put("winnerId", opponentId); // Opponent wins by default
                    opponent.sendNotification(msg);
                }
                removeSession(sessionId);
            }
        }
    }

    private JSONObject error(String msg) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("message", msg);
        return json;
    }
}
