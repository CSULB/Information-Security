package com.gauravbhor.securechat.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import com.gauravbhor.securechat.activities.SuperActivity;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.PreferenceKeys;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.Sodium;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.realm.Realm;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by bhorg on 12/5/2016.
 */

public class MessageService extends IntentService {

    private static long myUserId;
    private static long lastestID = 0;
    private byte[] privateKey = Base64.decode(PreferenceHelper.getString(PreferenceKeys.PRIVATE_KEY), StaticMembers.BASE64_SAFE_URL_FLAGS);
    private Realm realm = Realm.getDefaultInstance();

    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

            myUserId = PreferenceHelper.getLong(PreferenceKeys.USER_ID);

            RetroBuilder.buildOn(ChatServer.class).getMessages(myUserId, lastestID).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                    if (response.isSuccessful()) {

                        realm.executeTransaction(new Realm.Transaction() {

                            @Override
                            public void execute(Realm realm) {
                                try {
                                    JSONArray messages = new JSONArray(response.body().string());

                                    for (int i = 0; i < messages.length(); i++) {
                                        try {
                                            JSONObject singleMessage = messages.getJSONObject(i);
                                            int type = new JSONObject(singleMessage.getString("message")).getInt("type");

                                            if (type == 1) {
                                                decryptAndSaveMessage(singleMessage);
                                            } else if (type == 2) {
                                                getGroupDetails(singleMessage);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        try {
                            System.out.println(response.errorBody().string());
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

            handler.postDelayed(runnableCode, 6000);
        }
    };

    private void getGroupDetails(JSONObject singleMessage) throws JSONException {
        System.out.println("getGroupDetails");
        JSONObject innerMessage = new JSONObject(singleMessage.getString("message"));
        int from = singleMessage.getInt("from");
        lastestID = singleMessage.getLong("id");
        User user = realm.where(User.class).equalTo("user_id", from).findFirst();
        // Get all the parameters
        byte[] encryptedKey = Base64.decode(innerMessage.getString("message2"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] encryptedMessage = Base64.decode(innerMessage.getString("message1"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] nonce1 = Base64.decode(innerMessage.getString("nonce1"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] nonce2 = Base64.decode(innerMessage.getString("nonce2"), StaticMembers.BASE64_SAFE_URL_FLAGS);


        int symmetricKeyLength = innerMessage.getInt("length");

        byte[] decodedKey = new byte[symmetricKeyLength];

        // Decrypt key
        int result = Sodium.crypto_box_open_easy(decodedKey, encryptedKey, encryptedKey.length, nonce2, Base64.decode(user.getPublicKey(), StaticMembers.BASE64_SAFE_URL_FLAGS), privateKey);

        if (result == 0) {
            // Decryption successful

            byte[] decryptedMessage = new byte[Sodium.crypto_secretbox_macbytes() + encryptedMessage.length];

            // Decode actual message
            int result2 = Sodium.crypto_secretbox_open_easy(decryptedMessage, encryptedMessage, encryptedMessage.length, nonce1, decodedKey);

            if (result2 == 0) {
                JSONObject groupInvite = new JSONObject(new String(decryptedMessage, StandardCharsets.UTF_8));
                final String groupKey = groupInvite.getString("key");
                groupInvite.remove("key");
                groupInvite.put("id", user.getId());
                System.out.println("getGroupDetails: " + groupInvite);
                RetroBuilder.buildOn(ChatServer.class).getGroupDetails(groupInvite).enqueue(new Callback<Group>() {
                    @Override
                    public void onResponse(Call<Group> call, final Response<Group> response) {
                        if (response.isSuccessful()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Group group = response.body();
                                    group.setGroupKey(groupKey);
                                    realm.copyToRealmOrUpdate(group);
                                    sendBroadcast(new Intent(StaticMembers.UPDATE_GROUPS));
                                }
                            });
                        } else {
                            try {
                                System.err.println(response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Group> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            } else {
                Log.e("SecureChat", "Couldn't decrypt message");
            }
        } else {
            Log.e("SecureChat", "Couldn't decrypt key");
        }
    }

    private void decryptAndSaveMessage(JSONObject singleMessage) throws JSONException {
        JSONObject innerMessage = new JSONObject(singleMessage.getString("message"));
        int from = singleMessage.getInt("from");
        lastestID = singleMessage.getLong("id");
        User user = realm.where(User.class).equalTo("user_id", from).findFirst();
        // Get all the parameters
        byte[] encryptedKey = Base64.decode(innerMessage.getString("message2"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] encryptedMessage = Base64.decode(innerMessage.getString("message1"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] nonce1 = Base64.decode(innerMessage.getString("nonce1"), StaticMembers.BASE64_SAFE_URL_FLAGS);
        byte[] nonce2 = Base64.decode(innerMessage.getString("nonce2"), StaticMembers.BASE64_SAFE_URL_FLAGS);


        int symmetricKeyLength = innerMessage.getInt("length");

        byte[] decodedKey = new byte[symmetricKeyLength];

        // Decrypt key
        int result = Sodium.crypto_box_open_easy(decodedKey, encryptedKey, encryptedKey.length, nonce2, Base64.decode(user.getPublicKey(), StaticMembers.BASE64_SAFE_URL_FLAGS), privateKey);

        if (result == 0) {
            // Decryption successful
            byte[] decryptedMessage = new byte[Sodium.crypto_secretbox_macbytes() + encryptedMessage.length];

            // Decode actual message
            int result2 = Sodium.crypto_secretbox_open_easy(decryptedMessage, encryptedMessage, encryptedMessage.length, nonce1, decodedKey);

            if (result2 == 0) {
                String plainText = new String(decryptedMessage, StandardCharsets.UTF_8);

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setId(lastestID);
                chatMessage.setMessage(plainText);
                chatMessage.setSender(singleMessage.getLong("from"));
                chatMessage.setReceiver(myUserId);
                chatMessage.setType(1);
                realm.copyToRealmOrUpdate(chatMessage);
                sendBroadcast(new Intent(StaticMembers.UPDATE_MESSAGES));
            } else {
                Log.e("SecureChat", "Couldn't decrypt message");
            }
        } else {
            Log.e("SecureChat", "Couldn't decrypt key");
        }
    }

    public MessageService() {
        super("MessageService@CSULB");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler.post(runnableCode);
    }
}
