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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentAllCoursesBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.home.adapter.CourseAdapter;
import com.uni.gym.main.home.model.Course;

import java.util.ArrayList;
import java.util.List;

public class AllCoursesFragment extends Fragment {

    private FragmentAllCoursesBinding binding;
    private OnFragmentInteractionListener mListener;
    private CollectionReference CoursesRef;
    private CourseAdapter adapter;

    public static AllCoursesFragment newInstance() {
        return new AllCoursesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAllCoursesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member)))
            binding.fabAddCourses.setVisibility(View.INVISIBLE);

        initRecyclerView();

        binding.fabAddCourses.setOnClickListener(view -> {
            mListener.onFragmentInteraction(2);
        });
    }


    private void initRecyclerView() {
        CoursesRef = FirebaseFirestore.getInstance().collection("Courses");
        CoursesRef.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty())
                binding.tvNoCoursesAdded.setVisibility(View.VISIBLE);
            else
                binding.tvNoCoursesAdded.setVisibility(View.INVISIBLE);
        });
        Query query = CoursesRef.orderBy("time", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Course> options =
                new FirestoreRecyclerOptions.Builder<Course>().setLifecycleOwner(this).setQuery(query, Course.class).build();

        adapter = new CourseAdapter(options);
        binding.rvCourses.setHasFixedSize(true);
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourses.setAdapter(adapter);

        adapter.setOnItemClickListener((id) -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString("course_id", id);
            mListener.onFragmentInteraction(3, bundle1);
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

}