package com.uni.gym.main.blog.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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
import com.uni.gym.main.blog.model.Post;
import com.uni.gym.main.profile.model.GymUser;

import java.util.HashMap;
import java.util.Locale;

public class PostAdapter extends FirestoreRecyclerAdapter<Post, PostAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public PostAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_post, parent, false);
        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Post model) {
        holder.bind(model);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userImg;
        private TextView userName;
        private TextView timestamp;
        private ImageView postImg;
        private TextView postContent;
        private ImageView like;
        private TextView tvLikes;
        private ImageView comment;
        private TextView seeComment;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.userImg);
            userName = itemView.findViewById(R.id.userName);
            timestamp = itemView.findViewById(R.id.postTimeStamp);
            postImg = itemView.findViewById(R.id.postImg);
            postContent = itemView.findViewById(R.id.postContent);
            like = itemView.findViewById(R.id.like);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            comment = itemView.findViewById(R.id.comment);
            seeComment = itemView.findViewById(R.id.seeComment);

            like.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(1, position, getSnapshots().getSnapshot(position).getId());
                }
            });
            comment.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(2, position, getSnapshots().getSnapshot(position).getId());
                }
            });
            seeComment.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(2, position, getSnapshots().getSnapshot(position).getId());
                }
            });
//            postImg.setOnClickListener(view -> {
//                int position = getAdapterPosition();
//                if (position != RecyclerView.NO_POSITION && listener != null) {
//                    listener.onItemClick(3, position, getSnapshots().getSnapshot(position).getId());
//                }
//            });
        }

        public void bind(Post post) {
            if (post.getUserId() != null) {
                FirebaseFirestore.getInstance().collection("GymUsers").document(post.getUserId()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        GymUser model = task.getResult().toObject(GymUser.class);
                        if (model.getPhoto() != null)
                            Glide.with(userImg).load(model.getPhoto()).into(userImg);
                        if (model.getName() != null) {
                            userName.setText(model.getName());
                            userName.setTextColor(Color.BLACK);
                            userName.setBackgroundColor(Color.TRANSPARENT);
                        }
                    }
                });
            }
            postContent.setText(post.getContent());
            setTime(post.getTime());
            setPostImg(post.getPostPic());
            setLike();
            setLikes();
            postImg.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(3, position, post.getPostPic());
                }
            });
            getSnapshots().getSnapshot(getAdapterPosition()).getReference().collection("Comments").addSnapshotListener((queryDocumentSnapshots, e) -> {
                String commentText;
                if (queryDocumentSnapshots.isEmpty()) {
                    commentText = "no comments";
                } else {
                    commentText = queryDocumentSnapshots.size() + " comments";
                }
                seeComment.setText(commentText);
            });
        }

        private void setTime(Long time) {
            Locale languageTag = Locale.forLanguageTag("en");
            TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(languageTag).build();
            timestamp.setText(TimeAgo.using(time, messages));
        }

        private void setPostImg(String postPic) {
            if (postPic != null) {
                Glide.with(postImg).load(postPic).into(postImg);
                postImg.setVisibility(View.VISIBLE);
            } else {
                postImg.setVisibility(View.GONE);
            }
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
        void onItemClick(int operationId, int position, String docId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getReference().delete();
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
