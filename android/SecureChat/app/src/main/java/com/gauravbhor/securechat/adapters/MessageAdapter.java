package com.gauravbhor.securechat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.ChatMessage;

import java.util.List;

/**
 * Created by bhorg on 12/5/2016.
 */
public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    private long theirID, myID;

    public MessageAdapter(Context context, int resource, List<ChatMessage> objects, long theirID, long myID) {
        super(context, resource, objects);
        this.myID = myID;
        this.theirID = theirID;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder h;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, parent, false);
            h = new Holder();
            h.message = (TextView) v.findViewById(R.id.textview_message);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        ChatMessage chatMessage = getItem(position);
        h.message.setText(chatMessage.getMessage());

        if (chatMessage.getSender() == theirID) {
            h.message.setTextColor(Color.BLUE);
        }

        return v;
    }

    class Holder {
        TextView message;
    }
}
