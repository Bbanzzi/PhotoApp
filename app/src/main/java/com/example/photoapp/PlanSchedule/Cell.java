package com.example.photoapp.PlanSchedule;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class Cell {
    @Nullable
    private Object mData;
    private ArrayList<String> place;
    private ArrayList<String> memo;
    private ArrayList<Integer> time;

    public Cell(@Nullable Object data){
        this.mData = data;
        ArrayList<String> place_val = new ArrayList<>();
        place_val.add(data.toString());
        this.place = place_val;
    }

    public Cell(@Nullable Object data, String j, int i){
        ArrayList<Integer> time_val = new ArrayList<>();
        ArrayList<String> place_val = new ArrayList<>();
        place_val.add(j);
        time_val.add(i);
        this.mData = data;
        this.time =  time_val;;
        this.place = place_val;
    }

    public Cell(@Nullable Object data, ArrayList<String> p_arr, ArrayList<Integer> t_arr){
        this.mData = data;
        this.time = t_arr;
        this.place = p_arr;
    }

    @Nullable
    public Object getData() {
        return mData;
    }

    public ArrayList<String> getPlace() {
        return place;
    }

    public String getPlaceText() {
        String text = null;
        if(!(place==null)) {
            text = place.get(0);
            for (int i = 1; i < place.size(); i++) {
                String text_plus = place.get(i);
                text = text + "\n" + text_plus;
                return text;
            }
        }else{
            text = "-";
        }
        return text;

    }

    public ArrayList<Integer> getTime() {
        return time;
    }

    public void addPlace(String j){
        place.add(j);
    }
    public void addPlace(String j, int i){
        place.add(i,j);
    }
    public void addTime(int i){
        time.add(i);
    }
    public void sortTime(){
        Collections.sort(time);
    }

    public void delPlace(int i){
        place.remove(i);
    }
    public void delTime(int i){
        time.remove(i);
    }
}


