package com.uni.gym;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.github.marlonlom.utilities.timeago.TimeAgoMessages;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Locale;

public class TrialActivity extends AppCompatActivity {


    private static final int REQUEST_PICK_PHOTO = 100;
    private Uri imageUri;
    private ImageView tripPhotoImageView;
    private String photoUrl;
    private TextView tvTime;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private VideoView videoView;
    private boolean clicked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial);
        ImageView imageView=findViewById(R.id.image_view);

//        String base64String = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAA...";


        String encodedImage=getResources().getString(R.string.encodedImage);
//        String base64Image  = encodedImage.split(",")[1];
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imageView.setImageBitmap(decodedByte);

//
//        videoView=findViewById(R.id.video);
//        String videoPath="https://firebasestorage.googleapis.com/v0/b/gym-app-f1641.appspot.com/o/1596234522913.mp4?alt=media&token=abb68955-e240-4ebe-8022-193885ed77b3";
//        Uri uri= Uri.parse(videoPath);
//        videoView.setVideoURI(uri);
//
//        MediaController mediaController=new MediaController(this);
//        videoView.setMediaController(mediaController);
//        mediaController.setAnchorView(videoView);
//
//        videoView.start();
//
//
//        videoView.setOnClickListener(view -> {
////            getPhoto();
//        });

//        tvTime = findViewById(R.id.tv_time);
//        long timeInMillis = System.currentTimeMillis();
//        Locale LocaleBylanguageTag = Locale.forLanguageTag("en");
//        TimeAgoMessages messages = new TimeAgoMessages.Builder().withLocale(LocaleBylanguageTag).build();
//
//        String text = TimeAgo.using(timeInMillis, messages);
//        tvTime.setText(text);

//        tripPhotoImageView = findViewById(R.id.image_view);
//
//        Glide.with(tripPhotoImageView).load("https://scontent.fgza4-1.fna.fbcdn.net/v/t1.0-9/104107663_591656115061754_1755169535880935657_o.jpg?_nc_cat=110&_nc_sid=09cbfe&_nc_ohc=2T4bDa8fxWEAX-I6gKZ&_nc_ht=scontent.fgza4-1.fna&oh=cbd38597a40573ca19c5aa59af4b9ca5&oe=5F3B70A9").into(tripPhotoImageView);
//        imageUri= Uri.parse("http://graph.facebook.com/614972626063436/picture?type=small");
//        storeImg();https://scontent.fgza4-1.fna.fbcdn.net/v/t1.0-9/104107663_591656115061754_1755169535880935657_o.jpg?_nc_cat=110&_nc_sid=09cbfe&_nc_ohc=2T4bDa8fxWEAX-I6gKZ&_nc_ht=scontent.fgza4-1.fna&oh=cbd38597a40573ca19c5aa59af4b9ca5&oe=5F3B70A9
    }

    private void storeImg() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Toast.makeText(this, "stored", Toast.LENGTH_SHORT).show();
//                uri.toString();
//                Glide.with(tripPhotoImageView).load(uri.toString()).into(tripPhotoImageView);
            });
        });
    }

    private String getFileExtension(Uri imageUri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(imageUri));
    }

    public void getPhoto() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
//
//        Intent takeVideoIntent = new Intent(MediaStore.INTENT_ACTION_VIDEO_PLAY_FROM_SEARCH);
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            imageUri = intent.getData();
            videoView.setVideoURI(imageUri);
            storeImg();
        }
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
//            Uri videoUri = intent.getData();
//            videoView.setVideoURI(videoUri);
//        }
//        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            imageUri = data.getData();
//            if (imageUri != null) {
//                tripPhotoImageView.setImageURI(imageUri);
//            }
//        } else{
//            Toast.makeText(this, "you failed", Toast.LENGTH_SHORT).show();
//        }
//
//    }
}