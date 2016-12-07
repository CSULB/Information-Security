package com.gauravbhor.securechat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;

import com.gauravbhor.securechat.activities.SuperActivity;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.RetroBuilder;

/**
 * Created by bhorg on 12/5/2016.
 */

public class GroupMessageService extends IntentService {

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            RetroBuilder.buildOn(ChatServer.class).getGroupMessages(SuperActivity.user.getId())

            handler.postDelayed(runnableCode, 3000);
        }
    };

    public GroupMessageService() {
        super("GroupMessageService@CSULB");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler.post(runnableCode);
    }
}
