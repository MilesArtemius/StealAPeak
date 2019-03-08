package com.ekdorn.stealapeak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    public static final String DIALOG_TYPE       = "DIALOG_ACTION";
    public static final String DIALOG_TYPE_CLOSE = "close";
    public static final String DIALOG_TYPE_PHONE = "phone";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "onNewIntent: " + intent.getExtras() );
        if (intent.hasExtra(DIALOG_TYPE)) {
            switch (intent.getStringExtra(DIALOG_TYPE)) {
                case DIALOG_TYPE_CLOSE:
                    Log.e("TAG", "onNewIntent: closin..." );
                    NotificationsManager.dismissDialogNotification(context, intent.getStringExtra(DIALOG_TYPE_PHONE));
                    break;
            }
        }
    }
}
