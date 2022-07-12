package com.project.videofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogInActivity.activities.add(this);

        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.white));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_image, null);
        getSupportActionBar().setCustomView(v);

        BottomNavigationView bottom_nav = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottom_nav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, HomeScreen.newInstance(), "fragment_home_screen").commit();

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    boolean sn = true;

                    switch (item.getItemId()) {
                        case R.id.homeScreen:
                            if (item.isChecked()) {
                                sn = false;
                            } else {
                                selectedFragment = HomeScreen.newInstance();
                            }
                            break;
                        case R.id.searchScreen:
                            if (item.isChecked()) {
                                sn = false;
                            } else {
                                selectedFragment = SearchScreen.newInstance();
                            }
                            break;
                        case R.id.uploadScreen:
                            if (item.isChecked()) {
                                sn = false;
                            } else {
                                selectedFragment = UploadScreen.newInstance();
                            }
                            break;
                        case R.id.profileScreen:
                            if (item.isChecked()) {
                                sn = false;
                            } else {
                                selectedFragment = ProfileScreen.newInstance();
                            }
                            break;
                    }

                    if (sn) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, selectedFragment).commit();
                    }

                    return true;
                }
            };

    @Override
    public void onBackPressed() { }

    public void onDestroy()
    {
        super.onDestroy();
        LogInActivity.activities.remove(this);
    }
}