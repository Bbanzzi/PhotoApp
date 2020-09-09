package com.example.photoapp.PlanMain.Photo;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.example.photoapp.PlanMain.PlanPagerAdapter;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class PhotoMainActivity extends AppCompatActivity implements PhotoFragment.onFullScreenListener{

    private static String TAG="PHotoMainAcitivity";

    private ActionBar actionBar;
    private static boolean isImmersiveMode ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photomain);

        Intent intent = getIntent();
        ArrayList<PlanPhotoData> realTimeDataList = intent.getParcelableArrayListExtra("realTimeDataList");
        int selectedPosition= intent.getIntExtra("position" , -1);

        // 작업공간 많이 차지 할듯 바꿔야함 main 의 날짜별로 정렬되어있는거 가져오면 좋은데
        // realTimeDataList에는 realTiemdata 즉 , 계획아이템도 포함되어있어 실제 사진만 있는 개수와는 차이가 난다.
        int index=0;
        int frontPlaceCnt=0;
        ArrayList<PlanPhotoData> planPhotoDataList= (ArrayList<PlanPhotoData>) realTimeDataList.clone();
        Iterator<PlanPhotoData> iterator=planPhotoDataList.iterator();
        while(iterator.hasNext()){
            if(iterator.next().getImageUrl()==null){
                iterator.remove();
                if( selectedPosition > index){
                    frontPlaceCnt++;
                }
            }
            index++;
        }
        selectedPosition-=frontPlaceCnt;


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager_photomain);

        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar_photomain);
        // status bar padding 겹치는 거 방지
        toolbar.setPadding(0,getStatusBarHeight(),0,0);
        // 왠지 모르겠지만 가림
        toolbar.bringToFront();
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        toolbar.setNavigationOnClickListener(v -> finish());

        PhotoPagerAdapter adapter = new PhotoPagerAdapter(getSupportFragmentManager(), planPhotoDataList, 1);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(selectedPosition);

        // 없애면 status bar, navigation 없앤 부분이 하얀색으로 뜸
        // Translucent status bar and navigation bar
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        //Immersive mode
        hideSystemUI();
        actionBar.hide();
        PhotoFragment.setOnfullScreenListener(this);
        // when click image, the system and menu represented
        int uiOptions=getWindow().getDecorView().getSystemUiVisibility();
        isImmersiveMode= ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

    }

    @Override
    public void onFragmentClick() {
        if(isImmersiveMode){
            showSystemUI();
            actionBar.show();
            isImmersiveMode=false;
        }
        else{
            hideSystemUI();
            actionBar.hide();
            isImmersiveMode=true;
        }
    }
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photomain,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.appbar_menu_delete:
                Toast.makeText(this, "첫번째", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.appbar_menu_download:
                Toast.makeText(this, "두번째", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.appbar_menu_info:
                Toast.makeText(this, "세번째", Toast.LENGTH_SHORT).show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
