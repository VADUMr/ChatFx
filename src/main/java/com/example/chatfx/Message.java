package com.example.chatfx;

public class Message {
    private String nickname;
    private String message;
    private String ipAddress;
    private int id;

    public Message(int id, String nickname, String message, String ipAddress) {
        this.nickname = nickname;
        this.message = message;
        this.ipAddress = ipAddress;
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public String getNickname() {
        return nickname;
    }

    public String getText() {
        return message;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
