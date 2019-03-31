package com.example.collpasingtest.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.collpasingtest.customviews.RecyclerViewItemClickListener;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.RoomListAdapter;
import com.example.collpasingtest.models.Message;
import com.example.collpasingtest.models.Notification;
import com.example.collpasingtest.models.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ChatListFragment extends Fragment{

    // 방 목록 갱신 이벤트 태그
    public enum EVENT {
        ADD_EVENT, REMOVE_EVENT, UPDATE_EVENT
    }

    // 화면 구성 뷰 정보
    private RecyclerView mRoomListRecyclerView;
    private RoomListAdapter mAdapter;


    // 프래그먼트를 보여주는 액티비티
    private MainActivity mParentActivity;
    // 노티 보낼 객체
    private Notification mNotification;

    // Firebase 객체
    private FirebaseDatabase mFirebaseDB;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mRoomListRef, mMemRef, mUserRef, mTravelRef;
    private DatabaseReference mChatMemRef;
    private DatabaseReference mChatMsgRef;

    // 들어간 방 아이디 정보
    public static String sJoinRoomId = "";
    // 방 입장/퇴장 확인용 상수
    public static final int JOIN_ROOM_REQUEST_CODE = 199;

    public ChatListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_room_list, container, false);

        // [ Firebase 객체 초기화 Start ]
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser(); // 현재 사용자 객체
        mFirebaseDB = FirebaseDatabase.getInstance();               // Firebase DB 객체
        // user/{uid}/rooms/
        mRoomListRef = mFirebaseDB.getReference(FirebaseContract.USERS)
                .child(mFirebaseUser.getUid())
                .child(FirebaseContract.ROOM);                   // 사용자의 방 목록

        // chatMembers/{room_id}
        mChatMemRef = mFirebaseDB.getReference(FirebaseContract.CHAT_MEMBER); // 나중에 각 방의 참여자 목록을 가져올 때 사용

        mChatMsgRef = mFirebaseDB.getReference(FirebaseContract.CHAT_MESSAGES);

        // [ Firebase 객체 초기화 End ]

        // [ 노티피케이션 설정 Start ]
        mNotification = new Notification(mParentActivity);
        // [ 노티피케이션 설정 End ]


        // [ RecyclerView 설정 Start ]
        mRoomListRecyclerView = root.findViewById(R.id.recycler_view_fragment_rooms);
        mAdapter = new RoomListAdapter(mParentActivity);
        mAdapter.setRoomListFragment(this);  // 나중에 LeaveRoom 할 때 사용
        mRoomListRecyclerView.setAdapter(mAdapter);
        mRoomListRecyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false));
        mRoomListRecyclerView.addItemDecoration(new DividerItemDecoration(mRoomListRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // 방 눌렀을 때 이벤트 정의
        mRoomListRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mParentActivity, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                // 선택된 방 가져오기
                Room room = mAdapter.getRoom(pos);
                final Intent roomIntent = new Intent(mParentActivity, ChatActivity.class);
                // 입장한 방의 아이디 기록
                sJoinRoomId = room.getChatRoomId();
                roomIntent.putExtra(getString(R.string.room_id), sJoinRoomId);
                FirebaseDatabase.getInstance().getReference(FirebaseContract.CHAT_MEMBER).child(room.getChatRoomId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ArrayList<String> list = new ArrayList<>();
                                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                // 목록 가져오기
                                while (iterator.hasNext()) {
                                    list.add(iterator.next().getKey());
                                }
                                roomIntent.putStringArrayListExtra(getString(R.string.uids), list);
                                startActivityForResult(roomIntent, JOIN_ROOM_REQUEST_CODE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
            }
        }));

        addFirebaseChildEventListener(); // FirebaseDB 변경 이벤트 리스너 설정

        // [ RecyclerView 설정 End ]
        return root;
    }

    private void addFirebaseChildEventListener() {
        // 방의 변화 감지
        mRoomListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot roomDataSnapshot, @Nullable String s) {
                // 추가된 방 정보
                final Room room = roomDataSnapshot.getValue(Room.class);

                drawUI(room, EVENT.ADD_EVENT);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                // 변경된 방의 정보 수신
                Room updatedRoom = dataSnapshot.getValue(Room.class);

                // 현재 방의 정보
                Room oldRoom = mAdapter.getRoom(updatedRoom.getChatRoomId());

                // null 값이 딀어오는 경우 생략
                if (updatedRoom == null || oldRoom == null)
                    return;

                drawUI(updatedRoom, EVENT.UPDATE_EVENT); // UI 갱신

                // 새로운 방의 노티인 경우
                if(updatedRoom.getLastMessage() == null || updatedRoom.getLastMessage().getSentTime() == null) {
                    return;
                }

                // 내가 보낸 메시지가 아닌 경우만 노티 받음
                if (!updatedRoom.getLastMessage().getMessageSender().getUid().equals(mFirebaseUser.getUid())) {
                    // 방을 만들고 첫 메시지는 null
                    if (oldRoom.getLastMessage() == null || updatedRoom.getLastMessage().getSentTime().getTime() > oldRoom.getLastMessage().getSentTime().getTime()) {
                        // 내가 해당 방에 속해있으면 노티 생략
                        if (!updatedRoom.getChatRoomId().equals(sJoinRoomId)) {
                            Log.e("EVENT1", "3");
                            if(mParentActivity == null) return;
                            Intent msgIntent = new Intent(mParentActivity, ChatActivity.class);
                            msgIntent.putExtra("room_id", updatedRoom.getChatRoomId());
                            mNotification.setData(msgIntent)
                                    .setTitle(updatedRoom.getLastMessage().getMessageSender().getName())
                                    .setText(updatedRoom.getLastMessage().getMessageText())
                                    .notification();
                        }
                    }
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                final Room room = dataSnapshot.getValue(Room.class);
                dataSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        drawUI(room, EVENT.REMOVE_EVENT);
                        Snackbar.make(getView(), "방이 삭제되었습니다", Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void drawUI(Room room, EVENT event) {
        if (event == EVENT.ADD_EVENT) {
            mAdapter.addRoom(room);
        } else if (event == EVENT.UPDATE_EVENT) {
            mAdapter.updateItem(room);
        } else {
            mAdapter.removeRoom(room.getChatRoomId());
        }
        // 데이터 갱신
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mParentActivity = null;
    }

    public void setsJoinRoomId() {
        sJoinRoomId = "";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JOIN_ROOM_REQUEST_CODE) {
            sJoinRoomId = "";
        }
    }

    public void leaveRoom(final Room room) {

        mFirebaseDB.getReference(FirebaseContract.TRAVEL).child(room.getChatRoomId()).child("writerUid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String writer = dataSnapshot.getValue(String.class);
                if (writer.equals(mFirebaseUser.getUid())) {
                    Snackbar.make(getView(), "작성자는 방을 나갈 수 없습니다.", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // 메시지 출력
                Snackbar.make(getView(), getString(R.string.leave_msg), Snackbar.LENGTH_SHORT).
                        setAction(getString(R.string.positive_response), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // DB에서 해당 방 삭제
                                mRoomListRef.child(room.getChatRoomId()).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        // 나의 Profile List 에서 삭제하기

                                        // 삭제중 오류가 없는 경우만
                                        if (databaseError == null) {
                                            // 해당 채팅방의 멤버에서 제거
                                            mChatMemRef.child(room.getChatRoomId()).child(mFirebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        mFirebaseDB.getReference(FirebaseContract.CHAT_MESSAGES).child(room.getChatRoomId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                // 방 먼저 삭제
                                                                mAdapter.removeRoom(room.getChatRoomId());
                                                                mAdapter.notifyDataSetChanged();

                                                                // 방의 메시지 가져오기
                                                                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                                                                String myUid = mFirebaseUser.getUid();
                                                                // UnreadCount 변경
                                                                while (iterator.hasNext()) {
                                                                    DataSnapshot tmpSnapshot = iterator.next();
                                                                    Message msg = tmpSnapshot.getValue(Message.class);
                                                                    if (!msg.getReaderUids().contains(myUid))
                                                                        tmpSnapshot.child(FirebaseContract.UNREAD_COUNT).getRef().setValue(msg.getUnreadCount() - 1);
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
