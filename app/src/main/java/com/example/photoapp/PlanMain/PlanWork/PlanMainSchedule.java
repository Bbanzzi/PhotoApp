package com.example.photoapp.PlanMain.PlanWork;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlanMainSchedule {

    private static final String TAG="Schedule Listener";

    private DatabaseReference dbReference;
    private ArrayList< ArrayList<RealtimeData> > realTimeDataArrayList;
    private ChildEventListener childEventListener;

    private OnScheduleReadInterface onScheduleListener;
    public interface OnScheduleReadInterface{
        void onDataAdded();
        void onFailed();
    }

    public PlanMainSchedule(DatabaseReference dbReference, ArrayList< ArrayList<RealtimeData> > realTimeDataArrayList, OnScheduleReadInterface onScheduleListener ) {
        this.dbReference=dbReference;
        this.realTimeDataArrayList=realTimeDataArrayList;
        this.onScheduleListener =onScheduleListener;
    }

    public void registerReadValueEventListener(){
        ValueEventListener valueEventListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot days : snapshot.getChildren()) {
                    String dayString = days.getKey();
                    int day = Character.getNumericValue(dayString.charAt(0));
                    for(DataSnapshot planSchedule : days.getChildren()){
                        RealtimeData realtimeData = planSchedule.getValue(RealtimeData.class);
                        realTimeDataArrayList.get(day-1).add(realtimeData);
                    }
                }
                onScheduleListener.onDataAdded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onScheduleListener.onFailed();
            }
        };

        dbReference.addListenerForSingleValueEvent(valueEventListener);//orderByChild("time").
    }
    /*
    private static OnScheduleReadInterface onScheduleListener;
    public interface OnScheduleReadInterface{
        void onChildAdded();
        void onChildChanged();
        void onChildRemoved();
        void onChildFailed();
    }

    // data 읽어오는 비동기화 작업
    public void registerReadChildEventListener() {

        childEventListener=new ChildEventListener() {
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                int index = Character.getNumericValue(dataSnapshot.getKey().charAt(0));
                Log.i(TAG, "----onChildChanged---- : " + dataSnapshot.getKey() + "  " + index);

                Log.i(TAG, "----Enter onChildChanged else---- : ");
                ArrayList<RealtimeData> addRealTimeData = new ArrayList<>();
                addRealTimeData.add(new RealtimeData());
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RealtimeData realtimeData = snapshot.getValue(RealtimeData.class);
                    addRealTimeData.add(realtimeData);
                }
                realTimeDataArrayList.get(index-1).clear();
                realTimeDataArrayList.get(index-1).addAll(addRealTimeData);
                onScheduleListener.onChildChanged();
                Log.i(TAG, "----onChildchanhed---- : " + dataSnapshot.getKey() );
            }
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                Log.i(TAG, "----onChildAdded---- : " + snapshot.getKey() );
                String dayString = snapshot.getKey();
                int day = Character.getNumericValue(dayString.charAt(0));
                if(snapshot.exists()) {
                    // snapshot 날짜 -> datasnapshot : 계획
                    for (DataSnapshot planSchedule : snapshot.getChildren()) {
                        RealtimeData realtimeData = planSchedule.getValue(RealtimeData.class);
                        realTimeDataArrayList.get(day-1).add(realtimeData);
                    }
                }
                onScheduleListener.onChildAdded();
                Log.i(TAG, "----onChildAdded---- : " + snapshot.getKey() );
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.i(TAG,"----onChildRemoved----" + snapshot.getKey());
                int index = Character.getNumericValue(snapshot.getKey().charAt(0));
                realTimeDataArrayList.get(index-1).remove(1);
                onScheduleListener.onChildRemoved();

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i(TAG,"----onChildMoved----");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                onScheduleListener.onChildFailed();
                Log.i(TAG,"----onCancelled----");
            }
        };

        dbReference.getDbPlanScheduleRef().child(child).//orderByChild("time").
                addChildEventListener(childEventListener);
    }

    public void unregisterReadChildEventListener(){
        dbReference.getDbPlanScheduleRef().child(child).//orderByChild("time").
                    removeEventListener(childEventListener);
    }
    
     */
}
