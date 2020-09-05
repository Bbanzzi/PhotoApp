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

    public PhotoRequestSupplier(Context context, PlanItem planItem,  GooglePhotoReference googlePhotoReference){
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
        // 이건 확신 못하겠음;
        //long startTime = System.nanoTime();
        // google Photo의 사진 time이 어떤 것을 기준으로 작성되는지 몰름 어떻게 확인하지?
        Long nowDatesLong=startDates.getTimeInMillis()/1000;
        for( PlanPhotoData planPhotoData :planPhotoDataList){
            int day_index= (int) ((planPhotoData.getCreationTimeLong()-nowDatesLong) / oneDaysSecond);
            photos.get(day_index).add(planPhotoData);
            Log.i(TAG, "Time difference" + (planPhotoData.getCreationTimeLong()-nowDatesLong));
        }
        //long endTime = System.nanoTime();
        //long duration = endTime - startTime;
        //System.out.println("Second run: "+duration);
        return photos;


        // 20.08.31 문제점
        /*
        사진이 시간순대로 알아서 들어간다. 따라서 내가 굳이 뭐하지 않아도 알아서 시간순대로 request댐
        but, 내가 position을 바꾸면 위치가 바뀌어서 순서가 바뀐다. 그럼 밑의 순서대로 넣는 거는 그 사진을 제외하고 넣게 됌
         */
        /*
        Long nowDatesLong=startDates.getTimeInMillis()/1000;
        Long nextDatesLong=nowDatesLong + oneDaysSecond;
        Log.i(TAG, String.valueOf(startDates.getTimeInMillis()) + String.valueOf(nextDatesLong));

        //시간 순서대로 읽어올 때만 적용됌
        int index=0;
        for(int i=0; i<planPhotoDataList.size() ; i++){
            PlanPhotoData planPhotoData=planPhotoDataList.get(i);
            Long photoTime=planPhotoData.getCreationTimeLong();
            if ( nowDatesLong.compareTo(photoTime) <=0 && nextDatesLong.compareTo(photoTime) > 0 ){
                Log.i(TAG, "days is : " + photoTime );
                test.get(index).add(planPhotoData);
            }else if(nowDatesLong.compareTo(photoTime) >0){
                // 전의 사진은 그냥 넘어감
            }
            else{
                // 처음 다음날의 사진
                index++;
                i--;
                Log.i(TAG, "days is : " + index );
                nowDatesLong +=oneDaysSecond;
                nextDatesLong += oneDaysSecond;
            }
            // days를 넘어가면 종료
            if(index == days ){

                break;
            }
        }
        return test;

         */


    }
}
