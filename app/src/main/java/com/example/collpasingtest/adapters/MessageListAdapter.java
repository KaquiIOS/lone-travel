package com.example.collpasingtest.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.example.collpasingtest.R;
import com.example.collpasingtest.models.Message;
import com.example.collpasingtest.models.PhotoMessage;
import com.example.collpasingtest.models.TextMessage;
import com.example.collpasingtest.views.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    // Date Format setting
    private static SimpleDateFormat sMessageDateFormat = new SimpleDateFormat("a hh:mm:ss");
    public static final int NOT_FOUND = -1;

    private Context mContext;
    private List<Message> mMessages;
    private String mUid;

    private Map<String, Bitmap> userProfileList;

    public MessageListAdapter(Context context) {
        this.mContext = context;
        this.mMessages = new ArrayList<>();
        this.mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.userProfileList = new HashMap<>();

        DisplayImageOptions defaultOptions= new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

    }

    public void setUserProfileList(Map<String, Bitmap> list) {
        this.userProfileList = list;
    }

    public void removeUserProfile(final String uid) {
        userProfileList.remove(uid);
    }

    public void addUserProfile(final String uid, String url) {
        ImageLoader.getInstance().loadImage(url, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                userProfileList.put(uid, loadedImage);
                notifyDataSetChanged();
            }
        });
    }


    /*
     * addItem
     * Message 를 리스트에 추가 목적
     * */
    public void addItem(Message item) {
        if (!mMessages.contains(item))
            this.mMessages.add(item);
    }

    /*
     * addItems
     * Message 들을 리스트에 추가 목적
     * */
    public void addItems(List<Message> items) {
        this.mMessages.addAll(items);
    }

    /*
     * clearItems
     * Message 들을 리스트에서 삭제
     * */
    public void clearItems() {
        this.mMessages.clear();
    }


    /*
     * getItemPos
     * messageKey를 통해 메시지 확인. 헬퍼함수
     * 없는 메시지인 경우 NOT_FOUND(-1), 존재하는 메시지의 경우 pos 반환
     * */
    private int getItemPos(String messageKey) {
        int pos = 0;
        for (Message msg : mMessages) {
            if (msg.getMessageId().equals(messageKey))
                return pos;
            pos++;
        }
        return NOT_FOUND;
    }

    /*
     * getItem
     * pos 번의 item 반환
     * pos 범위 밖인 경우 null, 범위 안인 경우 pos번 아이템 반환
     * */
    public Message getItem(int pos) {
        return mMessages.size() <= pos ? null : mMessages.get(pos);
    }


    /*
     * updateItem
     * 기존에 존재하는 message 를 새로운 메시지로 update
     * 업데이트에 성공하면 true, 실패하면 false 반환
     * */
    public int updateItem(Message item) {
        int pos = getItemPos(item.getMessageId());

        if (pos == NOT_FOUND)
            return NOT_FOUND;
        // update
        mMessages.set(pos, item);

        return pos;
    }


    @NonNull
    @Override
    public MessageListAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListAdapter.MessageViewHolder holder, int position) {
        Message msg = mMessages.get(position);

        // 잘못된 메시지 예외처리
        if (msg.getMessageSender() == null) return;

        // get Message Type
        TextMessage txtMsg = null;
        PhotoMessage imgMsg = null;

        if (msg.getMessageType() == Message.MessageType.PHOTO)
            imgMsg = (PhotoMessage) msg;
        else if (msg.getMessageType() == Message.MessageType.TEXT)
            txtMsg = (TextMessage) msg;

        // 내가 보낸 메시지 처리
        if (mUid.equals(msg.getMessageSender().getUid())) {
            // 텍스트 메시지
            if (txtMsg != null) {
                holder.mySendText.setVisibility(View.VISIBLE);
                holder.mySendImage.setVisibility(View.GONE);
                holder.mySendText.setText(txtMsg.getMessageText());
            }
            // 이미지 메시지
            else if (imgMsg != null) {
                holder.mySendText.setVisibility(View.GONE);
                holder.mySendImage.setVisibility(View.VISIBLE);
                Glide.with(holder.myMsg).load(imgMsg.getPhotoUrl()).into(holder.mySendImage);
            }

            // unreadCount 처리
            if (msg.getUnreadCount() > 0)
                holder.myUnreadCount.setText(String.valueOf(msg.getUnreadCount()));
            else
                holder.myUnreadCount.setText("");

            holder.mySendDate.setText(sMessageDateFormat.format(msg.getSentTime()));
            holder.myMsg.setVisibility(View.VISIBLE);
            holder.othersMsg.setVisibility(View.GONE);
        }
        // 남이 보낸 메시지 처리
        else {
            holder.rcvName.setText(msg.getMessageSender().getName());
            if (userProfileList.get(msg.getMessageSender().getUid()) == null)
                holder.rcvProfile.setImageDrawable(mContext.getDrawable(R.drawable.ic_launcher_foreground));
            else
                holder.rcvProfile.setImageBitmap(userProfileList.get(msg.getMessageSender().getUid()));
                // 텍스트 메시지
                if (txtMsg != null) {
                    holder.rcvTxt.setVisibility(View.VISIBLE);
                    holder.rcvImage.setVisibility(View.GONE);
                    holder.rcvTxt.setText(txtMsg.getMessageText());
                }
                // 이미지 메시지
                else if (imgMsg != null) {
                    holder.rcvTxt.setVisibility(View.GONE);
                    holder.rcvImage.setVisibility(View.VISIBLE);
                    Glide.with(holder.othersMsg).load(imgMsg.getPhotoUrl()).into(holder.rcvImage);
                }

            // unreadCount 처리
            if (msg.getUnreadCount() > 0)
                holder.rcvUnreadCount.setText(String.valueOf(msg.getUnreadCount()));
            else
                holder.rcvUnreadCount.setText("");

            holder.rcvDate.setText(sMessageDateFormat.format(msg.getSentTime()));
            holder.othersMsg.setVisibility(View.VISIBLE);
            holder.myMsg.setVisibility(View.GONE);
            //Glide.with((ChatActivity)mContext).load(msg.getMessageSender().getUid()).into(holder.rcvProfile);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout myMsg;
        public ConstraintLayout othersMsg;

        public TextView rcvName;
        public ImageView rcvImage;
        public TextView rcvTxt;
        public TextView rcvUnreadCount;
        public TextView rcvDate;
        public CircleImageView rcvProfile;

        public ImageView mySendImage;
        public TextView myUnreadCount;
        public TextView mySendDate;
        public TextView mySendText;


        public MessageViewHolder(View itemView) {
            super(itemView);

            myMsg = itemView.findViewById(R.id.linear_chat_msg_item_send);
            othersMsg = itemView.findViewById(R.id.linear_chat_msg_item_rec);

            rcvName = itemView.findViewById(R.id.text_view_chat_msg_item_profile);
            rcvImage = itemView.findViewById(R.id.image_view_chat_msg_item_rcv_image);
            rcvTxt = itemView.findViewById(R.id.text_view_chat_msg_item_rcv_txt);
            rcvUnreadCount = itemView.findViewById(R.id.text_view_chat_msg_item_rcv_unread_count);
            rcvDate = itemView.findViewById(R.id.text_view_chat_msg_item_rcv_date);
            rcvProfile = itemView.findViewById(R.id.image_view_chat_msg_item_rcv_profile);
            rcvProfile = itemView.findViewById(R.id.image_view_chat_msg_item_rcv_profile);


            mySendText = itemView.findViewById(R.id.text_view_chat_msg_item_send_txt);
            mySendImage = itemView.findViewById(R.id.image_view_chat_msg_item_send_img);
            myUnreadCount = itemView.findViewById(R.id.text_view_chat_msg_item_send_unread_count);
            mySendDate = itemView.findViewById(R.id.text_view_chat_msg_send_date);
        }
    }
}
