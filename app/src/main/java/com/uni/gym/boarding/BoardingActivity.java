package com.uni.gym.boarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.viewpager.widget.ViewPager;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.uni.gym.auth.AuthActivity;
import com.uni.gym.R;
import com.uni.gym.auth.WelcomeActivity;
import com.uni.gym.databinding.ActivityBoardingBinding;
import com.uni.gym.main.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoardingActivity extends AppCompatActivity {

    private ActivityBoardingBinding binding;
    private ViewPager sliderPager;
    private TabLayout indicator;
    private SlidePagerAdapter adapter;
    private List<Slide> list;
    private Group group;
    private Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sliderPager = binding.slidPager;
        indicator = binding.indicator;
        group = binding.group;
        btn = binding.btn;

        list = new ArrayList<>(3);
        list.add(new Slide(R.drawable.ic_boarding_pic_1, getResources().getString(R.string.HealthyHabits), getResources().getString(R.string.DummyText)));
        list.add(new Slide(R.drawable.ic_boarding_pic_2, getResources().getString(R.string.ActivityTracker), getResources().getString(R.string.DummyText)));
        list.add(new Slide(R.drawable.ic_boarding_pic_3, getResources().getString(R.string.ActivityTracker), getResources().getString(R.string.DummyText)));

        adapter = new SlidePagerAdapter(list);
        sliderPager.setAdapter(adapter);
        indicator.setupWithViewPager(sliderPager, true);

        group.setVisibility(View.VISIBLE);
        btn.setVisibility(View.GONE);

        sliderPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (list.size() == 1) {
                    group.setVisibility(View.GONE);
                    btn.setVisibility(View.VISIBLE);
                } else {
                    if (position == 2) {
                        group.setVisibility(View.GONE);
                        btn.setVisibility(View.VISIBLE);
                        list.clear();
                        adapter.notifyDataSetChanged();
                    } else {
                        group.setVisibility(View.VISIBLE);
                        btn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void Next(View view) {
        if (sliderPager.getCurrentItem() < list.size() - 1) {
            sliderPager.setCurrentItem(sliderPager.getCurrentItem() + 1);
        }
    }


    public void getStarted(View view) {
        startActivity(new Intent(BoardingActivity.this, WelcomeActivity.class));
        finish();
    }

    public void Skip(View view) {
        startActivity(new Intent(BoardingActivity.this, WelcomeActivity.class));
        finish();
    }

}