package com.ekdorn.stealapeak.managers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ekdorn.stealapeak.parts.ContactViewer;
import com.ekdorn.stealapeak.R;
import com.ekdorn.stealapeak.services.Receiver;
import com.ekdorn.stealapeak.database.Contact;
import com.ekdorn.stealapeak.database.AppDatabase;
import com.ekdorn.stealapeak.database.Message;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationsManager {

    public static void activeNotification(@NotNull Context context, @NotNull String phone, @Nullable Message message) {
        String text = (message == null) ? AppDatabase.getDatabase(context).messageDao().getLatest(phone).getText()
                : message.getText();
        if (AppDatabase.getDatabase(context).contactDao().isContact(phone)) {
            Contact contact = AppDatabase.getDatabase(context).contactDao().getContact(phone);
            contact.setActive(true);
            AppDatabase.getDatabase(context).contactDao().updateContact(contact);

            if (message != null) AppDatabase.getDatabase(context).messageDao().setMessage(message);

            notifyChat(context, contact, text);
        } else if (message != null) {
            notifyCommon(context, message);
        }
    }

    private static void notifyChat(@NotNull Context context, @NotNull Contact contact, @NotNull String text) {
        Intent intentAction = new Intent(context, Receiver.class);
        intentAction.putExtra(Receiver.DIALOG_TYPE, Receiver.DIALOG_TYPE_CLOSE);
        intentAction.putExtra(Receiver.DIALOG_TYPE_PHONE, contact.getPhone());
        intentAction.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intentAction, PendingIntent.FLAG_ONE_SHOT);

        Intent intentDialog = new Intent(context, ContactViewer.class);
        intentDialog.putExtra(ContactViewer.PHONE, contact.getPhone());
        intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentDialog, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification.Builder(context)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .addAction(R.drawable.ic_menu_manage, "close dialog", cancelIntent)
                .setContentIntent(contentIntent)
                //.setLargeIcon()
                .setContentTitle(contact.getName())
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                .setNumber(AppDatabase.getDatabase(context).messageDao().getNumberOfMessages(contact.getPhone()))
                .build();
        n.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        nMN.notify(contact.getName().hashCode(), n);
    }

    private static void notifyCommon(@NotNull Context context, @NotNull Message message) {
        Intent intentDialog = new Intent(context, ContactViewer.class);
        intentDialog.putExtra(ContactViewer.PHONE, message.getReferal());
        intentDialog.putExtra(ContactViewer.DATA_KEY, message);
        intentDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intentDialog, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification.Builder(context)
                .setContentIntent(contentIntent)
                .setContentTitle(message.getReferal())
                .setContentText(message.getText())
                .setSmallIcon(R.drawable.ic_menu_slideshow)
                .build();
        nMN.notify(message.getReferal().hashCode(), n);
    }



    public static void dismissDialogNotification(@NotNull Context context, @NotNull String phone) {
        if (AppDatabase.getDatabase(context).contactDao().isContact(phone)) {
            Contact contact = AppDatabase.getDatabase(context).contactDao().getContact(phone);
            contact.setActive(false);
            AppDatabase.getDatabase(context).contactDao().updateContact(contact);
        }
        NotificationManager nMN = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nMN.cancel(phone.hashCode());
    }
}
