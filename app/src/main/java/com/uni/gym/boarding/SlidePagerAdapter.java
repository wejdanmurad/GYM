package com.uni.gym.boarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.uni.gym.R;

import java.util.List;

public class SlidePagerAdapter extends PagerAdapter {

    private List<Slide> mList;

    public SlidePagerAdapter(List<Slide> mList){
        this.mList=mList;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater =(LayoutInflater)container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View slideLayout=inflater.inflate(R.layout.item_slide_boarding,null);

        ImageView slideImg=slideLayout.findViewById(R.id.Splash_img);
        TextView slideTitle=slideLayout.findViewById(R.id.VCode);
        TextView slideText=slideLayout.findViewById(R.id.forgot_txt);

        slideImg.setImageResource(mList.get(position).getImage());
        slideTitle.setText(mList.get(position).getTitle());
        slideText.setText(mList.get(position).getTxt());

        container.addView(slideLayout);
        return slideLayout;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object ;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
