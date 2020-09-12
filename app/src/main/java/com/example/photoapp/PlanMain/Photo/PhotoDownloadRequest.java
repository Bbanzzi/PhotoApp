package com.example.photoapp.PlanMain.Photo;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.google.photos.types.proto.ContributorInfo;

import java.io.File;
import java.util.UUID;

public class PhotoDownloadRequest {

    private static String TAG="PhotoDownloadRequest";
    private Context context;
    private PlanItem planItem;
    public PhotoDownloadRequest(Context context,PlanItem planItem) {
        this.context=context;
        this.planItem=planItem;
    }

    public long downloadPhoto(PlanPhotoData planPhotoData){

        File direct =
                new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                        .getAbsolutePath() + File.separator + planItem.getAlbumTitle() + File.separator);

        Log.i(TAG, direct.getAbsolutePath());
        if (!direct.getAbsoluteFile().exists()) {
            direct.mkdir();
            Log.d("test", "dir created for first time");
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(planPhotoData.getImageUrl());
        //  원래 파일이름으로 변환
        String filename_uuid=planPhotoData.getFilename().substring(0,planPhotoData.getFilename().lastIndexOf("."));
        String filename=filename_uuid.substring(0,filename_uuid.length()-37);
        Log.i(TAG, filename );

        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setDescription("다운로드 완료")
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DCIM,
                        File.separator + planItem.getAlbumTitle() + File.separator + filename);
         return dm.enqueue(request);
    }

    private void downloadRequest(){

    }
}
