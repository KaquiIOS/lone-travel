package com.example.collpasingtest.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.MessageListAdapter;
import com.example.collpasingtest.models.Message;
import com.example.collpasingtest.models.PhotoMessage;
import com.example.collpasingtest.models.TextMessage;
import com.example.collpasingtest.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private static final int PHOTO_GALLERY_REQUEST_CODE = 201; // 사진 Uri 요청 코드

    // [ Room default info definition Start ]
    private String mRoomId = "";            // 방 고유  ID
    // [ Room default info definition End ]


    // [ View Definition Start ]
    private RecyclerView mRecyclerView;
    private ImageView mMsgSendBtn;
    private ImageView mPhotoBtn;
    private EditText mContentEdt;
    private Toolbar mToolbar;
    // [ View Definition End ]


    // [ Firebase Access Object Definition Start ]
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mRoomRef;
    private DatabaseReference mMemRef;
    private DatabaseReference mMsgRef;
    private DatabaseReference mUserDBRef;
    private StorageReference mImageStorageRef;
    private FirebaseUser mUser;
    // [ Firebase Access Object Definition End ]


    // [ Message default Info Definition Start ]
    private Message.MessageType mSentMessageType = Message.MessageType.TEXT;
    private String mPhotoUrl = null;
    private String mMessageKey = null;
    private Message mNewMessage = null;
    // [ Message default Info Definition End ]


    // [ Listener Definition Start ]
    private MessageEventListener mMessageEventListener = new MessageEventListener();
    private ChatActivityOnClickListener mChatActivityOnClickListener = new ChatActivityOnClickListener();
    // [ Listener Definition End ]


    // [ Adapter Definition Start ]
    private MessageListAdapter mMsgAdapter = null;
    // [ Adapter Definition End ]


    private Map<String, Bitmap> userImage = new HashMap<>();
    private MemberEventListener memberEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // set default room info
        initActivity();
    }


    /*
     * initActivity
     * 액티비티에서 사용되는 View 얻기 및 설정, 기본 정보 설정 목적
     * */
    private void initActivity() {
        Intent intent = getIntent();

        mRoomId = intent.getStringExtra(getString(R.string.room_id)); // 현재 방 고유 아이디 얻어오기

        memberEventListener = new MemberEventListener();

        // [ View Setting Start ]
        mToolbar = findViewById(R.id.toolbar_chat_activity_title);
        mMsgSendBtn = findViewById(R.id.btn_chat_activity_send);
        mContentEdt = findViewById(R.id.edit_text_chat_activity_content);
        mPhotoBtn = findViewById(R.id.btn_chat_activity_photo);
        mRecyclerView = findViewById(R.id.recycler_view_chat_activity);


            // set onClickListener
        mMsgSendBtn.setOnClickListener(mChatActivityOnClickListener);
        mPhotoBtn.setOnClickListener(mChatActivityOnClickListener);

            // set RecyclerView
        mMsgAdapter = new MessageListAdapter(this);
        mRecyclerView.setAdapter(mMsgAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        // [ View Setting End ]

        // [ Firebase Access Object Setting Start ]
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mUserDBRef = mFirebaseDB.getReference(FirebaseContract.USERS);

            // 글이 생기면 항상 방이 미리 생김
            // 항상 방 ID 를 가지게 됨
        mRoomRef = mFirebaseDB.getReference(FirebaseContract.USERS).child(mUser.getUid()).child(FirebaseContract.ROOM).child(mRoomId);
        mMsgRef = mFirebaseDB.getReference(FirebaseContract.CHAT_MESSAGES).child(mRoomId);
        mMemRef = mFirebaseDB.getReference(FirebaseContract.CHAT_MEMBER).child(mRoomId);

            // 방 타이틀 변경
        mRoomRef.child(FirebaseContract.TITLE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mToolbar.setTitle(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        initTotalUnreadCount();
        // [ Firebase Access Object Setting End ]
    }

    /*
    * initTotalUnreadCount
    * 방에 입장하는 경우 안읽은 메시지를 다 읽음 처리
    * */
    private void initTotalUnreadCount() {
        mRoomRef.child(FirebaseContract.TOTAL_UNREAD_CNT).setValue(0);
    }


    /*
    * onPause
    * 방을 끄는 경우 리스너 때기
    * */
    @Override
    protected void onPause() {
        super.onPause();
        mMsgRef.removeEventListener(mMessageEventListener);
        removeMemberEventListener();
    }

    /*
    * onResume
    * 방을 다시 들어오는 경우 메시지 리스너 다시 부착
    * */
    @Override
    protected void onResume() {
        super.onResume();
        // 아이템 초기화
        mMsgAdapter.clearItems();
        mMsgAdapter.notifyDataSetChanged();

        // 리사이클러 뷰 pos 맨 밑으로 설정 (1회용)
        addMessagePosCheckListener();

        addMemberEventListener();

        // 메시지 이벤트 리스너 부착
        addMessageEventListener();
    }


    /*
    * addMessageEventListener
    * 메시지 이벤트 리스너 등록
    * */
    private void addMessageEventListener() {
        mMsgRef.addChildEventListener(mMessageEventListener);
    }

    /*
    * addMessagePosCheckListener
    * 새로운 메시지가 있으면 맨 아래로 recycler_view pos 변경
    * */
    private void addMessagePosCheckListener() {
        mMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long totalMessageCount = dataSnapshot.getChildrenCount();
                mMessageEventListener.setTotalMessageCount(totalMessageCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    /*
    * 이미지 선
    * */
    private void setPhotoUrl() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PHOTO_GALLERY_REQUEST_CODE);
    }

    /*
    * uploadImage
    * 선택한 이미지를 Firebase Storage 에 업로드한다.
    * */
    private void uploadImage(Uri uri) {
        // Firebase Storage 객체 설정
        if(mImageStorageRef == null)
            mImageStorageRef = FirebaseStorage.getInstance().getReference(FirebaseContract.ROOM).child(mRoomId);

        mImageStorageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()) {
                    // download url 가져오기
                    mImageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mPhotoUrl = uri.toString();
                            mSentMessageType = Message.MessageType.PHOTO;
                            sendMessage();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_GALLERY_REQUEST_CODE && data != null) {
            uploadImage(data.getData());
        }
    }

    /*
    * sendMessage
    * 현재 방에 메시지 전송 목적
    * */
    private void sendMessage() {

        // 보낼 메시지의 키 생성(push 를 통해)
        mMessageKey = mMsgRef.push().getKey();

        if(mSentMessageType == Message.MessageType.TEXT) {
            String sendMsg = mContentEdt.getText().toString();

            if(sendMsg.isEmpty()) {
                Snackbar.make(mContentEdt, getString(R.string.empty_msg_error), Snackbar.LENGTH_SHORT).show();
                return;
            }
            mNewMessage = new TextMessage(sendMsg);
        } else if(mSentMessageType == Message.MessageType.PHOTO) {
            if(mPhotoUrl == null || mPhotoUrl.equals("")) {
                Snackbar.make(mContentEdt, getString(R.string.photo_not_selected_error), Snackbar.LENGTH_SHORT).show();
                return;
            }
            mNewMessage = new PhotoMessage(mPhotoUrl);
        }


        mNewMessage.setChatRoomId(mRoomId);
        mNewMessage.setSentTime(new Date());
        mNewMessage.setMessageId(mMessageKey);
        mNewMessage.setMessageType(mSentMessageType);
        mNewMessage.setMessageSender(new User(mUser));
        mNewMessage.setReaderUids(new ArrayList<>(Arrays.asList(new String[]{mUser.getUid()})));

        //  default setting
        mContentEdt.setText("");
        mSentMessageType = Message.MessageType.TEXT;

        // 방에 속한 사람들의 정보 수정(가지고 있는 방의 LastMessage 수정)
        mMemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                // 방에 참여한 사람 수
                long numOfMember = dataSnapshot.getChildrenCount();
                // 나를 제외한 안읽은 사람 수 설정
                mNewMessage.setUnreadCount(numOfMember - 1);

                // 설정된 메시지 DB에 입력
                mMsgRef.child(mNewMessage.getMessageId()).setValue(mNewMessage, new DatabaseReference.CompletionListener() {
                    // 입력이 끝난 경우
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        // 에러가 없는 경우
                        if(databaseError == null) {
                            // 방에 참여한 사람들의 users/{user_id}/rooms/{room_id}/lastMessage 값 변경
                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                            while(iterator.hasNext()) {
                                final User user = iterator.next().getValue(User.class);
                                // Last Message 변경
                                mUserDBRef.child(user.getUid()).child(FirebaseContract.ROOM)
                                        .child(mRoomId).child(FirebaseContract.LAST_MESSAGE)
                                        .setValue(mNewMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            // 안읽은 유저의 TotalUnreadCount + 1 설정
                                            // 나는 메시지를 보낸 주체 -> 따로 설정 X
                                            if(!user.getUid().equals(mUser.getUid())) {
                                                mUserDBRef.child(user.getUid()).child(FirebaseContract.ROOM)
                                                        .child(mRoomId).child(FirebaseContract.TOTAL_UNREAD_CNT)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                long prevTotalUnreadCount = dataSnapshot.getValue(long.class);
                                                                dataSnapshot.getRef().setValue(prevTotalUnreadCount + 1);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                                        });
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void addMemberEventListener() {
        mMemRef.addChildEventListener(memberEventListener);
    }

    private void removeMemberEventListener() {
        mMemRef.removeEventListener(memberEventListener);
    }

    private class MemberEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            User user = dataSnapshot.getValue(User.class);
            if(!user.getUid().equals(mUser.getUid()))
                mMsgAdapter.addUserProfile(user.getUid(), user.getProfileUrl());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            mMsgAdapter.removeUserProfile(user.getUid());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    }


    /*
     * MessageEventListener
     * ChatActivity 에서 발생하는 Message send, receive Event 처리 목적
     */
    private class MessageEventListener implements ChildEventListener {

        private long totalMessageCount = 0;
        private long eventCallCount = 1;       // onChildAdded Event

        public void setTotalMessageCount(long totalMessageCount) {
            this.totalMessageCount = totalMessageCount;
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Message item = null;

            // 가끔 item을 잘 못받아오는 경우 발생
            try {
                item = dataSnapshot.getValue(Message.class);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return;
            }

            // 내가 안읽은 메시지들만 대상으로 readCount - 1 해주기
            final List<String> readerUids = item.getReaderUids();

            if(readerUids != null) {
                // 안읽은 메시지만 Transaction을 통해 처리
                if(!readerUids.contains(mUser.getUid())) {
                    dataSnapshot.getRef().runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            // 안읽은 메시지의 unreadCount - 1 설정 및 읽은 UIDS 에 나의 UID 추가
                            Message mutableMsg = mutableData.getValue(Message.class);

                            List<String> unreadUsers = mutableMsg.getReaderUids(); // 안읽은 사람 목록
                            unreadUsers.add(mUser.getUid());       // 나의 uid 추가 (읽음 처리)

                            long unreadCount = mutableMsg.getUnreadCount() - 1;

                            // 각 타입별로 처리하지 않으면 메시지 타입이 하나로 통합되어버림
                            if(mutableMsg.getMessageType() == Message.MessageType.PHOTO) {
                                PhotoMessage imgMsg = mutableData.getValue(PhotoMessage.class);
                                imgMsg.setReaderUids(unreadUsers);
                                imgMsg.setUnreadCount(unreadCount);
                                mutableData.setValue(imgMsg);
                            } else if(mutableMsg.getMessageType() == Message.MessageType.TEXT) {
                                TextMessage txtMsg = mutableData.getValue(TextMessage.class);
                                txtMsg.setReaderUids(unreadUsers);
                                txtMsg.setUnreadCount(unreadCount);
                                mutableData.setValue(txtMsg);
                            }

                            // Transaction이 실패하면 abort 반환
                            // 제대로 작업이 수행되는 경우엔 success 반환
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                            // 에러가 없는 경우 전체 메시지 읽음 처리
                            if(databaseError == null)
                                initTotalUnreadCount();
                        }
                    });
                }
            }

            // 추가된 메시지 더해주기
            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage txtMsg = dataSnapshot.getValue(TextMessage.class);
                mMsgAdapter.addItem(txtMsg);
            } else if(item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage imgMsg = dataSnapshot.getValue(PhotoMessage.class);
                mMsgAdapter.addItem(imgMsg);
            }
            // 아이템 추가 알려주기
            mMsgAdapter.notifyDataSetChanged();

            // 스크롤 맨 아래로 내려주기
            if(totalMessageCount <= eventCallCount)
                mRecyclerView.scrollToPosition(mMsgAdapter.getItemCount() - 1); // 맨마지막 위치로

            eventCallCount++;
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            // 메시지를 누군가 읽으면 readCount 변경
            Message item = null;

            try {
                item = dataSnapshot.getValue(Message.class);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return;
            }

            int updateMessagePos = MessageListAdapter.NOT_FOUND;

            if(item.getMessageType() == Message.MessageType.TEXT) {
                TextMessage txtMsg = dataSnapshot.getValue(TextMessage.class);
                updateMessagePos = mMsgAdapter.updateItem(txtMsg);
            } else if(item.getMessageType() == Message.MessageType.PHOTO) {
                PhotoMessage imgMsg = dataSnapshot.getValue(PhotoMessage.class);
                updateMessagePos = mMsgAdapter.updateItem(imgMsg);
            }

            if(updateMessagePos != MessageListAdapter.NOT_FOUND)
                mMsgAdapter.notifyItemChanged(updateMessagePos);
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    /*
     * ChatActivityOnClickListener
     * ChatActivity 에서 일어나는 Click Event 처리 목적
     */
    private class ChatActivityOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_chat_activity_send:
                    sendMessage();
                    break;

                case R.id.btn_chat_activity_photo:
                    setPhotoUrl();
                    break;
            }
        }
    }
}
