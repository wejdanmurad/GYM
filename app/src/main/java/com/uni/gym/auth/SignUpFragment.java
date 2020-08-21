package com.uni.gym.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uni.gym.R;
import com.uni.gym.databinding.FragmentSignUpBinding;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.main.profile.model.GymUser;

import static android.content.Context.MODE_PRIVATE;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;
    private FirebaseAuth mAuth;
    private OnFragmentInteractionListener mListener;
    public static final String TAG = SignUpFragment.class.getSimpleName();

    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        binding.progressBar.setVisibility(View.INVISIBLE);

        binding.register.setOnClickListener(view -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.register.setEnabled(false);
            binding.checkBox.setError(null);
            if (binding.etFullName.getText().toString().isEmpty()) {
                binding.etFullName.setError("please enter your name");
                binding.etFullName.requestFocus();
                enableBtn();
            } else if (binding.etEmail.getText().toString().isEmpty()) {
                binding.etEmail.setError("please enter your email");
                binding.etEmail.requestFocus();
                enableBtn();
            } else if (binding.etPass.getText().toString().isEmpty()) {
                binding.etPass.setError("please enter your password");
                binding.etPass.requestFocus();
                enableBtn();
            } else if (binding.etConfirmPass.getText().toString().isEmpty()) {
                binding.etConfirmPass.setError("please confirm your password");
                binding.etConfirmPass.requestFocus();
                enableBtn();
            } else if (!binding.checkBox.isChecked()) {
                binding.checkBox.setError("Please Check to agree to our terms!");
                binding.checkBox.requestFocus();
                enableBtn();
            } else {
                if (binding.etPass.getText().toString().equals(binding.etConfirmPass.getText().toString())) {
                    mAuth.createUserWithEmailAndPassword(binding.etEmail.getText().toString(), binding.etPass.getText().toString())
                            .addOnCompleteListener(getActivity(), task -> {
                                if (task.isSuccessful()) {
                                    createUser();
                                }
//                                else
//                                    Toast.makeText(getContext(), "please make sure you entered a real email", Toast.LENGTH_SHORT).show();


                            });
                } else {
                    binding.checkBox.setError("password is not the same");
                    binding.checkBox.requestFocus();
                }
            }
        });

        binding.SignIn.setOnClickListener(view -> {
            mListener.onFragmentInteraction(32);
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

    public void createUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        GymUser gymUser = new GymUser();
        gymUser.setName(binding.etFullName.getText().toString());

        // save in shared pref
        saveId(user.getUid());
        saveName(binding.etFullName.getText().toString());


        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("GymUsers").document(user.getUid())
                .set(gymUser).addOnSuccessListener(documentReference -> {
            Toast.makeText(getContext(), "congrats u made it ", Toast.LENGTH_SHORT).show();
            mListener.onFragmentInteraction(31);
            enableBtn();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            enableBtn();
        });

    }

    private void enableBtn() {
        binding.register.setEnabled(true);
        binding.progressBar.setVisibility(View.INVISIBLE);
    }

    // shared pref methods
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
}