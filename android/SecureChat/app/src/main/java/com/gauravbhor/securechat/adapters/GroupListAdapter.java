package com.gauravbhor.securechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.Group;

import java.util.List;

/**
 * Created by bhorg on 12/6/2016.
 */
public class GroupListAdapter extends ArrayAdapter<Group> {
    public GroupListAdapter(Context context, int resource, List<Group> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder h;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_friend, parent, false);
            h = new Holder();
            h.name = (TextView) v.findViewById(R.id.textview_name);
            h.memberCount = (TextView) v.findViewById(R.id.textview_phone);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        Group group = getItem(position);
        h.name.setText(group.getName());
        String[] members = group.getMembers().replace("[", "").replace("]", "").split(", ");
        h.memberCount.setText("Participants: " + String.valueOf(members.length));
        return v;
    }

    class Holder {
        TextView name, memberCount;
    }

}
