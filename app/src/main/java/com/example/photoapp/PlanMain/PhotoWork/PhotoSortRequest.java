package com.example.photoapp.PlanMain.PhotoWork;

import android.util.Log;


import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class PhotoSortRequest {

    private static final String TAG="SORTING";

    public static void planScheduleChanged(PlanItem planItem, int days, ArrayList<RealtimeData> realTimeDataArrayList, List<PlanPhotoData> lists){
        sortOneDay(planItem,days,realTimeDataArrayList,lists);
    }

    public static void planScheduleAdded(ArrayList<RealtimeData> realTimeDataArrayList){
        for(RealtimeData realtimeData : realTimeDataArrayList){

        }
    }

    public static void planScheduleRemoved(ArrayList<RealtimeData> realTimeDataArrayList, List<PlanPhotoData> lists){
        realTimeDataArrayList.add(0,new RealtimeData());
        for(PlanPhotoData photo: lists){
            realTimeDataArrayList.get(0).getPhotoDataList().add(photo);
        }

        for(int i=1; i<realTimeDataArrayList.size(); i++){
            realTimeDataArrayList.remove(i);
        }
    }


    public static void sortingRequest(PlanItem planItem, ArrayList<ArrayList<RealtimeData>> realTimeDataArrayList, List<List<PlanPhotoData>> lists) {

        //날짜별로 정렬
        //PlanData index
        // 시작날짜
        Calendar startDates = (Calendar) planItem.putStartDates().clone();
        for (int i = 0; i < lists.size(); i++) { // 날짜 days로 바꿔되댐 날짜 for문

            Log.i(TAG, (i+1) + "일쨰");
            int planPhotoData_index = 0;

            List<RealtimeData> realTimeDataList = realTimeDataArrayList.get(i); // 하루 날짜의 모든 계획
            List<PlanPhotoData> planPhotoDataList = lists.get(i); // 하루 날짜의 모든 사진
            Log.i(TAG, "PHotoDATA SIZE : " + planPhotoDataList.size() + " RealTimeDATA Size: " + realTimeDataList.size());

            for( int realTimeData_index = 0 ; realTimeData_index < realTimeDataList.size(); realTimeData_index++) {
                RealtimeData plan = realTimeDataList.get(realTimeData_index); // 계획
                Long planTime = getPlanTime(startDates, plan, i,false); // realtimedate의 Timestamp얻기
                // 다음 계획 시간
                Long nextPlanTime;
                if (realTimeDataList.size() <= (realTimeData_index + 1)) { // 그날의 마지막 계획일때
                    nextPlanTime = getPlanTime(startDates, null ,i, true);
                } else {
                    nextPlanTime = getPlanTime(startDates, realTimeDataList.get( realTimeData_index + 1),i, false);
                }
                Log.i(TAG,"Place index: " + realTimeData_index +" PLACE : " + plan.getPlace() + " Time :" + planTime + " NEXT TIME :" + nextPlanTime);

                // Photo Loop
                for ( ; planPhotoData_index < planPhotoDataList.size() ; planPhotoData_index++) {
                    //사진
                    PlanPhotoData planPhotoData = planPhotoDataList.get(planPhotoData_index);
                    Log.i(TAG, "PHOTO TIME : " + planPhotoData.getCreationTimeLong() + " Photo Index" + planPhotoData_index);

                    // 사진이 계획 사이에 있다면
                    if (planTime.compareTo(planPhotoData.getCreationTimeLong() ) <= 0 &&
                            nextPlanTime.compareTo(planPhotoData.getCreationTimeLong() ) > 0)
                    {
                        plan.getPhotoDataList().add(planPhotoData); //사이에 있을때 저장
                    }
                    else if (planTime.compareTo(planPhotoData.getCreationTimeLong() ) > 0)
                    { // 아예 범위에 안어오는 경우는 일단 넘김
                    }
                    else {  // 다음날의 계획보다 큰경우에는 일단 저장후 다시 realtime for문으로 루픈
                        Log.i(TAG, "NEXT Iterator");
                        break;
                    }
                }
            }
        }
        Log.i(TAG, "Sorting is finished");
    }

    private static Long getPlanTime(Calendar startDates, RealtimeData realTimeData, int realTimeData_index, Boolean lastPlan){
        Calendar calendar=(Calendar) startDates.clone();
        int days=calendar.get(Calendar.DAY_OF_MONTH)+realTimeData_index;
        Log.i(TAG, "days + : " + days);
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
    //날짜안의 사진들을 정렬하는 방법
    private static void sortOneDay(PlanItem planItem, int days, ArrayList<RealtimeData> realTimeDataList, List<PlanPhotoData> lists){

        int planPhotoData_index = 0;
        Calendar startDates = (Calendar) planItem.putStartDates().clone();
        startDates.set(Calendar.DAY_OF_MONTH, startDates.get(Calendar.DAY_OF_MONTH) + days );
        Log.i(TAG, "days" + startDates.get(Calendar.DAY_OF_MONTH));
        for( int realTimeData_index = 0 ; realTimeData_index < realTimeDataList.size(); realTimeData_index++) {
            RealtimeData plan = realTimeDataList.get(realTimeData_index); // 계획
            plan.getPhotoDataList().clear();
            Long planTime = getPlanTime(startDates, plan, 0 ,false); // realtimedate의 Timestamp얻기
            // 다음 계획 시간
            Long nextPlanTime;
            if (realTimeDataList.size() <= (realTimeData_index + 1)) { // 그날의 마지막 계획일때
                nextPlanTime = getPlanTime(startDates, null ,0 ,true);
            } else {
                nextPlanTime = getPlanTime(startDates, realTimeDataList.get( realTimeData_index + 1), 0 ,false);
            }
            Log.i(TAG,"Place index: " + realTimeData_index +" PLACE : " + plan.getPlace() + " Time :" + planTime + " NEXT TIME :" + nextPlanTime);

            // Photo Loop
            for ( ; planPhotoData_index < lists.size() ; planPhotoData_index++) {
                //사진
                PlanPhotoData planPhotoData = lists.get(planPhotoData_index);
                Log.i(TAG, "PHOTO TIME : " + planPhotoData.getCreationTimeLong() + " Photo Index" + planPhotoData_index);

                // 사진이 계획 사이에 있다면
                if (planTime.compareTo(planPhotoData.getCreationTimeLong() ) <= 0 &&
                        nextPlanTime.compareTo(planPhotoData.getCreationTimeLong() ) > 0)
                {
                    plan.getPhotoDataList().add(planPhotoData); //사이에 있을때 저장
                }
                else if (planTime.compareTo(planPhotoData.getCreationTimeLong() ) > 0)
                { // 아예 범위에 안어오는 경우는 일단 넘김
                }
                else {  // 다음날의 계획보다 큰경우에는 일단 저장후 다시 realtime for문으로 루픈
                    Log.i(TAG, "NEXT Iterator");
                    Log.i(TAG, "size:" + plan.getPhotoDataList().size());
                    break;
                }
            }
        }
    }

    public static void sortOnePhoto(){

    }
}
