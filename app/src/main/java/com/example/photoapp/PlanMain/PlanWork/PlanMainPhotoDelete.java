package com.example.photoapp.PlanMain.PlanWork;

import android.renderscript.Sampler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PhotoWork.PhotoDeleteRequest;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlanMainPhotoDelete {

    private static final String TAG="PhotoDelete";

    private interface OnTrashDataListener {
        public void onSuccess();
        public void onFailed(DatabaseError databaseError);
    }
    /*
    private void readTrashPhotos(OnTrashDataListener listener){
        int cnt=0;
        dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()).orderByValue().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    String timestamp=snapshot.getValue(String.class);
                    int days= Integer.parseInt(timestamp.substring(0, String.valueOf(planItem.getPlanDates()).length()));
                    trashPhotos.get(days).put(snapshot.getKey() , Long.parseLong(timestamp.substring(String.valueOf(planItem.getPlanDates()).length())));
                }
                listener.onSuccess();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

     */

    public static List<Map<String,Long>> firstReadTrashPhotos(DatabaseReference databaseReference, PlanItem planItem){
        List<Map<String,Long>> trashPhotos=new ArrayList<>();
        for(int i=0; i<planItem.getPlanDates(); i++){
            trashPhotos.add(new HashMap<>());
        }

        ValueEventListener eventListener= new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                   String timestamp=dataSnapshot.getValue(String.class);
                   // value day_index + timestamp;
                   // day_index : 계획의 날짜가 10일 넘어가면 01 00 이렇게 저장되도록 설정해놨음
                   int days= Integer.parseInt(timestamp.substring(0, String.valueOf(planItem.getPlanDates()).length()));
                   trashPhotos.get(days).put(dataSnapshot.getKey() , Long.parseLong(timestamp.substring(String.valueOf(planItem.getPlanDates()).length())));
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       };

       databaseReference.addListenerForSingleValueEvent(eventListener);

       return trashPhotos;
    }

    /*
    public static void readTrashPhotos(){
        private void readTrashPhotos(PlanMainActivity.OnTrashDataListener listener){
            dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()).orderByValue().addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot.exists()){
                        String timestamp=snapshot.getValue(String.class);
                        int days= Integer.parseInt(timestamp.substring(0, String.valueOf(planItem.getPlanDates()).length()));
                        trashPhotos.get(days).put(snapshot.getKey() , Long.parseLong(timestamp.substring(String.valueOf(planItem.getPlanDates()).length())));
                    }
                    listener.onSuccess();
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                }
                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    readTrashPhotos(new PlanMainActivity.OnTrashDataListener() {
        @Override
        public void onSuccess() {
            //맨 처음 trashphoto 읽지 않을때 or 내가 삭제한것이 아닐때
            if ( ReadDBDeletionFirst & !MyDeletion ){
                CompletableFuture.runAsync(new Runnable() {
                    @Override
                    public void run() {
                        while(!AllListingPhotos){
                            try {
                                Thread.sleep(500);
                                Log.i(TAG, "Others is waited");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        PhotoDeleteRequest.deleteOtherRequest(planItem, realTimeDataArrayList, trashPhotos);
                        Log.i(TAG, "Others is deleted");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });

                    }
                });
            }else{
                // 1. 처음 삭제하는 경우
                // 2. 내가 삭제하는 경우
                // 3. 내가 sorting or deletion중에 누군가가 삭제를 하는경우 => 일단 보류류
                MyDeletion=false;
            }

        }
        @Override
        public void onFailed(DatabaseError databaseError) {

        }
    });

     */
}
