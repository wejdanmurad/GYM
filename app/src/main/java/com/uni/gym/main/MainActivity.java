package com.uni.gym.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.uni.gym.main.blog.view.AddPostActivity;
import com.uni.gym.R;
import com.uni.gym.auth.WelcomeActivity;
import com.uni.gym.databinding.ActivityMainBinding;
import com.uni.gym.main.home.view.AddCourseActivity;
import com.uni.gym.main.home.view.AddSessionActivity;
import com.uni.gym.main.profile.EditProfileActivity;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private ActivityMainBinding binding;
    private BottomNavigationView bottomNavigationView;
    public NavController navController;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private MenuItem menuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals("FrGXVmaSwqPZyZ4a22B08lRuz0Y2"))
            saveRole(getString(R.string.admin));
        else
            saveRole(getString(R.string.member));

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        bottomNavigationView = binding.bottomNavigation;
        navController = Navigation.findNavController(this, R.id.my_nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            toolbarTitle.setText(destination.getLabel());
            if (menuItem != null)
                if (destination.getId() == R.id.profileFragment)
                    menuItem.setVisible(true);
                else
                    menuItem.setVisible(false);

        });

    }

    @Override
    public void onFragmentInteraction(int id) {
        switch (id) {
            case 1:
                startActivity(new Intent(MainActivity.this, AddPostActivity.class));
                break;
            case 2:
                startActivity(new Intent(MainActivity.this, AddCourseActivity.class));
                break;
            case 3:
                navController.navigate(R.id.allCoursesFragment);
                break;
            case 41:
                navController.navigate(R.id.profileFragment);
                break;
        }
    }

    @Override
    public void onFragmentInteraction(int id, Bundle bundle) {
        switch (id) {
            case 1:
                navController.navigate(R.id.commentFragment, bundle);
                break;
            case 2:
                navController.navigate(R.id.photoFragment, bundle);
                break;
            case 3:
                navController.navigate(R.id.courseFragment, bundle);
                break;
            case 4:
                navController.navigate(R.id.sessionsFragment, bundle);
                break;
            case 5:
                Intent intent = new Intent(MainActivity.this, AddSessionActivity.class);
                intent.putExtra("course_id", bundle.getString("course_id"));
                startActivity(intent);
                break;
            case 6:
                navController.navigate(R.id.sessionFragment, bundle);
                break;
        }
    }

    //The methods for options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        menuItem = menu.getItem(0);
        menuItem.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_profile:
                startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                return true;
            case R.id.change_password:
                navController.navigate(R.id.forgotPassFragment);
                return true;
            case R.id.log_out:
                LogOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void LogOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        saveId(null);
                        saveName(null);
                        savePic(null);
                        saveRole(null);

                        startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                        finish();
                    }
                });
    }

    // save in shared pref
    public void saveId(String id) {
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userId), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userId), id);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void saveName(String name) {
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userName), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userName), name);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void savePic(String pic) {
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userPic), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userPic), pic);
        userSnEditor.apply();
        userSnEditor.commit();
    }

    public void saveRole(String role) {
        SharedPreferences.Editor userSnEditor = getSharedPreferences(getString(R.string.key_userRole), MODE_PRIVATE).edit();
        userSnEditor.putString(getString(R.string.key_userRole), role);
        userSnEditor.apply();
        userSnEditor.commit();
    }
}