package com.uni.gym.main.home.view;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.uni.gym.R;
import com.uni.gym.databinding.ActivityAddSessionBinding;
import com.uni.gym.main.home.model.Session;

public class AddSessionActivity extends AppCompatActivity {

    private ActivityAddSessionBinding binding;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private Uri VidUri;
    private Session session;
    private Toolbar toolbar;
    private DocumentReference documentReference;
    private String courseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new Session();

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        binding.arrowBack.setOnClickListener(view -> {
            this.finishAndRemoveTask();
        });

        binding.progressBar.setVisibility(View.INVISIBLE);

        if (getIntent().getStringExtra("course_id") != null) {
            courseId = getIntent().getStringExtra("course_id");

            binding.videoView.setOnClickListener(view -> {
                getVid();
            });

            binding.toolbarPublish.setOnClickListener(view -> {
                if (binding.etName.getText().toString().isEmpty() ||
                        binding.etSteps.getText().toString().isEmpty() ||
                        binding.etBreath.getText().toString().isEmpty() ||
                        VidUri == null) {
                    Toast.makeText(this, "please fill the required fields", Toast.LENGTH_SHORT).show();
                } else {
                    publish();
                }
            });

        }
    }

    private void publish() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(Color.GRAY);

        session.setName(binding.etName.getText().toString());
        session.setSteps(binding.etSteps.getText().toString());
        session.setBreath(binding.etBreath.getText().toString());
        session.setTime(System.currentTimeMillis());
        storeVid();
    }


    public void getVid() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            VidUri = intent.getData();
            binding.videoView.setVideoURI(VidUri);
            MediaController mediaController = new MediaController(this);
            binding.videoView.setMediaController(mediaController);
            mediaController.setAnchorView(binding.videoView);
            binding.videoView.start();
            binding.videoView.setEnabled(false);
        }
        binding.toolbarPublish.setEnabled(true);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    private void storeVid() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(System.currentTimeMillis() + "." + getFileExtension(VidUri));
        storageReference.putFile(VidUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Toast.makeText(this, "stored", Toast.LENGTH_SHORT).show();
                session.setVid(uri.toString());
                storeSession();
            });
        });
    }

    private void storeSession() {
        documentReference = FirebaseFirestore.getInstance().collection("Courses").document(courseId).collection("Sessions").document();
        documentReference.set(session).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                this.finishAndRemoveTask();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.toolbarPublish.setEnabled(true);
            binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
        });
    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(imageUri));
    }
}