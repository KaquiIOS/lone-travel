package com.example.collpasingtest.models;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Message {

    // 메세지 타입
    public enum MessageType {
        TEXT, PHOTO
    }

    private String messageId;
    private String chatRoomId;
    private User messageSender;
    private Date sentTime;
    private long unreadCount;
    // 읽은 사람들의 UID
    private List<String> readerUids;
    private MessageType messageType;
    // 마지막 메시지
    //private TextMessage lastMessage;

    public Message() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public User getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(User messageSender) {
        this.messageSender = messageSender;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public String getSentTimeAsString() {
        return new SimpleDateFormat("yy-MM-dd hh:mm").format(sentTime);
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<String> getReaderUids() {
        return readerUids;
    }

    public void setReaderUids(List<String> readerUids) {
        this.readerUids = readerUids;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
/*
    public TextMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(TextMessage lastMessage) {
        this.lastMessage = lastMessage;
    }*/
}
