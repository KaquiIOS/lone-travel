package com.example.collpasingtest.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Comment {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private String commentContent;
    private Date commentDate;
    private String commentId;
    private String contentId;
    private String writerUid;

    public Comment() {

    }

    public Comment(String contentId, String writerUid, String commentId, String commentContent, Date commentDate) {
        this.contentId = contentId;
        this.writerUid = writerUid;
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.commentDate = commentDate;
    }

    public String getWriterUid() {
        return writerUid;
    }

    public void setWriterUid(String writerUid) {
        this.writerUid = writerUid;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public String getCommentDateString() {
        return format.format(commentDate);
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }
}
