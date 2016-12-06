package com.gauravbhor.securechat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by bhorg on 12/5/2016.
 */

public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

    private String tabTitles[] = new String[]{"Friends", "Groups"};
    private List<Fragment> fragmentList;

    public SampleFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}