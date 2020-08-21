package com.uni.gym.main.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentProfileBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.blog.adapter.PostAdapter;
import com.uni.gym.main.blog.model.Post;
import com.uni.gym.main.profile.model.GymUser;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DocumentReference documentReference;
    public static final String TAG = ProfileFragment.class.getSimpleName();

    private OnFragmentInteractionListener mListener;
    private CollectionReference postsRef;
    private PostAdapter adapter;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fillData();

        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        postsRef = FirebaseFirestore.getInstance().collection("Posts");
        postsRef.whereEqualTo("userId", SharedPrefHelper.getUserId(getContext())).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty())
                binding.tvNoPostsAdded.setVisibility(View.VISIBLE);
            else
                binding.tvNoPostsAdded.setVisibility(View.INVISIBLE);
        });
        Query query = postsRef.whereEqualTo("userId", SharedPrefHelper.getUserId(getContext())).orderBy("time", Query.Direction.DESCENDING).limit(20);
        FirestoreRecyclerOptions<Post> options =
                new FirestoreRecyclerOptions.Builder<Post>().setLifecycleOwner(this).setQuery(query, Post.class).build();

        adapter = new PostAdapter(options);
        binding.profRecycler.setHasFixedSize(true);
        binding.profRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.profRecycler.setAdapter(adapter);

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
                    mListener.onFragmentInteraction(2, bundle1);
                    break;
            }
        });

    }

    private void fillData() {

        if (SharedPrefHelper.getUserPic(getContext()) != null)
            Glide.with(binding.userImg).load(SharedPrefHelper.getUserPic(getContext())).into(binding.userImg);
        if (SharedPrefHelper.getUserName(getContext()) != null) {
            binding.userName.setText(SharedPrefHelper.getUserName(getContext()));
            binding.userName.setTextColor(getResources().getColor(R.color.black));
            binding.userName.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        documentReference =
                FirebaseFirestore.getInstance().collection("GymUsers").document(SharedPrefHelper.getUserId(getContext()));

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            GymUser model = documentSnapshot.toObject(GymUser.class);

            if (model.getBio() != null) {
                binding.userBio.setText(model.getBio());
                binding.userBio.setTextColor(getResources().getColor(R.color.card_view_courses_txt2));
                binding.userBio.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            } else {
                binding.userBio.setText("");
                binding.userBio.setTextColor(getResources().getColor(R.color.card_view_courses_txt2));
                binding.userBio.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
            if (model.getPhoto() != null) {
                Glide.with(binding.userImg).load(model.getPhoto()).into(binding.userImg);
            }
            if (model.getName() != null) {
                binding.userName.setText(model.getName());
                binding.userName.setTextColor(getResources().getColor(R.color.black));
                binding.userName.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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
    public void onResume() {
        super.onResume();
        fillData();
    }

}