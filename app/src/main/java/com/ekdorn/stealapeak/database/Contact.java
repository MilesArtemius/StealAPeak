package com.ekdorn.stealapeak.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "contact",
        indices = {@Index(value = "phone", unique = true)})
public class Contact implements Serializable {
    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "phone")
    private String phone;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "key")
    private String key;

    @ColumnInfo(name = "active")
    private boolean active;

    public Contact(String phone, String name, String key, boolean active) {
        this.phone = phone;
        this.name = name;
        this.key = key;
        this.active = active;
    }

    public String getKey() {
        return key;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.key + " " + this.name;
    }
}
