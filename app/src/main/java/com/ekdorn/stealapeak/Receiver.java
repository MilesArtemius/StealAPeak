package com.ekdorn.stealapeak;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", "onNewIntent: " + intent.getExtras() );
        if (intent.hasExtra("DIALOG_ACTION")) {
            switch (intent.getStringExtra("DIALOG_ACTION")) {
                case "close":
                    Log.e("TAG", "onNewIntent: closin..." );
                    NotificationsManager.dismissDialogNotification(context, intent.getStringExtra("phone"));
                    break;
            }
        }
    }
}
