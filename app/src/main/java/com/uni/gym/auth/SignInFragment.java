package com.uni.gym.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uni.gym.R;
import com.uni.gym.databinding.FragmentSignInBinding;
import com.uni.gym.main.MainActivity;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.profile.model.GymUser;

import static android.content.Context.MODE_PRIVATE;

public class SignInFragment extends Fragment {

    private FragmentSignInBinding binding;
    private FirebaseAuth mAuth;
    private OnFragmentInteractionListener mListener;

    public static SignInFragment newInstance() {
        return new SignInFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        binding.LogIn.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.LogIn.setEnabled(false);
            String email = binding.loginEmail.getText().toString();
            String pass = binding.loginPass.getText().toString();
            if (email.isEmpty() && pass.isEmpty()) {
                Toast.makeText(getContext(), "Enter your email and password!", Toast.LENGTH_SHORT).show();
                binding.loginEmail.setError("please enter your email");
                binding.loginPass.setError("please enter your password");
                binding.loginEmail.requestFocus();
            } else if (email.isEmpty()) {
                binding.loginEmail.setError("please enter email");
                binding.loginEmail.requestFocus();
            } else if (pass.isEmpty()) {
                binding.loginPass.setError("please enter password");
                binding.loginPass.requestFocus();
            } else {
                binding.LogIn.setEnabled(false);
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(getActivity(), task -> {
                            if(task.isSuccessful()){
                                getUserDataFromFirebase();
                            }else {
                                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
            binding.LogIn.setEnabled(true);
            binding.progressBar.setVisibility(View.INVISIBLE);
        });

        binding.SignUp.setOnClickListener(view -> {
            mListener.onFragmentInteraction(22);
        });

        binding.forgotPass.setOnClickListener(view -> {
            mListener.onFragmentInteraction(23);
        });

    }

    private void getUserDataFromFirebase() {
        FirebaseFirestore.getInstance().collection("GymUsers").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("task succeeded");
                if (task.getResult().exists()) {
                    goToMain(task.getResult().toObject(GymUser.class));
                    System.out.println("user exists");
                } else {
                    System.out.println("user does not exist");
                }
            } else {
                System.out.println("task did not succeed");
            }
        });
    }

    private void goToMain(GymUser myUser) {
        // save in shared pref
        saveId(mAuth.getCurrentUser().getUid());
        saveName(myUser.getName());
        savePic(myUser.getPhoto());

        mListener.onFragmentInteraction(21);
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    // save in shared pref
    public void saveId(String id){
        SharedPreferences.Editor userSnEditor = getActivity().getSharedPreferences(getString(R.string.key_userId), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userId), id);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void saveName(String name){
        SharedPreferences.Editor userSnEditor = getActivity().getSharedPreferences(getString(R.string.key_userName), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userName), name);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void savePic(String pic){
        SharedPreferences.Editor userSnEditor = getActivity().getSharedPreferences(getString(R.string.key_userPic), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userPic), pic);
        userSnEditor.apply();
        userSnEditor.commit();
    }

}