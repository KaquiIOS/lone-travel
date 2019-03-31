package com.example.collpasingtest.views;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.collpasingtest.adapters.AddedRouteAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.CommentListAdapter;
import com.example.collpasingtest.models.Comment;
import com.example.collpasingtest.models.MyTrip;
import com.example.collpasingtest.models.Room;
import com.example.collpasingtest.models.Travel;
import com.example.collpasingtest.models.User;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailTravelInfoActivity extends AppCompatActivity {

    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static int UPDATE_WRITING_REQ_CODE = 101;

    private String contentId;

    private FirebaseUser mUser;
    private FirebaseDatabase mDatabase;
    private FirebaseUser mCurrUser;
    private DatabaseReference mUsersRef, mMsgRef, mCommentRef, mMemRef, mTravelRef, mLikeRef, mRouteRef;

    private EditText mCommentEditText;
    private ImageButton mCommentAddBtn;
    private TextView mRegionTextView;

    private Travel curTravelInfo;

    private RecyclerView mRecyclerView;
    private CommentListAdapter mCommentAdapter;
    private AddedRouteAdapter mRouteAdapter;
    private int mCommentCnt = 0;
    private LinearLayout mParticipantsContainer;
    private TextView mLikeNumTextView, mChatNumTextView, mTitleTextView;

    private boolean isParticipatedIn = false;

    private ImageView mCommentBtn, mRouteBtn;

    private CommentUpdateEventListener listener;

    private CircleImageView[] mParticipants = new CircleImageView[4];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_travel_info);

        listener = new CommentUpdateEventListener();

        curTravelInfo = (Travel) getIntent().getSerializableExtra("travel_info");

        contentId = curTravelInfo.getContentId();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance();
        mMemRef = mDatabase.getReference(FirebaseContract.CHAT_MEMBER);
        mMsgRef = mDatabase.getReference(FirebaseContract.CHAT_MESSAGES);
        mUsersRef = mDatabase.getReference(FirebaseContract.USERS);
        mCommentRef = mDatabase.getReference(FirebaseContract.COMMENT_LIST).child(curTravelInfo.getContentId());
        mTravelRef = mDatabase.getReference(FirebaseContract.TRAVEL);
        mLikeRef = mDatabase.getReference(FirebaseContract.LIKE_USER_LIST);
        mRouteRef = mDatabase.getReference(FirebaseContract.ROUTE_LIST);

        mCurrUser = FirebaseAuth.getInstance().getCurrentUser();

        mRegionTextView = findViewById(R.id.text_view_detail_travel_info_location);
        mLikeNumTextView = findViewById(R.id.text_view_detail_travel_info_like_num);
        mChatNumTextView = findViewById(R.id.text_view_detail_travel_info_chat_num);

        mCommentBtn = findViewById(R.id.image_view_detail_travel_info_chat);
        mRouteBtn = findViewById(R.id.image_view_detail_travel_info_route);

        mCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setAdapter(mCommentAdapter);
                mCommentAdapter.notifyDataSetChanged();
            }
        });

        mRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setAdapter(mRouteAdapter);
                mRouteAdapter.notifyDataSetChanged();
            }
        });

        mRecyclerView = findViewById(R.id.recycler_view_detail_travel_info_comments);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mCommentAdapter = new CommentListAdapter(this, curTravelInfo);

        mRouteAdapter = new AddedRouteAdapter(this);
        mRouteAdapter.setCanMove(false);
        mRouteAdapter.setCanRemove(false);
        if (curTravelInfo.getRouteInfo() != null)
            mRouteAdapter.addLocations(curTravelInfo.getRouteInfo().getRouteList());

        mRecyclerView.setAdapter(mCommentAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar_detail_travel_info_top);
        mTitleTextView = toolbar.findViewById(R.id.text_view_detail_travel_info_title);
        mTitleTextView.setText("여행 이야기");
        Button btnBack = toolbar.findViewById(R.id.btn_detail_travel_info_back);


        ImageView showMapBtn = findViewById(R.id.image_view_detail_travel_info_open_map);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curTravelInfo.getRouteInfo() != null && curTravelInfo.getRouteInfo().getRouteList().size() == 0) {
                    Toast.makeText(DetailTravelInfoActivity.this, "설정된 여행 경로가 없습니다", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(DetailTravelInfoActivity.this, RouteMapActivity.class);
                intent.putExtra("info", curTravelInfo.getRouteInfo());
                startActivity(intent);
                mCommentCnt = 0;
            }
        });

        mParticipants[0] = findViewById(R.id.image_view_detail_travel_info_part1);
        mParticipants[1] = findViewById(R.id.image_view_detail_travel_info_part2);
        mParticipants[2] = findViewById(R.id.image_view_detail_travel_info_part3);
        mParticipants[3] = findViewById(R.id.image_view_detail_travel_info_part4);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("travel_info", curTravelInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mCommentEditText = findViewById(R.id.edit_text_detail_travel_info_comment);
        mCommentAddBtn = findViewById(R.id.btn_detail_travel_info_comment_add);

        mCommentAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCommentEditText.getText().toString().isEmpty()) {
                    Snackbar.make(getWindow().getDecorView().getRootView(), "내용은 먼저 입력해주세요 !", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                String comment = mCommentEditText.getText().toString();
                mCommentEditText.setText("");
                addComment(comment);
            }
        });


        final CircleImageView writerProfile = findViewById(R.id.image_view_detail_travel_info_profile);

        mUsersRef.child(curTravelInfo.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User writer = dataSnapshot.getValue(User.class);
                Glide.with(DetailTravelInfoActivity.this).load(writer.getProfileUrl()).into(writerProfile);
                TextView textView = findViewById(R.id.text_view_detail_travel_info_id);
                textView.setText(writer.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Placeholder 설정
            }
        });

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 현재 사용자에게만 보여주기
        if (curTravelInfo.getWriterUid().equals(mUser.getUid())) {

            ImageView btnRemove = toolbar.findViewById(R.id.image_view_detail_travel_info_remove);
            btnRemove.setVisibility(View.VISIBLE);
            ImageView btnModify = toolbar.findViewById(R.id.image_view_detail_travel_info_modify);
            btnModify.setVisibility(View.VISIBLE);

            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 글 삭제 후
                    // 채팅방 삭제
                    mMemRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();

                            // 유저 목록에서 방 제거
                            while (it.hasNext()) {
                                User user = it.next().getValue(User.class);
                                if (user.getUid().equals(curTravelInfo.getWriterUid())) {
                                    mUsersRef.child(user.getUid()).child(FirebaseContract.TRAVEL).child(curTravelInfo.getContentId()).removeValue();
                                }
                                mUsersRef.child(user.getUid()).child(FirebaseContract.ROOM).child(curTravelInfo.getContentId()).removeValue();
                            }

                            // 글 삭제
                            mTravelRef.child(curTravelInfo.getContentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // 채팅방 삭제
                                    mMemRef.child(curTravelInfo.getContentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            // 댓글 목록 삭제
                                            mCommentRef.child(curTravelInfo.getContentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    // 채팅방 메시지 삭제
                                                    mMsgRef.child(curTravelInfo.getContentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            // 삭제되었다는 정보 알려주기
                                                            Intent intent = new Intent();
                                                            intent.putExtra("isDeleted", true);
                                                            intent.putExtra("travel_info", curTravelInfo);
                                                            setResult(RESULT_OK, intent);
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            });

            btnModify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //수정 실행
                    Intent intent = new Intent(DetailTravelInfoActivity.this, TravelAddActivity.class);
                    intent.putExtra("travel_info", curTravelInfo);
                    startActivityForResult(intent, DetailTravelInfoActivity.UPDATE_WRITING_REQ_CODE);
                }
            });
        }
        // 참가자 사진 목록 보여주기
        mParticipantsContainer = findViewById(R.id.container_detail_travel_info_participants);

        Button chatParticipateBtn = findViewById(R.id.btn_detail_travel_info_participate_chat);
        chatParticipateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 내 방목록에서 확인해보기
                mUsersRef.child(mUser.getUid()).child(FirebaseContract.ROOM).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                        while (it.hasNext()) {
                            if (it.next().equals(curTravelInfo.getContentId())) {
                                isParticipatedIn = true;
                                break;
                            }
                        }

                        final Intent intent = new Intent(DetailTravelInfoActivity.this, ChatActivity.class);
                        intent.putExtra("room_id", contentId);
                        ChatListFragment.sJoinRoomId = contentId;

                        // 참여한 방
                        if (isParticipatedIn) {
                            startActivity(intent);
                        } else {
                            // 참여하지 않은 방
                            // 사용자 방 목록 추가
                            mUsersRef.child(curTravelInfo.getWriterUid()).child(FirebaseContract.ROOM)
                                    .child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    final Room room = dataSnapshot.getValue(Room.class);

                                    mUsersRef.child(mUser.getUid()).child(FirebaseContract.ROOM)
                                            .child(room.getChatRoomId()).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {

                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                User user = new User(mUser);
                                                mMemRef.child(room.getChatRoomId()).child(user.getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(intent);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        final ImageView likeBtn = findViewById(R.id.image_view_detail_travel_info_like);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLikeRef.child(curTravelInfo.getContentId()).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String val = dataSnapshot.getValue(String.class);

                        mLikeRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (val != null) {
                                    mLikeRef.child(curTravelInfo.getContentId()).child(mUser.getUid()).getRef().removeValue();
                                    likeBtn.setImageResource(R.drawable.ic_like_tavel_fragment_default_32);
                                    mLikeNumTextView.setText(String.valueOf(dataSnapshot.getChildrenCount() - 1));
                                } else {
                                    mLikeRef.child(curTravelInfo.getContentId()).child(mUser.getUid()).getRef().setValue(mUser.getUid());
                                    likeBtn.setImageResource(R.drawable.ic_like_tavel_fragment_liked_32);
                                    mLikeNumTextView.setText(String.valueOf(dataSnapshot.getChildrenCount() + 1));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mLikeRef.child(curTravelInfo.getContentId()).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String val = dataSnapshot.getValue(String.class);
                mLikeRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (val != null) {
                            likeBtn.setImageResource(R.drawable.ic_like_tavel_fragment_liked_32);
                        } else {
                            likeBtn.setImageResource(R.drawable.ic_like_tavel_fragment_default_32);
                        }
                        mLikeNumTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });


        // contentId 로 정보 불러오기
        initActivity();
    }

    private void addComment(String comment) {

        final String newKey = mCommentRef.push().getKey();

        final Comment newComment = new Comment(curTravelInfo.getContentId(), mCurrUser.getUid(), newKey, comment, new Date());

        mCommentRef.child(newKey).setValue(newComment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mCommentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int numOfChat = (int) dataSnapshot.getChildrenCount();
                            mTravelRef.child(curTravelInfo.getContentId()).child("numComment").setValue(numOfChat).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //mCommentAdapter.addItem(newComment);
                                        //mCommentAdapter.notifyDataSetChanged();
                                        //mChatNumTextView.setText(String.valueOf(mCommentAdapter.size()));
                                        Snackbar.make(getWindow().getDecorView().getRootView(), "댓글 등록이 완료되었습니다", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCommentAdapter.clear();
        mCommentCnt = 0;
        mCommentRef.addChildEventListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCommentRef.removeEventListener(listener);
    }

    private void initActivity() {

        // Like 에서 가져오기
        mLikeRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mLikeNumTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mCommentRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatNumTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // 기간 설정
        TextView periodTextView = findViewById(R.id.text_view_detail_travel_info_period);
        periodTextView.setText(sSimpleDateFormat.format(curTravelInfo.getTravelStartDate()) + "~" +
                sSimpleDateFormat.format(curTravelInfo.getTravelEndDate()));

        // 컨텐츠 설정
        TextView contentTextView = findViewById(R.id.text_view_detail_travel_info_content);
        contentTextView.setText(curTravelInfo.getContent());

        mRegionTextView.setText(curTravelInfo.getRegion());

        ImageView userImageView = findViewById(R.id.image_view_detail_travel_info_selected_image);
        // 이미지 설정
        if (curTravelInfo.getPhotoPath() != null) {
            Glide.with(this).load(curTravelInfo.getPhotoPath()).into(userImageView);
            userImageView.setVisibility(View.VISIBLE);
        } else {
            // remove User Image View
            userImageView.setImageResource(0);
            userImageView.setVisibility(View.GONE);
        }

        if (curTravelInfo.getRouteInfo() != null)
            mRouteAdapter.setLocations(curTravelInfo.getRouteInfo().getRouteList());

        mMemRef.child(curTravelInfo.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (int i = 0; i < 4; ++i) {
                    mParticipants[i].setVisibility(View.GONE);
                }

                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();

                long cnt = dataSnapshot.getChildrenCount() < 4 ? dataSnapshot.getChildrenCount() : 4;

                for (int i = 0; i < cnt; ++i) {

                    String imageUri = it.next().child(FirebaseContract.PROFILE_URL).getValue(String.class);

                    mParticipants[i].setBorderWidth(4);
                    mParticipants[i].setVisibility(View.VISIBLE);
                    mParticipants[i].setBorderColor(getColor(R.color.main_red));
                    Glide.with(DetailTravelInfoActivity.this).load(imageUri).into(mParticipants[i]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // 수정 삭제 자기만 보이기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_WRITING_REQ_CODE && data != null) {
            // 업데이트
            curTravelInfo = (Travel) data.getSerializableExtra("travel_info");
            // 유저랑
            mUsersRef.child(curTravelInfo.getWriterUid()).child(FirebaseContract.TRAVEL).child(curTravelInfo.getContentId()).setValue(curTravelInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        initActivity();
                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("travel_info", curTravelInfo);
        setResult(RESULT_OK, intent);
        finish();
    }


    private class CommentUpdateEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Comment c = dataSnapshot.getValue(Comment.class);
            if (c.getCommentId() == null) return;
            mCommentAdapter.addItem(c);
            mChatNumTextView.setText(String.valueOf(++mCommentCnt));
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Comment c = dataSnapshot.getValue(Comment.class);
            mCommentAdapter.removeImte(c);
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
        }
    }
}
