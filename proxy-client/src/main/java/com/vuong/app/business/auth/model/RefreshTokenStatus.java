package com.vuong.app.business.auth.model;

public enum RefreshTokenStatus {
    READY(1),
    USED(2);

    private int value;

    RefreshTokenStatus(int value) {
        this.value = value;
    }

    public int getNumber() {
        return value;
    }

    public static RefreshTokenStatus forNumber(int value) {
        switch (value) {
            case 1: return READY;
            case 2: return USED;
            default: return null;
        }
    }
}
