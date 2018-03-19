package com.gavinandre.mvpsocketclient.bean;

/**
 * Created by gavinandre on 18-3-19.
 */

public class MessageEvent {

    private String message;

    public MessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
