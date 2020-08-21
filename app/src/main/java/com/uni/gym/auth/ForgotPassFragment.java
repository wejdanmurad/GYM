package com.uni.gym.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.uni.gym.R;
import com.uni.gym.databinding.FragmentForgotPassBinding;
import com.uni.gym.databinding.FragmentSignInBinding;
import com.uni.gym.main.OnFragmentInteractionListener;

public class ForgotPassFragment extends Fragment {

    private FragmentForgotPassBinding binding;
    private FirebaseAuth mAuth;
    private OnFragmentInteractionListener mListener;

    public static ForgotPassFragment newInstance() {
        return new ForgotPassFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPassBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        binding.send.setOnClickListener(view -> {
            if (binding.etEmail.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), "Please enter your EMAIL !!", Toast.LENGTH_LONG).show();
            } else {
                mAuth.sendPasswordResetEmail(binding.etEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mListener.onFragmentInteraction(41);
                        } else {
                            Toast.makeText(getContext(), "Please make sure your EMAIL is a real one !!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

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

}