package com.ekdorn.stealapeak.database;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ContactViewModel extends AndroidViewModel {
    private ContactDao contactDao;
    private LiveData<List<Contact>> contactLiveData;

    public ContactViewModel(@NonNull Application application) {
        super(application);
        String myPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        contactDao = AppDatabase.getDatabase(application).contactDao();
        contactLiveData = contactDao.getAllContacts(myPhone);
    }

    public LiveData<List<Contact>> getContactsList() {
        return contactLiveData;
    }

    public Contact getContact(String phone) {
        return contactDao.getContact(phone);
    }



    public boolean isContact(String phone) {
        return contactDao.isContact(phone);
    }

    public void setContact(Contact contact) {
        contactDao.setContact(contact);
    }



    public void deleteContact(String phone) {
        contactDao.deleteContact(phone);
    }
}
