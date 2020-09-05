package com.example.photoapp.PlanMain.PhotoWork;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanItem;
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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class PhotoUploadRunnable implements Runnable {

    public PhotoUploadRunnable(Context context, PlanItem planItem, GooglePhotoReference googlePhotoReference){
        this.context=context;
        this.planItem=planItem;
        this.googlePhotoReference=googlePhotoReference;
        planSetting= String.valueOf(planItem.getStartDatesTimeStamp()) + String.valueOf(planItem.getEndDatesTimeStamp()) + planItem.getPlanTitle();
    }

    private static final String TAG="Photo Upload";
    // which image properties are we querying
    private String[] projection = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
    };

    // content:// style URI for the "primary" external storage volume
    private Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    //  private String selection=MediaStore.MediaColumns.DATE_TAKEN + ">=? and "+MediaStore.MediaColumns.DATE_TAKEN +"<=?";
    // private String[] selectionArgs;
    private Context context;
    private PlanItem planItem;
    private GooglePhotoReference googlePhotoReference;

    private List<String> filepaths;

    //전에 어디까지 저장하다가 껐는지를 확인하기 위함
    private String planSetting; //TimeStamp와 title합쳐서 이용 다른 방안 있으면 추천?
    private int index=0;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {

        Log.i(TAG, Thread.currentThread().getName());
        getPhotosAccordingToDate(planItem.putStartDates(), planItem.putEndDates());
        findUploadedFiles();
        if (googlePhotoReference.getAlbumIsWriteable(planItem.getAlbumId())) {
            UploadRequest();
        }
    }




    // Make the query.
    // 날짜에 해당하는 사진 전부 정보 뽑기

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    public void getPhotosAccordingToDate(Calendar startDates, Calendar endDates) {

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

        Log.i(TAG, String.valueOf(startDatesLong));
        Log.i(TAG,String.valueOf(startDates.getTime()) + endDates.getTime());
        filepaths=new ArrayList<>();
        // 어플 쓰는 경우 dateTime이 지워짐
        //selectionArgs= new String[]{String.valueOf(startDates.getTimeInMillis()), String.valueOf(endDates.getTimeInMillis())};
        // _ID와 DATE_ADDED순으로 정렬한다 ASC
        // ADDED도 저장해서 여기서도 query 하는 것도 좋은 방법일듯?
        Cursor cur = context.getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                MediaStore.Images.Media.DATE_ADDED + " ASC," +
                        MediaStore.Images.Media._ID + " ASC"
                //MediaStore.Images.Media.DATE_TAKEN + " ASC"
        );

        int count=0;
        if (cur.moveToFirst()) {
            String Id;
            String Date_added;

            int IdColumn = cur.getColumnIndex(
                    MediaStore.Images.Media._ID);
            int DateColumn= cur.getColumnIndex(MediaStore.Images.Media.DATE_ADDED );

            do {
                // Get the field values
                Id = cur.getString(IdColumn);
                Date_added = cur.getString(DateColumn);
                //Path of Images
                Uri uriImage = Uri.withAppendedPath(images, String.valueOf(Id));
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uriImage);
                    ExifInterface exif = new ExifInterface(inputStream);
                    if( startDatesLong.compareTo(exif.getDateTime()) <= 0 && endDatesLong.compareTo(exif.getDateTime()) >0 ) {
                        filepaths.add(PathUtils.getPath(this.context, uriImage));
                        Log.i(TAG, PathUtils.getPath(this.context, uriImage));
                        count++;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Do something with the values.
            } while (cur.moveToNext());
        }
        Log.i(TAG, String.valueOf(count));
    }

    //지난 번에 저장한 곳부터 다시 시작함
    private void findUploadedFiles(){
        //TEst용 삭제
        //PlanSetting.removePhotoUploaded(context,planSetting);
        if(PlanSetting.getPhotoUploaded(context,planSetting) != null){
            index=Integer.parseInt(PlanSetting.getPhotoUploaded(context,planSetting)) + 1 ;
            filepaths=filepaths.subList(index, filepaths.size());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void UploadRequest() {
        for (String filepath : filepaths) {
            File file_temp = new File(filepath);
            try (RandomAccessFile file = new RandomAccessFile(file_temp, "r")) {
                // Create a new upload request
                UploadMediaItemRequest uploadRequest =
                        UploadMediaItemRequest.newBuilder()
                                // The media type (e.g. "image/png")
                                .setMimeType("image/*")
                                // The file to upload
                                .setDataFile(file)
                                .build();
                // Upload and capture the response

                PhotosLibraryClient photosLibraryClient = GooglePhotoReference.getPhotosLibraryClient();
                UploadMediaItemResponse uploadResponse = photosLibraryClient.uploadMediaItem(uploadRequest);

                if (uploadResponse.getError().isPresent()) {
                    // If the upload results in an error, handle it
                    UploadMediaItemResponse.Error error = uploadResponse.getError().get();
                } else {
                    // If the upload is successful, get the uploadToken
                    String uploadToken = uploadResponse.getUploadToken().get();
                    NewMediaItem newMediaItem = NewMediaItemFactory
                            .createNewMediaItem(uploadToken, "test", "test");
                    List<NewMediaItem> newItems = Arrays.asList(newMediaItem);

                    BatchCreateMediaItemsResponse response = photosLibraryClient.batchCreateMediaItems(planItem.getAlbumId(),newItems);
                    for (NewMediaItemResult itemsResponse : response.getNewMediaItemResultsList()) {
                        Status status = itemsResponse.getStatus();
                        if (status.getCode() == Code.OK_VALUE) {
                            // The item is successfully created in the user's library
                            MediaItem createdItem = itemsResponse.getMediaItem();
                            PlanSetting.setPhotoUploaded(context, planSetting, index);
                            Log.i(TAG, String.valueOf(createdItem.getMediaMetadata().getCreationTime()));
                        } else {
                            // The item could not be created. Check the status and try again
                        }
                    }
                    // Use this upload token to create a media item
                }
            } catch (IOException e) {
                // Error accessing the local file
            }

            index++;

        }
    }

}

