package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.Functions;
import com.gauravbhor.securechat.utils.PreferenceHelper;
import com.gauravbhor.securechat.utils.PreferenceKeys;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONException;
import org.json.JSONObject;
import org.libsodium.jni.Sodium;

import java.util.ArrayList;
import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends SuperActivity {

    private EditText etGroupName;
    private ListView listView;
    private ArrayAdapter adapter;
    private Realm realm;
    private Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etGroupName = (EditText) findViewById(R.id.edittext_group_name);
        listView = (ListView) findViewById(R.id.listview_groupfriends);
        final Button createGroup = (Button) findViewById(R.id.button_create_group);

        realm = Realm.getDefaultInstance();
        RealmQuery<User> query = realm.where(User.class);
        final RealmResults<User> result = query.findAll();

        String[] names = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            User user = result.get(i);
            names[i] = user.getFirst_name() + " " + user.getLast_name();
        }

        adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_multiple_choice, names);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        createGroup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String groupName = etGroupName.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getApplicationContext(), "Please enter a group name.", Toast.LENGTH_SHORT).show();
                } else {
                    SparseBooleanArray array = listView.getCheckedItemPositions();
                    ArrayList<String> members = new ArrayList<>();
                    for (int i = 0; i < result.size(); i++) {
                        if (array.get(i)) {
                            User user = result.get(i);
                            members.add(String.valueOf(user.getId()));
                        }
                    }
                    if (members.size() > 0) {
                        // Add yourself to the group
                        String userId = String.valueOf(user.getId());
                        if (!members.contains(userId)) {
                            members.add(userId);
                        }
                        createGroup(groupName, user.getId(), members.toArray(new String[members.size()]));
                    } else {
                        Toast.makeText(getApplicationContext(), "Please select at least 1 friend.", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }

    private void createGroup(String groupName, long id, String[] members) {

        group = new Group();
        group.setName(groupName);
        group.setAdmin_id(id);
        group.setMembers(Arrays.toString(members));

        RetroBuilder.buildOn(ChatServer.class).createGroup(group).enqueue(new Callback<Group>() {

            @Override
            public void onResponse(Call<Group> call, Response<Group> response) {
                if (response.isSuccessful()) {
                    group = response.body();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.copyToRealm(group);
                            setResult(RESULT_OK);
                            finish();
                        }
                    });


                    byte[] groupKey = new byte[Sodium.crypto_secretbox_noncebytes()];
                    Sodium.randombytes(groupKey, groupKey.length);

                    String gKey = Base64.encodeToString(groupKey, StaticMembers.BASE64_SAFE_URL_FLAGS);

                    JSONObject json = new JSONObject();
                    try {
                        json.put("key", gKey);
                        json.put("group_id", group.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    byte[] selfPrivateKey = Base64.decode(PreferenceHelper.getString(PreferenceKeys.PRIVATE_KEY), StaticMembers.BASE64_SAFE_URL_FLAGS);

                    String[] members = group.getMembers().replace("[", "").replace("]", "").split(", ");
                    final Integer[] memberIDs = new Integer[members.length];
                    for (int i = 0; i < members.length; i++) {
                        memberIDs[i] = Integer.parseInt(members[i]);
                    }

                    realm = Realm.getDefaultInstance();
                    RealmQuery<User> query = realm.where(User.class).in("user_id", memberIDs);
                    RealmResults<User> result = query.findAll();

                    for (int i = 0; i < result.size(); i++) {
                        User u = result.get(i);
                        byte[] userPublicKey = Base64.decode(u.getPublicKey(), StaticMembers.BASE64_SAFE_URL_FLAGS);

                        // Store each user's message in an array to e sent separately.
                        JSONObject finalMessages = Functions.getEncryptedMessage(json.toString(), 2, getApplicationContext(), userPublicKey, selfPrivateKey, user.getId());
                        sendMessage(finalMessages, u.getId());
                    }
                } else {
                    try {
                        JSONObject json = new JSONObject(response.errorBody().string());

                        switch (json.getInt("code")) {
                            case 0:
                                Toast.makeText(getApplicationContext(), "Please verify your account first.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED);
                                finish();
                                break;
                            case 1:
                                Toast.makeText(getApplicationContext(), "Group name already exists.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED);
                                break;
                            default:
                                Toast.makeText(getApplicationContext(), "Error in creation. Please try again.", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED);
                                finish();
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error in creation. Please try again.", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<Group> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error in creation. Please try again.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    private void sendMessage(JSONObject finalMessages, long receiverID) {
        RetroBuilder.buildOn(ChatServer.class).sendMessage(finalMessages, receiverID).enqueue(new Callback<ChatMessage>() {

            @Override
            public void onResponse(Call<ChatMessage> call, final Response<ChatMessage> response) {
                if (response.isSuccessful()) {
                    // Messages sent. Awesome.
                } else {
                    Toast.makeText(getApplicationContext(), "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ChatMessage> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error sending message. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.close();
    }
}
