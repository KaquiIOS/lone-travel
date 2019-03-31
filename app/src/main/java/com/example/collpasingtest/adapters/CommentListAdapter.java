package com.example.collpasingtest.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.models.Comment;
import com.example.collpasingtest.models.Travel;
import com.example.collpasingtest.models.User;
import com.example.collpasingtest.views.DetailTravelInfoActivity;
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
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.CommentHolder>{

    private Context mContext;
    private List<Comment> mCommentList;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef, mCommentRef;
    private FirebaseUser mCurrUser;
    private Travel mTravel;

    public CommentListAdapter(Context mContext, Travel travel) {
        this.mContext = mContext;
        this.mCommentList = new ArrayList<>();
        this.mTravel = travel;

        mDatabase = FirebaseDatabase.getInstance();
        mCurrUser = FirebaseAuth.getInstance().getCurrentUser();

        mUserRef = mDatabase.getReference(FirebaseContract.USERS);
        mCommentRef = mDatabase.getReference(FirebaseContract.COMMENT_LIST);
    }

    public void clear() {
        mCommentList.clear();
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext).inflate(R.layout.item_comment, parent, false);
        return new CommentHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentHolder holder, final int position) {
        final Comment item = mCommentList.get(position);

        mUserRef.child(item.getWriterUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                holder.nameTextView.setText(user.getName());
                Glide.with(mContext).load(user.getProfileUrl()).thumbnail(0.2f).into(holder.profileImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        // 나머지 정보 설정
        holder.dateTextView.setText(item.getCommentDateString());
        holder.contentTextView.setText(item.getCommentContent());

        // 내가 쓴 글이면

        if(mCurrUser.getUid().equals(item.getWriterUid())) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                            .setTitle("댓글 삭제하기")
                            .setMessage("댓글을 삭제하시겠습니까 ?")
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mCommentRef.child(item.getContentId()).child(item.getCommentId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                mCommentList.remove(item);
                                                notifyItemChanged(position);
                                                Snackbar.make(((DetailTravelInfoActivity)mContext).getWindow().getDecorView().getRootView(), "삭제 성공", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                Snackbar.make(((DetailTravelInfoActivity)mContext).getWindow().getDecorView().getRootView(), "삭제 실패", Snackbar.LENGTH_SHORT).show();
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
                    return false;
                }
            });
        }
    }

    public void updateItem(Comment item) {
        int pos = 0;
        for(Comment cur : mCommentList) {
            if(cur.getContentId().equals(item.getContentId())) {
                mCommentList.set(pos, item);
                notifyItemChanged(pos);
                break;
            }
            pos++;
        }
    }

    public int size() {
        return mCommentList.size();
    }

    public void addItem(Comment comment) {
        mCommentList.add(comment);
        notifyItemChanged(mCommentList.size() - 1);
    }

    public void addItems(List<Comment> comments) {
        mCommentList.addAll(comments);
    }

    public void removeImte(Comment c) {
        int pos = 0;
        Iterator<Comment> it = mCommentList.iterator();
        while(it.hasNext()) {
            if(it.next().getCommentId().equals(c.getCommentId())) {
                it.remove();
                notifyItemChanged(pos);
                return;
            }
            pos++;
        }
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView, dateTextView, contentTextView;
        public CircleImageView profileImageView;

        public CommentHolder(View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.image_view_item_comment_profile);
            profileImageView.setBackground(new ShapeDrawable(new OvalShape()));

            nameTextView = itemView.findViewById(R.id.text_view_item_comment_name);
            dateTextView = itemView.findViewById(R.id.text_view_item_comment_time);
            contentTextView = itemView.findViewById(R.id.text_view_item_comment_content);
        }
    }
}
