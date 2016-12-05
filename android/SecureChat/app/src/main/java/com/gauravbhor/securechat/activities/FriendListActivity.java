package com.gauravbhor.securechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.adapters.FriendsAdapter;
import com.gauravbhor.securechat.pojos.User;
import com.gauravbhor.securechat.rest.ChatServer;
import com.gauravbhor.securechat.services.GroupMessageService;
import com.gauravbhor.securechat.services.MessageService;
import com.gauravbhor.securechat.utils.RetroBuilder;
import com.gauravbhor.securechat.utils.StaticMembers;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FriendListActivity extends SuperActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private Realm realm;
    private FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showandscan);
        listView = (ListView) findViewById(R.id.listview_friends);

        realm = Realm.getDefaultInstance();
        RealmQuery<User> query = realm.where(User.class);
        RealmResults<User> result = query.findAll();

        startService(new Intent(this, MessageService.class));
        startService(new Intent(this, GroupMessageService.class));

        adapter = new FriendsAdapter(this, 0, result);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_key:
                Intent intent1 = new Intent(FriendListActivity.this, GeneratorActivity.class);
                startActivity(intent1);
                break;
            case R.id.scan_key:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            } else {
                String publicKeyID = result.getContents();
                final String[] info = publicKeyID.split(StaticMembers.DELIMITER);
                JSONObject json = new JSONObject();
                try {
                    json.put("sender_id", String.valueOf(user.getId()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RetroBuilder.buildOn(ChatServer.class).getUser(json, info[1]).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful()) {
                            final User user = response.body();
                            user.setPublicKey(info[0]);
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    realm.copyToRealmOrUpdate(user);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } else {
                            Toast.makeText(FriendListActivity.this, "Error adding friend. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(FriendListActivity.this, "Error adding friend. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        User user = (User) adapterView.getAdapter().getItem(i);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onStop() {
        super.onStop();
        realm.close();
    }
}
