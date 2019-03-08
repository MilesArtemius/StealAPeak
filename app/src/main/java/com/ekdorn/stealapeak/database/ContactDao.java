package com.ekdorn.stealapeak.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contact")
    LiveData<List<Contact>> getAllUsers();

    @Query("SELECT * FROM contact WHERE phone LIKE :phone LIMIT 1")
    Contact getUser(String phone);


    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM contact WHERE phone LIKE :phone) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END")
    boolean isUser(String phone);

    @Insert
    void setUser(Contact contact);

    @Update
    void updateUser(Contact contact);



    @Query("DELETE FROM contact WHERE phone = :phone")
    void deleteUser(String phone);

    @Query("DELETE FROM contact")
    void deleteAllUsers();
}
