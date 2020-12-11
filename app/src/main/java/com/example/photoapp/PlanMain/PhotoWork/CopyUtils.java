package com.example.photoapp.PlanMain.PhotoWork;

import com.example.photoapp.PlanMain.PlanPagerAdapter;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.squareup.okhttp.internal.tls.RealTrustRootIndex;

import java.util.ArrayList;
import java.util.List;

public class CopyUtils {

    public static ArrayList<ArrayList<RealtimeData>> deepCopy(ArrayList<ArrayList<RealtimeData>> original){
        if(original==null) return null;

        ArrayList<ArrayList<RealtimeData>> changed=new ArrayList<>();
        for(int i=0; i<original.size(); i++){
            changed.add(oneDayDeepCopy(original.get(i)));
        }

        return changed;
    }

    public static ArrayList<RealtimeData> oneDayDeepCopy(ArrayList<RealtimeData> original){
        if(original ==null) return null;

        ArrayList<RealtimeData> changed=new ArrayList<>();
        for(int i=0; i<original.size() ; i++){
            changed.add(original.get(i));
            changed.get(i).setPhotoDataList(photoDeepCopy(original.get(i).getPhotoDataList()));
        }

        return changed;
    }


    private static List<PlanPhotoData> photoDeepCopy(List<PlanPhotoData> original){
        if(original==null) return null;

        List<PlanPhotoData> changed=new ArrayList<>();
        for(PlanPhotoData planPhotoData: original){
            changed.add(planPhotoData);
        }

        return changed;
    }
}
