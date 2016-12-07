package com.gauravbhor.securechat.pojos;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by bhorg on 12/7/2016.
 */

public class GroupChatMessage extends RealmObject {

    @PrimaryKey
    private long id;

    private long sender_id, group_id;

    private String message;
    private String senderName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public long getSenderID() {
        return sender_id;
    }

    public void setSenderID(long senderID) {
        this.sender_id = senderID;
    }

    public long getGroupID() {
        return group_id;
    }

    public void setGroupID(long groupID) {
        this.group_id = groupID;
    }
}
