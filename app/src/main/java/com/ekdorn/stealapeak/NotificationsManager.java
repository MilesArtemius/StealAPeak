package com.ekdorn.stealapeak;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.AppDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationsManager {
    public static void activeNotification(@NotNull Context context, @NotNull String phone, @Nullable Map<String, String> content) {
        String username, text;
        text = (content == null) ? "You've just started the dialog!" : content.get("text");
        if (AppDatabase.getDatabase(context).contactDao().isUser(phone)) {
            Contact contact = AppDatabase.getDatabase(context).contactDao().getUser(phone);
            username = contact.getName();
            contact.setActive(true);
            AppDatabase.getDatabase(context).contactDao().updateUser(contact);

            notifyChat(context, phone, username, text);
        } else {
            username = phone;
            notifyCommon(context, username, content);
        }
    }

    public static void notifyChat(@NotNull Context context, @NotNull String phone, @NonNull String username, @NotNull String text) {
        Intent intentAction = new Intent(context, Receiver.class);
        intentAction.putExtra("DIALOG_ACTION", "close");
        intentAction.putExtra("phone", phone);
        intentAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, phone.hashCode(), intentAction, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification.Builder(context)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .addAction(R.drawable.ic_menu_manage, "close", pIntent)
                //.setLargeIcon()
                .setContentTitle(username)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                .setNumber(1)
                .build();
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        nMN.notify(phone.hashCode(), n);
    }

    public static void notifyCommon(@NotNull Context context, @NotNull String phone, @Nullable Map<String, String> content) {
        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification.Builder(context)
                .setContentTitle("Steal A Peak!")
                .setContentText("You've got some new messages")
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                .setNumber(1)
                .build();
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        nMN.notify(phone.hashCode(), n);
    }



    public static void dismissDialogNotification(@NotNull Context context, @NotNull String phone) {
        if (AppDatabase.getDatabase(context).contactDao().isUser(phone)) {
            Contact contact = AppDatabase.getDatabase(context).contactDao().getUser(phone);
            contact.setActive(false);
            AppDatabase.getDatabase(context).contactDao().updateUser(contact);
        }
        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nMN.cancel(phone.hashCode());
    }
}
