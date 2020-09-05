package com.example.photoapp.PlanMain;

import android.text.format.DateFormat;

import com.google.protobuf.Timestamp;

import java.util.Calendar;
import java.util.Locale;

public class PlanPhotoData {

    private String place;
    private String memo;
    private String time;
    private int time_i;
    private String Id;
    private String imageUrl;
    private Timestamp creationTime;
    private Long creationTimeLong;


    private Boolean check=false;

    public PlanPhotoData(String Id, String imageUrl, Timestamp date){
        this.Id=Id;
        this.imageUrl = imageUrl;
        this.creationTime = date;
        this.creationTimeLong=date.getSeconds();
    }

    public PlanPhotoData(String place,String time) {
        this.place = place;
        this.time = time;
    }

    public PlanPhotoData(String place, String time, String memo, int time_i){
        this.place = place;
        this.time = time;
        this.memo = memo;
        this.time_i = time_i;
    }


    public String getPlace() { return place; }
    public String getTime() { return time; }
    public String getMemo() { return memo; }
    public int getTime_i() { return time_i; }

    public void setPlace(String place) { this.place = place; }
    public void setTime(String time) { this.time = time; }
    public void setMemo(String memo) { this.memo = memo; }
    public void setTime_i(int time_i) { this.time_i = time_i;}

    public String getId() { return Id; }
    public void setId(String id) { Id = id; }

    public void setImageUrl(String imageUrl) {this.imageUrl=imageUrl;}
    public String getImageUrl () {return imageUrl;}

    public Long getCreationTimeLong() { return creationTimeLong; }
    public void setCreationTimeLong(Long creationTimeLong) { this.creationTimeLong = creationTimeLong; }


    public Boolean getCheck() { return check; }
    public void setCheck(Boolean check) { this.check = check; }

    public String changeTimeToDate() {
        Calendar cal = Calendar.getInstance(Locale.KOREAN);
        cal.setTimeInMillis(creationTime.getSeconds() * 1000);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }
    public Calendar changeTimeToCalendar() {
        Calendar cal = Calendar.getInstance(Locale.KOREAN);
        cal.setTimeInMillis(creationTime.getSeconds() * 1000);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return cal;
    }
}
