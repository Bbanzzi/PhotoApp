package com.example.photoapp.PlanList;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.LoginInfoProvider;
import com.example.photoapp.PlanSchedule.Cell;
import com.example.photoapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreatePlanActivity extends AppCompatActivity{

    private static final String TAG="CreatedActivity";

    private static CustomCalendarDialog customCalendarDialog;
    private static Calendar startDates;
    private static Calendar endDates;
    private static List<Calendar> selectedDays;
    private static int index;
    private Map<String,String> albumInfo;
    private EditText gettitle = null;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private static final int RC_CREATE_PLAN=1005;
    private static int CheckEdit = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createplan);

        setActionBar_create();
        Intent intent = getIntent();
        CheckEdit = intent.getExtras().getInt("CheckEdit");

        final DatabaseReferenceData dbReference=(DatabaseReferenceData) getApplication();
        dbReference.setContext(this);

        TextView startdates=(TextView)findViewById(R.id.textview_startdates);
        final TextView enddates=(TextView)findViewById(R.id.textview_enddates);
        EditText getdest=(EditText)findViewById(R.id.edittext_getdest);
        EditText getpersonnel=(EditText)findViewById(R.id.edittext_getpersonnel);
        ImageButton btn_close = (ImageButton) findViewById(R.id.button_close);

        gettitle=(EditText)findViewById(R.id.edittext_gettitle);
        ImageButton buttonNext=(ImageButton) findViewById(R.id.button_next);

        if(CheckEdit == 1){
            PlanItem planItem_intent = intent.getExtras().getParcelable("planItem");
            String personNum = String.valueOf(planItem_intent.getPlanPersonnel());
            String endDates_str = "~ " + planItem_intent.getEndDates_str();

            gettitle.setText(planItem_intent.getPlanTitle());
            getdest.setText( planItem_intent.getPlanDest());
            getpersonnel.setText(personNum);
            startdates.setText(planItem_intent.getStartDates_str());
            enddates.setText(endDates_str);

        }
        //달력창
        customCalendarDialog= new CustomCalendarDialog(this);
        customCalendarDialog.setCustomCalendarDialogListener(new CustomCalendarDialog.CustomCalendarDialogListener() {
            @Override
            public void onPositiveClicked(Calendar getStartDates, Calendar getEndDates, List<Calendar> getSelectedDays, int getIndex) {
                startDates=getStartDates;
                endDates=getEndDates;
                index = getIndex;
                selectedDays = getSelectedDays;
                String endDates_add = "~ " + setCalendartoStringDates(endDates);
                startdates.setText(setCalendartoStringDates(startDates));
                enddates.setText(endDates_add);
            }

            @Override
            public void onNegativeClicked() {
                finish();
            }
        });

        LinearLayout btn_chooseDate = (LinearLayout) findViewById(R.id.textview_dates);
        btn_chooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customCalendarDialog.show();
            }
        });

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TAG","----check----" + CheckEdit);
                finish();
            }
        });

        //다음
        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(CheckEdit == 0) {
                    getGooglePhotoSharedAlbum();
                    Log.i("TAG","----check is 0----" + CheckEdit);
                }


                if ( gettitle.getText().toString().length()==0) {
                    Toast.makeText(v.getContext().getApplicationContext(),"제목를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if( getdest.getText().toString().length()==0 ){
                    Toast.makeText(v.getContext().getApplicationContext(), "나라를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else if( getpersonnel.getText().toString().length()==0 ){
                    Toast.makeText(v.getContext().getApplicationContext(), "인원를 입력해주세요", Toast.LENGTH_SHORT).show();
                } else
                {
                    try {
                        //AlbumActivity로 정보를 옮김
                        String planTitle=gettitle.getText().toString();
                        String planDest=getdest.getText().toString();
                        int planPersonnel=Integer.parseInt(getpersonnel.getText().toString());
                        String selectedDays_str = transCalendarToStr(selectedDays);

                        PlanItem planItem=new PlanItem(planTitle,planDest,planPersonnel,startDates,endDates,selectedDays_str,index+1);
                        planItem.setAlbumId(albumInfo.get("AlbumId"));
                        planItem.setAlbumTitle(albumInfo.get("AlbumTitle"));
                        planItem.setAlbumSharedToken(albumInfo.get("AlbumSharedToken"));


                        Map<String, Object> userinfo = new HashMap<>();
                        userinfo.put("userName", LoginInfoProvider.getUserName(CreatePlanActivity.this));
                        userinfo.put("userEmail", LoginInfoProvider.getUserEmail(CreatePlanActivity.this));
                        userinfo.put("userUID", LoginInfoProvider.getUserUID(CreatePlanActivity.this));

                        dbReference.getCreateDbPlansRef().setValue(planItem);
                        dbReference.getCreateDbPlanUsersRef().setValue(userinfo);
                        dbReference.getCreateDbUserPlansRef().child(dbReference.getCreateDbPlansRef().getKey()).setValue(planItem);

                        Intent intent = new Intent();
                        intent.putExtra("planItem", planItem);
                        setResult(RC_CREATE_PLAN, intent);
                        finish();

                    } catch(NumberFormatException e){
                        Toast.makeText(v.getContext().getApplicationContext(), "인원에는 숫자만 입력해 주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


    }


    private void setActionBar_create() {

        Toolbar toolbar = findViewById(R.id.toolbar_createPlan);
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //View mToolCustom = LayoutInflater.from(this).inflate(R.layout.actionbar_plansetting,null);
        //actionBar.setCustomView(mToolCustom);
        actionBar.setDisplayShowCustomEnabled(true); //custom된 actionbar를 add
        actionBar.setDisplayShowTitleEnabled(false); //원래 title을 actionbar에서 제거
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        /*
        Toolbar parent = (Toolbar) mToolCustom.getParent();
        parent.setContentInsetsAbsolute(0,0);

        ActionBar.LayoutParams parms = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        actionBar1.setCustomView(mToolCustom,parms);

         */

        ImageButton btn_backTOPlanList = (ImageButton) findViewById(R.id.btn_back_inPlanCreate);
        btn_backTOPlanList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    public String setCalendartoStringDates(Calendar calendar){
        String dates = new SimpleDateFormat("yyyy년 MM월 dd일 EEE요일", Locale.KOREAN).format(calendar.getTimeInMillis());
        return dates;
    }

    public String transCalendarToStr(List<Calendar> selectedDays){
        int sizeDays = selectedDays.size();
        String daysStrList = null;
        for(int i=0;i<sizeDays;i++){
            Calendar cal = selectedDays.get(i);
            String monthday = new SimpleDateFormat("MM.dd",Locale.KOREAN).format(cal.getTimeInMillis());
            daysStrList = daysStrList + monthday;
        }
        return daysStrList;
    }

    //Album 고르는 dialog => 중복된 경우 ?
    //+ 새로만드는 것
    //+ 한계획에는 한개의 앨범만 shared되게 구분짓기?
    public void getGooglePhotoSharedAlbum(){

        final GooglePhotoReference googlePhotoReference=(GooglePhotoReference) getApplication();
        if ( gettitle == null) {
            Toast.makeText(this,"제목를 입력해주세요", Toast.LENGTH_SHORT).show();
            Log.i("Tag","----albumtitle is null---- :");
        }else {
            String albumTitle = gettitle.getText().toString();
            Log.i("Tag","----albumtitle---- :"+albumTitle);
            albumInfo = googlePhotoReference.setAlbumTitle(albumTitle);
        }

        /*
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(CreatePlanActivity.this);
        builderSingle.setTitle("사진들을 저장할 앨범을 새로 만들거나 기존의 앨범을 고르시오");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(CreatePlanActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("새로 만들기");

        GooglePhotoProvider googlePhotoProvider=new GooglePhotoProvider(token);
        final ArrayList<String> albumInfo=googlePhotoProvider.getSharedAlbumInfo();
        final ArrayList<String> albumIdArrayList=new ArrayList<>();
        for (int i=0; i<albumInfo.size() ; i++){
            if(i % 2 ==0) {
                arrayAdapter.add(albumInfo.get(i));
            }
            else {
                albumIdArrayList.add(albumInfo.get(i));
            }
        }

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                albumtitle = arrayAdapter.getItem(which);
                albumId = albumIdArrayList.get(which-1);

                AlertDialog.Builder builderInner = new AlertDialog.Builder(CreatePlanActivity.this);
                builderInner.setMessage(albumtitle);
                sharedalbumtitle.setText(albumtitle);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
        */
    }
}
