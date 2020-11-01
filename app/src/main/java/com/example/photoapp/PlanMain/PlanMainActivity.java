package com.example.photoapp.PlanMain;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.Photo.PhotoDownloadRequest;
import com.example.photoapp.PlanMain.PhotoWork.GalleryUploadRunable;
import com.example.photoapp.PlanMain.PhotoWork.PhotoDeleteRequest;
import com.example.photoapp.PlanMain.PlanWork.PlanDynamicLink;
import com.example.photoapp.PlanMain.PlanWork.PlanMainPhotoDelete;
import com.example.photoapp.PlanMain.PlanWork.PlanMainPhotoDownload;
import com.example.photoapp.PlanMain.PlanWork.PlanMainConnection;
import com.example.photoapp.PlanMain.PlanWork.PlanMainPhotoListing;
import com.example.photoapp.PlanMain.PlanWork.PlanMainSchedule;
import com.example.photoapp.PlanSchedule.EditPlanScheduleActivity;
import com.example.photoapp.PlanSchedule.EditPlanScheduleChange;
import com.example.photoapp.PlanSchedule.PlanScheduleActivity;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PlanMainActivity extends AppCompatActivity implements View.OnClickListener, PlanMainPhotoListing.OnListingInterface {

    private static final String TAG = "PlanMainActivity";
    public static final String MIME_TYPE_CONTACT = "vnd.android.cursor.item/vnd.example.contact";

    private static PlanItem planItem;
    private static ViewPager viewPager;
    private static Spinner spinner;
    private PlanPagerAdapter adapter;
    private int days;
    private String selectDays;
    private boolean GALLERY_CHECK_MAIN;

    private ArrayList< ArrayList<RealtimeData> > realTimeDataArrayList=new ArrayList<>();
    private List<List<PlanPhotoData>> planPhotoArrayList;
    private List<Map<String, Long>> trashPhotos;

    public static final int RC_PLAN_MAIN=1007;
    private DatabaseReferenceData dbReference;
    private static DatabaseReferenceData dbReference_2;
    private static DatabaseReference dbDataReference_2;

    public static int day_check = 0;
    public static int time_check;


    NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    private ImageButton btn_back_inPlanMain;
    private ImageButton  planschedulebtn;
    private ImageButton btn_invitePlanItem;
    private ImageButton btn_addGalleryImg;
    private ImageButton btn_deletephoto;
    private ImageButton btn_downloadphoto;
    private ImageButton btn_photoselectmenu;

    public static boolean CHECK_EDIT_MAIN = false;
    public static boolean CHECK_DEL_MAIN = false;
    public static boolean CHECK_CHA_MAIN = false;
    Bundle saveInstanceState_re;
    public static String[] col_day;
    public static String[] col_day_firebase;

    //Snackbar layout
    private ConstraintLayout mainlayout;
    //Download 작업을 위한 Class 객체
    private PlanMainPhotoDownload planMainPhotoDownload;
    //Schedule Dbread를 위한 class 객체
    private PlanMainSchedule planMainSchedule;
    //Photo Listingd을 위한 class + network receiver
    private PlanMainConnection planMainConnection;
    //Photo Listing
    private PlanMainPhotoListing planMainPhotoListing;

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planmain);
        // SnackBar를 위한 layout
        mainlayout =(ConstraintLayout) findViewById(R.id.constraintlayout_planmain);

        saveInstanceState_re = savedInstanceState;

        Intent intent = getIntent();
        planItem = intent.getParcelableExtra("planItem");
        //액션바에 나타내줄 날짜도 intent로 받아야함 -> 캘린더에서 start, end date 외에도 그 사이 날짜도 받아야 할듯

        dbReference=(DatabaseReferenceData) getApplication();
        dbReference.setContext(this);

        days = (int) planItem.getDayNum();
        selectDays = planItem.getSelectedDays();
        if(planItem.getGalleryCheck() == 1){
            GALLERY_CHECK_MAIN = true;
        }else{
            GALLERY_CHECK_MAIN = false;
        }
        col_day = new String[days];
        col_day_firebase = new String[days];
        for (int i = 0; i < days; i++) {
            col_day[i] = selectDays.substring(5 * i + 4, 5 * i + 9);
            col_day_firebase[i] = (i+1) + "일" + col_day[i].replace(".","");
        }

        btn_back_inPlanMain=(ImageButton)findViewById(R.id.btn_back_inPlanMain);
        planschedulebtn=(ImageButton)findViewById(R.id.btn_menu);
        btn_invitePlanItem=(ImageButton) findViewById(R.id.btn_inviteplanitem);
        btn_deletephoto=(ImageButton) findViewById(R.id.btn_deletephoto);
        btn_downloadphoto=(ImageButton) findViewById(R.id.btn_downloadphoto);
        btn_photoselectmenu=(ImageButton) findViewById(R.id.btn_photoselectmenu);
        btn_addGalleryImg=(ImageButton) findViewById(R.id.btn_add_galleryImg);

        btn_back_inPlanMain.setOnClickListener(this);
        planschedulebtn.setOnClickListener(this);
        btn_invitePlanItem.setOnClickListener(this);
        btn_deletephoto.setOnClickListener(this);
        btn_downloadphoto.setOnClickListener(this);
        btn_photoselectmenu.setOnClickListener(this);
        btn_addGalleryImg.setOnClickListener(this);

        for(int i=0; i<days ; i++){
            ArrayList<RealtimeData> EmptyRealTimeData=new ArrayList<RealtimeData>();
            EmptyRealTimeData.add(new RealtimeData());
            realTimeDataArrayList.add(EmptyRealTimeData);
        }

        // 화면 생성
        setActionBar();
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        adapter = new PlanPagerAdapter(getSupportFragmentManager(),planItem, realTimeDataArrayList, 1);
        adapter.setDays(days);
        //뷰 페이저
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                spinner.setSelection(position);
                day_check = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        //다운로드 BroadcastReceiver
        planMainPhotoDownload=new PlanMainPhotoDownload(this);
        planMainPhotoDownload.registerDownloadManagerReceiver();

        //PlanSchedule Listener
        //일단 동기화는 주석처리
        planMainSchedule=new PlanMainSchedule(dbReference.getDbPlanScheduleRef().child(planItem.getKey()), realTimeDataArrayList, new PlanMainSchedule.OnScheduleReadInterface() {
            @Override
            public void onDataAdded() {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed() {

            }
        });
        //Listing 작업
        planMainPhotoListing=new PlanMainPhotoListing(this, planItem,
                dbReference, (GooglePhotoReference)getApplication(),this);

        //PhotoConnection check
        planMainConnection=new PlanMainConnection(this, new PlanMainConnection.OnConnectionListenerInterface() {
            @Override
            public void onConnected() {
                try {
                    planMainPhotoListing.listingAllPhotos();
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailed(boolean Wifi, boolean data, boolean downloadOnlyWIFI) {
                if(data & downloadOnlyWIFI)
                    Toast.makeText(getBaseContext(), "WIFI로만 설정되어있습니다.", Toast.LENGTH_LONG).show();
                else if(!data)
                    Toast.makeText(getBaseContext(), "연결이 끊겨있습니다.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Wifi " + Wifi + "Data" + data + "My setting :" + downloadOnlyWIFI);
            }
        });
        planMainConnection.registerNetworkReceiver();
    }


    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "----onPause main----");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "----onDestroy main----");

        //다운로드 BroadcastReceiver unregister
        planMainPhotoDownload.unregisterDownloadManagerReceiver();
        //PlanSchedule Listener unregister
        //planMainSchedule.unregisterReadChildEventListener();
        //PhotoConnection
        planMainConnection.unregisterNetworkReceiver();

        // deleteChildListener 제거
        if(planMainPhotoListing.registerChileEventListener()) planMainPhotoListing.removeDeleteChildEventListener();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "----onResume main----");
    }

    @Override
    public void onStart () {
        super.onStart();
        //listener 제거?
        Log.i(TAG, "----onStart----");
        planMainSchedule.registerReadValueEventListener();
    }

    // Photo Listing listner에 따른 작업
    @Override
    public void onListed(List<List<PlanPhotoData>> lists, List<Map<String, Long>> trashPhotos) {
        this.trashPhotos=trashPhotos;
        planMainPhotoListing.addDeleteChileEventListener(realTimeDataArrayList);
        planMainPhotoListing.sortingAllPhotos(realTimeDataArrayList, lists);
    }
    @Override
    public void onSorted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onUpdated() {
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onFailed() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_back_inPlanMain:
                onBackPressed();
                break;

            case R.id.btn_menu:
                Intent intent = new Intent( getApplicationContext(), PlanScheduleActivity.class);
                intent.putExtra("planItem",planItem);
                startActivityForResult(intent,RC_PLAN_MAIN);
                break;

            case R.id.btn_inviteplanitem:
                if(planItem.getDynamicLink()!=null){
                    Log.i(TAG,"Dynamic link : " + planItem.getDynamicLink());
                }else{
                    Task<ShortDynamicLink> task= PlanDynamicLink.createDynamicLink(planItem);
                    task.addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                        @Override
                        public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                            Map<String, Object> link=new HashMap<>();
                            link.put("dynamicLink", planItem.getDynamicLink());
                            dbReference.getDbPlansRef().child(planItem.getKey()).updateChildren(link);
                            dbReference.getDbUserPlansRef().child(planItem.getKey()).updateChildren(link);
                        }
                    });
                }
                break;

            case R.id.btn_deletephoto:
                deleteTrashPhotos();
                break;

            case R.id.btn_downloadphoto:
                downloadSelectedPhoto();
                break;

            case R.id.btn_photoselectmenu:
                Log.i(TAG, "not 구현");
                break;

            case R.id.btn_add_galleryImg:
                openGallery();
                break;
        }
    }


    //customActionBar
    private void setActionBar() {

        Toolbar toolbar = findViewById(R.id.toolbar_planMain);
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //custom된 actionbar를 add
        actionBar.setDisplayShowTitleEnabled(false); //원래 title을 actionbar에서 제거
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        setSpinner();

    }
    //setSpinner
    public void setSpinner() {

        List<String> spinnerArray = new ArrayList<String>();
        for (int i = 0; i < days; i++) {
            String day_now = selectDays.substring(5*i+4,5*i+9);
            spinnerArray.add(day_now);

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) this.findViewById(R.id.spinner_day);
        spinner.setAdapter(adapter);
        //넘기거나 바꿀때마다 spinner 표시도 바꿔줌
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

     public void clickGoPlusPlan(View view){
        Intent intent = new Intent(PlanMainActivity.this, EditPlanScheduleActivity.class);
        intent.putExtra("day_check",day_check);
        startActivityForResult(intent,RC_PLAN_MAIN);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK); // 사진 선택
        //Intent intent = new Intent("android.intent.action.MULTIPLE_PICK");
        intent.setType("image/*"); //이미지만 보이게
        //intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        //intent.setAction("android.intent.action.MULTIPLE_PICK");
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),RC_PLAN_MAIN);
        //intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

        //startActivityForResult(intent, RC_PLAN_MAIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PLAN_MAIN){
            if(resultCode == RESULT_OK){
                if(data != null //&& data.getData() != null
                 ){
                    if(data.getClipData() == null){
                        Log.i(TAG, "----다중 선택이 불가능---- :");
                    }
                    Uri uri = data.getData();
                    Log.i(TAG, "----uri of selected img---- :"  + uri.toString());
                    ClipData clipData = data.getClipData();
                    Log.i(TAG, "----number of selected img---- :"  + data.getClipData().getItemCount());
                    //Uri selectedImageUri = clipData.getItemAt(0).getUri();
                    GooglePhotoReference googlePhotoReference=(GooglePhotoReference) getApplication();
                    Context context=this;
                    GalleryUploadRunable galleryUploadRunable = new GalleryUploadRunable(this, planItem, data, googlePhotoReference);
                    Log.i(TAG, "----galleryUploadRunable implement---- :" );
                    CompletableFuture.runAsync(galleryUploadRunable);

                }else{
                    Log.i(TAG, "----사진 데이터가 없음---- :" );
                }
            }
            if(resultCode == EditPlanScheduleChange.RC_EDIT_PLAN_DEL){
                Log.i(TAG,"----onActivityResult:MainDEL----");

                CHECK_DEL_MAIN = true;
                String time_i = String.valueOf(data.getExtras().getInt("time"));
                String day = col_day_firebase[day_check];
                dbReference.getDbPlanScheduleRef().child(planItem.getKey()).child(day).child(time_i).removeValue();


            }
            if(resultCode == EditPlanScheduleChange.RC_EDIT_PLAN_CHA){
                Log.i(TAG,"----onActivityResult:MainCHA----");

                CHECK_CHA_MAIN = true;
                String hourVal = data.getExtras().getString("hourVal");
                String minVal =  data.getExtras().getString("minVal");
                String placeVal = data.getExtras().getString("placeVal");
                String memoVal = data.getExtras().getString("memoVal");
                String spinVal = data.getExtras().getString("spinVal");
                String time_orgin = String.valueOf(data.getExtras().getInt("time"));
                int time_add = 0;
                assert spinVal != null;
                if(spinVal.equals("오전")) {
                    if(Integer.parseInt(hourVal) == 0){ hourVal = "0"; }
                    time_add = Integer.parseInt(hourVal) * 60 + Integer.parseInt(minVal);
                }else{
                    if(Integer.parseInt(hourVal) == 12){ hourVal = "0"; }
                    time_add = (Integer.parseInt(hourVal)+12) * 60 + Integer.parseInt(minVal);
                    hourVal = String.valueOf( Integer.parseInt(hourVal) + 12);
                }
                String day = col_day_firebase[day_check];
                String time_i = String.valueOf(time_add);
                dbReference.getDbPlanScheduleRef().child(planItem.getKey()).child(day).child(time_orgin).removeValue();

                HashMap<String, Object> dataN = new HashMap<>();
                RealtimeData data_edit = new RealtimeData(placeVal, memoVal, hourVal, minVal, day_check + 1);
                dataN.put(String.valueOf(time_add), data_edit);
                dbReference.getDbPlanScheduleRef().child(planItem.getKey()).child(day).updateChildren(dataN);
            }
            if(resultCode == EditPlanScheduleActivity.RC_EDIT_PLAN){
                Log.i(TAG,"----onActivityResult:MainPLAN----");
                CHECK_EDIT_MAIN = true;

                String hourVal = data.getExtras().getString("hourVal");
                String minVal =  data.getExtras().getString("minVal");
                String placeVal = data.getExtras().getString("placeVal");
                String memoVal = data.getExtras().getString("memoVal");
                String spinVal = data.getExtras().getString("spinVal");
                int time_add = 0;
                assert spinVal != null;
                if(spinVal.equals("오전")) {
                    if(Integer.parseInt(hourVal) == 0){ hourVal = "0"; }
                    time_add = Integer.parseInt(hourVal) * 60 + Integer.parseInt(minVal);
                }else{
                    if(Integer.parseInt(hourVal) == 12){ hourVal = "0"; }
                    time_add = (Integer.parseInt(hourVal)+12) * 60 + Integer.parseInt(minVal);
                    hourVal = String.valueOf( Integer.parseInt(hourVal) + 12);
                }

                dbReference_2 = (DatabaseReferenceData) getApplication();
                dbReference_2.setContext(this);
                dbDataReference_2 = dbReference_2.getDbPlanScheduleRef().child(planItem.getKey());
                HashMap<String, Object> dataN = new HashMap<>();
                RealtimeData data_edit = new RealtimeData(placeVal, memoVal, hourVal, minVal, day_check + 1);
                String day = col_day_firebase[day_check];
                dataN.put(String.valueOf(time_add), data_edit);
                dbDataReference_2.child(day).updateChildren(dataN);

            }
        }
    }

    public void showMessage(){
        builder = null;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.createNotificationChannel(
                    new NotificationChannel("channel1","Channel1", NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(this, "channel1");
        }else{
            builder = new NotificationCompat.Builder(this);
        }
        Intent intent = new Intent(this, PlanMainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentTitle("사진 업로드");
        builder.setContentText("사진이 업로드되었습니다.");
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.ic_baseline_add_a_photo_24);
        //알림창 터치시 intent 전달
        //intent를 전달 후 알림을 클릭하면 해당 액티비티로 들어가지는데
        //이때 onCreate부터 실행하게됨
        //다시 사진을 다운받게되므로 수정필요
        //builder.setContentIntent(pendingIntent);
        // notification = builder.build();
        //notificationManager.notify(1, notification);

        Toast.makeText(getApplicationContext(), "message complete", Toast.LENGTH_SHORT).show();

    }

    public void startEditPlanWithClick(String place, String memo, int time){
        Intent intent = new Intent(PlanMainActivity.this, EditPlanScheduleChange.class);
        intent.putExtra("place",place);
        intent.putExtra("memo",memo);
        intent.putExtra("hourmin",time);
        time_check = time;
        startActivityForResult(intent,RC_PLAN_MAIN);
    }

    // check상태를 관리
    private static Boolean checkBoxState=false;
    private onKeyBackPressedListener mOnKeyBackPressedListener;
    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener){
        this.mOnKeyBackPressedListener=listener;
    }

    // Icon들을 바꿈
    public void changeCheckState(Boolean checkState){
        if(!checkState)
            mOnKeyBackPressedListener.onBack(false);
        checkBoxState=checkState;
        if(checkBoxState){
            planschedulebtn.setVisibility(View.GONE);
            btn_invitePlanItem.setVisibility(View.GONE);
            btn_deletephoto.setVisibility(View.VISIBLE);
            btn_downloadphoto.setVisibility(View.VISIBLE);
            btn_photoselectmenu.setVisibility(View.VISIBLE);
        }else{
            planschedulebtn.setVisibility(View.VISIBLE);
            btn_invitePlanItem.setVisibility(View.VISIBLE);
            btn_deletephoto.setVisibility(View.GONE);
            btn_downloadphoto.setVisibility(View.GONE);
            btn_photoselectmenu.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }
    // 뒤로가기 눌렀을 경우 checkbox상태인 경우는 check box만 원래대로 되돌린다.
    @Override
    public void onBackPressed() {
        if(checkBoxState){
            changeCheckState(false);
        }else{
            super.onBackPressed();
        }
    }


    private final static Comparator<RealtimeData> timeComparator = new Comparator<RealtimeData>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(RealtimeData o1, RealtimeData o2) {
            return collator.compare(o1.getTime(),o2.getTime());
        }
    };

    // Download 코드
    private void downloadSelectedPhoto(){

        PhotoDownloadRequest downloadRequest=new PhotoDownloadRequest(this, planItem);
        List<PlanPhotoData> downloadList=new ArrayList<>();
        planMainPhotoDownload.setPhotoDownload(downloadRequest, downloadList);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("알림") ;//제목
        dialog.setMessage("체크된 사진들을 모두 다운로드합니다.\n정말 다운로드하시겠습니까?"); // 메시지

        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(ArrayList<RealtimeData> realtimeDataList: realTimeDataArrayList){
                    for(RealtimeData realtimeData : realtimeDataList){
                        for (PlanPhotoData planPhotoData : realtimeData.getPhotoDataList()) {
                            if (planPhotoData.getCheck()) {
                                //(100+day_index) 최대 3자리가능, 365최대 다시 받아올때 -100
                                downloadList.add(planPhotoData);
                            }
                        }
                    }
                }
                changeCheckState(!checkBoxState);
                //Snackbar
                // Asnyc작업
                planMainPhotoDownload.downloadPhotos(mainlayout);
            }
        });
        dialog.show();
    }

    public void deleteTrashPhotos(){

        Map<String, Object> photos=new HashMap<>();

        androidx.appcompat.app.AlertDialog.Builder dialog = new androidx.appcompat.app.AlertDialog.Builder(this);
        dialog.setTitle("알림") ;//제목
        dialog.setMessage("체크된 사진들을 모두 삭제합니다.\n정말 삭제하시겠습니까?"); // 메시지

        dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int day_index=0;
                for(ArrayList<RealtimeData> realtimeDataList: realTimeDataArrayList){
                    for(RealtimeData realtimeData : realtimeDataList){
                        Iterator<PlanPhotoData> iterator=realtimeData.getPhotoDataList().iterator();
                        while (iterator.hasNext()) {
                            PlanPhotoData planPhotoData = iterator.next();
                            if(planPhotoData.getCheck()){
                                //(100+day_index) 최대 3자리가능, 365최대 다시 받아올때 -100
                                String[] filename =planPhotoData.getFilename().split("\\.");
                                photos.put(filename[0], day_index + String.valueOf(planPhotoData.getCreationTimeLong()));
                                trashPhotos.get(day_index).put(filename[0], Long.parseLong(day_index + String.valueOf(planPhotoData.getCreationTimeLong())));
                                iterator.remove();
                            }
                        }
                    }
                    day_index++;
                }
                dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()).updateChildren(photos);
                changeCheckState(!checkBoxState);
            }
        });
        dialog.show();

    }

}