package com.gauravbhor.securechat.pojos;

import io.realm.RealmObject;

/**
 * Created by bhorg on 12/5/2016.
 */
public class ChatMessage extends RealmObject {

    private long id;
    private long sender;
    private long receiver;
    private String message;
    private int type;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public long getReceiver() {
        return receiver;
    }

    public void setReceiver(long receiver) {
        this.receiver = receiver;
    }
}
