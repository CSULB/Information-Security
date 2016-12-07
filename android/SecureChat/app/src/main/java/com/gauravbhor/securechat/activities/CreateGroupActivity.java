package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.Group;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;

import org.json.JSONObject;

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
                    ArrayList<String> members = new ArrayList<String>();
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
