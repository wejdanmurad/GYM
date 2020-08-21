package com.uni.gym.main.blog.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentCommentBinding;
import com.uni.gym.main.blog.adapter.CommentAdapter;
import com.uni.gym.main.blog.model.Comment;
import com.uni.gym.main.blog.model.Post;
import com.uni.gym.main.profile.model.GymUser;

import java.util.HashMap;
import java.util.Locale;

public class CommentFragment extends Fragment {

    private FragmentCommentBinding binding;
    private String postId;
    private DocumentReference documentReference;
    private CommentAdapter adapter;
    private Comment comment;

    public static CommentFragment newInstance() {
        return new CommentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCommentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        comment = new Comment();

        binding.noComments.setVisibility(View.INVISIBLE);

        if (SharedPrefHelper.getUserName(getContext()) != null) {

            comment.setUsername(SharedPrefHelper.getUserName(getContext()));
        }
        if (SharedPrefHelper.getUserPic(getContext()) != null) {
            Glide.with(binding.commentUserImg).load(SharedPrefHelper.getUserPic(getContext())).into(binding.commentUserImg);
            comment.setUserPic(SharedPrefHelper.getUserPic(getContext()));
        }

        binding.postImg.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.sendComment.setVisibility(View.GONE);

        if (getArguments().getString("post_id") != null) {
            postId = getArguments().getString("post_id");
            getPost();
            setUpRecyclerView();
            binding.sendComment.setOnClickListener(view -> {
                publish();
            });
            binding.userComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().isEmpty()) {
                        binding.sendComment.setVisibility(View.VISIBLE);
                    } else {
                        binding.sendComment.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }

    private void publish() {
        binding.sendComment.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.userComment.getWindowToken(), 0);

        comment.setTime(System.currentTimeMillis());
        comment.setComment(binding.userComment.getText().toString());
        binding.userComment.setText("");

        documentReference.collection("Comments").document().set(comment).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.INVISIBLE);
        });

    }

    private void getPost() {
        documentReference = FirebaseFirestore.getInstance().collection("Posts").document(postId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    Post post = task.getResult().toObject(Post.class);
                    updatePost(post);
                }
            }
        });
    }

    private void updatePost(Post post) {
        if (post.getUserId() != null) {
            FirebaseFirestore.getInstance().collection("GymUsers").document(post.getUserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    GymUser model = task.getResult().toObject(GymUser.class);
                    if (model.getPhoto() != null)
                        Glide.with(binding.userImg).load(model.getPhoto()).into(binding.userImg);
                    if (model.getName() != null) {
                        binding.userName.setText(model.getName());
                        binding.userName.setTextColor(Color.BLACK);
                        binding.userName.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            });
        }

        binding.postContent.setText(post.getContent());
        binding.postContent.setTextColor(getResources().getColor(R.color.dark_gray));
        binding.postContent.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        setTime(post.getTime());
        setPostImg(post.getPostPic());
        setLike();
        setLikes();

        binding.like.setOnClickListener(view -> {
            HashMap like = new HashMap<String, String>();
            like.put("like", SharedPrefHelper.getUserId(getContext()));
            documentReference.collection("Likes")
                    .document(SharedPrefHelper.getUserId(getContext())).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        documentReference.collection("Likes")
                                .document(SharedPrefHelper.getUserId(getContext())).delete();
                    } else {
                        documentReference.collection("Likes")
                                .document(SharedPrefHelper.getUserId(getContext())).set(like);
                    }
                }
            });
        });

        documentReference.collection("Comments").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty()) {
                binding.noComments.setVisibility(View.VISIBLE);
            } else {
                binding.noComments.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setTime(Long time) {
        Locale languageTag = Locale.forLanguageTag("en");
        TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(languageTag).build();
        binding.postTimeStamp.setText(TimeAgo.using(time, messages));
        binding.postTimeStamp.setTextColor(getResources().getColor(R.color.card_view_courses_txt2));
        binding.postTimeStamp.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void setPostImg(String postPic) {
        if (postPic != null) {
            Glide.with(binding.postImg).load(postPic).into(binding.postImg);
            binding.postImg.setVisibility(View.VISIBLE);
        } else {
            binding.postImg.setVisibility(View.GONE);
        }
    }

    private void setLike() {
        documentReference.collection("Likes").document(SharedPrefHelper.getUserId(getContext()))
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (documentSnapshot.exists())
                        binding.like.setSelected(true);
                    else
                        binding.like.setSelected(false);
                });
    }

    private void setLikes() {
        documentReference.collection("Likes").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty())
                binding.tvLikes.setText("0 likes");
            else
                binding.tvLikes.setText(queryDocumentSnapshots.size() + " likes");
        });
    }

    private void setUpRecyclerView() {
        Query query = documentReference.collection("Comments").orderBy("time", Query.Direction.ASCENDING).limit(20);
        FirestoreRecyclerOptions<Comment> options =
                new FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment.class).build();

        adapter = new CommentAdapter(options);
        binding.rvComments.setHasFixedSize(true);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvComments.setAdapter(adapter);

        adapter.setOnItemClickListener((position) -> {
            adapter.likeItem(position);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}