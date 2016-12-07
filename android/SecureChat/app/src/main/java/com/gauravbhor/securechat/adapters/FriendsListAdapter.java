package com.gauravbhor.securechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.User;

import java.util.List;

/**
 * Created by bhorg on 12/4/2016.
 */
public class FriendsListAdapter extends ArrayAdapter<User> {

    public FriendsListAdapter(Context context, int resource, List objects) {
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
            h.phone = (TextView) v.findViewById(R.id.textview_phone);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        User user = getItem(position);
        h.name.setText(user.getFirst_name() + " " + user.getLast_name());
        h.phone.setText("Phone: " + user.getPhone());

        return v;
    }

    class Holder {
        TextView name, phone;
    }
}
