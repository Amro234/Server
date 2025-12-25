package com.mycompany.server.model;

import org.json.JSONObject;

public class GameSession {
    private final String sessionId;
    private final int player1Id;
    private final int player2Id;
    private char[][] board;
    private boolean isPlayer1Turn; // true = X (p1), false = O (p2)
    private boolean isActive;

    // Session State Tracking
    private int p1Wins;
    private int p2Wins;
    private int draws;

    public GameSession(String sessionId, int player1Id, int player2Id) {
        this.sessionId = sessionId;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.board = new char[3][3];
        this.isPlayer1Turn = true; // X always starts
        this.isActive = true;
        this.p1Wins = 0;
        this.p2Wins = 0;
        this.draws = 0;

        // Initialize board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' '; // Empty
            }
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public int getP1Wins() {
        return p1Wins;
    }

    public int getP2Wins() {
        return p2Wins;
    }

    public int getDraws() {
        return draws;
    }

    public void incrementP1Wins() {
        p1Wins++;
    }

    public void incrementP2Wins() {
        p2Wins++;
    }

    public void incrementDraws() {
        draws++;
    }

    public boolean isTurn(int userId) {
        if (isPlayer1Turn) {
            return userId == player1Id;
        } else {
            return userId == player2Id;
        }
    }

    public JSONObject processMove(int userId, int row, int col) {
        JSONObject result = new JSONObject();
        result.put("valid", false);

        if (!isActive) {
            result.put("message", "Game is not active");
            return result;
        }

        if (!isTurn(userId)) {
            result.put("message", "Not your turn");
            return result;
        }

        if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != ' ') {
            result.put("message", "Invalid move");
            return result;
        }

        // Make move
        char symbol = isPlayer1Turn ? 'X' : 'O';
        board[row][col] = symbol;

        // Check win
        WinInfo win = checkWin();
        if (win != null) {
            isActive = false;
            result.put("status", "WIN");
            result.put("winnerId", userId);
            result.put("winType", win.type); // optional
        } else if (isBoardFull()) {
            isActive = false;
            result.put("status", "DRAW");
        } else {
            result.put("status", "CONTINUE");
            isPlayer1Turn = !isPlayer1Turn;
        }

        result.put("valid", true);
        result.put("row", row);
        result.put("col", col);
        result.put("symbol", String.valueOf(symbol));
        result.put("nextTurn", isPlayer1Turn ? player1Id : player2Id);

        return result;
    }

    public void reset() {
        // Reset board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
        // Switch starting player for next game?
        // Let's alternate from initial state.
        // For simplicity, let's keep X as P1 and O as P2, but maybe logic decides who
        // plays X?
        // In this implementation, P1 is always X.
        // So we just reset turn to P1 (X).
        this.isPlayer1Turn = true;
        this.isActive = true;
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ')
                    return false;
            }
        }
        return true;
    }

    private WinInfo checkWin() {
        // Rows
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return new WinInfo(board[i][0], "HORIZONTAL");
            }
        }
        // Cols
        for (int j = 0; j < 3; j++) {
            if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return new WinInfo(board[0][j], "VERTICAL");
            }
        }
        // Diagonals
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return new WinInfo(board[0][0], "DIAGONAL_MAIN");
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return new WinInfo(board[0][2], "DIAGONAL_ANTI");
        }
        return null;
    }

    private static class WinInfo {
        char winner;
        String type;

        WinInfo(char winner, String type) {
            this.winner = winner;
            this.type = type;
        }
    }
}
