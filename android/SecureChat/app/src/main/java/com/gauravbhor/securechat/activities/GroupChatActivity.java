package com.gauravbhor.securechat.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.adapters.GroupMessageAdapter;
import com.gauravbhor.securechat.adapters.MessageAdapter;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.GroupChatMessage;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.Functions;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gauravbhor.securechat.activities.SuperActivity.user;

public class GroupChatActivity extends AppCompatActivity {

    private long groupID;
    private Realm realm;
    private Group group;
    private byte[] selfPrivateKey, groupPrivateKey;
    private EditText etMessage;
    private Button button;
    private ListView listView;
    private BroadcastReceiver receiver;
    private boolean isRegistered;
    private GroupMessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        groupID = getIntent().getLongExtra("group_id", 0);
        if (groupID == 0) {
            finish();
            Toast.makeText(this, "Error loading chat.", Toast.LENGTH_LONG).show();
        }

        realm = Realm.getDefaultInstance();
        group = realm.where(Group.class).equalTo("id", groupID).findFirst();

        RealmQuery<GroupChatMessage> query = realm.where(GroupChatMessage.class).equalTo("group_id", groupID);
        RealmResults<GroupChatMessage> result = query.findAll();

        listView = (ListView) findViewById(R.id.listview_messages);
        adapter = new GroupMessageAdapter(this, 0, result, groupID, user.getId());

        listView.setAdapter(adapter);
        listView.setSelection(adapter.getCount() - 1);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                adapter.notifyDataSetChanged();
                listView.setSelection(adapter.getCount() - 1);
            }
        };

        getSupportActionBar().setTitle(group.getName());

        groupPrivateKey = Base64.decode(group.getGroupKey(), StaticMembers.BASE64_SAFE_URL_FLAGS);

        button = (Button) findViewById(R.id.button_send_message);
        etMessage = (EditText) findViewById(R.id.edittext_message);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                if (message.trim().length() > 0) {
                    message = user.getFirst_name() + ": " + message.trim();

                    JSONObject parent = null;
                    try {
                        parent = Functions.getEncryptedGroupMessage(message, getApplicationContext(), groupPrivateKey, user.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(GroupChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    final String finalMessage = message;
                    RetroBuilder.buildOn(ChatServer.class).sendGroupMessage(parent, groupID).enqueue(new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    System.out.println(response.body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                etMessage.setText("");
//                              ######  Process group message in service.   ######
//                                realm.executeTransaction(new Realm.Transaction() {
//
//                                    @Override
//                                    public void execute(Realm realm) {
//
//                                        try {
//                                            JSONObject json = new JSONObject(response.body().string());
//
//                                            GroupChatMessage groupChatMessage = new GroupChatMessage();
//                                            groupChatMessage.setMessage(finalMessage);
//                                            groupChatMessage.setSenderName(user.getFirst_name() + " " + user.getLast_name());
//                                            groupChatMessage.setGroupID(groupID);
//                                            groupChatMessage.setId(json.getLong("id"));
//                                            realm.copyToRealm(groupChatMessage);
//
//                                            adapter.notifyDataSetChanged();
//                                            listView.setSelection(adapter.getCount() - 1);
//                                            etMessage.setText("");
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                            Toast.makeText(GroupChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
//                                        }
//
//                                    }
//                                });
                            } else {
                                Toast.makeText(GroupChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(GroupChatActivity.this, "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
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