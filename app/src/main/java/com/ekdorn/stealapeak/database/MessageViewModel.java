package com.ekdorn.stealapeak.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {
    private MessageDao messageDao;
    private LiveData<List<Message>> messageLiveData;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        messageDao = AppDatabase.getDatabase(application).messageDao();
        messageLiveData = messageDao.getAllMessages();
    }

    public LiveData<List<Message>> getMessagesList() {
        return messageLiveData;
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
