package com.ekdorn.stealapeak;

public class User {
    private String name, token;

    public User(String name, String token) {
        this.name = name;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
