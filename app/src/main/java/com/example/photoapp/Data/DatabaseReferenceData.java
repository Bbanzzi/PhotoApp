package com.example.photoapp.Data;

import android.app.Application;
import android.content.Context;

import com.example.photoapp.LoginInfoProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseReferenceData extends Application {

    private Context context;
    //for New Plan
    private DatabaseReference dbCreatePlansRef;
    private DatabaseReference dbCreatePlanUsersRef;
    private DatabaseReference dbCreateUserPlansRef;

    //each Reference
    private DatabaseReference dbPlansRef;
    private DatabaseReference dbPlanUsersRef;
    private DatabaseReference dbUserPlansRef;
    private DatabaseReference dbPlanScheduleRef;
    private DatabaseReference dbPlanTrashPhotosRef;
    private DatabaseReference dbPlanUploadPhotoRef;

    public DatabaseReference getCreateDbPlansRef() { return this.dbCreatePlansRef; }
    public DatabaseReference getCreateDbPlanUsersRef() { return this.dbCreatePlanUsersRef; }
    public DatabaseReference getCreateDbUserPlansRef() { return this.dbCreateUserPlansRef; }

    public DatabaseReference getDbPlansRef() { return dbPlansRef; }
    public DatabaseReference getDbPlanUsersRef() { return dbPlanUsersRef; }
    public DatabaseReference getDbUserPlansRef() { return dbUserPlansRef; }
    public DatabaseReference getDbPlanScheduleRef() {return dbPlanScheduleRef;}
    public DatabaseReference getDbPlanTrashPhotosRef() {return  dbPlanTrashPhotosRef;}
    public DatabaseReference getDbPlanUploadPhotoRef() { return dbPlanUploadPhotoRef; }

    public DatabaseReferenceData(){}

    public void setContext(Context context) {
        this.context=context;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        dbCreatePlansRef = database.getReference("Plans").push();
        dbCreatePlanUsersRef = database.getReference("PlanUsers").child(dbCreatePlansRef.getKey()).child(LoginInfoProvider.getUserUID(context));
        dbCreateUserPlansRef = database.getReference("UserPlans").child(LoginInfoProvider.getUserUID(context));

        dbPlansRef = database.getReference("Plans");
        dbPlanUsersRef = database.getReference("PlanUsers");
        dbUserPlansRef = database.getReference("UserPlans").child(LoginInfoProvider.getUserUID(context));
        dbPlanScheduleRef= database.getReference("PlanSchedule");
        dbPlanTrashPhotosRef=database.getReference("PlanTrashPhotos");
        dbPlanUploadPhotoRef =database.getReference( "PlanUploadPhoto");
    }

}
