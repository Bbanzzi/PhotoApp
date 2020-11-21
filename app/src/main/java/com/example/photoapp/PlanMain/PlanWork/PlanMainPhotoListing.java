package com.example.photoapp.PlanMain.PlanWork;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PhotoWork.PhotoDeleteRequest;
import com.example.photoapp.PlanMain.PhotoWork.PhotoListRequestSupplier;
import com.example.photoapp.PlanMain.PhotoWork.PhotoSortRequest;
import com.example.photoapp.PlanMain.PhotoWork.PhotoUploadRunnable;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.google.common.util.concurrent.FutureCallback;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlanMainPhotoListing {

    private static final String TAG="PlanMainPhotoListing";
    private Context context;
    private PlanItem planItem;
    private DatabaseReferenceData dbReference;
    private GooglePhotoReference googlePhotoReference;
    private List<Map<String,Long>> trashPhotos;

    private OnListingInterface onListingListener;
    public interface OnListingInterface{
        void onListed(List<List<PlanPhotoData>> lists, List<Map<String, Long>> trashPhotos);
        void onSorted();
        void onUpdated();
        void onFailed();
    }

    public PlanMainPhotoListing(Context context, PlanItem planItem, DatabaseReferenceData dbReference, GooglePhotoReference googlePhotoReference, OnListingInterface onListingListener){
        this.context=context;
        this.planItem=planItem;
        this.dbReference=dbReference;
        this.googlePhotoReference=googlePhotoReference;
        this.onListingListener=onListingListener;
    }

    //처음 Listing
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void listingAllPhotos() throws ExecutionException, InterruptedException {

        //PhotoUploadRunnable photoUploadRunnable = new PhotoUploadRunnable(this, planItem, googlePhotoReference);
        PhotoListRequestSupplier photoListRequestSupplier = new PhotoListRequestSupplier(context, planItem, googlePhotoReference);
        //CompletableFuture.runAsync(photoUploadRunnable);
        // 지금은 Thread하나만 이용해서 upload중인데 이거 바꿀생각

        CompletableFuture<List<List<PlanPhotoData>>> photos=CompletableFuture.supplyAsync(photoListRequestSupplier)
                .thenCombine
                        (CompletableFuture.supplyAsync(new Supplier< List<Map<String,Long>>>() {
                            @Override
                            public List<Map<String, Long>> get() {
                                return trashPhotos=PlanMainPhotoDelete.addDeleteSingleValueEvent(
                                        dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()), planItem);
                            }
                        })
                        , new BiFunction<List<List<PlanPhotoData>>, List<Map<String, Long>>, List<List<PlanPhotoData>>>() {
                    @Override
                    public List<List<PlanPhotoData>> apply(List<List<PlanPhotoData>> lists, List<Map<String, Long>> trashPhotos) {
                        PhotoDeleteRequest.deleteFirstRequest(lists, trashPhotos);
                        onListingListener.onListed(lists, trashPhotos);
                        return lists;
                    }
                        });
    }

    //다음 sorting
    public void sortingAllPhotos(ArrayList<ArrayList<RealtimeData>> realTimeDataArrayList, List<List<PlanPhotoData>> photos){
        CompletableFuture.supplyAsync(new Supplier<ArrayList<ArrayList<RealtimeData>>>() {
            @Override
            public ArrayList<ArrayList<RealtimeData>> get() {
                PhotoSortRequest.sortingRequest(planItem,realTimeDataArrayList, photos);
                onListingListener.onSorted();
                return null;
            }
        });
    }

    private ChildEventListener childEventListener;
    public void addDeleteChileEventListener(ArrayList<ArrayList<RealtimeData>> realTimeDataArrayList){
        PlanMainPhotoDelete.addDeleteChileEventListener(dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()), planItem, childEventListener, trashPhotos, new PlanMainPhotoDelete.OnTrashDataListener() {
            @Override
            public void onSuccess(int days, String fileName , long time) {
                PhotoDeleteRequest.deleteOtherRequest(planItem, realTimeDataArrayList, days, fileName, time);
                onListingListener.onUpdated();
            }

            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
    }
    public boolean registerChileEventListener(){
        return childEventListener != null;
    }

    public void removeDeleteChildEventListener(){
        PlanMainPhotoDelete.removeDeleteChildEventListener(dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()), childEventListener);
    }

}


