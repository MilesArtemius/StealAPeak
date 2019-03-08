package com.ekdorn.stealapeak.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private ContactDao contactDao;
    private LiveData<List<Contact>> contactLiveData;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        contactDao = AppDatabase.getDatabase(application).contactDao();
        contactLiveData = contactDao.getAllUsers();
    }

    public LiveData<List<Contact>> getContactsList() {
        return contactLiveData;
    }

    public Contact getContact(String phone) {
        return contactDao.getUser(phone);
    }



    public boolean isContact(String phone) {
        return contactDao.isUser(phone);
    }

    public void setContact(Contact contact) {
        contactDao.setUser(contact);
    }



    public void deleteContact(String phone) {
        contactDao.deleteUser(phone);
    }
}
