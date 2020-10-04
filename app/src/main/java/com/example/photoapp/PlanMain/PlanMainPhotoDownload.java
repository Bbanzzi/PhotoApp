package com.example.photoapp.PlanMain;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.graphics.drawable.DrawableWrapper;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.photoapp.PlanMain.Photo.PhotoDownloadRequest;
import com.example.photoapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.rey.material.widget.CheckedImageView;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlanMainPhotoDownload {

    private static final String TAG= "PlanMainPhotoDownload";

    private Context context;
    private DownloadManager downloadManager;
    private PhotoDownloadRequest downloadRequest;
    // 선택된 리스트
    private List<PlanPhotoData> downloadList;
    // enqueue후 unique id 삭제할 때 필요
    private List<Long> downloadIdList;
    private int size;

    private Snackbar snackbar;
    private static BroadcastReceiver receiver;
    // 오류 방지
    private static boolean AllEnqueued=false;
    private ConstraintLayout layout;

    public PlanMainPhotoDownload(Context context){
        this.context=context;
        this.downloadManager=(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    //다운로드 전 준비
    public void setPhotoDownload(PhotoDownloadRequest downloadRequest, List<PlanPhotoData> downloadList) {
        this.downloadList=downloadList;
        this.downloadRequest=downloadRequest;
    }

    //Download작업업
   public void downloadPhotos(ConstraintLayout layout){

        if(downloadList.size() > 5) showRunningDownloadSnackbar(layout, true);
        else showRunningDownloadSnackbar(layout, false);

        CompletableFuture.supplyAsync(new Supplier<List<Long>>() {
            @Override
            public List<Long> get() {
                downloadIdList= downloadRequest.downloadPhotoList(downloadList);
                Log.i(TAG, "ASDFASDF");
                if(downloadIdList.isEmpty()) size=0;
                else size=downloadIdList.size();
                AllEnqueued=true;
                return  downloadIdList;
            }
        });
    }

    //Snackbar
    private void showRunningDownloadSnackbar(ConstraintLayout layout, boolean size){
        snackbar=Snackbar.make(layout, " 다운로드 중입니다. ", Snackbar.LENGTH_INDEFINITE);
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
        if(size) snackbar.show();
    }

    // 다운로드를 취소
    //  취소하면 있던것 까지 전부 사라짐
    private void cancelPhotos(){

        downloadList.clear();
        while(!AllEnqueued){
            snackbar.setText("취소중 입니다.");
            snackbar.show();
            try {
                Thread.sleep(500);
                Log.i(TAG, "Others is waited");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!downloadIdList.isEmpty()){
            for(long id: downloadIdList) {
               downloadManager.remove(id);
            }
        }
    }

    private void showCompleteDownloadSnackbar(){
        snackbar.setText("다운로드가 완료되었습니다.");
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

        ViewGroup contentLay = (ViewGroup) textView.getParent();
        contentLay.removeViewAt(0);
        snackbar.show();
    }

    public void registerDownloadManagerReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    if(AllEnqueued) {
                        size--;
                        if (size == 0) showCompleteDownloadSnackbar();
                    }else{
                        Toast.makeText(context, "다운로드 완료", Toast.LENGTH_SHORT).show();
                    }
                    // Do something on download complete
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregisterDownloadManagerReceiver(){
        context.unregisterReceiver(receiver);
    }
}
