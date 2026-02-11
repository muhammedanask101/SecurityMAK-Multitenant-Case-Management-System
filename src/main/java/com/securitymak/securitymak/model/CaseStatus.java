package com.securitymak.securitymak.model;

public enum CaseStatus {

    OPEN {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == IN_PROGRESS;
        }
    },

    IN_PROGRESS {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == REVIEW;
        }
    },

    REVIEW {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == CLOSED || next == IN_PROGRESS; // allow rollback
        }
    },

    CLOSED {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return next == ARCHIVED; // only archival allowed
        }
    },

    ARCHIVED {
        @Override
        public boolean canTransitionTo(CaseStatus next) {
            return false; // permanently locked
        }
    };

    public abstract boolean canTransitionTo(CaseStatus next);
}