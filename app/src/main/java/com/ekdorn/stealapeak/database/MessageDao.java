package com.ekdorn.stealapeak.database;

import java.util.List;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

@Dao
public interface MessageDao {
    @Query("SELECT *, MAX(time) FROM message LIMIT 1")
    Message getLatest();

    @Query("SELECT * FROM message")
    LiveData<List<Message>> getAllMessages();



    @Insert
    void setMessage(Message message);

    @Query("DELETE FROM message WHERE time = :time")
    void deleteMessage(long time);

    @Query("DELETE FROM message WHERE sender = :phone")
    void deleteMessageFrom(String phone);

    @Query("DELETE FROM message")
    void deleteAllMessages();
}