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
import com.uni.gym.main.home.model.Course;
import com.uni.gym.main.home.model.Session;

public class SessionAdapter extends FirestoreRecyclerAdapter<Session, SessionAdapter.ViewHolder> {

    private SessionAdapter.OnItemClickListener listener;

    public SessionAdapter(@NonNull FirestoreRecyclerOptions<Session> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Session model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SessionAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_session, parent, false));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private TextView tv_session;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.name);
            tv_session = itemView.findViewById(R.id.session_no);
            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position).getId());
                }
            });
        }

        public void bind(Session session) {
            tv_name.setText(session.getName());
            tv_session.setText("Session " + (getAdapterPosition() + 1));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String docId);
    }

    public void setOnItemClickListener(SessionAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
