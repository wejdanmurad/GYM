package com.uni.gym.main.profile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.uni.gym.auth.WelcomeActivity;
import com.uni.gym.databinding.ActivityEditProfileBinding;
import com.uni.gym.main.MainActivity;
import com.uni.gym.main.profile.model.GymUser;

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PICK_PHOTO = 100;
    private Uri imageUri;
    private ActivityEditProfileBinding binding;
    private DocumentReference documentReference;
    public static final String TAG = EditProfileActivity.class.getSimpleName();
    private GymUser gymUser;
    private Boolean isImgSelected;
    private String myUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isImgSelected = false;

        gymUser = new GymUser();

        binding.progressBar.setVisibility(View.INVISIBLE);
        fillData();

        binding.editImg.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.send.setEnabled(false);
            isImgSelected = true;
            getPhoto();
        });

        if (!isImgSelected) {
            binding.send.setEnabled(true);
            binding.send.setOnClickListener(view -> {
                editGymUser();
            });
        }
    }

    private void fillData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        myUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        documentReference =
                FirebaseFirestore.getInstance().collection("GymUsers").document(myUserId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            Log.d(TAG, "createUser: " + documentSnapshot.getId());
            GymUser model = documentSnapshot.toObject(GymUser.class);

            if (model.getPhoto() != null) {
                Glide.with(binding.editImg).load(model.getPhoto()).into(binding.editImg);
                gymUser.setPhoto(model.getPhoto());
            }
            if (model.getName() != null)
                binding.etName.setText(model.getName());
            if (model.getBio() != null)
                binding.tvBio.setText(model.getBio());
            if (model.getPhone() != null)
                binding.tvPhone.setText(model.getPhone());
            if (model.getAddress() != null)
                binding.tvAddress.setText(model.getAddress());
            binding.progressBar.setVisibility(View.INVISIBLE);
        }).addOnFailureListener(e -> {
            Log.d(TAG, "createUser: " + e.getMessage());
            binding.progressBar.setVisibility(View.INVISIBLE);
        });
        Log.d(TAG, "model : data is here");

    }

    public void getPhoto() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this);

//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
            if (imageUri != null) {
                storeImg();
            }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(this, "you failed", Toast.LENGTH_SHORT).show();
            isImgSelected = false;
            binding.send.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
            }
        }
//        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            if (imageUri != null) {
//                storeImg();
//            }
//        } else {
//            Toast.makeText(this, "you failed", Toast.LENGTH_SHORT).show();
//            isImgSelected = false;
//            binding.send.setEnabled(true);
//            binding.progressBar.setVisibility(View.INVISIBLE);
//        }
    }

    private void storeImg() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(System.currentTimeMillis() + ".jpg"
//                        + getFileExtension(imageUri)
                );
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                gymUser.setPhoto(uri.toString());
                binding.editImg.setImageURI(imageUri);
                isImgSelected = false;
                binding.send.setEnabled(true);
                binding.progressBar.setVisibility(View.INVISIBLE);
            });
        });
    }

    public void editGymUser() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.send.setEnabled(false);
        
        gymUser.setName(binding.etName.getText().toString());
        gymUser.setBio(binding.tvBio.getText().toString());
        gymUser.setPhone(binding.tvPhone.getText().toString());
        gymUser.setAddress(binding.tvAddress.getText().toString());

        documentReference.set(gymUser).addOnSuccessListener(aVoid -> {
            goToMain(gymUser);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.send.setEnabled(true);
        });

    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(imageUri));
    }

    private void goToMain(GymUser myUser) {
        // save in shared pref
        saveId(myUserId);
        saveName(myUser.getName());
        savePic(myUser.getPhoto());

        Toast.makeText(this, "profile was edited successfully", Toast.LENGTH_SHORT).show();
        binding.progressBar.setVisibility(View.INVISIBLE);
        binding.send.setEnabled(true);
        this.finishAndRemoveTask();
    }

    // save in shared pref
    public void saveId(String id){
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userId), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userId), id);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void saveName(String name){
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userName), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userName), name);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void savePic(String pic){
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userPic), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userPic), pic);
        userSnEditor.apply();
        userSnEditor.commit();
    }
}