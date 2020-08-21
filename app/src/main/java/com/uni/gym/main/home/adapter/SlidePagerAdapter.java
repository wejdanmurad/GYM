package com.uni.gym.main.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.uni.gym.R;
import com.uni.gym.main.home.model.Slide;

import java.util.List;

public class SlidePagerAdapter extends PagerAdapter {

    private List<Slide> mList;
    private SlidePagerAdapter.OnItemClickListener listener;

    public SlidePagerAdapter(List<Slide> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View slideLayout = inflater.inflate(R.layout.item_slide_home_vp, null);

        ImageView slideImg = slideLayout.findViewById(R.id.slide_img);
        TextView slideText = slideLayout.findViewById(R.id.slide_title);

        Glide.with(slideImg).load(mList.get(position).getImage()).into(slideImg);
        slideText.setText(mList.get(position).getTitle());

        slideLayout.findViewById(R.id.btn_get_started).setOnClickListener(view -> {
            listener.onItemClick(mList.get(position).getDocId());
        });

        container.addView(slideLayout);
        return slideLayout;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public interface OnItemClickListener {
        void onItemClick(String docId);
    }

    public void setOnItemClickListener(SlidePagerAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

}
