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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.FragmentHomeBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.home.adapter.CourseAdapter;
import com.uni.gym.main.home.model.Course;
import com.uni.gym.main.home.adapter.SlidePagerAdapter;
import com.uni.gym.main.home.model.Slide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static com.google.firebase.firestore.FieldPath.documentId;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<Slide> listSlides;
    private OnFragmentInteractionListener mListener;
    private CollectionReference CoursesRef;
    private CourseAdapter adapter;
    private SlidePagerAdapter pagerAdapter;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (SharedPrefHelper.getUserRole(getContext()).equals(getResources().getString(R.string.member)))
            binding.fabAddCourses.setVisibility(View.INVISIBLE);

        binding.seeAll.setOnClickListener(view -> {
            mListener.onFragmentInteraction(3);
        });

        CoursesRef = FirebaseFirestore.getInstance().collection("Courses");

        initViewPager();
        initRecyclerView();

        binding.fabAddCourses.setOnClickListener(view -> {
            mListener.onFragmentInteraction(2);
        });
    }

    private void initViewPager() {

        listSlides = new ArrayList<>();
        pagerAdapter = new SlidePagerAdapter(listSlides);

        CoursesRef.orderBy("rate", Query.Direction.DESCENDING).addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                    if (listSlides.size() < 3) {
                        Course course = snapshot.toObject(Course.class);
                        listSlides.add(new Slide(course.getPicture(), course.getCourseName(), snapshot.getId()));
                    }
                }
                pagerAdapter.notifyDataSetChanged();
            }
        });

        binding.sliderPager.setAdapter(pagerAdapter);
        pagerAdapter.setOnItemClickListener((id) -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString("course_id", id);
            mListener.onFragmentInteraction(3, bundle1);
        });
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SliderTimer(), 4000, 3000);
        binding.indicator.setupWithViewPager(binding.sliderPager, true);

    }

    class SliderTimer extends TimerTask {

        @Override
        public void run() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding.sliderPager.getCurrentItem() < listSlides.size() - 1) {
                        binding.sliderPager.setCurrentItem(binding.sliderPager.getCurrentItem() + 1);
                    } else {
                        binding.sliderPager.setCurrentItem(0);
                    }
                });
            }
        }
    }

    private void initRecyclerView() {
        Query query = CoursesRef.orderBy("time", Query.Direction.DESCENDING).limit(6);

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