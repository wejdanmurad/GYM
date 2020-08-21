package com.uni.gym.main.blog.view;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.uni.gym.R;
import com.uni.gym.common.SharedPrefHelper;
import com.uni.gym.databinding.ActivityAddPostBinding;
import com.uni.gym.main.blog.model.Post;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private Toolbar toolbar;
    private Uri imageUri;
    private DocumentReference documentReference;
    private Post post;
    private Boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        post = new Post();

        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        binding.arrowBack.setOnClickListener(view -> {
            this.finishAndRemoveTask();
        });

        binding.toolbarPublish.setOnClickListener(view -> {
            Toast.makeText(this, "publish", Toast.LENGTH_SHORT).show();
            publish();
        });

        if (SharedPrefHelper.getUserId(getApplication()) != null) {
            post.setUserId(SharedPrefHelper.getUserId(getApplication()));
        }
        if (SharedPrefHelper.getUserName(getApplication()) != null) {
            binding.userName.setText(SharedPrefHelper.getUserName(getApplication()));
            post.setUsername(SharedPrefHelper.getUserName(getApplication()));
        }
        if (SharedPrefHelper.getUserPic(getApplication()) != null) {
            Glide.with(binding.userImg).load(SharedPrefHelper.getUserPic(getApplication())).into(binding.userImg);
            post.setUserPic(SharedPrefHelper.getUserPic(getApplication()));
        }

        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.addImg.setOnClickListener(view -> {
            getPhoto();
        });

        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));
        binding.postContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty() && !isLoading) {
                    binding.toolbarPublish.setEnabled(true);
                    binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
                } else {
                    binding.toolbarPublish.setEnabled(false);
                    binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void publish() {
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));
        binding.progressBar.setVisibility(View.VISIBLE);

        post.setTime(System.currentTimeMillis());
        post.setContent(binding.postContent.getText().toString());

        if (binding.postImg.getDrawable() != null)
            storeImg();
        else
            storePost();

    }

    private void storePost()
    {
        documentReference = FirebaseFirestore.getInstance().collection("Posts").document();
        documentReference.set(post).addOnCompleteListener(task -> {
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

    public void getPhoto() {
        isLoading = true;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.toolbarPublish.setEnabled(false);
        binding.toolbarPublish.setTextColor(getResources().getColor(R.color.gray));
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
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
                    binding.postImg.setImageURI(imageUri);
                }
            }
            isLoading = false;
            binding.toolbarPublish.setEnabled(true);
            binding.toolbarPublish.setTextColor(getResources().getColor(R.color.colorAccent));
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void storeImg() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(System.currentTimeMillis() + ".jpg");
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                post.setPostPic(uri.toString());
                storePost();
            });
        });
    }
}