package com.uni.gym.main.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.uni.gym.R;
import com.uni.gym.main.blog.adapter.PostAdapter;
import com.uni.gym.main.home.model.Course;

import java.util.List;

public class CourseAdapter extends FirestoreRecyclerAdapter<Course, CourseAdapter.ViewHolder> {

    private OnItemClickListener listener;

    public CourseAdapter(@NonNull FirestoreRecyclerOptions<Course> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_course, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Course model) {
        holder.bind(model);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_course_name;
        private TextView tv_price;
        private ImageView img_course;
        private TextView tv_no_sessions;
        private RatingBar rate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_course_name = itemView.findViewById(R.id.tv_name_of_course);
            tv_price = itemView.findViewById(R.id.tv_price);
            img_course = itemView.findViewById(R.id.img_course);
            tv_no_sessions = itemView.findViewById(R.id.tv_no_sessions);
            rate = itemView.findViewById(R.id.ratingBar);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position).getId());
                }
            });
        }

        public void bind(Course course) {
            tv_course_name.setText(course.getCourseName());
            String price = course.getPrice() + " US$";
            tv_price.setText(price);
            Glide.with(img_course).load(course.getPicture()).into(img_course);

            getSnapshots().getSnapshot(getAdapterPosition()).getReference().collection("Sessions").addSnapshotListener((queryDocumentSnapshots, e) -> {
                String sessions;
                if (queryDocumentSnapshots.isEmpty()) {
                    sessions = "no sessions";
                } else {
                    sessions = queryDocumentSnapshots.size() + " sessions";
                }
                tv_no_sessions.setText(sessions);
            });

            rate.setRating(course.getRate());
            getSnapshots().getSnapshot(getAdapterPosition()).getReference().addSnapshotListener((documentSnapshot, e) -> {
                if (documentSnapshot.exists()) {
                    Course myCourse = documentSnapshot.toObject(Course.class);
                    rate.setRating(myCourse.getRate());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String docId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
