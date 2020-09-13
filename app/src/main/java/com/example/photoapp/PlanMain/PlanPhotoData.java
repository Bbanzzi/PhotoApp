package com.example.photoapp.PlanMain;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import com.example.photoapp.PlanSchedule.RealtimeData;
import com.google.protobuf.Timestamp;

import java.sql.Time;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class PlanPhotoData implements Parcelable {

    private String place;
    private String memo;
    private String time;
    private int time_i;

    private String filename;
    private String Id;
    private String imageUrl;
    private Timestamp creationTime;
    private Long creationTimeLong;


    private Boolean check=false;

    public PlanPhotoData(String filename, String Id, String imageUrl, Timestamp date){
        this.filename=filename;
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
    // for Photo delete Request
    public PlanPhotoData(String Id){ this.Id=Id; }

    private  PlanPhotoData(Parcel in) {
        place=in.readString();
        memo=in.readString();
        time = in.readString();
        time_i = in.readInt();

        filename = in.readString();
        Id = in.readString();
        imageUrl = in.readString();

        creationTimeLong = in.readLong();
        creationTime = Timestamp.newBuilder().setSeconds(creationTimeLong).build();
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

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(place);
        dest.writeString(memo);
        dest.writeString(time);
        dest.writeInt(time_i);

        dest.writeString(filename);
        dest.writeString(Id);
        dest.writeString(imageUrl);
        if(creationTimeLong!=null) {
            dest.writeLong(creationTimeLong);
        }else{
            dest.writeLong(0);
        }
    }


    public static final Creator<PlanPhotoData> CREATOR = new Creator<PlanPhotoData>() {
        @Override
        public PlanPhotoData createFromParcel(Parcel in) {
            return new PlanPhotoData(in);
        }

        @Override
        public PlanPhotoData[] newArray(int size) {
            return new PlanPhotoData[size];
        }
    };
}
