package com.example.photoapp.PlanList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.security.acl.Owner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class PlanItem implements Parcelable {

    private final String TAG="CLass planitem<";
    private String planTitle;
    private String planDest;

    private String albumTitle;
    private String albumId;
    private String albumSharedToken;

    private int planPersonnel;
    private int planDates;

    private Calendar startDates;
    private Calendar endDates;

    private long startDatesTimeStamp;
    private long endDatesTimeStamp;
    private String timezone_id;
    private String key;
    private String dynamicLink;

    private String selectedDays;
    private int dayNum;
    private int pos_nation;
    private int galleryCheck;


    private PlanItem(Parcel in) {
        selectedDays = in.readString();
        dayNum = in.readInt();
        planTitle = in.readString();
        planDest = in.readString();
        albumTitle=in.readString();
        albumId=in.readString();
        planPersonnel = in.readInt();
        //galleryCheck = in.readBoolean();
        galleryCheck = in.readInt();
        pos_nation = in.readInt();
        long milliseconds_startDates = in.readLong();
        long milliseconds_endDates = in.readLong();
        timezone_id = in.readString();
        key=in.readString();
        dynamicLink=in.readString();

        startDates = new GregorianCalendar(TimeZone.getTimeZone(timezone_id));
        startDates.setTimeInMillis(milliseconds_startDates);

        endDates = new GregorianCalendar(TimeZone.getTimeZone(timezone_id));
        endDates.setTimeInMillis(milliseconds_endDates);
    }
    //constructor for getvalue from database
    public PlanItem() {};
    PlanItem(String planTitle, String planDest, int planPersonnel, Calendar startDates, Calendar endDates,String selectedDays,int dayNum)
    {
        this.selectedDays=selectedDays;
        this.dayNum=dayNum;
        this.planTitle = planTitle;
        this.planDest =planDest;
        this.albumTitle= albumTitle;
        this.albumId=albumId;
        this.planPersonnel=planPersonnel;
        this.galleryCheck=galleryCheck;
        this.pos_nation=pos_nation;

        this.startDates=startDates;
        this.endDates=endDates;

        this.startDatesTimeStamp=startDates.getTimeInMillis();
        this.endDatesTimeStamp=endDates.getTimeInMillis();
        this.timezone_id=startDates.getTimeZone().getID();
    }

    public void setPlanTitle(String planTitle) { this.planTitle = planTitle; }
    public void setPlanDest(String planDest) { this.planDest = planDest; }
    public void setAlbumTitle(String albumTitle) { this.albumTitle = albumTitle; }
    public void setPlanPersonnel(int planPersonnel) { this.planPersonnel = planPersonnel; }
    public void setPlanDates(int planDates) {this.planDates = planDates;}
    public void setStartDates(long startDatesTimeStamp) { this.startDatesTimeStamp = startDatesTimeStamp; }
    public void setEndDates(long endDatesTimeStamp) { this.endDatesTimeStamp = endDatesTimeStamp; }
    public void setTimezone_id(String timezone_id) {this.timezone_id=timezone_id;}
    public void setKey(String key) {this.key=key;}
    public void setDynamicLink(String dynamicLink) {this.dynamicLink=dynamicLink;}
    public void setAlbumId(String albumId) {this.albumId=albumId;}
    public void setAlbumSharedToken(String albumSharedToken) {this.albumSharedToken=albumSharedToken;}
    public void setSelectedDays(String selectedDays) {this.selectedDays=selectedDays;}
    public void setGalleryCheck(int galleryCheck){this.galleryCheck=galleryCheck;}
    public void setPosNation(int pos_nation){this.pos_nation=pos_nation; }

    void setStartDates(Calendar startDates) { this.startDates = startDates; }
    void setEndDates(Calendar endDates) { this.endDates = endDates; }

    //public 만 database에 저장됨

    public String getPlanTitle(){ return this.planTitle; }
    public String getPlanDest(){
        return this.planDest;
    }
    public String getAlbumTitle() { return this.albumTitle ; }
    public Calendar putStartDates(){ return this.startDates;}
    public Calendar putEndDates(){ return this.endDates;}
    public int getPlanPersonnel(){ return this.planPersonnel;}
    public long getStartDatesTimeStamp(){ return this.startDatesTimeStamp;}
    public long getEndDatesTimeStamp(){ return this.endDatesTimeStamp;}
    public String getTimezone_id(){ return this.timezone_id;}
    public String getKey() {return this.key;}
    public String getDynamicLink() {return this.dynamicLink;}
    public String getAlbumId() {return this.albumId;}
    public String getAlbumSharedToken() {return  this.albumSharedToken;}
    public String getSelectedDays() { return this.selectedDays;}
    public int getGalleryCheck() { return this.galleryCheck; }
    public int getPosNation() {return this.pos_nation; }
    public int getDayNum(){ return this.dayNum;}

    public String getStartDates_str() {
        String startDates_str = getCalendarToStringDates(this.startDates);
        return startDates_str;
    }
    public String getEndDates_str(){
        return getCalendarToStringDates(this.endDates);
    }


    /*
    //Database String 날짜
    public String getStringStartDates(){ return getCalendarToStringDates(this.startDates);}
    public String getStringEndDates(){ return getCalendarToStringDates(this.endDates);}
    */

    //AlbumActivity에 표시
    String getStartNEndDates(){
        String dates=getCalendarToStringDates(this.startDates) + " ~ " +getCalendarToStringDates(this.endDates);
        return dates;
    }


    private String getCalendarToStringDates(Calendar dates){
        String week = new SimpleDateFormat("yyyy년 MM월 dd일 EEE요일", Locale.KOREAN).format(dates.getTimeInMillis());
        return week;
    }

    private String getMonthDates(Calendar dates){
        String Month = new SimpleDateFormat("MM",Locale.KOREAN).format(dates.getTimeInMillis());
        return Month;
    }

    private String getDayDates(Calendar dates){
        String Day = new SimpleDateFormat("dd",Locale.KOREAN).format(dates.getTimeInMillis());
        return Day;
    }

    public void setTimestamptoCalendarDates(){
        this.startDates = new GregorianCalendar(TimeZone.getTimeZone(timezone_id));
        this.startDates.setTimeInMillis(startDatesTimeStamp);

        this.endDates = new GregorianCalendar(TimeZone.getTimeZone(timezone_id));
        this.endDates.setTimeInMillis(endDatesTimeStamp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(selectedDays);
        dest.writeInt(dayNum);
        dest.writeString(planTitle);
        dest.writeString(planDest);
        dest.writeString(albumTitle);
        dest.writeString(albumId);
        dest.writeInt(planPersonnel);
        dest.writeInt(galleryCheck);
        dest.writeInt(pos_nation);
        dest.writeLong(startDates.getTimeInMillis());
        dest.writeLong(endDates.getTimeInMillis());
        dest.writeString(startDates.getTimeZone().getID());
        dest.writeString(key);
        dest.writeString(dynamicLink);

    }


    public static final Creator<PlanItem> CREATOR = new Creator<PlanItem>() {
        @Override
        public PlanItem createFromParcel(Parcel in) {
            return new PlanItem(in);
        }

        @Override
        public PlanItem[] newArray(int size) {
            return new PlanItem[size];
        }
    };


    public int getPlanDates(){
        long start=this.startDates.getTimeInMillis();
        long end=this.endDates.getTimeInMillis();

        return (int) (end-start) / 1000 / (24 * 60 * 60) +1;
    }

}
