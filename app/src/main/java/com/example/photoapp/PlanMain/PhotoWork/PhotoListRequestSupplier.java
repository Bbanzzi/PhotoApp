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
import com.google.photos.library.v1.proto.ListSharedAlbumsResponse;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.ContributorInfo;
import com.google.photos.types.proto.MediaItem;
import com.google.protobuf.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

@RequiresApi(api = Build.VERSION_CODES.N)
public class PhotoListRequestSupplier implements Supplier<List<List<PlanPhotoData>>> {
    @Override
    public List<List<PlanPhotoData>> get() {
        Log.i(TAG, "ASDFASDFASDf0");
        Log.i(TAG, Thread.currentThread().getName());
        getPhotoUrl(planItem.getAlbumId());
        return filterByDate(planItem.putStartDates(),planItem.getPlanDates());
    }

    private static final String TAG="PhotoRequestSupplier";

    private Context context;
    private PlanItem planItem;
    private GooglePhotoReference googlePhotoReference;
    private List<PlanPhotoData> planPhotoDataList= new ArrayList<>();

    public PhotoListRequestSupplier(Context context, PlanItem planItem, GooglePhotoReference googlePhotoReference){
        this.context=context;
        this.planItem=planItem;
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
                ContributorInfo contributorInfo=item.getContributorInfo();
                PlanPhotoData temp = new PlanPhotoData(filename, id,  baseUrl, item.getMediaMetadata().getCreationTime());
                planPhotoDataList.add(temp);
            }

        }catch (ApiException e){
            Log.i(TAG, albumId);
            Log.i(TAG, "failed");
        }
    }

    private List< List < PlanPhotoData>> filterByDate(Calendar startDates, int days) {

        // 자동백업 => 그때 위치에 맞게 Timezone 설정
        // 앱으로 저장 => 그냥 exif GMT 로 설정
        Long oneDaysSecond=86400L;
        List< List < PlanPhotoData>> photos=new ArrayList<List<PlanPhotoData>>();
        for( int i=0; i<days; i++){
            photos.add(new ArrayList<PlanPhotoData>());
        }
        // 나라 or 위치에 따른 timezone
        startDates.setTimeZone(TimeZone.getDefault());
        startDates.set(Calendar.HOUR_OF_DAY, 0);
        startDates.set(Calendar.MINUTE, 0);
        startDates.set(Calendar.SECOND, 0);
        startDates.set(Calendar.MILLISECOND, 0);

        Long nowDatesLong=startDates.getTimeInMillis()/1000;
        for( PlanPhotoData planPhotoData :planPhotoDataList){
            int day_index= (int) ((planPhotoData.getCreationTimeLong()-nowDatesLong) / oneDaysSecond);
            photos.get(day_index).add(planPhotoData);
        }
        return photos;



    }
}
