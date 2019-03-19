package com.ekdorn.stealapeak.services;

import android.util.Log;

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

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            //Log.e("TAG", "onMessageReceived: received! " + remoteMessage.getData());
            if (remoteMessage.getData().get(TYPE_FIELD).equals(TYPE_FIELD_DATA)) {
                String phone = remoteMessage.getData().get(SENDER_FIELD);
                String text = CryptoManager.decode(this, remoteMessage.getData().get(TEXT_FIELD));

                Message message = new Message(phone, false, remoteMessage.getSentTime(), text);
                NotificationsManager.activeNotification(this, phone, message);
            } else {
                // something.
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
