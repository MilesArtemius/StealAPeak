package com.ekdorn.stealapeak.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contact")
    LiveData<List<Contact>> getAllContacts();

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
