package com.mycompany.server.manager;

import com.mycompany.server.model.Challenge;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengeManager {
    private static ChallengeManager instance;
    private final ConcurrentHashMap<Integer, Challenge> pendingChallenges; // Key: challengedUserId

    private ChallengeManager() {
        pendingChallenges = new ConcurrentHashMap<>();
    }

    public static synchronized ChallengeManager getInstance() {
        if (instance == null) {
            instance = new ChallengeManager();
        }
        return instance;
    }

    public boolean createChallenge(Challenge challenge) {
        // Check if the challenged user already has a pending challenge
        if (pendingChallenges.containsKey(challenge.getChallengedId())) {
            return false; // User already has a pending challenge
        }

        pendingChallenges.put(challenge.getChallengedId(), challenge);
        System.out.println("[CHALLENGE] " + challenge.getChallengerUsername() +
                " challenged " + challenge.getChallengedUsername());
        return true;
    }

    public Challenge getChallenge(int challengedUserId) {
        return pendingChallenges.get(challengedUserId);
    }

    public Challenge removePendingChallenge(int challengedUserId) {
        Challenge challenge = pendingChallenges.remove(challengedUserId);
        if (challenge != null) {
            System.out.println("[CHALLENGE] Challenge removed for user: " + challenge.getChallengedUsername());
        }
        return challenge;
    }

    public boolean hasPendingChallenge(int userId) {
        return pendingChallenges.containsKey(userId);
    }

    public void clearUserChallenges(int userId) {
        // Remove if user is being challenged
        pendingChallenges.remove(userId);

        // Remove if user was the challenger
        pendingChallenges.values().removeIf(challenge -> challenge.getChallengerId() == userId);
    }
}
