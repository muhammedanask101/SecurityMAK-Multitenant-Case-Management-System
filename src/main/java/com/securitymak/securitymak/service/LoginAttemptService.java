package com.securitymak.securitymak.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 300; // 5 minutes

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public void loginFailed(String email) {

        Attempt attempt = attempts.getOrDefault(email,
                new Attempt(0, Instant.now()));

        if (isLocked(email)) {
            return;
        }

        attempt.count++;
        attempt.lastAttempt = Instant.now();

        attempts.put(email, attempt);
    }

    public boolean isLocked(String email) {

        Attempt attempt = attempts.get(email);

        if (attempt == null) return false;

        if (attempt.count < MAX_ATTEMPTS) return false;

        long secondsSinceLast =
                Instant.now().getEpochSecond() -
                        attempt.lastAttempt.getEpochSecond();

        if (secondsSinceLast > LOCK_DURATION_SECONDS) {
            attempts.remove(email);
            return false;
        }

        return true;
    }

    private static class Attempt {
        int count;
        Instant lastAttempt;

        Attempt(int count, Instant lastAttempt) {
            this.count = count;
            this.lastAttempt = lastAttempt;
        }
    }
}
