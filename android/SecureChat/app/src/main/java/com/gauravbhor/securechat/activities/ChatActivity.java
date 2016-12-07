package com.gauravbhor.securechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.adapters.MessageAdapter;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.PreferenceKeys;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.SodiumConstants;

import java.nio.charset.StandardCharsets;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends SuperActivity {

    private long id;
    private Realm realm;
    private User chatUser;
    private byte[] selfPrivateKey, receiverPublicKey;
    private EditText etMessage;
    private Button button;
    private ListView listView;
    private BroadcastReceiver receiver;
    private boolean isRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        id = getIntent().getLongExtra("id", 0);
        if (id == 0) {
            finish();
            Toast.makeText(ChatActivity.this, "Error loading chat.", Toast.LENGTH_LONG).show();
        }

        realm = Realm.getDefaultInstance();
        chatUser = realm.where(User.class).equalTo("user_id", id).findFirst();

        RealmQuery<ChatMessage> query = realm.where(ChatMessage.class).equalTo("sender", id).equalTo("receiver", user.getId()).or().equalTo("sender", user.getId()).equalTo("receiver", id);
        RealmResults<ChatMessage> result = query.findAll();

        listView = (ListView) findViewById(R.id.listview_messages);
        final MessageAdapter adapter = new MessageAdapter(this, 0, result, id, user.getId());
        listView.setAdapter(adapter);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);
            }
        };

        getSupportActionBar().setTitle(chatUser.getFirst_name());

        selfPrivateKey = Base64.decode(PreferenceHelper.getString(PreferenceKeys.PRIVATE_KEY), StaticMembers.BASE64_SAFE_URL_FLAGS);
        receiverPublicKey = Base64.decode(chatUser.getPublicKey(), StaticMembers.BASE64_SAFE_URL_FLAGS);

        button = (Button) findViewById(R.id.button_send_message);
        etMessage = (EditText) findViewById(R.id.edittext_message);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                if (message.trim().length() > 0) {
                    message = message.trim();

                    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                    byte[] cipher1 = new byte[Sodium.crypto_secretbox_macbytes() + messageBytes.length];
                    byte[] nonce1 = new byte[Sodium.crypto_secretbox_noncebytes()];
                    byte[] symmetricKey = new byte[SodiumConstants.SECRETKEY_BYTES];

                    Sodium.randombytes(nonce1, nonce1.length);
                    Sodium.randombytes(symmetricKey, symmetricKey.length);

                    // Encrypt the message
                    if (Sodium.crypto_secretbox_easy(cipher1, messageBytes, messageBytes.length, nonce1, symmetricKey) != 0) {
                        Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    byte[] cipher2 = new byte[Sodium.crypto_box_macbytes() + symmetricKey.length];
                    byte[] nonce2 = new byte[Sodium.crypto_box_noncebytes()];
                    Sodium.randombytes(nonce2, nonce2.length);

                    // Encrypt the key
                    if (Sodium.crypto_box_easy(cipher2, symmetricKey, symmetricKey.length, nonce2, receiverPublicKey, selfPrivateKey) != 0) {
                        Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    JSONObject parent = new JSONObject();
                    try {
                        JSONObject mess = new JSONObject();
                        mess.put("type", 1);
                        mess.put("nonce1", Base64.encodeToString(nonce1, StaticMembers.BASE64_SAFE_URL_FLAGS));
                        mess.put("nonce2", Base64.encodeToString(nonce2, StaticMembers.BASE64_SAFE_URL_FLAGS));
                        mess.put("message1", Base64.encodeToString(cipher1, StaticMembers.BASE64_SAFE_URL_FLAGS));
                        mess.put("message2", Base64.encodeToString(cipher2, StaticMembers.BASE64_SAFE_URL_FLAGS));
                        mess.put("length", symmetricKey.length);

                        parent.put("sender_id", user.getId());
                        parent.put("message", mess.toString());

                        final String finalMessage = message;
                        RetroBuilder.buildOn(ChatServer.class).sendMessage(parent, id).enqueue(new Callback<ChatMessage>() {

                            @Override
                            public void onResponse(Call<ChatMessage> call, final Response<ChatMessage> response) {
                                if (response.isSuccessful()) {
                                    realm.executeTransaction(new Realm.Transaction() {

                                        @Override
                                        public void execute(Realm realm) {
                                            ChatMessage chatMessage = response.body();
                                            chatMessage.setReceiver(id);
                                            chatMessage.setSender(user.getId());
                                            chatMessage.setMessage(finalMessage);
                                            realm.copyToRealm(chatMessage);
                                            adapter.notifyDataSetChanged();
                                            listView.setSelection(adapter.getCount() - 1);
                                            etMessage.setText("");
                                        }
                                    });
                                } else {
                                    Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ChatMessage> call, Throwable t) {
                                Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Don't send blank message
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
        if (!isRegistered) {
            registerReceiver(receiver, new IntentFilter(StaticMembers.UPDATE_MESSAGES));
            isRegistered = true;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
        if (isRegistered) {
            unregisterReceiver(receiver);
            isRegistered = false;
        }
    }

}
