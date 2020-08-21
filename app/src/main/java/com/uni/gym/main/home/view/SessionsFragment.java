package com.uni.gym.main.home.view;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentSessionsBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.blog.adapter.CommentAdapter;
import com.uni.gym.main.blog.model.Comment;
import com.uni.gym.main.home.adapter.SessionAdapter;
import com.uni.gym.main.home.model.Session;

public class SessionsFragment extends Fragment {

    private FragmentSessionsBinding binding;
    private String courseId;
    private DocumentReference documentReference;
    private FirebaseFirestore db;
    private OnFragmentInteractionListener mListener;
    private SessionAdapter adapter;

    public static SessionsFragment newInstance() {
        return new SessionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSessionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member)))
            binding.fabAddSessions.setVisibility(View.INVISIBLE);
        db = FirebaseFirestore.getInstance();
        if (getArguments().getString("course_id") != null) {
            courseId = getArguments().getString("course_id");
            documentReference = db.collection("Courses").document(courseId);
            binding.fabAddSessions.setOnClickListener(view -> {
                Bundle bundle1 = new Bundle();
                bundle1.putString("course_id", courseId);
                mListener.onFragmentInteraction(5, bundle1);
            });
            setUpRecyclerView();
        }

    }

    private void setUpRecyclerView() {
        Query query = documentReference.collection("Sessions").orderBy("time", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<Session> options =
                new FirestoreRecyclerOptions.Builder<Session>().setLifecycleOwner(this).setQuery(query, Session.class).build();

        adapter = new SessionAdapter(options);
        binding.rvSessions.setHasFixedSize(true);
        binding.rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSessions.setAdapter(adapter);

        documentReference.collection("Sessions").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty()) {
                binding.noSessions.setVisibility(View.VISIBLE);
            } else {
                binding.noSessions.setVisibility(View.INVISIBLE);
            }
        });

        adapter.setOnItemClickListener(docId -> {
            Bundle bundle = new Bundle();
            bundle.putString("docId", docId);
            bundle.putString("course_id", courseId);
            mListener.onFragmentInteraction(6, bundle);
        });
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
}