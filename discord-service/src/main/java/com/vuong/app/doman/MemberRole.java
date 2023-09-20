package com.vuong.app.doman;

public enum MemberRole {
    ADMIN(1),
    MODERATOR(2),
    GUEST(3);

    private int value;

    MemberRole(int value) {
        this.value = value;
    }

    public int getNumber() {
        return value;
    }

    public static MemberRole forNumber(int value) {
        switch (value) {
            case 1: return ADMIN;
            case 2: return MODERATOR;
            case 3: return GUEST;
            default: return null;
        }
    }
}
