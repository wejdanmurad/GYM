package com.uni.gym.main.blog.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.databinding.FragmentBlogBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.blog.adapter.PostAdapter;
import com.uni.gym.main.blog.model.Post;

public class BlogFragment extends Fragment {

    private FragmentBlogBinding binding;
    private OnFragmentInteractionListener mListener;
    private CollectionReference postsRef;
    private PostAdapter adapter;

    public static BlogFragment newInstance() {
        return new BlogFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBlogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.fabAddPosts.setOnClickListener(view -> {
            mListener.onFragmentInteraction(1);
        });

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        postsRef = FirebaseFirestore.getInstance().collection("Posts");
        Query query = postsRef.orderBy("time", Query.Direction.DESCENDING).limit(20);
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post.class).build();

        adapter = new PostAdapter(options);
        binding.rvPosts.setHasFixedSize(true);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvPosts.setAdapter(adapter);

        adapter.setOnItemClickListener((operationId, position, id) -> {
            switch (operationId) {
                case 1:
                    adapter.likeItem(position);
                    break;
                case 2:
                    Bundle bundle = new Bundle();
                    bundle.putString("post_id", id);
                    mListener.onFragmentInteraction(1, bundle);
                    break;
                case 3:
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("post_id", id);
                    mListener.onFragmentInteraction(2,bundle1);
                    break;
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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