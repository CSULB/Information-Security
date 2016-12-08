package com.gauravbhor.securechat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Base64;
import android.widget.Toast;

import com.gauravbhor.securechat.activities.SuperActivity;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.GroupChatMessage;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.Sodium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gauravbhor.securechat.activities.SuperActivity.user;

/**
 * Created by bhorg on 12/5/2016.
 */

public class GroupMessageService extends IntentService {

    private Realm realm = Realm.getDefaultInstance();

    private Handler handler = new Handler();
    private long latestID;
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {


            try {
                JSONObject json = new JSONObject();
                json.put("sender_id", user.getId());
                json.put("latest_id", latestID);
                RetroBuilder.buildOn(ChatServer.class).getGroupMessages(json).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            // You have messages. Process and store them.
                            try {
                                JSONArray messages = new JSONArray(response.body().string());

                                for (int i = 0; i < messages.length(); i++) {
                                    try {
                                        JSONObject singleMessage = messages.getJSONObject(i);
                                        decryptAndSaveMessage(singleMessage);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                System.err.println(response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.postDelayed(runnableCode, 3000);
        }
    };

    private void decryptAndSaveMessage(JSONObject singleMessage) throws JSONException {

        JSONObject innerJson = new JSONObject(singleMessage.getString("message"));

        System.out.println("innerJson: " + innerJson);

        byte[] encryptedMessage = Base64.decode(innerJson.getString("message"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] nonce = Base64.decode(innerJson.getString("nonce"), StaticMembers.BASE64_SAFE_URL_FLAGS);

        final long messageID = singleMessage.getLong("id");
        final long groupID = singleMessage.getLong("group_id");
        final Group group = realm.where(Group.class).equalTo("id", groupID).findFirst();
        // Decrypt message

        final byte[] decryptedMessage = new byte[Sodium.crypto_secretbox_macbytes() + encryptedMessage.length];

        // Decode actual message
        int result2 = Sodium.crypto_secretbox_open_easy(decryptedMessage, encryptedMessage, encryptedMessage.length, nonce, Base64.decode(group.getGroupKey(), StaticMembers.BASE64_SAFE_URL_FLAGS));

        latestID = messageID;

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                GroupChatMessage groupChatMessage = new GroupChatMessage();
//                groupChatMessage.setId(messageID);
                groupChatMessage.setGroupID(groupID);
                groupChatMessage.setMessage(new String(decryptedMessage, StandardCharsets.UTF_8));
                realm.copyToRealmOrUpdate(groupChatMessage);
                sendBroadcast(new Intent(StaticMembers.UPDATE_MESSAGES));
            }
        });
    }

    public GroupMessageService() {
        super("GroupMessageService@CSULB");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler.post(runnableCode);
    }
}
