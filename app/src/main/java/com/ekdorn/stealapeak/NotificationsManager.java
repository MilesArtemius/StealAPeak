package com.ekdorn.stealapeak;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationsManager {
    public static void addToDialogNotification(@NotNull Context context, @NotNull String phone, @Nullable Map<String, String> content) {
        String username;
        if (PrefManager.get(context).isUser(phone)) {
            User user = PrefManager.get(context).getUser(phone);
            username = user.getName();
            user.setNotificationOpened(true);
            PrefManager.get(context).setUser(phone, user);
        } else {
            username = phone;
        }
        String text = (content == null) ?
                "You've just started the dialog!" : content.get("text");

        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification n  = new Notification.Builder(context)
                .setContentTitle(username)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                //.setOngoing(true)
                .getNotification();
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        nMN.notify(phone.hashCode(), n);
    }

    public static void dismissDialogNotification(@NotNull Context context, @NotNull String phone) {
        if (PrefManager.get(context).isUser(phone)) {
            User user = PrefManager.get(context).getUser(phone);
            user.setNotificationOpened(false);
            PrefManager.get(context).setUser(phone, user);
        }
        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nMN.cancel(phone.hashCode());
    }
}
