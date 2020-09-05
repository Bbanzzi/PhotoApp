package com.example.photoapp.PlanMain;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.photoapp.PlanSchedule.RealtimeData;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class PlanPagerAdapter extends FragmentStatePagerAdapter {

    private String TAG = "PagerAdapter";

    private int days;
    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<ArrayList<RealtimeData>> realTimeDataArrayList;

    public PlanPagerAdapter(@NonNull FragmentManager fm, ArrayList<ArrayList<RealtimeData>> realTimeDataArrayList, int behavior) {
        super(fm, behavior);
        this.realTimeDataArrayList = realTimeDataArrayList;
    }

    @NonNull
    @Override
    public PlanFragment getItem(int position) {
        Log.d(TAG, " postiion");
        return PlanFragment.newInstance(position, titles.get(position), realTimeDataArrayList.get(position));//,titles.get(position));
    }

    @Override
    public int getCount() {
        return days;
    }

    @Override
    public int getItemPosition(Object object) {
        Log.d(TAG, " getItemPosition");
        PlanFragment fragment = (PlanFragment) object;
        String title = fragment.getTitle();
        int position = titles.indexOf(title);
        //Log.i(TAG, String.valueOf(position));

        if (position >= 0) {
            fragment.update(realTimeDataArrayList.get(position));
            return position;
        } else {
            return POSITION_NONE;
        }
    }

    void setDays(int days) {
        this.days = days;
        for (int i = 0; i < days; i++)
            titles.add("Day " + (i + 1));
    }
}
