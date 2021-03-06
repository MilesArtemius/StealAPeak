package com.ekdorn.stealapeak.database;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface MessageDao {
    @Query("SELECT *, MAX(time) FROM message WHERE referal = :referal LIMIT 1")
    Message getLatest(String referal);

    @Query("SELECT * FROM message WHERE referal = :referal")
    LiveData<List<Message>> getAllMessages(String referal);

    @Query("SELECT COUNT(referal) FROM message WHERE referal = :referal")
    int getNumberOfMessages(String referal);



    @Insert
    void setMessage(Message message);

    @Query("DELETE FROM message WHERE time = :time")
    void deleteMessage(long time);

    @Query("DELETE FROM message WHERE referal = :phone")
    void deleteMessageFrom(String phone);

    @Query("DELETE FROM message")
    void deleteAllMessages();
}