package com.example.photoapp.PlanMain.PhotoWork;

import android.util.Log;

import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class TimeUtils {
    private static String TAG="TimeUtils";

    public static int getDayIndexFromTime(long time, Calendar startDates){
        Long oneDaysSecond=86400L;
        // 나라 or 위치에 따른 timezone
        Calendar dates= (Calendar) startDates.clone();
        dates.setTimeZone(TimeZone.getDefault());
        dates.set(Calendar.HOUR_OF_DAY, 0);
        dates.set(Calendar.MINUTE, 0);
        dates.set(Calendar.SECOND, 0);
        dates.set(Calendar.MILLISECOND, 0);

        Long nowDatesLong=dates.getTimeInMillis()/1000;
        int day_index= (int) ((time-nowDatesLong) / oneDaysSecond);
        return day_index;
    }

    public static int getPhotoIndexFromTime(List<PlanPhotoData> planPhotoDataList,long time){
        for(int i=0; i<planPhotoDataList.size() ; ++i){
            if( planPhotoDataList.get(i).getCreationTimeLong() > time){
                return i;
            }
        }
        return planPhotoDataList.size();
    }

    public static List<Integer> getSchedulePhotoIndexFromTime(PlanItem planItem, int days, ArrayList<RealtimeData> realTimeDataList, PlanPhotoData planPhotoData) {

        List<Integer> index=new ArrayList<>();
        Calendar startDates = (Calendar) planItem.putStartDates().clone();
        startDates.set(Calendar.DAY_OF_MONTH, startDates.get(Calendar.DAY_OF_MONTH) + days);
        Log.i(TAG, "days" + startDates.get(Calendar.DAY_OF_MONTH) + realTimeDataList.size());
        for (int realTimeData_index = 0; realTimeData_index < realTimeDataList.size(); realTimeData_index++) {
            RealtimeData plan = realTimeDataList.get(realTimeData_index); // 계획
            Long planTime = getPlanTime(startDates, plan, false); // realtimedate의 Timestamp얻기
            // 다음 계획 시간
            Long nextPlanTime;
            if (realTimeDataList.size() <= (realTimeData_index + 1)) { // 그날의 마지막 계획일때
                nextPlanTime = getPlanTime(startDates, null, true);
            } else {
                nextPlanTime = getPlanTime(startDates, realTimeDataList.get(realTimeData_index + 1),  false);
            }
            Log.i(TAG, "Place index: " + realTimeData_index + " PLACE : " + plan.getPlace() + " Time :" + planTime + " NEXT TIME :" + nextPlanTime);

            if( planTime <= planPhotoData.getCreationTimeLong() && nextPlanTime > planPhotoData.getCreationTimeLong()){
                index.add(realTimeData_index);
                index.add(getPhotoIndexFromTime(plan.getPhotoDataList() , planPhotoData.getCreationTimeLong()));
                plan.getPhotoDataList().add(index.get(1), planPhotoData);

                return index;
            }
        }
        return index;
    }

    private static Long getPlanTime(Calendar startDates, RealtimeData realTimeData, Boolean lastPlan){
        Calendar calendar=(Calendar) startDates.clone();
        int days=calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, days);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeZone(TimeZone.getDefault());
        if(lastPlan) {
            calendar.set(Calendar.DAY_OF_MONTH,days+1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            Long planTime = calendar.getTimeInMillis() / 1000;
            return planTime;
        }else {
            int hour = realTimeData.getTime() / 60;
            int min = realTimeData.getTime() % 60;
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            Long planTime = calendar.getTimeInMillis() / 1000;
            return planTime;
        }
    }
}
