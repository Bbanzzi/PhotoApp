package com.example.photoapp.PlanMain.PlanWork;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoapp.PlanList.PlanItem;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

public class PlanMainPhotoUpload {
    private static final String TAG="PhotoDelete";

    //삭제 동기화를 위해 후에 불러오는 것
    public interface OnUploadPhotoDataListener {
        public void onSuccess(int days, long time , String Id);
        public void onFailed(DatabaseError databaseError);
    }
    public static void addUploadChildEventListener(DatabaseReference databaseReference, PlanItem planItem, ChildEventListener childEventListener, OnUploadPhotoDataListener onUploadPhotoDataListener){
        int dayLength= String.valueOf(planItem.getPlanDates()).length();
        childEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    String time=snapshot.getKey();
                    String photoId=snapshot.getValue(String.class);
                    onUploadPhotoDataListener.onSuccess(Integer.parseInt(time.substring(0,dayLength)), Long.parseLong(time.substring(dayLength)) , photoId);
                    Log.i(TAG, time.indexOf(0,dayLength) + "time : " + Long.parseLong(time.substring(dayLength)));
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
                onUploadPhotoDataListener.onFailed(error);
                Log.i(TAG, "Errored");
            }
        };

        databaseReference.addChildEventListener(childEventListener);

    }

    public static void removeUploadChildEventListener(DatabaseReference databaseReference, ChildEventListener childEventListener){
        databaseReference.removeEventListener(childEventListener);
    }
}
