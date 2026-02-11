package com.securitymak.securitymak.model;

public enum SensitivityLevel {

    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    SensitivityLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean canAccess(SensitivityLevel required) {
        return this.level >= required.level;
    }
}