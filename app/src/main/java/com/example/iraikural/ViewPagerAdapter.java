package com.example.iraikural;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MainFragment(); // First screen
        } else {
            return new SecondFragment(); // Second screen
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two pages
    }
}
