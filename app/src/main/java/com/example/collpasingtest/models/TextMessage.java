package com.example.collpasingtest.models;

public class TextMessage extends Message {

    private String messageText;

    public TextMessage() { super(); }

    public TextMessage(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
