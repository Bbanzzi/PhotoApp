package com.example.photoapp.PlanSchedule;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.photoapp.PlanMain.PlanPhotoData;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class RealtimeData implements Parcelable {

    public String place;
    public String memo;
    public int time;
    public int days;

    private List<PlanPhotoData> photoDataList=new ArrayList<>();

    public RealtimeData(){ }

    public RealtimeData(String place, String memo, String time_h, String time_m, int col ){
        this.place = place;
        this.memo = memo;
        this.time = Integer.parseInt(time_h)*60 + Integer.parseInt(time_m);
        this.days = col;
    }
    public RealtimeData(String place, int time, String memo, int col){
        this.place=place;
        this.time= time * 60 * 3;//3뭐엿징
        this.memo=memo;
        this.days=col;
    }

    private RealtimeData(Parcel in) {
        memo=in.readString();
        place=in.readString();
        time=in.readInt();
        days=in.readInt();
    }

    /*
    public int getDaysFromStr(String col){
        int days = 1;
        switch (col){
            case "1일차" : days = 1;break;
            case "2일차" : days = 2;break;
            case "3일차" : days = 3;break;
            case "4일차" : days = 4;break;
            case "5일차" : days = 5;break;
        }
        return days;
    }

     */

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("place", place);
        data.put("memo", memo);
        data.put("time", time);
        data.put("days",days);
        return data;
    }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public String getPlace() { return place; }
    public void setDays(int days) {this.days=days;}

    public void setPlace(String place) { this.place = place; }
    public int getTime() { return time; }

    public String getTimeStr(){
        int hour = time/60;
        int min = time%60;
        String timeStr;
        if(hour <= 12){
            if(min == 0){
                timeStr = "오전 " + hour + " : " + "00   ";
            }else{
                timeStr = "오전 " + hour + " : " + min + "   ";
            }
        }else{
            hour = hour-12;
            if(min == 0){
                timeStr = "오후 " + hour + " : " + "00   ";
            }else{
                timeStr = "오후 " + hour + " : " + min + "   ";;
            }
        }
        return timeStr;
    }

    public void setTime(int time) { this.time = time; }
    public int getDays() {return days;}

    public List<PlanPhotoData> getPhotoDataList() { return photoDataList; }
    public void setPhotoDataList(List<PlanPhotoData> photoDataMapphotoDataList) { this.photoDataList = photoDataList; }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(memo);
        dest.writeString(place);
        dest.writeInt(time);
        dest.writeInt(days);
    }

    public static final Creator<RealtimeData> CREATOR = new Creator<RealtimeData>() {
        @Override
        public RealtimeData createFromParcel(Parcel in) {
            return new RealtimeData(in);
        }

        @Override
        public RealtimeData[] newArray(int size) {
            return new RealtimeData[size];
        }
    };

}
