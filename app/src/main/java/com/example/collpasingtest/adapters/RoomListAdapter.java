package com.example.collpasingtest.adapters;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.models.Message;
import com.example.collpasingtest.models.Room;
import com.example.collpasingtest.views.ChatListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    private static final SimpleDateFormat sSimpleDataFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static final int NOT_FOUND = -1;

    private Context mContext;
    private ChatListFragment mChatListFragment;
    private List<Room> roomList;

    public RoomListAdapter(Context mContext) {
        this.mContext = mContext;
        this.roomList = new ArrayList<>();
    }

    public void setRoomListFragment(@NonNull ChatListFragment fg) {
        this.mChatListFragment = fg;
    }

    public void removeRoom(String roomId) {
        int pos = 0;
        Iterator<Room> item = roomList.iterator();
        while (item.hasNext()) {
            if (item.next().getChatRoomId().equals(roomId)) {
                item.remove();
                notifyItemChanged(pos);
                break;
            }
            pos++;
        }
    }

    public void addRoom(Room room) {
        this.roomList.add(room);
    }

    public void addRooms(List<Room> rooms) {
        this.roomList.addAll(rooms);
    }

    public int getRoomPos(String roomId) {
        int pos = 0;
        for (Room cur : roomList) {
            if (cur.getChatRoomId().equals(roomId))
                return pos;
            pos++;
        }
        return NOT_FOUND;
    }

    public Room getRoom(int pos) {
        return roomList.get(pos);
    }

    public Room getRoom(String roomId) {
        int pos = getRoomPos(roomId);
        return pos == NOT_FOUND ? null : roomList.get(pos);
    }

    public void updateItem(Room room) {
        int pos = getRoomPos(room.getChatRoomId());
        if (pos != NOT_FOUND)
            roomList.set(pos, room);
    }


    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_room, parent, false);

        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {

        final Room room = roomList.get(position);

        // 해당 방에서 안읽은 메시지 카운트 설정
        if(room.getTotalUnreadCount() ==0) {
            holder.totalUnreadCntTextView.setText(String.valueOf(0));
            holder.totalUnreadCntTextView.setVisibility(View.GONE);
        } else {
            holder.totalUnreadCntTextView.setText(String.valueOf(room.getTotalUnreadCount()));
            holder.totalUnreadCntTextView.setVisibility(View.VISIBLE);
        }

        // 해당 방 제목 설정
        holder.participantsTextView.setText(room.getTitle());

        // 마지막 메시지 텍스트 지정
        if (room.getLastMessage() != null) {
            if (room.getLastMessage().getMessageType() == Message.MessageType.TEXT) // 문자 메시지
                holder.lastMessageTextView.setText(room.getLastMessage().getMessageText());
            else // 사진 메시지
                holder.lastMessageTextView.setText("사진 메시지가 왔습니다");

            // 마지막 시간 설정
            holder.lastMessageTimeTextView.setText(room.getLastMessage().getSentTimeAsString());// 메시지 마지막 시간 설정
        } else {
            holder.lastMessageTextView.setText("새로 만들어진 동행 모임입니다 !");
        }

        // 미구현
        holder.roomImgView.setImageResource(R.drawable.ic_common_default_profile_32);

        // 방 나가기 이벤트 구현
        holder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mChatListFragment.leaveRoom(room);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {

        CircleImageView roomImgView;
        TextView participantsTextView;
        TextView lastMessageTextView;
        TextView totalUnreadCntTextView;
        TextView lastMessageTimeTextView;

        ConstraintLayout rootView;

        public RoomViewHolder(View itemView) {
            super(itemView);

            // set view
            roomImgView = itemView.findViewById(R.id.image_view_room_item);
            roomImgView.setBackground(new ShapeDrawable(new OvalShape()));             // set square image to round image

            participantsTextView = itemView.findViewById(R.id.text_view_room_item_chat_participants);
            lastMessageTextView = itemView.findViewById(R.id.text_view_room_item_last_msg);
            totalUnreadCntTextView = itemView.findViewById(R.id.text_view_room_item_total_unread_count);
            lastMessageTimeTextView = itemView.findViewById(R.id.text_view_room_item_last_time);

            rootView = itemView.findViewById(R.id.rootview_room_item);
        }
    }
}
