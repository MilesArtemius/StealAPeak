package com.ekdorn.stealapeak.database;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {
    private MessageDao messageDao;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageDao = AppDatabase.getDatabase(application).messageDao();
    }

    public LiveData<List<Message>> getMessagesList(String phone) {
        return messageDao.getAllMessages(phone);
    }



    public void setMessage(Message message) {
        messageDao.setMessage(message);
    }

    public void deleteMessage(long time) {
        messageDao.deleteMessage(time);
    }

    public void deleteMessageFrom(String phone) {
        messageDao.deleteMessageFrom(phone);
    }

    public void deleteAllMessages() {
        messageDao.deleteAllMessages();
    }
}
