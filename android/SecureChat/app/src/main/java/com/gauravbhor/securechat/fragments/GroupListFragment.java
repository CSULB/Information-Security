package com.gauravbhor.securechat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.activities.GroupChatActivity;
import com.gauravbhor.securechat.adapters.GroupListAdapter;
import com.gauravbhor.securechat.pojos.Group;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by bhorg on 12/5/2016.
 */

public class GroupListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listView;
    private GroupListAdapter adapter;
    private Realm realm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group_list, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_groups);

        realm = Realm.getDefaultInstance();
        RealmQuery<Group> query = realm.where(Group.class);
        RealmResults<Group> result = query.findAll();

        adapter = new GroupListAdapter(getContext(), 0, result);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    public void refresh() {
        adapter.notifyDataSetChanged();
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Group group = (Group) adapterView.getAdapter().getItem(i);
        Intent intent = new Intent(getContext(), GroupChatActivity.class);
        intent.putExtra("id", group.getId());
        startActivity(intent);
    }
}
