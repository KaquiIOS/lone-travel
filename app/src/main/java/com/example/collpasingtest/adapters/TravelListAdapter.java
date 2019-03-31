package com.example.collpasingtest.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.OnTravelRecyclerViewClickListener;
import com.example.collpasingtest.models.Comment;
import com.example.collpasingtest.models.Travel;
import com.example.collpasingtest.models.User;
import com.example.collpasingtest.views.MainActivity;
import com.example.collpasingtest.views.TravelAddActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TravelListAdapter extends RecyclerView.Adapter<TravelListAdapter.TravelRecyclerViewHolder> {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Context
    private Context mContext;
    private MainActivity mainActivity;

    // ItemList
    private List<Travel> mItems = new ArrayList<>();

    // ClickListener
    private OnTravelRecyclerViewClickListener mClickListener;

    private DatabaseReference mUserRef, mTravelRef, mMemRef, mMsgRef, mLikeRef, mCommentRef;
    private String curUID;

    public TravelListAdapter(@NonNull Context mContext, String curUID) {
        this.mContext = mContext;
        this.mUserRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS);
        this.mTravelRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.TRAVEL);
        this.mMemRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.CHAT_MEMBER);
        this.mMsgRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.CHAT_MESSAGES);
        this.mLikeRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.LIKE_USER_LIST);
        this.mCommentRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.COMMENT_LIST);
        this.curUID = curUID;
        mainActivity = (MainActivity) mContext;
    }

    public TravelListAdapter(@NonNull Context mContext, @NonNull List<Travel> mItems) {
        this.mContext = mContext;
        this.mItems = new ArrayList<>(mItems);
    }

    public void setOnClickListener(OnTravelRecyclerViewClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public String getContentUID(int pos) {
        return mItems.get(pos).getContentId();
    }

    public Travel getTravelItem(int pos) {
        return mItems.get(pos);
    }

    public boolean updatePost(Travel travel) {
        Iterator<Travel> it = mItems.iterator();
        int pos = 0;
        while(it.hasNext()) {
            Travel t = it.next();
            if (t.getContentId().equals(travel.getContentId())) {
                // 변경되지 않은 정보
                if(t.equals(travel))
                    return true;
                mItems.set(pos, travel);
                return true;
            }
            pos++;
        }
        return false;
    }

    public void clear() {
        mItems.clear();
    }

    @NonNull
    @Override
    public TravelRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(mContext).inflate(R.layout.item_travel, parent, false);

        return new TravelRecyclerViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull final TravelRecyclerViewHolder holder, final int position) {

        // bind View
        final Travel item = mItems.get(position);

        // itemClick Event 처리
        if (mClickListener != null) {
            holder.contentTextVIew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.setOnClickListener(holder, holder.itemView, position);
                }
            });
        }

        // 좋아요 처리
        mLikeRef.child(item.getContentId()).child(curUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String val = dataSnapshot.getValue(String.class);
                if(val == null) {
                    holder.btnLike.setImageResource(R.drawable.ic_like_travel_fragment_default_24);
                } else {
                    holder.btnLike.setImageResource(R.drawable.ic_like_travel_fragment_liked_24);
                }

                mLikeRef.child(item.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        holder.numOfLike.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 댓글 수 처리
        mCommentRef.child(item.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.numOfChat.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 내 글이면 수정, 삭제 버튼 보이기
        if (curUID.equals(item.getWriterUid())) {
            holder.btnAlter.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.VISIBLE);
            // 변경 이벤트 처리
            holder.btnAlter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 현해 TravelItem을 보내기
                    Intent intent = new Intent(mContext, TravelAddActivity.class);
                    intent.putExtra("travel_info", item);
                    ((MainActivity) mContext).startActivityForResult(intent, MainActivity.TRAVEL_UPDATE_CODE);
                }
            });
            // 삭제 이벤트 처리
            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                            .setTitle("글 삭제하기")
                            .setMessage("글을 삭제하시겠습니까 ?")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mMemRef.child(item.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                                            while(iterator.hasNext()) {
                                                User user = iterator.next().getValue(User.class);
                                                // 유저들 목록에서 채팅방 먼저 삭제
                                                mUserRef.child(user.getUid()).child(FirebaseContract.ROOM).child(item.getContentId()).removeValue();
                                            }
                                            // 글 삭제
                                            mUserRef.child(item.getWriterUid()).child(FirebaseContract.TRAVEL)
                                                    .child(item.getContentId()).removeValue();

                                            // 채팅 멤버 목록에서 삭제
                                            mMemRef.child(item.getContentId()).removeValue();
                                            mMsgRef.child(item.getContentId()).removeValue();
                                            mTravelRef.child(item.getContentId()).removeValue();

                                            mItems.remove(position);
                                            notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                                }
                            }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                }
            });
        }

        // set RoundImage
        holder.profileImageView.setBackground(new ShapeDrawable(new OvalShape()));
        holder.profileImageView.setClipToOutline(true);

        mUserRef.child(item.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                // 유저 이름
                holder.nameTextView.setText(user.getName());
                // 유저 이미지 설정
                if (user.getProfileUrl() != null)
                    Glide.with(mContext).load(user.getProfileUrl()).thumbnail(0.2f).into(holder.profileImageView);
                else
                    holder.profileImageView.setImageResource(R.drawable.ic_common_default_profile_32); // set default image

                // 누르기
                holder.btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mLikeRef.child(item.getContentId()).child(curUID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                final String val = dataSnapshot.getValue(String.class);

                                mLikeRef.child(item.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // 있는 값
                                        if(val != null) {
                                            mLikeRef.child(item.getContentId()).child(curUID).getRef().removeValue();
                                            holder.btnLike.setImageResource(R.drawable.ic_like_tavel_fragment_default_32);
                                            holder.numOfLike.setText(String.valueOf(dataSnapshot.getChildrenCount() - 1));
                                        } else {
                                            mLikeRef.child(item.getContentId()).child(curUID).getRef().setValue(curUID);
                                            holder.btnLike.setImageResource(R.drawable.ic_like_tavel_fragment_liked_32);
                                            holder.numOfLike.setText(String.valueOf(dataSnapshot.getChildrenCount() + 1));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 사용자가 등록한 이미지 설정
        if (item.getPhotoPath() == null)
            holder.selectedImageView.setVisibility(View.GONE);
        else {
            holder.selectedImageView.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(item.getPhotoPath()).into(holder.selectedImageView);
        }

        // 여행 기간 설정
        holder.periodTextView.setText(String.format("%s ~ %s",
                SIMPLE_DATE_FORMAT.format(item.getTravelStartDate()), SIMPLE_DATE_FORMAT.format(item.getTravelEndDate())));
        // 지역 설정
        holder.regionTextView.setText(item.getRegion());
        // 내용 설정
        holder.contentTextVIew.setText(item.getContent());

        // 채팅에 들어와있는 사람들 목록 보여주기
        mMemRef.child(item.getContentId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 유저들의 Profile 사진 가져오기
                for(int i = 0; i < 4; ++i)
                    holder.participants[i].setVisibility(View.GONE);

                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                int len = (int) (dataSnapshot.getChildrenCount() < 4 ? dataSnapshot.getChildrenCount() : 4);

                for (int i = 0; i < len; ++i) {
                    final User user = iterator.next().getValue(User.class);
                    holder.participants[i].setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(user.getProfileUrl()).thumbnail(0.2f).into(holder.participants[i]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public int removeItem(Travel travel) {
        int pos = 0;
        Iterator<Travel> it = mItems.iterator();
        while(it.hasNext()) {
            if(it.next().getContentId().equals(travel.getContentId())) {
                it.remove();
                notifyItemRemoved(pos);
                break;
            }
            pos++;
        }
        return pos;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    public void addViewItem(Travel item) {
        mItems.add(item);
    }

    @NonNull
    public void addViewItems(List<Travel> items) {
        mItems.addAll(items);
    }


    public static class TravelRecyclerViewHolder extends RecyclerView.ViewHolder {

        public ImageView profileImageView, selectedImageView;
        public ImageView btnLike, btnAlter, btnRemove;
        public TextView numOfChat, numOfLike, nameTextView, periodTextView, regionTextView, contentTextVIew;
        public LinearLayout chatParticipantsContainer;

        public CircleImageView[] participants = new CircleImageView[4];

        public TravelRecyclerViewHolder(final View itemView) {
            super(itemView);

            chatParticipantsContainer = itemView.findViewById(R.id.linear_item_trevel_chat_mem_profiles);
            profileImageView = itemView.findViewById(R.id.image_view_travel_item_profile);
            selectedImageView = itemView.findViewById(R.id.image_view_travel_item_photo);
            btnLike = itemView.findViewById(R.id.image_view_travel_item_like);
            numOfChat = itemView.findViewById(R.id.text_view_travel_item_comment_num);
            numOfLike = itemView.findViewById(R.id.text_view_travel_item_like_num);
            nameTextView = itemView.findViewById(R.id.text_view_travel_item_id);
            periodTextView = itemView.findViewById(R.id.text_view_travel_item_travel_period);
            regionTextView = itemView.findViewById(R.id.text_view_travel_item_location);
            contentTextVIew = itemView.findViewById(R.id.text_view_travel_item_content);
            btnAlter = itemView.findViewById(R.id.image_view_travel_item_modify);
            btnRemove = itemView.findViewById(R.id.image_view_travel_item_remove);

            participants[0] = itemView.findViewById(R.id.image_view_travel_chat_mem1);
            participants[1] = itemView.findViewById(R.id.image_view_travel_chat_mem2);
            participants[2] = itemView.findViewById(R.id.image_view_travel_chat_mem3);
            participants[3] = itemView.findViewById(R.id.image_view_travel_chat_mem4);

            for (int i = 0; i < 4; ++i) {
                participants[i].setVisibility(View.GONE);
            }
        }
    }
}
