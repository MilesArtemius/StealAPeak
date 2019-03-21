package com.ekdorn.stealapeak.services;

import android.util.Log;
import android.widget.Toast;

import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.Message;
import com.ekdorn.stealapeak.managers.Console;
import com.ekdorn.stealapeak.managers.CryptoManager;
import com.ekdorn.stealapeak.managers.NotificationsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    private static final String SENDER_FIELD      = "sender";
    private static final String TEXT_FIELD        = "text";

    private static final String TYPE_FIELD        = "type";
    public static final String TYPE_FIELD_DATA    = "DATA";
    public static final String TYPE_FIELD_SERVICE = "SERVICE";

    public static final String SERVICE_CONTACT_CR = "CONTACT";
    public static final String SERVICE_IMAGE_CH   = "IMAGE_CH";
    public static final String SERVICE_NAME_CH    = "NAME_CH";
    public static final String SERVICE_RELOGIN    = "RELOGIN";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            final String phone = remoteMessage.getData().get(SENDER_FIELD);

            if (remoteMessage.getData().get(TYPE_FIELD).equals(TYPE_FIELD_DATA)) {
                String text = CryptoManager.decode(this, remoteMessage.getData().get(TEXT_FIELD));
                Message message = new Message(phone, false, remoteMessage.getSentTime(), text);
                NotificationsManager.activeNotification(this, phone, message);
            } else {
                switch (remoteMessage.getData().get(TEXT_FIELD)) {
                    case SERVICE_CONTACT_CR:
                        Console.getUserByPhone(phone, new Console.OnLoaded() {
                            @Override
                            public void onGot(Contact contact, boolean successful) {
                                if (successful) {
                                    if (!AppDatabase.getDatabase(MessagingService.this).contactDao().isContact(contact.getPhone())) {
                                        AppDatabase.getDatabase(MessagingService.this).contactDao().setContact(contact);
                                        NotificationsManager.activeNotification(MessagingService.this, phone, null);
                                    }
                                }
                            }
                        });
                        break;

                    case SERVICE_IMAGE_CH:
                        break;

                    case SERVICE_NAME_CH:
                        Console.getUserByPhone(phone, new Console.OnLoaded() {
                            @Override
                            public void onGot(Contact contact, boolean successful) {
                                if (successful) {
                                    if (!AppDatabase.getDatabase(MessagingService.this).contactDao().isContact(contact.getPhone())) {
                                        AppDatabase.getDatabase(MessagingService.this).contactDao().updateContact(contact);
                                    }
                                }
                            }
                        });
                        break;

                    case SERVICE_RELOGIN:
                        Console.getUserByPhone(phone, new Console.OnLoaded() {
                            @Override
                            public void onGot(Contact contact, boolean successful) {
                                if (successful) {
                                    if (!AppDatabase.getDatabase(MessagingService.this).contactDao().isContact(contact.getPhone())) {
                                        if (!AppDatabase.getDatabase(MessagingService.this).contactDao().getContact(contact.getPhone()).getKey().equals(contact.getKey())) {
                                            AppDatabase.getDatabase(MessagingService.this).contactDao().updateContact(contact);
                                        } else {
                                            Toast.makeText(MessagingService.this, "Contact " + contact.getPhone() + " is no more available", Toast.LENGTH_SHORT).show();
                                            AppDatabase.getDatabase(MessagingService.this).contactDao().deleteContact(contact.getPhone());
                                        }
                                    }
                                }
                            }
                        });
                        break;
                }
            }
        } else {
            Log.e("TAG", "onMessageReceived: notification malformed");
        }
    }



    @Override
    public void onNewToken(String token) {
        Log.e("TAG", "Refreshed token: " + token + " " + (FirebaseAuth.getInstance().getCurrentUser() != null));

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Console.reloadToken(token);
        }
    }
}
