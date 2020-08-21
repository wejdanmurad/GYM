package com.uni.gym.main.search;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.databinding.FragmentSearchBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.home.adapter.CourseAdapter;
import com.uni.gym.main.home.model.Course;


public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private CollectionReference CoursesRef;
    private CourseAdapter adapter;
    private OnFragmentInteractionListener mListener;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchText) {
                if (!searchText.isEmpty())
                    searchLetters(searchText);
                else
                    searchLetters("-1");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if (!searchText.isEmpty())
                    searchLetters(searchText);
                else
                    searchLetters("-1");
                return false;
            }
        });
    }

    private void searchLetters(String searchText) {
        CoursesRef = FirebaseFirestore.getInstance().collection("Courses");
        Query query = CoursesRef.orderBy("courseName").startAt(searchText).endAt(searchText + "\uf8ff");

        FirestoreRecyclerOptions<Course> options =
                new FirestoreRecyclerOptions
                        .Builder<Course>()
                        .setQuery(query, Course.class)
                        .setLifecycleOwner(this)
                        .build();

        adapter = new CourseAdapter(options);
        binding.rvCourses.setHasFixedSize(true);
        binding.rvCourses.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCourses.setAdapter(adapter);

        adapter.setOnItemClickListener((id) -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString("course_id", id);
            mListener.onFragmentInteraction(3, bundle1);
        });

        CoursesRef.orderBy("courseName").startAt(searchText).endAt(searchText + "\uf8ff").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots.isEmpty())
                binding.tvSearchForCourses.setVisibility(View.VISIBLE);
            else
                binding.tvSearchForCourses.setVisibility(View.INVISIBLE);
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