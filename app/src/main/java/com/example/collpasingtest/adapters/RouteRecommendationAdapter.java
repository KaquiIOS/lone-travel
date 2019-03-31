package com.example.collpasingtest.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SharedRouteInfo;
import com.example.collpasingtest.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RouteRecommendationAdapter extends RecyclerView.Adapter<RouteRecommendationAdapter.RecItemHolder> {

    private ArrayList<SharedRouteInfo> sharedRouteInfos;
    private Context context;

    private RecyclerViewOnClickListener clickListener;
    private TagAdapter tagAdapter;
    private AddedRouteAdapter routeAdapter;

    private DatabaseReference mUserRef, mRouteRef, mSharedRef, mSharedLikeRef;
    private FirebaseUser mUser;

    public RouteRecommendationAdapter(Context context) {
        this.context = context;
        this.sharedRouteInfos = new ArrayList<>();

        tagAdapter = new TagAdapter(context);
        tagAdapter.setCanRemove(false);

        routeAdapter = new AddedRouteAdapter(context);
        routeAdapter.setCanRemove(false);
        routeAdapter.setCanMove(false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mRouteRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseContract.ROUTE);

        mUserRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS);
        mSharedRef = FirebaseDatabase.getInstance().getReference("sharedRoute");
        mSharedLikeRef = FirebaseDatabase.getInstance().getReference("sharedRouteLike");
    }

    public void setClickListener(RecyclerViewOnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public RecItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggested_route, parent, false);
        return new RecItemHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return sharedRouteInfos.get(position).getSharingUserID().hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull final RecItemHolder holder, final int position) {

        final SharedRouteInfo info = sharedRouteInfos.get(position);

        holder.userProfile.setBorderWidth(2);
        holder.userProfile.setBorderColor(context.getColor(R.color.main_red));

        routeAdapter.setLocations(new ArrayList<PathType>(info.getRouteList()));
        holder.routeList.setAdapter(routeAdapter);
        holder.routeList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        tagAdapter.setTagList( new ArrayList<>(info.getTags()));
        holder.tagList.setAdapter(tagAdapter);
        holder.tagList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        holder.title.setText(info.getTitle());
        holder.content.setText(info.getContent());
        holder.likeNum.setText(String.valueOf(info.getLikeNum()));
        holder.shareNum.setText(String.valueOf(info.getSharedNum()));

        mUserRef.child(info.getSharingUserID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(context).load(user.getProfileUrl()).into(holder.userProfile);
                holder.userName.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // 초기 이미지 설정
        mSharedLikeRef.child(info.getRouteKey()).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final String val = dataSnapshot.getValue(String.class);
                if(val == null) {
                    holder.like.setImageResource(R.drawable.ic_like_travel_fragment_default_24);
                } else {
                    holder.like.setImageResource(R.drawable.ic_like_travel_fragment_liked_24);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 좋아요 버튼 누르기 이벤트 처리
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSharedLikeRef.child(info.getRouteKey()).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final @NonNull DataSnapshot sharedInfoSnapshot) {
                        final String val = sharedInfoSnapshot.getValue(String.class);

                        mSharedRef.child(info.getRouteKey()).child("likeNum").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                int temp = dataSnapshot.getValue(Integer.class);
                                final int curLikeNum = (val == null) ? temp + 1: temp - 1;

                                // 좋아요를 누르지 않은 게시물
                                if(val == null) {
                                    holder.like.setImageResource(R.drawable.ic_like_travel_fragment_liked_24);
                                    sharedInfoSnapshot.getRef().setValue(mUser.getUid());
                                }
                                // 좋아요를 누른 게시물
                                else {
                                    holder.like.setImageResource(R.drawable.ic_like_travel_fragment_default_24);
                                    sharedInfoSnapshot.getRef().removeValue();
                                }

                                dataSnapshot.getRef().setValue(curLikeNum);
                                holder.likeNum.setText(String.valueOf(curLikeNum));
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

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(info.getSharingUserID())) {
                    Toast.makeText(context, "나의 게시물입니다 !", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 다이얼로그를 보여주고 공유받기
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("공유받기")
                        .setMessage("경로를 공유받으시겠습니까 ?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 글 삭제
                                String newKey = mRouteRef.push().getKey();

                                RouteInfo sinfo = new RouteInfo(new ArrayList<PathType>(info.getRouteList()), info.getTitle(),
                                        info.getContent(), newKey, false);

                                mSharedRef.child(info.getRouteKey()).child("sharedNum").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        int val = dataSnapshot.getValue(Integer.class) + 1;
                                        dataSnapshot.getRef().setValue(val);
                                        holder.shareNum.setText(String.valueOf(val));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });

                                mRouteRef.child(newKey).setValue(sinfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "경로를 받아왔습니다 !", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
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

    @Override
    public int getItemCount() {
        return sharedRouteInfos.size();
    }

    public SharedRouteInfo getSharedInfo(int pos) {
        return sharedRouteInfos.get(pos);
    }

    public void addRoute(SharedRouteInfo route) {
        sharedRouteInfos.add(route);

    }

    public void clear() {
        sharedRouteInfos.clear();
    }

    public static class RecItemHolder extends RecyclerView.ViewHolder {

        public CircleImageView userProfile;
        public ImageView like, share, show;
        public TextView userName, title, content, likeNum, shareNum;
        public RecyclerView tagList, routeList;

        public RecItemHolder(View itemView) {
            super(itemView);

            userProfile = itemView.findViewById(R.id.image_view_item_suggest_profile);
            like = itemView.findViewById(R.id.image_view_item_suggest_like);
            share = itemView.findViewById(R.id.image_view_item_suggest_shared);

            userName = itemView.findViewById(R.id.text_view_item_suggest_name);
            title = itemView.findViewById(R.id.text_view_item_suggest_title);
            content = itemView.findViewById(R.id.text_view_item_suggest_content);
            likeNum = itemView.findViewById(R.id.text_view_item_suggest_like_num);
            shareNum = itemView.findViewById(R.id.text_view_item_suggest_shared_num);

            tagList = itemView.findViewById(R.id.recycler_view_item_suggest_tags);
            routeList = itemView.findViewById(R.id.recycler_view_item_suggest_route);
        }
    }
}
