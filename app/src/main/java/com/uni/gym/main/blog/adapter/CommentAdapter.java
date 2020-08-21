package com.uni.gym.main.blog.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.main.blog.model.Comment;
import com.uni.gym.main.profile.model.GymUser;

import java.util.HashMap;
import java.util.Locale;

public class CommentAdapter extends FirestoreRecyclerAdapter<Comment, CommentAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public CommentAdapter(@NonNull FirestoreRecyclerOptions<Comment> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_comment, parent, false);
        context = parent.getContext();
        return new CommentAdapter.ViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Comment model) {
        holder.bind(model);
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImg;
        private TextView userName;
        private TextView timestamp;
        private TextView commentContent;
        private ImageView like;
        private TextView tvLikes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.userImg);
            userName = itemView.findViewById(R.id.userName);
            timestamp = itemView.findViewById(R.id.commentTimeStamp);
            commentContent = itemView.findViewById(R.id.commentContent);
            like = itemView.findViewById(R.id.like);
            tvLikes = itemView.findViewById(R.id.tvLikes);

            like.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(position);
                }
            });
        }

        public void bind(Comment comment) {
            if (comment.getUserId() != null) {
                FirebaseFirestore.getInstance().collection("GymUsers").document(comment.getUserId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        GymUser model = task.getResult().toObject(GymUser.class);
                        if (model.getPhoto() != null)
                            Glide.with(userImg).load(model.getPhoto()).into(userImg);
                        if (model.getName() != null) {
                            userName.setText(model.getName());
                            userName.setTextColor(Color.BLACK);
                            userName.setBackgroundColor(Color.TRANSPARENT);
                        }
                        commentContent.setText(comment.getComment());
                        setTime(comment.getTime());
                        setLike();
                        setLikes();
                    }
                });
            }
        }

        private void setTime(Long time) {
            Locale languageTag = Locale.forLanguageTag("en");
            TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(languageTag).build();
            timestamp.setText(TimeAgo.using(time, messages));
        }

        private void setLike() {
            getSnapshots().getSnapshot(getAdapterPosition()).getReference().collection("Likes")
                    .document(SharedPrefHelper.getUserId(context)).addSnapshotListener((documentSnapshot, e) -> {
                if (documentSnapshot.exists())
                    like.setSelected(true);
                else
                    like.setSelected(false);
            });
        }

        private void setLikes() {
            getSnapshots().getSnapshot(getAdapterPosition()).getReference()
                    .collection("Likes").addSnapshotListener((queryDocumentSnapshots, e) -> {
                if (queryDocumentSnapshots.isEmpty())
                    tvLikes.setText("0 likes");
                else
                    tvLikes.setText(queryDocumentSnapshots.size() + " likes");
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void likeItem(int position) {
        HashMap like = new HashMap<String, String>();
        like.put("like", SharedPrefHelper.getUserId(context));
        getSnapshots().getSnapshot(position).getReference().collection("Likes")
                .document(SharedPrefHelper.getUserId(context)).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    getSnapshots().getSnapshot(position).getReference().collection("Likes")
                            .document(SharedPrefHelper.getUserId(context)).delete();
                } else {
                    getSnapshots().getSnapshot(position).getReference().collection("Likes")
                            .document(SharedPrefHelper.getUserId(context)).set(like);
                }
            }
        });
    }
}
