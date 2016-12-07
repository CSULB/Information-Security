package com.gauravbhor.securechat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gauravbhor.securechat.R;
import com.gauravbhor.securechat.pojos.ChatMessage;
import com.gauravbhor.securechat.pojos.GroupChatMessage;

import java.util.List;

/**
 * Created by bhorg on 12/5/2016.
 */
public class GroupMessageAdapter extends ArrayAdapter<GroupChatMessage> {

    private long groupID, myID;

    public GroupMessageAdapter(Context context, int resource, List<GroupChatMessage> objects, long groupID, long myID) {
        super(context, resource, objects);
        this.groupID = groupID;
        this.myID = myID;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder h;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.list_item_group_message, parent, false);
            h = new Holder();
            h.relativeLayoutGroupMessage = (RelativeLayout) v.findViewById(R.id.relativeLayoutGroupMessage);
            h.message = (TextView) v.findViewById(R.id.textview_message);
            h.name = (TextView) v.findViewById(R.id.textview_name);
            v.setTag(h);
        } else {
            h = (Holder) v.getTag();
        }

        GroupChatMessage chatMessage = getItem(position);
        h.message.setText(chatMessage.getMessage());

        if (chatMessage.getSenderID() != myID) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(h.relativeLayoutGroupMessage.getLayoutParams());
            params.addRule(Gravity.LEFT);
            h.relativeLayoutGroupMessage.setLayoutParams(params);
            Drawable d = h.relativeLayoutGroupMessage.getBackground();
            d.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC);
            h.name.setText(chatMessage.getSenderName());
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(h.relativeLayoutGroupMessage.getLayoutParams());
            params.addRule(Gravity.RIGHT);
            h.relativeLayoutGroupMessage.setLayoutParams(params);
            Drawable d = h.relativeLayoutGroupMessage.getBackground();
            d.setColorFilter(Color.parseColor("#ff33b5e5"), PorterDuff.Mode.SRC);
            h.name.setText("You");
        }

        return v;
    }

    class Holder {
        RelativeLayout relativeLayoutGroupMessage;
        TextView name;
        TextView message;
    }
}
