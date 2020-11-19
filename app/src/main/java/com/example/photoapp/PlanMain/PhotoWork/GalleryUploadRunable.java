package com.example.photoapp.PlanMain.PhotoWork;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.PlanMain.PlanSetting;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.MediaItem;
import com.google.rpc.Code;
import com.google.rpc.Status;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class GalleryUploadRunable implements Runnable {

    private final static String TAG = "GalleryUploadRunable";
    private String[] projection = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
    };
    private Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private Uri gallery_image = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
    private Context context;
    private PlanItem planItem;
    private GooglePhotoReference googlePhotoReference;
    private String planSetting;
    private Intent data;
    private String nameNation;
    private int clipSize;
    private int index=0;
    private List<String> uripaths;
    private String uripath1;
    private List<String> filename = new ArrayList<>();

    // 갤러리에서 사진을 올리는것도 사진 시간 지정이 필요한지
    // 시간 범위 제한 안하는게 좋을꺼같은데
    // 사진 앨범에서 가져오는 과정에서 범위 이외에것 들어가면 어떻게 되는지

    public GalleryUploadRunable(Context context, PlanItem planItem, Intent Data, GooglePhotoReference googlePhotoReference, String nameNation){
        this.context=context;
        this.planItem=planItem;
        this.data=Data;
        this.googlePhotoReference=googlePhotoReference;
        this.nameNation=nameNation;
        planSetting= String.valueOf(planItem.getStartDatesTimeStamp()) + String.valueOf(planItem.getEndDatesTimeStamp()) + planItem.getPlanTitle();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run(){

        Log.i(TAG,"----GalleryUpladRunable run ----");
        if(data.getClipData()!= null){
            Log.i(TAG,"clipdata size : " + data.getClipData().getItemCount());
        }
        //갤러리에서 불러온 사진의 uri들을 uripahts에 저장
        /*clipSize = clipdata.getItemCount();
        uripaths = new ArrayList<>();
        //for(int i=0; i<clipSize;i++){
            Uri imageUri = clipdata.getItemAt(0).getUri();
            uripaths.add(PathUtils.getPath(this.context,imageUri));

            //filename 부여 어떤식으로 할지
            filename.add(UUID.randomUUID().toString() + 0);
            Log.i(TAG,"----gallery uri---- : " + imageUri.toString());
        //}
         */
        getGalleryPhotosAccordingToDate(planItem.putStartDates(),planItem.putEndDates());
       // findGalleryUploadedFiles();

        /*Uri uri1 = data.getData();
        uripath1 = PathUtils.getPath(this.context,uri1);
        //uripaths = new ArrayList<>();
        uripaths.add(uripath1);
        filename.add(UUID.randomUUID().toString() + ".jpg");
        Log.i(TAG,"----gallery uri22---- : " + uripath1);

         */

        if(googlePhotoReference.getAlbumIsWriteable(planItem.getAlbumId())) {
            Log.i(TAG, "writeable check : " + googlePhotoReference.getAlbumIsWriteable(planItem.getAlbumId()));
            UploadRequestGallery();
        }

    }

    private void findGalleryUploadedFiles(){

        //TEst용 삭제
        //PlanSetting.removePhotoUploaded(context,planSetting);
        if(PlanSetting.getPhotoUploaded(context,planSetting) != null){
            index=Integer.parseInt(PlanSetting.getPhotoUploaded(context,planSetting)) + 1 ;
            uripaths=uripaths.subList(index, uripaths.size());
            Log.i(TAG, "findGalleryCheck : " + uripaths.size() );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    public void getGalleryPhotosAccordingToDate(Calendar startDates, Calendar endDates) {

        // Exif 정보는 모든 사진의 시간 GMT 기준으로 생각함
        startDates.setTimeZone(TimeZone.getTimeZone("GMT"));
        startDates.set(Calendar.HOUR_OF_DAY, 0);
        startDates.set(Calendar.MINUTE, 0);
        startDates.set(Calendar.SECOND, 0);
        startDates.set(Calendar.MILLISECOND, 0);

        endDates.setTimeZone(TimeZone.getTimeZone("GMT"));
        endDates.set(Calendar.HOUR_OF_DAY, 23);
        endDates.set(Calendar.MINUTE, 59);
        endDates.set(Calendar.SECOND, 59);
        endDates.set(Calendar.MILLISECOND, 999);

        Long startDatesLong= startDates.getTimeInMillis();
        Long endDatesLong= endDates.getTimeInMillis();

        Log.i(TAG,"Start Dates :"  +  startDatesLong + " End Dates :"+ endDatesLong);
        uripaths=new ArrayList<>();

        int num_data;
        if(data.getClipData() != null){
            num_data = data.getClipData().getItemCount();
        }else{
            num_data = 1;
        }

        List<Uri> uripathCol = new ArrayList<>();
        if(data.getClipData() != null) {
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uriImg = clipData.getItemAt(i).getUri();
                uripathCol.add(uriImg);
            }
        }

        Cursor cur = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                MediaStore.Images.Media.DATE_ADDED + " ASC," +
                        MediaStore.Images.Media._ID + " ASC"
                //MediaStore.Images.Media.DATE_TAKEN + " ASC"
        );


        int count=0;
        int idcolumn = 0;
        if (cur.moveToFirst()) {
            String Id;
            String Date_added;
            String[] FileName;
            Uri uriImage;

            int IdColumn = cur.getColumnIndex(
                    MediaStore.Images.Media._ID);
            int DateColumn= cur.getColumnIndex(MediaStore.Images.Media.DATE_ADDED );
            int FileNameColumn=cur.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            idcolumn = IdColumn;
            do {
                // Get the field values
                Id = cur.getString(IdColumn);
                Date_added = cur.getString(DateColumn);
                FileName = cur.getString(FileNameColumn).split("\\.");
                //Path of Images
                //Uri uriImage = Uri.withAppendedPath(images, String.valueOf(Id));
                if(data.getClipData() == null) {
                    uriImage = data.getData();
                }else{
                    uriImage = data.getClipData().getItemAt(count).getUri();
                }
                try {
                    //InputStream inputStream = context.getContentResolver().openInputStream(uriImage);
                    ExifInterface exif = new ExifInterface(PathUtils.getPath(this.context, uriImage));
                    String uniqueID = UUID.randomUUID().toString();
                    // 나라 or 위치에 따른 timezone
                    exif.setAttribute(ExifInterface.TAG_OFFSET_TIME, nameNation);
                    exif.setAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID, uniqueID);
                    exif.saveAttributes();

                    Log.i(TAG, "DATe_added + " + Date_added  + "photo Time is " + exif.getDateTime() + " Paths " + PathUtils.getPath(this.context, uriImage));

                    if( startDatesLong.compareTo(exif.getDateTime()) <= 0 && endDatesLong.compareTo(exif.getDateTime()) >0 ) {
                        uripaths.add(PathUtils.getPath(this.context, uriImage));
                        filename.add( FileName[0] + "-" + uniqueID + "." + FileName[1] );
                        Log.i(TAG, "photo Time is " + exif.getDateTime() + " Paths " + PathUtils.getPath(this.context, uriImage));
                        count++;
                    }else{
                        Log.i(TAG, "날짜가 다릅니다");
                        Toast.makeText(context,"날짜가 다릅니다",Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if( count == num_data){
                    break;
                }
                // Do something with the values.
            } while (cur.moveToNext() );
        }
        Log.i(TAG, "----count value---- : " + count);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UploadRequestGallery() {
        for (String uripath : uripaths){
            File file_temp = new File(uripath);
            Log.i(TAG,"----gallery uri22---- : " + uripath);

            try(RandomAccessFile file = new RandomAccessFile(file_temp,"r")){
                UploadMediaItemRequest uploadRequest =
                        UploadMediaItemRequest.newBuilder()
                                .setMimeType("image/**")
                                .setDataFile(file)
                                .build();

                PhotosLibraryClient photosLibraryClient = GooglePhotoReference.getPhotosLibraryClient();
                UploadMediaItemResponse uploadResponse = photosLibraryClient.uploadMediaItem(uploadRequest);

                if(uploadResponse.getError().isPresent()){
                    UploadMediaItemResponse.Error error = uploadResponse.getError().get();
                }else{
                    String uploadToken = uploadResponse.getUploadToken().get();
                    NewMediaItem newMediaItem = NewMediaItemFactory
                            .createNewMediaItem(uploadToken,filename.get(index)," ");
                    List<NewMediaItem> newItems = Arrays.asList(newMediaItem);
                    BatchCreateMediaItemsResponse response = photosLibraryClient.batchCreateMediaItems(planItem.getAlbumId(),newItems);
                    for (NewMediaItemResult itemsResponse : response.getNewMediaItemResultsList()){
                        Status status = itemsResponse.getStatus();
                        if(status.getCode() == Code.OK_VALUE){
                            MediaItem createdItem = itemsResponse.getMediaItem();
                            PlanSetting.setPhotoUploaded(context, planSetting,index);
                        }else{
                            //item -> not created. Check the status
                        }
                    }
                }
            }catch (IOException e){

            }
            index++;

        }
    }


}
