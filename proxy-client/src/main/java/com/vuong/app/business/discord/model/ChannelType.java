package com.vuong.app.business.discord.model;

public enum ChannelType {
    TEXT(1),
    AUDIO(2),
    VIDEO(3);

    private int value;

    ChannelType(int value) {
        this.value = value;
    }

    public int getNumber() {
        return value;
    }

    public static ChannelType forNumber(int value) {
        switch (value) {
            case 1: return TEXT;
            case 2: return AUDIO;
            case 3: return VIDEO;
            default: return null;
        }
    }
}
