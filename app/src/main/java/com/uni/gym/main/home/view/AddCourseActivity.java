package com.uni.gym.main.home.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.uni.gym.R;
import com.uni.gym.databinding.ActivityAddCourseBinding;
import com.uni.gym.main.home.model.Course;

public class AddCourseActivity extends AppCompatActivity {

    private ActivityAddCourseBinding binding;
    private Toolbar toolbar;
    private Course course;
    private Uri imageUri;
    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddCourseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        course = new Course();

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        binding.arrowBack.setOnClickListener(view -> {
            this.finishAndRemoveTask();
        });

        binding.toolbarPublish.setOnClickListener(view -> {
            if (binding.etNameCourse.getText().toString().isEmpty() ||
                    binding.etPriceCourse.getText().toString().isEmpty() ||
                    binding.etOverviewCourse.getText().toString().isEmpty() ||
                    binding.courseImg.getDrawable() == null)
                Toast.makeText(this, "please fill the required fields", Toast.LENGTH_SHORT).show();
            else
                publish();
        });

        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.addImg.setOnClickListener(view -> {
            getPhoto();
        });
    }

    private void getPhoto() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(16, 12)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                if (imageUri != null) {
                    binding.courseImg.setImageURI(imageUri);
                }
            }
            binding.toolbarPublish.setEnabled(true);
            binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void publish() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));

        course.setCourseName(binding.etNameCourse.getText().toString());
        course.setPrice(binding.etPriceCourse.getText().toString());
        course.setOverview(binding.etOverviewCourse.getText().toString());
        course.setSessionsCount(String.valueOf(0));
        course.setRate(0);
        course.setRateCount(0);
        course.setTime(System.currentTimeMillis());
        storeImg();
    }

    private void storeImg() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Courses").child(System.currentTimeMillis() + ".jpg");
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                course.setPicture(uri.toString());
                storeCourse();
            });
        });
    }

    private void storeCourse() {
        collectionReference = FirebaseFirestore.getInstance().collection("Courses");
        String id = collectionReference.document().getId();
        course.setCourseId(id);
        collectionReference.document(id).set(course).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println(" doc id " + id);
                this.finishAndRemoveTask();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.toolbarPublish.setEnabled(true);
            binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
        });
    }
}