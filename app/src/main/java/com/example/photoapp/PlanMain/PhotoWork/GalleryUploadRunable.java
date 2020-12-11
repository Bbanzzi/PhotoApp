package com.example.photoapp.PlanMain.PhotoWork;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.service.quicksettings.Tile;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.exifinterface.media.ExifInterface;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanMain.PlanSetting;
import com.example.photoapp.PlanMain.PlanWork.PlanMainSchedule;
import com.example.photoapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.MediaItem;
import com.google.protobuf.Timestamp;
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
    private List<List<PlanPhotoData>> planPhotoArrayList;

    private ConstraintLayout layout;
    // 갤러리에서 사진을 올리는것도 사진 시간 지정이 필요한지
    // 시간 범위 제한 안하는게 좋을꺼같은데
    // 사진 앨범에서 가져오는 과정에서 범위 이외에것 들어가면 어떻게 되는지

    public GalleryUploadRunable(Context context, PlanItem planItem, Intent Data, GooglePhotoReference googlePhotoReference, String nameNation , List<List<PlanPhotoData>> planPhotoArrayList, ConstraintLayout layout){
        this.context=context;
        this.planItem=planItem;
        this.data=Data;
        this.googlePhotoReference=googlePhotoReference;
        this.nameNation=nameNation;
        this.planPhotoArrayList=planPhotoArrayList;
        this.layout=layout;
        planSetting= String.valueOf(planItem.getStartDatesTimeStamp()) + String.valueOf(planItem.getEndDatesTimeStamp()) + planItem.getPlanTitle();
    }
    private onUploadInterface onUploadListener;
    public interface  onUploadInterface{
        void onUploaded(int days, int index, long time, String id);
        void onFailed();
    }

    public onUploadInterface getOnUploadListener() { return onUploadListener; }
    public void setOnUploadListener(onUploadInterface onUploadListener) { this.onUploadListener = onUploadListener; }

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
        showRunningUploadSnackbar();
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

        if(!cancel) {
            showCompleteUploadSnackbar();
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
                    onUploadListener.onFailed();
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
                            Timestamp time=createdItem.getMediaMetadata().getCreationTime();
                            int day=TimeUtils.getDayIndexFromTime(time.getSeconds() , planItem.putStartDates());
                            int index= TimeUtils.getPhotoIndexFromTime(planPhotoArrayList.get(day), time.getSeconds());
                            if(day >=0 && index >= 0 ) {
                                planPhotoArrayList.get(day).add(index,
                                        new PlanPhotoData(createdItem.getFilename(), createdItem.getId(), photosLibraryClient.getMediaItem(itemsResponse.getMediaItem().getId()).getBaseUrl(), time));
                                onUploadListener.onUploaded(day, index, time.getSeconds(), createdItem.getId());
                            }
                            Log.i(TAG, "days : " + day + "index : " + index + createdItem.getFilename());
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

    private Snackbar snackbar;

    //Snackbar
    private void showRunningUploadSnackbar(){
        snackbar= Snackbar.make(layout, " 업로드 중입니다. ", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.setAction("취소", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPhotos();
                snackbar.dismiss();
            }
        });
        ViewGroup contentLay = (ViewGroup) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(context);
        contentLay.addView(item,0);
        snackbar.show();
    }

    private boolean cancel=false;
    // 다운로드를 취소
    //  취소하면 있던것 까지 전부 사라짐
    private void cancelPhotos(){
        cancel=true;
        uripaths.clear();
    }

    private void showCompleteUploadSnackbar(){
        snackbar=Snackbar.make(layout, " 업로드를 완료하였습니다. ", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.setAction("확인", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_check_24, 0, 0, 0);
        textView.setCompoundDrawablePadding(context.getResources().getDimensionPixelOffset(R.dimen.snackbar_check_icon_padding));
        textView.setGravity(Gravity.CENTER);
        snackbar.show();
    }
}
