package com.gauravbhor.securechat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
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

        if (chatMessage.getSender() != myID) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(h.message.getLayoutParams());
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            h.message.setLayoutParams(params);
            Drawable d = h.message.getBackground();
            d.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(h.message.getLayoutParams());
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            h.message.setLayoutParams(params);
            Drawable d = h.message.getBackground();
            d.setColorFilter(Color.parseColor("#ff33b5e5"), PorterDuff.Mode.SRC);
        }

        return v;
    }

    class Holder {
        TextView message;
    }
}
