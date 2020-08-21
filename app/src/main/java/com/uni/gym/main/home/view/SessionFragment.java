package com.uni.gym.main.home.view;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentSessionBinding;
import com.uni.gym.main.home.model.Session;

public class SessionFragment extends Fragment {

    private FragmentSessionBinding binding;
    private String docId,courseId;
    private DocumentReference documentReference;
    private FirebaseFirestore db;

    public static SessionFragment newInstance() {
        return new SessionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSessionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments().getString("docId") != null && getArguments().getString("course_id") != null) {
            docId= getArguments().getString("docId");
            courseId = getArguments().getString("course_id");
            getSession();
        }
    }

    private void getSession() {
        documentReference = db.collection("Courses").document(courseId).collection("Sessions").document(docId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult() != null) {
                    Session session = task.getResult().toObject(Session.class);
                    updateSession(session);
                }
            }
        });
    }

    private void updateSession(Session session) {
        binding.videoView.setVideoURI(Uri.parse(session.getVid()));
        MediaController mediaController=new MediaController(getContext());
        binding.videoView.setMediaController(mediaController);
        mediaController.setAnchorView(binding.videoView);
        binding.videoView.start();

        binding.name.setText(session.getName());
        binding.name.setTextColor(getResources().getColor(R.color.black));
        binding.name.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tvSteps.setText(session.getSteps());
        binding.tvSteps.setTextColor(getResources().getColor(R.color.dark_gray));
        binding.tvSteps.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        binding.tvBreath.setText(session.getBreath());
        binding.tvBreath.setTextColor(getResources().getColor(R.color.dark_gray));
        binding.tvBreath.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

}