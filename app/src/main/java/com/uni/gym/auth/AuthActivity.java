package com.uni.gym.auth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.google.android.material.navigation.NavigationView;
import com.uni.gym.R;
import com.uni.gym.boarding.BoardingActivity;
import com.uni.gym.databinding.ActivityAuthBinding;
import com.uni.gym.main.MainActivity;
import com.uni.gym.main.OnFragmentInteractionListener;
import com.uni.gym.splash.SplashActivity;

public class AuthActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ActivityAuthBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Boolean sign_up = getIntent().getBooleanExtra("sign_up",true);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.my_nav_host_fragment_auth);
        navController = navHost.getNavController();

        NavInflater navInflater = navController.getNavInflater();
        NavGraph graph = navInflater.inflate(R.navigation.navigation_graph_auth);

        if (sign_up) {
            graph.setStartDestination(R.id.signUpFragment);
        } else {
            graph.setStartDestination(R.id.signInFragment);
        }

        navController.setGraph(graph);
    }

    @Override
    public void onFragmentInteraction(int id) {
        switch (id) {
            case 11:
            case 22:
                navController.navigate(R.id.signUpFragment);
                break;
            case 23:
                navController.navigate(R.id.forgotPassFragment);
                break;
            case 41:
                navController.navigate(R.id.goToHomeFragment);
                break;
            case 21:
            case 31:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case 12:
            case 32:
            case 51:
                navController.navigate(R.id.signInFragment);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(int id, Bundle bundle) {

    }



}