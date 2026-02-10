package com.securitymak.securitymak.model;

public enum CaseStatus {

    OPEN {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == IN_REVIEW;
        }
    },

    IN_REVIEW {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == CLOSED;
        }
    },

    CLOSED {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return false;
        }
    };

    public abstract boolean canTransitionTo(CaseStatus next);
}