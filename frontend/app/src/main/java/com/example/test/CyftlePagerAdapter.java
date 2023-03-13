package com.example.test;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class CyftlePagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragments;
    private static final int NUM_PAGES = 10;

    public CyftlePagerAdapter(FragmentActivity fragmentActivity, List<Fragment> fragments) {
        super(fragmentActivity);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) { //포지션마다 있을 fragment설정
        if (position == 0) return new CameraFragment();
        else if (position == 1) return new CyftleFragment();
        else if (position == 2) return new TaleMakeFragment();
        else if (position == 3) return new Page1Fragment();
        else if (position == 4) return new Page2Fragment();
        else if (position == 5) return new Page3Fragment();
        else if (position == 6) return new Page4Fragment();
        else if (position == 7) return new Page5Fragment();
        else if (position == 8) return new Page6Fragment();
        else return new Page7Fragment();
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;  //페이지 수 지정.
    }
}

