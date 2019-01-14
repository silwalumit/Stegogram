package com.umitsilwal.stegogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class HomeActivity extends AppCompatActivity {
    private MenuItem contactItem;
    public static final String LOGOUT_ACTION = "com.umitsilwal.stegogram.LOGOUT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactItem = findViewById(R.id.contact_nav);

        //set the layout activity_home
        setContentView(R.layout.activity_home);

        //set toolbar support and toolbar tittle
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contacts");

        //set Contacts as the defualt fragment in the home screen
        if (savedInstanceState == null) {
            replaceFragment(new Contacts());
        }

        //Set Bottom Navigation Listener
        BottomNavigationView bottomNavigationView =  findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.contact_nav:
                        toolbar.setTitle("Contacts");
                        replaceFragment(new Contacts());
                        return true;

                    case R.id.chat_nav:
                        toolbar.setTitle("Chats");
                        replaceFragment(new ChatList());
                        return true;

                    case R.id.settings_nav:
                        toolbar.setTitle("Settings");
                        replaceFragment(new Settings());
                        return true;
                }
                return false;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    //clink to the next fragment.
    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    public void logout(View view){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("xmpp_username", null).putString("xmpp_password", null).apply();

        Log.d(NetworkConnectionService.TAG, "Logging out.");
        Intent logout = new Intent(this, NetworkConnectionService.class);
        logout.setAction(LOGOUT_ACTION);
        startService(logout);

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
