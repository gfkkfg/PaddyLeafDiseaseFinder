package com.example.paddyleafdiseasedetection;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        // Initialize Firebase Authentication
        mauth = FirebaseAuth.getInstance();

        // Check login status and load the appropriate fragment
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser != null) {
            loadFragment(new HomeFragment());
        } else {
            loadFragment(new LoginFragment());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.login) {
            loadFragment(new LoginFragment());
        } else if (id == R.id.history) {
            loadFragment(new HistoryFragment());
        } else if (id == R.id.logout) {
            mauth.signOut();
            Toast.makeText(this, getString(R.string.logout_successful), Toast.LENGTH_LONG).show();
            loadFragment(new LoginFragment());
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Check if the user is logged in
            if (mauth.getCurrentUser() == null) {
                // User not logged in, always load LoginFragment
                selectedFragment = new LoginFragment();
                Toast.makeText(this, getString(R.string.please_log_in), Toast.LENGTH_SHORT).show();
            } else {
                // User is logged in, load selected fragment
                if (itemId == R.id.home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.settings) {
                    selectedFragment = new SettingsFragment();
                } else if (itemId == R.id.help) {
                    selectedFragment = new HelpFragment();
                }
            }

            // Load the selected fragment
            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frameView, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        }
        return false;
    }

    public void onLocaleChanged(String localeCode) {
        setAppLocale(localeCode);
        recreate();
    }

    private void setAppLocale(String localeCode) {
        Locale locale = new Locale(localeCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        if (!language.isEmpty()) {
            setAppLocale(language);
        }
    }
}
