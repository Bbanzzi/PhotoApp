package com.example.photoapp.PlanMain.Photo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;

import java.util.ArrayList;

public class PhotoPagerAdapter extends FragmentStatePagerAdapter {

    private String TAG = "PhotoPagerAdapter";

    private ArrayList<PlanPhotoData> realTimeDataList;

    public PhotoPagerAdapter(@NonNull FragmentManager fm, ArrayList<PlanPhotoData> realTimeDataList, int behavior) {
        super(fm, behavior);
        this.realTimeDataList = realTimeDataList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return PhotoFragment.newInstance(position, realTimeDataList.get(position));

    }

    @Override
    public int getCount() {
        return realTimeDataList.size();
    }
}
