package com.gauravbhor.securechat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.activities.ChatActivity;
import com.gauravbhor.securechat.adapters.FriendsListAdapter;
import com.gauravbhor.securechat.pojos.User;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by bhorg on 12/5/2016.
 */

public class FriendListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private Realm realm;
    private FriendsListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_friends);

        realm = Realm.getDefaultInstance();
        RealmQuery<User> query = realm.where(User.class);
        RealmResults<User> result = query.findAll();

        adapter = new FriendsListAdapter(getContext(), 0, result);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        User user = (User) adapterView.getAdapter().getItem(i);
        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra("id", user.getId());
        startActivity(intent);
    }

    public void addUser(final User user) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(user);
                adapter.notifyDataSetChanged();
            }
        });
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
