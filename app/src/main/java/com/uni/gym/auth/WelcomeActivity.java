package com.uni.gym.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uni.gym.R;
import com.uni.gym.databinding.ActivityWelcomeBinding;
import com.uni.gym.main.MainActivity;
import com.uni.gym.main.profile.model.GymUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class WelcomeActivity extends AppCompatActivity {

    public static final int RC_SIGN_IN = 100;
    private FirebaseUser user;
    private GoogleSignInClient googleSignIn;
    public static final String TAG = "facebookwejdan";
    private FirebaseAuth mAuth;
    private ActivityWelcomeBinding binding;

    private CallbackManager callbackManager;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Intent intent = new Intent(this, AuthActivity.class);
        binding.signUp.setOnClickListener(v -> {
            intent.putExtra("sign_up", true);
            startActivity(intent);
            finish();
        });
        binding.signIn.setOnClickListener(v -> {
            intent.putExtra("sign_up", false);
            startActivity(intent);
            finish();
        });

        // google///////////////////////////////////////////////////////////////////
        setGoogle();

        //facebook//////////////////////////////////////////////////////////////////
        setFacebook();

    }

    //facebook//////////////////////////////////////////////////////////////////
    private void setFacebook() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", object.toString());
                                try {
                                    String name = object.getString("name");
                                    String image_url = object.getJSONObject("picture").getJSONObject("data").getString("url");

                                    firebaseAuthWithFacebook(loginResult.getAccessToken().getToken(), image_url, name);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();

                Toast.makeText(WelcomeActivity.this, "Login succeeded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(WelcomeActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                binding.progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(WelcomeActivity.this, "facebook error", Toast.LENGTH_LONG).show();
                binding.progressBar.setVisibility(View.INVISIBLE);
            }
        });

        binding.facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("public_profile", "email", "user_birthday", "user_friends"));
            }
        });
    }

    // google///////////////////////////////////////////////////////////////////
    private void setGoogle() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignIn = GoogleSignIn.getClient(this, gso);

        binding.google.setOnClickListener(view1 -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            startActivityForResult(googleSignIn.getSignInIntent(), RC_SIGN_IN);
        });
    }

    //google result
    //facebook result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId() + " " + account.getDisplayName());
                firebaseAuthWithGoogle(account.getIdToken(), account.getDisplayName(), account.getPhotoUrl().toString());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    //facebook auth
    private void firebaseAuthWithFacebook(String token, String image_url, String name) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            createUser(name, image_url);
                        }
                    } else {
                        Toast.makeText(WelcomeActivity.this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                            Toast.makeText(WelcomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        }
                );
    }

    //google auth
    private void firebaseAuthWithGoogle(String idToken, String name, String photo) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null) {
                            createUser(name, photo);
                        }
                    } else {
                        Toast.makeText(WelcomeActivity.this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                    }
                    binding.progressBar.setVisibility(View.INVISIBLE);

                })
                .addOnFailureListener(this, e -> {
                            Toast.makeText(WelcomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.progressBar.setVisibility(View.INVISIBLE);
                        }
                );
    }



    //user creating and checking methods

    public void createUser(String name, String photoUrl) {
        user = mAuth.getCurrentUser();
        GymUser gymUser = new GymUser();
        gymUser.setName(name);
        gymUser.setPhoto(photoUrl);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("GymUsers").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("task succeeded");
                if (task.getResult().exists()) {
                    goToMain(task.getResult().toObject(GymUser.class));
                    System.out.println("user exists");
                } else {
                    setUserDoc(gymUser);
                    System.out.println("user does not exist");
                }
            } else {
                Toast.makeText(this, "task did not succeed", Toast.LENGTH_SHORT).show();
                System.out.println("task did not succeed");
            }
        });


    }

    private void setUserDoc(GymUser gymUser) {
        firebaseFirestore.collection("GymUsers").document(user.getUid()).set(gymUser).addOnSuccessListener(documentReference -> {
            goToMain(gymUser);
            System.out.println("user was created");
        }).addOnFailureListener(e -> {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("user was not created" + e.getMessage());
        });
    }

    private void goToMain(GymUser myUser) {
        // save in shared pref
        saveId(user.getUid());
        saveName(myUser.getName());
        savePic(myUser.getPhoto());

        binding.progressBar.setVisibility(View.INVISIBLE);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        WelcomeActivity.this.finish();
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