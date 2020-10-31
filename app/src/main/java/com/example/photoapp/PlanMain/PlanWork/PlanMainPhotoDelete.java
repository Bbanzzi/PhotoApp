package com.example.photoapp.PlanMain.PlanWork;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlanMainPhotoDelete {

    private static final String TAG="PhotoDelete";

    //삭제 동기화를 위해 후에 불러오는 것
    public interface OnTrashDataListener {
        public void onSuccess(int days, String fileName, long time);
        public void onFailed(DatabaseError databaseError);
    }
    public static void addDeleteChileEventListener(DatabaseReference databaseReference, PlanItem planItem,ChildEventListener childEventListener, List<Map<String,Long>> trashPhotos, OnTrashDataListener listener){
        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    String timestamp=snapshot.getValue(String.class);
                    int days= Integer.parseInt(timestamp.substring(0, String.valueOf(planItem.getPlanDates()).length()));
                    if(trashPhotos.get(days).get(snapshot.getKey())==null) {
                        trashPhotos.get(days).put(snapshot.getKey(), Long.parseLong(timestamp.substring(String.valueOf(planItem.getPlanDates()).length())));
                        listener.onSuccess(days, snapshot.getKey(), Long.parseLong(timestamp.substring(String.valueOf(planItem.getPlanDates()).length())));
                    }
                }
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
        };

        databaseReference.orderByValue().addChildEventListener(childEventListener);
    }

    public static void removeDeleteChildEventListener(DatabaseReference databaseReference, ChildEventListener childEventListener){
        databaseReference.removeEventListener(childEventListener);
    }

    //처음 삭제를 위해 모두 불러오는 것
    public static List<Map<String,Long>> addDeleteSingleValueEvent(DatabaseReference databaseReference, PlanItem planItem){
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
}
