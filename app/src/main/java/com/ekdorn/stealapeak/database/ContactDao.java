package com.ekdorn.stealapeak.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contact WHERE phone != :myPhone")
    LiveData<List<Contact>> getAllContacts(String myPhone);

    @Query("SELECT * FROM contact WHERE phone LIKE :phone LIMIT 1")
    Contact getContact(String phone);


    @Query("SELECT CASE WHEN EXISTS (SELECT * FROM contact WHERE phone = :phone) THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END")
    boolean isContact(String phone);

    @Insert
    void setContact(Contact contact);

    @Update
    void updateContact(Contact contact);



    @Query("DELETE FROM contact WHERE phone = :phone")
    void deleteContact(String phone);

    @Query("DELETE FROM contact")
    void deleteAllContacts();
}
