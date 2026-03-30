package com.example.chat_app_frontend.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.example.chat_app_frontend.repository.CallRepository;

public class CallActionReceiver extends BroadcastReceiver {
    
    private static final String TAG = "CallActionReceiver";
    
    public static final String ACTION_DECLINE_CALL = "com.example.chat_app_frontend.ACTION_DECLINE_CALL";
    
    public static final String EXTRA_CALL_ID = "extra_call_id";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "CallActionReceiver onReceive: " + action);
        
        if (action == null) return;
        
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        String callId = intent.getStringExtra(EXTRA_CALL_ID);
        
        if (notificationId != -1) {
            Log.d(TAG, "Canceling notification " + notificationId);
            NotificationManagerCompat.from(context).cancel(notificationId);
        }
        
        if (ACTION_DECLINE_CALL.equals(action) && callId != null) {
            Log.d(TAG, "Rejecting call " + callId);
            CallRepository.getInstance().updateStatus(callId, "rejected");
        }
    }
}
