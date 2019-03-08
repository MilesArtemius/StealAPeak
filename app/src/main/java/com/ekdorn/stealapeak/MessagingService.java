package com.ekdorn.stealapeak;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e("TAG", "onMessageReceived: received! " + remoteMessage.getData());
            if (remoteMessage.getData().get("type").equals("DATA")) {
                String phone = remoteMessage.getData().get("sender");
                NotificationsManager.activeNotification(this, phone, remoteMessage.getData());
            } else {

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
