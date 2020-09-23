package com.example.photoapp.PlanMain;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanMain.PhotoWork.PhotoDeleteRequest;
import com.example.photoapp.PlanMain.PhotoWork.PhotoRequestSupplier;
import com.example.photoapp.PlanMain.PhotoWork.PhotoSortRequest;
import com.example.photoapp.PlanMain.PhotoWork.PhotoUploadRunnable;
import com.example.photoapp.PlanSchedule.Cell;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.protobuf.Timestamp;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public class PlanMainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PlanMainActivity";

    private static PlanItem planItem;
    private static ViewPager viewPager;
    private static Spinner spinner;
    private PlanPagerAdapter adapter;
    private int days;
    private String selectDays;

    private ArrayList< ArrayList<RealtimeData> > realTimeDataArrayList=new ArrayList<>();
    private List<Map<String, Long>> trashPhotos=new ArrayList<>();

    public static final int RC_PLAN_MAIN=1007;
    private DatabaseReferenceData dbReference;
    private static DatabaseReferenceData dbReference_2;
    private static DatabaseReference dbDataReference_2;

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    public static boolean wifiConnected = false;
    public static boolean mobileConnected = false;
    public static boolean refreshDisplay = true;
    public static boolean downLoadOnlyWIFI = true;
    public static boolean messageCheck = false;
    public static int day_check = 0;
    public static int time_check;

    public static String sPref = null;

    private NetworkReceiver receiver = new NetworkReceiver();

    NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    Toolbar toolbar;

    private ImageButton btn_back_inPlanMain;
    private ImageButton  planschedulebtn;
    private ImageButton btn_invitePlanItem;
    private ImageButton btn_deletephoto;
    private ImageButton btn_photoselectmenu;

    private static boolean ReadDbSchedule=false; // firebase 가 늦게 읽었을 경우 대비
    private static boolean ReadDBDeletionFirst=false; // fireabse 가 늦게 읽었을 경우 대비
    private static boolean MyDeletion=false;   // 내가 사진을 지운 경우 firebase에서 다시 읽어오는 것보다 바로 삭제하기
    private static boolean AllListingPhotos=false; // 모든 사진을 처음 다 정렬함

    public static boolean FIRST_READ_MAIN = true;
    public static boolean NOW_READ_MAIN = true;
    public static boolean CHECK_EDIT_MAIN = false;
    public static boolean CHECK_DEL_MAIN = false;
    public static boolean CHECK_CHA_MAIN = false;
    Bundle saveInstanceState_re;
    public static String[] col_day;
    public static String[] col_day_firebase;

    OnGetDataListener onGetDataListener_Main = new OnGetDataListener() {
        @Override
        public void onStart() {

        }

        @Override
        public void onSuccess() {
            ReadDbSchedule=true;
            adapter.notifyDataSetChanged(); // pagerstate의 getitemposition이 실행됨
            Log.i("TAG","----adapter.onSuccess----");
        }

        @Override
        public void onFailed(DatabaseError databaseError) {

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planmain);


        saveInstanceState_re = savedInstanceState;

        //onCreate와 onStart 둘다 하는데 충돌이 없을까?
        updateConnectedFlags();

        Intent intent = getIntent();
        planItem = intent.getParcelableExtra("planItem");
        //액션바에 나타내줄 날짜도 intent로 받아야함 -> 캘린더에서 start, end date 외에도 그 사이 날짜도 받아야 할듯

        dbReference=(DatabaseReferenceData) getApplication();
        dbReference.setContext(this);

        days = (int) planItem.getDayNum();
        selectDays = planItem.getSelectedDays();
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
        btn_photoselectmenu=(ImageButton) findViewById(R.id.btn_photoselectmenu);

        btn_back_inPlanMain.setOnClickListener(this);
        planschedulebtn.setOnClickListener(this);
        btn_invitePlanItem.setOnClickListener(this);
        btn_deletephoto.setOnClickListener(this);
        btn_photoselectmenu.setOnClickListener(this);

        for(int i=0; i<days ; i++){
            ArrayList<RealtimeData> EmptyRealTimeData=new ArrayList<RealtimeData>();
            Map<String, Long> empty=new HashMap<>();
            EmptyRealTimeData.add(new RealtimeData());
            realTimeDataArrayList.add(EmptyRealTimeData);
            trashPhotos.add(empty);
        }


        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkReceiver();
        this.registerReceiver(receiver, filter);

    }


    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "----onPause main----");
        FIRST_READ_MAIN = false;
        NOW_READ_MAIN = false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "----onDestroy main----");
        FIRST_READ_MAIN = true;
        //listener 제거하기기
       if (receiver != null) {
            this.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "----onResume main----");
    }

    @Override
    public void onStart () {
        super.onStart();
        NOW_READ_MAIN = true;
        //listener 제거?
        readPlanSchedule(planItem.getKey());

        readTrashPhotos(new OnTrashDataListener() {
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
                    Log.i(TAG, "doenstaasdfjl");
                }

            }
            @Override
            public void onFailed(DatabaseError databaseError) {

            }
        });
        if(FIRST_READ_MAIN) {
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
        }



        Log.i(TAG, "----onStart main----" + FIRST_READ_MAIN);
        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sPref = sharedPrefs.getString("listPref", "Wi-Fi");

        updateConnectedFlags();

        /*
        if(PlanScheduleActivity.CHECK_ACCESS_PLANSCH){
            DAY_CHECK_firebase = 1;
            Log.i("TAG", "----onRestart Main----");
            PlanScheduleActivity.CHECK_ACCESS_PLANSCH = false;
            onRestart();
        }

         */
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
                invitePlanItem();
                break;
            case R.id.btn_deletephoto:
                deleteSelectedPhoto();
                break;
            case R.id.btn_photoselectmenu:
                Log.i(TAG, "not 구현");
                break;
        }
    }


    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    public void updateConnectedFlags() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }

    }

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    public void loadPage() {

        if ( downLoadOnlyWIFI && wifiConnected
                || ( !downLoadOnlyWIFI && (wifiConnected || mobileConnected ))) {
            // AsyncTask subclass
            //new DownloadXmlTask().execute(URL);
            try {
                listingAllImage();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        } else {
            Log.e("TAG","----loadPage() error in PlanMainActivity.class----");
        }

    }




    //customActionBar
    private void setActionBar() {
        toolbar = findViewById(R.id.toolbar_planMain);
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
    // 아직 kakaotalk으로 공유 + 복사하는것 만들어야함
    public void invitePlanItem(){
        /*
        String url="https://www.example.com/?shared_token="+planItem.getAlbumSharedToken();
        Uri baseUrl = Uri.parse(url);
        final DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //?
                .setLink(baseUrl)
                .setDomainUriPrefix("https://photoapp.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                //.setIosParameters(new DynamicLink.IosParameters.Builder("com.example.ios").build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.i(TAG,"Dynamic link : " +  dynamicLinkUri);
         */
        //Dynamic Link를 한번이라도 생성했으면 받아오고 아니면 새로 만들어서 저장
        //밑의 dbPlanReference는 지금 해당되는 plan에 대한 path로 새로 만듬
        if(planItem.getDynamicLink()!=null){
            Log.i(TAG,"Dynamic link : " + planItem.getDynamicLink());
        }else{
            createDynamicLink();
        }
    }

    public void createDynamicLink(){
        // 비밀번호 대용으로 이용
        String url="https://www.example.com/?planKey="+planItem.getKey();
        Uri baseUrl = Uri.parse(url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(baseUrl)
                .setDomainUriPrefix("https://prototypephotoapp.page.link")

                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Set parameters
                // ...
                .buildShortDynamicLink() //ShortDynamicLink.Suffix.SHORT 안에 넣어서 길이 조절가능
                .addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Map<String, Object> link=new HashMap<>();
                            link.put("dynamicLink",shortLink.toString());

                            dbReference.getDbPlansRef().child(planItem.getKey()).updateChildren(link);
                            dbReference.getDbUserPlansRef().child(planItem.getKey()).updateChildren(link);

                            planItem.setDynamicLink(shortLink.toString());
                        } else {
                            Log.e(TAG,"ERRER : Dynamic link faile");
                            // Error
                            // ...
                        }
                    }
                });

    }

    // data 읽어오는 비동기화 작업
    public void readPlanSchedule(String child) {
        onGetDataListener_Main.onStart();

        /*
            realTimeDataArrayList.clear();
            for (int i = 0; i < days; i++) {
                ArrayList<RealtimeData> EmptyRealTimeData = new ArrayList<RealtimeData>();
                EmptyRealTimeData.add(new RealtimeData());
                realTimeDataArrayList.add(EmptyRealTimeData);
            }

         */

        Log.i(TAG, "----readPlanSchedule----");
        dbReference.getDbPlanScheduleRef().child(child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    ReadDbSchedule=true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        dbReference.getDbPlanScheduleRef().child(child).//orderByChild("time").
        addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                int inDex = Character.getNumericValue(dataSnapshot.getKey().charAt(0));
                Log.i(TAG, "----onChildChanged---- : " + dataSnapshot.getKey() + "  " + inDex);

                Log.i(TAG, "----Enter onChildChanged else---- : ");
                ArrayList<RealtimeData> addRealTimeData = new ArrayList<>();
                addRealTimeData.add(new RealtimeData());
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RealtimeData realtimeData = snapshot.getValue(RealtimeData.class);
                    addRealTimeData.add(realtimeData);
                }
                realTimeDataArrayList.get(inDex-1).clear();
                realTimeDataArrayList.get(inDex-1).addAll(addRealTimeData);
                onGetDataListener_Main.onSuccess();

                /*
                if(CHECK_DEL_MAIN || CHECK_CHA_MAIN || CHECK_EDIT_MAIN ) {
                    Log.i(TAG,"----onChildChanged first loop check---- ");
                    if (CHECK_DEL_MAIN || CHECK_CHA_MAIN) {
                        Log.i(TAG, "----onChildChanged 1 Enter----" + CHECK_DEL_MAIN + " : " + CHECK_CHA_MAIN);
                        int i = 0;
                        for (RealtimeData searchData : realTimeDataArrayList.get(day_check)) {
                            if (searchData.getTime() == time_check) {
                                realTimeDataArrayList.get(day_check).remove(i);
                                if (CHECK_DEL_MAIN) {
                                    onGetDataListener_Main.onSuccess();
                                }
                                CHECK_DEL_MAIN = false;
                                break;
                            }
                            i++;
                        }
                    }
                    if (dataSnapshot.exists() && (CHECK_EDIT_MAIN || CHECK_CHA_MAIN)) {
                        Log.i(TAG, "----onChildChanged 2 Enter----" + CHECK_EDIT_MAIN + " : " + CHECK_CHA_MAIN);
                        int i = 1;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            RealtimeData realtimeData = snapshot.getValue(RealtimeData.class);
                            try {
                                if (Integer.parseInt(snapshot.getKey()) != realTimeDataArrayList.get(inDex - 1).get(i).getTime()) {
                                    realTimeDataArrayList.get(inDex - 1).add(i, realtimeData);
                                    onGetDataListener_Main.onSuccess();
                                    CHECK_EDIT_MAIN = false;
                                    CHECK_CHA_MAIN = false;
                                    break;
                                }
                            } catch (IndexOutOfBoundsException e) {
                                CHECK_EDIT_MAIN = false;
                                CHECK_CHA_MAIN = false;
                                realTimeDataArrayList.get(inDex - 1).add(realtimeData);
                                Log.i(TAG, "----outofboundException1---- : ");
                                onGetDataListener_Main.onSuccess();
                            }

                            i++;
                        }

                    }
                }else{
                    Log.i(TAG, "----Enter onChildChanged else---- : ");
                    ArrayList<RealtimeData> addRealTimeData = new ArrayList<>();
                    addRealTimeData.add(new RealtimeData());
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RealtimeData realtimeData = snapshot.getValue(RealtimeData.class);
                        addRealTimeData.add(realtimeData);
                    }
                    realTimeDataArrayList.get(inDex-1).clear();
                    realTimeDataArrayList.get(inDex-1).addAll(addRealTimeData);
                    onGetDataListener_Main.onSuccess();
                }

                 */
            }
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName){
                Log.i(TAG, "----onChildAdded---- : " + snapshot.getKey() );
                String index_check = snapshot.getKey();
                int DAY_CHECK_FIRE = Character.getNumericValue(index_check.charAt(0));
                if(snapshot.exists() && (FIRST_READ_MAIN )) {
                    int i = 1;
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        RealtimeData realtimeData1 = snapshot1.getValue(RealtimeData.class);
                        realTimeDataArrayList.get(DAY_CHECK_FIRE-1).add(realtimeData1);
                        Log.i(TAG, "----check snapshot1 enter---- : " + realTimeDataArrayList.get(DAY_CHECK_FIRE-1).get(i).getPlace() );
                        i++;
                       }
                    onGetDataListener_Main.onSuccess();
                }else{
                    if(realTimeDataArrayList.get(DAY_CHECK_FIRE-1).size() == 1){
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            RealtimeData realtimeData1 = snapshot1.getValue(RealtimeData.class);
                            realTimeDataArrayList.get(DAY_CHECK_FIRE - 1).add(realtimeData1);
                            Log.i(TAG, "----check snapshot1 enter---- : " + realtimeData1.getPlace());
                        }
                        onGetDataListener_Main.onSuccess();
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.i(TAG,"----onChildRemoved----" + snapshot.getKey());
                int inDex = Character.getNumericValue(snapshot.getKey().charAt(0));
                realTimeDataArrayList.get(inDex-1).remove(1);
                onGetDataListener_Main.onSuccess();

            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.i(TAG,"----onChildMoved----");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                onGetDataListener_Main.onFailed(databaseError);
                Log.i(TAG,"----onCancelled----");
            }
        });
    }

     public void clickGoPlusPlan(View view){
        Intent intent = new Intent(PlanMainActivity.this, EditPlanScheduleActivity.class);
        intent.putExtra("day_check",day_check);
        startActivityForResult(intent,RC_PLAN_MAIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PLAN_MAIN){
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



    public interface OnGetDataListener {
        public void onStart();
        public void onSuccess();
        public void onFailed(DatabaseError databaseError);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void listingAllImage() throws ExecutionException, InterruptedException {

        GooglePhotoReference googlePhotoReference=(GooglePhotoReference) getApplication();
        Context context=this;
        PhotoUploadRunnable photoUploadRunnable = new PhotoUploadRunnable(this, planItem, googlePhotoReference);
        PhotoRequestSupplier photoRequestSupplier = new PhotoRequestSupplier(context, planItem, googlePhotoReference);
        CompletableFuture.runAsync(photoUploadRunnable);
        // 지금은 Thread하나만 이용해서 upload중인데 이거 바꿀생각

        if ( downLoadOnlyWIFI && wifiConnected
                || ( !downLoadOnlyWIFI && (wifiConnected || mobileConnected ))) {

            CompletableFuture.supplyAsync(photoRequestSupplier)
                    .thenApply(new Function<List<List<PlanPhotoData>>, List<List<PlanPhotoData>>>() {
                        @Override
                        public List<List<PlanPhotoData>> apply(List<List<PlanPhotoData>> lists) {
                            Log.i(TAG, Thread.currentThread().getName());
                            //만약 realtimedata가 더 늦게 받아진다면? 그럴일을 거의 없긴함 while(realTimeDataArrayList!=null)
                            while (!ReadDbSchedule) {
                                Log.i(TAG, "Not Yet Read Schedule" + ReadDbSchedule);
                            }
                            // 처음 delete 밑의것이 true로 바뀜에 따라 onSuccess실행
                            ReadDBDeletionFirst = true;
                            PhotoDeleteRequest.DeleteFirstRequest(lists, trashPhotos);
                            PhotoSortRequest.sortingRequest(planItem, realTimeDataArrayList, lists);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AllListingPhotos = true;
                                    adapter.notifyDataSetChanged();
                                }
                            });

                            if (messageCheck = true) {
                                showMessage();
                            }
                            return lists;
                        }
                    });

        }
        /*
        GooglePhotoProvider googlePhotoProvider=new GooglePhotoProvider(token);
        photoBaseUrl = googlePhotoProvider.getPhotoUrl(planItem.getAlbumId());
        // 날짜별로 나누는 것
        // 그 날짜의 11:59:59초
        Calendar dates=planItem.putStartDates();
        dates.set(Calendar.HOUR_OF_DAY, 23);
        dates.set(Calendar.MINUTE, 59);
        dates.set(Calendar.SECOND, 59);
        dates.set(Calendar.MILLISECOND, 999);

        ArrayList< ArrayList< PlanPhotoData > > photoData=new ArrayList<>();
        ArrayList<PlanPhotoData> oneDayData=new ArrayList<>();
        Iterator<PlanPhotoData> it=photoBaseUrl.iterator();
        while(it.hasNext()) {
            if(dates.before(it.next().getCreationTime()) ){
                oneDayData.add(it.next());
            } else{
                photoData.add(oneDayData);
                dates.add(Calendar.DATE, 1);
                oneDayData.clear();
            }
        }
        /*
        while(it.hasNext()) {
            if(dates.getTimeInMillis() > it.next().getCreationTime().getSeconds()*1000 ){
                oneDayData.add(it.next());
            } else{
                photoData.add(oneDayData);
                dates.add(Calendar.DATE, 1);
                oneDayData.clear();
            }
        }


        Log.i(TAG, String.valueOf(photoData.size()));
    }

    public class ExampleRunnable implements Runnable {

        @Override
        public void run() {

        }
    }
    */

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


    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conn = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = conn.getActiveNetworkInfo();

            // Checks the user prefs and the network connection. Based on the result, decides whether
            // to refresh the display or keep the current display.
            // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
            if (WIFI.equals(sPref) && networkInfo != null
                    && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // If device has its Wi-Fi connection, sets refreshDisplay
                // to true. This causes the display to be refreshed when the user
                // returns to the app.
                refreshDisplay = true;
                wifiConnected = true;
                Toast.makeText(context, "wifi connect", Toast.LENGTH_SHORT).show();

                try {
                    listingAllImage();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //text_wifi.setText(wifi_connect);
                //text_download.setText(download_accept);

            }
            else {
                refreshDisplay = false;
                wifiConnected = false;
                Toast.makeText(context, "wifi lost_connection", Toast.LENGTH_SHORT).show();

            }

        }
    }

    private static Boolean checkBoxState=false;
    public void changeCheckState(Boolean checkState){

        checkBoxState=checkState;
        if(checkBoxState){
            planschedulebtn.setVisibility(View.GONE);
            btn_invitePlanItem.setVisibility(View.GONE);
            btn_deletephoto.setVisibility(View.VISIBLE);
            btn_photoselectmenu.setVisibility(View.VISIBLE);
        }else{
            planschedulebtn.setVisibility(View.VISIBLE);
            btn_invitePlanItem.setVisibility(View.VISIBLE);
            btn_deletephoto.setVisibility(View.GONE);
            btn_photoselectmenu.setVisibility(View.GONE);
        }

        adapter.notifyDataSetChanged();
    }


    private void deleteSelectedPhoto(){

        Map<String, Object> photos=new HashMap<>();
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
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
                                iterator.remove();
                            }
                        }
                    }
                    day_index++;
                }
                dbReference.getDbPlanTrashPhotosRef().child(planItem.getKey()).updateChildren(photos);
                mOnKeyBackPressedListener.onBack(true);
                changeCheckState(!checkBoxState);
                MyDeletion=true;
                //adapter.notifyDataSetChanged();
            }
        });
        dialog.show();
    }

    private interface OnTrashDataListener {
        public void onSuccess();
        public void onFailed(DatabaseError databaseError);
    }

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

    public interface onKeyBackPressedListener {
        public void onBack(Boolean checkBoxState);
    }
    private onKeyBackPressedListener mOnKeyBackPressedListener;

    public void setOnKeyBackPressedListener(onKeyBackPressedListener listner){
        this.mOnKeyBackPressedListener=listner;
    }

    @Override
    public void onBackPressed() {
        if(checkBoxState){
            mOnKeyBackPressedListener.onBack(true);
            changeCheckState(!checkBoxState);
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



}