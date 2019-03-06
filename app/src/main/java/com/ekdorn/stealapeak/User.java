package com.ekdorn.stealapeak;

public class User {
    private String name, key;
    private boolean notificationOpened;

    public User(String name, String key, boolean notificationOpened) {
        this.name = name;
        this.key = key;
        this.notificationOpened = notificationOpened;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public boolean isNotificationOpened() {
        return notificationOpened;
    }

    public void setNotificationOpened(boolean notificationOpened) {
        this.notificationOpened = notificationOpened;
    }

    @Override
    public String toString() {
        return this.key + " " + this.name;
    }
}
