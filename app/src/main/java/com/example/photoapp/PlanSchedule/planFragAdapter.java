package com.example.photoapp.PlanSchedule;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.R;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class planFragAdapter extends BaseAdapter {

    private Cell itemList;
    private String[] arr_time = new String[10];
    private String[] arr_place = new String[10];
    public int[] arr_pos = {0,1,2,3,4,5,6,7,8,9};

    @Override
    public int getCount(){
        if(itemList.getData() == "-"){
            return 1;
        }else {
            return itemList.getPlace().size();
        }
    }

    @Override
    public Object getItem(int position) {
        return itemList.getData();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        WindowItem windowItem = new WindowItem(parent.getContext());
        windowItem.setText_time(arr_time[position]);
        windowItem.setText_place(arr_place[position]);
        windowItem.setPosition(arr_pos[position]);

        return windowItem;
    }

    public void setPlanWinItems(Cell mItemList){
        itemList = mItemList;
        if(itemList.getData() != "-") {
            for (int i = 0; i < itemList.getPlace().size(); i++) {
                int time = itemList.getTime().get(i);
                int time_h = time / 60;
                int time_m = time % 60;
                String time_val = time_h + "시  " + time_m + "분";
                arr_time[i] = time_val;
                arr_place[i] = itemList.getPlace().get(i);
            }
        }else{
            arr_place[0] = "-";
            arr_time[0] = "-";
        }
    }


}
