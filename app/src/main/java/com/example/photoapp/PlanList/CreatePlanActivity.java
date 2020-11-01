package com.example.photoapp.PlanList;

import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.LoginInfoProvider;
import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanSchedule.Cell;
import com.example.photoapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.Result;

public class CreatePlanActivity extends AppCompatActivity{

    private static final String TAG="CreatedActivity";

    private static CustomCalendarDialog customCalendarDialog;
    private static Calendar startDates;
    private static Calendar endDates;
    private static List<Calendar> selectedDays;
    private static int index;
    private Map<String,String> albumInfo;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private static final int RC_CREATE_PLAN=1005;
    private static int CheckEdit = 0;
    private Boolean galleryCheck = false;
    private Boolean radioCheck;
    private static int pos_nation;

    // Dialog를 위한 context -> theme설정
    private ContextThemeWrapper ctx ;

    private TextView startdates;
    private TextView enddates;
    private EditText getdest;
    private EditText getpersonnel;
    private EditText gettitle = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createplan);

        setActionBar_create();
        ctx = new ContextThemeWrapper(this, R.style.AppTheme);
        Intent intent = getIntent();
        CheckEdit = intent.getExtras().getInt("CheckEdit");

        dbReference=(DatabaseReferenceData) getApplication();
        dbReference.setContext(this);

        startdates=(TextView)findViewById(R.id.textview_startdates);
        enddates=(TextView)findViewById(R.id.textview_enddates);
        getdest=(EditText)findViewById(R.id.edittext_getdest);
        getpersonnel=(EditText)findViewById(R.id.edittext_getpersonnel);
        ImageButton btn_close = (ImageButton) findViewById(R.id.button_close);

        gettitle=(EditText)findViewById(R.id.edittext_gettitle);
        ImageButton buttonNext=(ImageButton) findViewById(R.id.button_next);

        if(CheckEdit == 1){
            PlanItem planItem_intent = intent.getExtras().getParcelable("planItem");
            String personNum = String.valueOf(planItem_intent.getPlanPersonnel());
            String endDates_str = "~ " + planItem_intent.getEndDates_str();

            gettitle.setText(planItem_intent.getPlanTitle());
            getdest.setSelection(planItem_intent.getPosNation());
            pos_nation = planItem_intent.getPosNation();
            getpersonnel.setText(personNum);
            startdates.setText(planItem_intent.getStartDates_str());
            enddates.setText(endDates_str);

        }

        pos_nation = 0;
        getdest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos_nation = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "나라를 선택해주세요", Toast.LENGTH_SHORT).show();
            }
        });
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
                    // 앨범을 처음 만들때 Progress circle을 위한 asynctask
                    // task안에도 setplan있음
                    AlbumCreateTask task=new AlbumCreateTask(v);
                    task.execute();
                    Log.i("TAG","----check is 0----" + CheckEdit);
                }else{
                    // 앨범을 수정할때 그냥 수정
                    setPlan(v);
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


    private class AlbumCreateTask extends AsyncTask{

        private CustomProgressCircleDialog dialog;
        private View v;

        public AlbumCreateTask(View v) {
            this.v=v;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            getGooglePhotoSharedAlbum();
            return null;
        }

        @Override
        protected void onPreExecute() {
            dialog=new CustomProgressCircleDialog(ctx);
            dialog.setCancelable(false); // 주변 클릭 터치 시 프로그래서 사라지지 않게 하기
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            dialog.dismiss();
            setPlan(v);
            super.onPostExecute(o);
        }
    }

    public void getGooglePhotoSharedAlbum(){

        final GooglePhotoReference googlePhotoReference=(GooglePhotoReference) getApplication();
        if ( gettitle == null) {
            Toast.makeText(this,"제목를 입력해주세요", Toast.LENGTH_SHORT).show();
            Log.i("Tag","----albumtitle is null---- :");
        }else {
            String albumTitle = gettitle.getText().toString();
            Log.i("Tag","----albumtitle---- :"+albumTitle);
            albumInfo = googlePhotoReference.setAlbumTitle( albumTitle);
        }
    }

    public void setPlan(View v) {
        if (gettitle.getText().toString().length() == 0) {
            Toast.makeText(v.getContext().getApplicationContext(), "제목를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else if (getdest.getText().toString().length() == 0) {
            Toast.makeText(v.getContext().getApplicationContext(), "나라를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else if (getpersonnel.getText().toString().length() == 0) {
            Toast.makeText(v.getContext().getApplicationContext(), "인원를 입력해주세요", Toast.LENGTH_SHORT).show();
        } else {
            try {
                //AlbumActivity로 정보를 옮김
                String planTitle = gettitle.getText().toString();
                String planDest = getdest.getText().toString();
                int planPersonnel = Integer.parseInt(getpersonnel.getText().toString());
                String selectedDays_str = transCalendarToStr(selectedDays);
                int galleryCheck_int=0;
                //if구문 수정
                if(galleryCheck) {
                    galleryCheck_int = 1;
                }
                planItem.setGalleryCheck(galleryCheck_int);
                planItem.setPosNation(pos_nation);

                PlanItem planItem = new PlanItem(planTitle, planDest, planPersonnel, startDates, endDates, selectedDays_str, index + 1);
                planItem.setAlbumId(albumInfo.get("AlbumId"));
                planItem.setAlbumTitle(albumInfo.get("AlbumTitle"));
                planItem.setAlbumSharedToken(albumInfo.get("AlbumSharedToken"));


                Map<String, Object> userinfo = new HashMap<>();
                userinfo.put("userName", LoginInfoProvider.getUserName(CreatePlanActivity.this));
                userinfo.put("userEmail", LoginInfoProvider.getUserEmail(CreatePlanActivity.this));
                userinfo.put("userUID", LoginInfoProvider.getUserUID(CreatePlanActivity.this));

                planItem.setGalleryCheck(galleryCheck_int);
                dbReference.getCreateDbPlansRef().setValue(planItem);
                dbReference.getCreateDbPlanUsersRef().setValue(userinfo);
                dbReference.getCreateDbUserPlansRef().child(dbReference.getCreateDbPlansRef().getKey()).setValue(planItem);
                Intent intent = new Intent();
                intent.putExtra("planItem", planItem);
                setResult(RC_CREATE_PLAN, intent);
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(v.getContext().getApplicationContext(), "인원에는 숫자만 입력해 주세요", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onRadioBtnClicked(View view) {
        radioCheck = ((RadioButton)view).isChecked();
        switch (view.getId()){
            case R.id.radiobtn_auto:
                galleryCheck = false;
            case R.id.radiobtn_gallery:
                galleryCheck = true;
        }
    }
}
