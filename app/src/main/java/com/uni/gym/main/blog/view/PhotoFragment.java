package com.uni.gym.main.blog.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.uni.gym.databinding.FragmentPhotoBinding;

public class PhotoFragment extends Fragment {

    private FragmentPhotoBinding binding;
    private String postId;
    private DocumentReference documentReference;

    public static PhotoFragment newInstance() {
        return new PhotoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments().getString("post_id") != null) {
            postId = getArguments().getString("post_id");
//            documentReference = FirebaseFirestore.getInstance().collection("Posts").document(postId);
//            documentReference.get().addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    if (task.getResult() != null) {
//                        Post post = task.getResult().toObject(Post.class);
                        Glide.with(binding.myPhoto).load(getArguments().getString("post_id")).into(binding.myPhoto);
//                    }
//                }
//            });
        }
    }

}