package com.example.collpasingtest.models;

import java.util.Date;

public class Room {

    private String chatRoomId;
    private String title;
    private TextMessage lastMessage;
    private Date createDate;
    private boolean isExpired;
    private int totalUnreadCount;

    public Room() {
    }

    public Room(String chatRoomId, String title, TextMessage lastMessage, Date createDate, boolean isExpired, int totalUnreadCount) {
        this.chatRoomId = chatRoomId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.createDate = createDate;
        this.isExpired = isExpired;
        this.totalUnreadCount = totalUnreadCount;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public int getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(int totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public TextMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(TextMessage lastMessage) {
        this.lastMessage = lastMessage;
    }
}
