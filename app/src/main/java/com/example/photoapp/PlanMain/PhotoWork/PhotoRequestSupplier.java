package com.example.photoapp.PlanMain.PhotoWork;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.google.api.gax.rpc.ApiException;
import com.google.common.base.Suppliers;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient;
import com.google.photos.types.proto.MediaItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

@RequiresApi(api = Build.VERSION_CODES.N)
public class PhotoRequestSupplier implements Supplier<List<List<PlanPhotoData>>> {
    @Override
    public List<List<PlanPhotoData>> get() {
        Log.i(TAG, Thread.currentThread().getName());
        getPhotoUrl(planItem.getAlbumId());
        return filterByDate(planItem.putStartDates(),planItem.getPlanDates());
    }

    private static final String TAG="PhotoRequestSupplier";

    private Context context;
    private PlanItem planItem;
    private GooglePhotoReference googlePhotoReference;
    private List<PlanPhotoData> planPhotoDataList= new ArrayList<>();
    private Map<String, String> trashPhotos;

    public PhotoRequestSupplier(Context context, PlanItem planItem, Map<String,String> trashPhotos, GooglePhotoReference googlePhotoReference){
        this.context=context;
        this.planItem=planItem;
        this.trashPhotos=trashPhotos;
        this.googlePhotoReference=googlePhotoReference;
    }

    private void getPhotoUrl(String albumId) {
        try {

            InternalPhotosLibraryClient.SearchMediaItemsPagedResponse response = GooglePhotoReference.getPhotosLibraryClient().searchMediaItems(albumId);

            for (MediaItem item : response.iterateAll()) {
                String id = item.getId();
                String description = item.getDescription();
                String mimeType = item.getMimeType();
                String baseUrl = item.getBaseUrl();
                String productUrl = item.getProductUrl();
                String filename = item.getFilename();
                PlanPhotoData temp = new PlanPhotoData(id,  baseUrl, item.getMediaMetadata().getCreationTime());
                if(trashPhotos.get(id)==null) {
                    planPhotoDataList.add(temp);
                    Log.i(TAG, filename + "Time is " + item.getMediaMetadata().getCreationTime());
                }else{
                    trashPhotos.remove(id);
                    Log.i(TAG, "DeletedFil" + filename + "Time is " + item.getMediaMetadata().getCreationTime());

                }
            }

        }catch (ApiException e){
            Log.i(TAG, albumId);
            Log.i(TAG, "failed");
        }
    }

    private List< List < PlanPhotoData>> filterByDate(Calendar startDates, int days) {

        Long oneDaysSecond=86400L;
        List< List < PlanPhotoData>> test=new ArrayList<List<PlanPhotoData>>();
        for( int i=0; i<days; i++){
            test.add(new ArrayList<PlanPhotoData>());
        }
        startDates.set(Calendar.HOUR_OF_DAY, 0);
        startDates.set(Calendar.MINUTE, 0);
        startDates.set(Calendar.SECOND, 0);
        startDates.set(Calendar.MILLISECOND, 0);
        // 이건 확신 못하겠음;
        // google Photo의 사진 time이 어떤 것을 기준으로 작성되는지 몰름 어떻게 확인하지?
        startDates.setTimeZone(TimeZone.getDefault());

        Long nowDatesLong=startDates.getTimeInMillis()/1000;
        Long nextDatesLong=nowDatesLong + oneDaysSecond;
        Log.i(TAG, String.valueOf(startDates.getTimeInMillis()) + String.valueOf(nextDatesLong));

        //시간 순서대로 읽어올 때만 적용됌
        int index=0;
        for(PlanPhotoData planPhotoData : planPhotoDataList){
            Long photoTime=planPhotoData.getCreationTimeLong();
            if ( nowDatesLong.compareTo(photoTime) <=0 && nextDatesLong.compareTo(photoTime) > 0 ){
                test.get(index).add(planPhotoData);
            }else{
                // 처음 다음날의 사진
                index++;
                test.get(index).add(planPhotoData);

                nowDatesLong +=oneDaysSecond;
                nextDatesLong += oneDaysSecond;
            }
            // days를 넘어가면 종료
            if(index == days ){
                break;
            }
        }
        return test;

        /*
        for( int i=0 ; i<test.size() ; i++){

            Log.i(TAG, "SIZE : " + test.get(i).size());
            for( PlanPhotoData temp : test.get(i)) {
                Log.i(TAG, "Days" + i+1 + "Time" +String.valueOf(temp.getCreationTime()));
            }
        }

         */
    }
}
