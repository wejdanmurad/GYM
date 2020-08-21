package com.uni.gym.main.courses;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentCoursesBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.home.adapter.CourseAdapter;
import com.uni.gym.main.home.model.Course;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoursesFragment extends Fragment {

    private FragmentCoursesBinding binding;
    private OnFragmentInteractionListener mListener;
    private CollectionReference CoursesRef;
    private CourseAdapter adapter;
    private List<String> ids;

    public static CoursesFragment newInstance() {
        return new CoursesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCoursesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member))) {
            ids = new ArrayList<>();
            FirebaseFirestore.getInstance().collection("GymUsers").document(SharedPrefHelper.getUserId(getContext())).collection("Courses")
                    .addSnapshotListener((queryDocumentSnapshots, e) -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                ids.add(doc.getId());
                                System.out.println(doc.getId());
                            }
                            initRecyclerView();
                        } else {
                            binding.tvNoCoursesAdded.setVisibility(View.VISIBLE);
                        }
                    });
        }else{
            initRV();
        }
    }

    private void initRecyclerView() {
        CoursesRef = FirebaseFirestore.getInstance().collection("Courses");
        CoursesRef.whereIn("courseId", ids).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty())
                binding.tvNoCoursesAdded.setVisibility(View.VISIBLE);
            else
                binding.tvNoCoursesAdded.setVisibility(View.INVISIBLE);
        });
        Query query = CoursesRef.whereIn("courseId", ids);
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

    private void initRV() {
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