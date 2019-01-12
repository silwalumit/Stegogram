package com.umitsilwal.stegogram;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class HomeActivity extends AppCompatActivity {
    private MenuItem contactItem, addContact;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactItem = (MenuItem) findViewById(R.id.contact_nav);

        //set the layout activity_home
        setContentView(R.layout.activity_home);

        //set toolbar support and toolbar tittle
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Contacts");

        //Set Bottom Navigation Listener
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
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

        bottomNavigationView.setSelectedItemId(0);
        /*final TabLayout tabLayout = findViewById(R.id.tab_layout);

        //TabLayout.Tab contacts, settings,chatList;

        //add three tabs in the Tab Layout View
        tabLayout.addTab(tabLayout.newTab().setText("Contacts")
                .setIcon(R.drawable.ic_contacts));
        tabLayout.addTab(tabLayout.newTab().setText("Chat List")
                .setIcon(R.drawable.ic_chat));
        tabLayout.addTab(tabLayout.newTab().setText("Settings")
                .setIcon(R.drawable.ic_settings));
        //sets the tabs to fill the entire layout.
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //use PageAdapter to manage page views in fragments
        //final ViewPager viewPager = findViewById(R.id.pager);
        //final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        //viewPager.setAdapter(adapter);

        //setting a listener for clicks
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
              //  viewPager.setCurrentItem(tab.getPosition());
                switch(tab.getPosition())
                {
                    case 0:
                        replaceFragment(new Contacts());
                    case 1:
                        replaceFragment(new ChatList());
                        return;
                    case 2:
                        replaceFragment(new Settings());
                        return;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
*/
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
}
