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
public class FriendsAdapter extends ArrayAdapter<User> {

    public FriendsAdapter(Context context, int resource, List objects) {
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
            h.fName = (TextView) v.findViewById(R.id.textview_first_name);
            h.lName = (TextView) v.findViewById(R.id.textview_last_name);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        User user = getItem(position);
        h.fName.setText(user.getFirst_name());
        h.lName.setText(user.getLast_name());

        return v;
    }

    class Holder {
        TextView fName, lName;
    }
}
