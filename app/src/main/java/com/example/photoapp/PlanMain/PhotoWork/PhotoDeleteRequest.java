package com.example.photoapp.PlanMain.PhotoWork;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;
import com.google.protobuf.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

public class PhotoDeleteRequest{

    private static final String TAG = "PhotoDeleteRequest";


    public static void deleteFirstRequest(List<List<PlanPhotoData>> planPhotoDataArrayList , List<Map <String , Long>> trashPhotoDataList) {

        int day_index = 0;
        for (Map <String, Long> trashPhotoList : trashPhotoDataList) {
            Iterator<String> iterator= trashPhotoList.keySet().iterator();
            while(iterator.hasNext()){
                //이게 좀더 느림
                /*
                if(planPhotoDataArrayList.get(day_index).contains(new PlanPhotoData(trashPhoto))){
                    Log.i(TAG, "DEleted");
                }
                */
                String trashPhoto=iterator.next();
                for(int i=0; i<planPhotoDataArrayList.get(day_index).size() ; i++){
                    if(planPhotoDataArrayList.get(day_index).get(i).getFilename().split("\\.")[0].equals(trashPhoto)){
                        planPhotoDataArrayList.get(day_index).remove(i);
                        break;
                    }
                }
            }
            day_index++;
        }
    }

    public static void deleteOtherRequest(PlanItem planItem, ArrayList<ArrayList<RealtimeData>> lists , int days, String filename, long time){

        int realTimeData_index= getRealTimeDataIndex(planItem.putStartDates(), days, lists.get(days), time);

        for( int i=0 ; i<lists.get(days).get(realTimeData_index).getPhotoDataList().size() ; i++){
            Log.i(TAG, lists.get(days).get(realTimeData_index).getPhotoDataList().get(i).getFilename() + filename);
            if(lists.get(days).get(realTimeData_index).getPhotoDataList().get(i).getFilename().split("\\.")[0].equals(filename)){
                lists.get(days).get(realTimeData_index).getPhotoDataList().remove(i);
                break;
            }
        }
    }

    private static int getRealTimeDataIndex(Calendar startDates, int day, ArrayList<RealtimeData> realTimeData, Long trashPhotoTime){

        Long oneDayLong=86400L;

        for(int i=0; i<realTimeData.size() ; i++) {
            Long planTime = startDates.getTimeInMillis()/1000 + day * oneDayLong + realTimeData.get(i).getTime() * 60;
            Long nextPlanTime;
            if (i >= realTimeData.size()-1) {
                nextPlanTime = startDates.getTimeInMillis()/1000 + (day + 1) * oneDayLong;
            } else {
                nextPlanTime = startDates.getTimeInMillis()/1000 + day * oneDayLong + realTimeData.get(i+1).getTime() * 60;
            }

            Log.i(TAG, "Plan TIme : " + planTime + "  nextPlanTime : " + nextPlanTime);

            if (planTime.compareTo(trashPhotoTime ) <= 0 &&
                    nextPlanTime.compareTo(trashPhotoTime ) > 0) {
                Log.i(TAG, "Plan index is " + i);
               return i;
            }
        }
        return 0;
        /*
        if(lastPlan) {

            int days=calendar.get(Calendar.DAY_OF_MONTH);
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

         */

    }
}
