package com.umitsilwal.stegogram.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.umitsilwal.stegogram.Fragments.ChatFragment;
import com.umitsilwal.stegogram.Fragments.Contacts;
import com.umitsilwal.stegogram.Fragments.SettingsFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int numOfTabs = 3;
    public PagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return new Contacts();
            case 1: return new ChatFragment();
            case 2: return new SettingsFragment();
            default: return null;
        }
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return numOfTabs;
    }
}
