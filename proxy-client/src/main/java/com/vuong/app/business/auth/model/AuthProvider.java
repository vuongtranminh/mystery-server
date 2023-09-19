package com.vuong.app.business.auth.model;

public enum AuthProvider {
    local(1),
    facebook(2),
    google(3),
    github(4);

    private int value;

    AuthProvider(int value) {
        this.value = value;
    }

    public int getNumber() {
        return value;
    }

    public static AuthProvider forNumber(int value) {
        switch (value) {
            case 1: return local;
            case 2: return facebook;
            case 3: return google;
            case 4: return github;
            default: return null;
        }
    }
}
