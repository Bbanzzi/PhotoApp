package com.example.photoapp.PlanList;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.LoginInfoProvider;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.R;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.photos.library.v1.PhotosLibraryClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//불러오면서 기다리게 만들 장치 필요
//Listener https://onlyfor-me-blog.tistory.com/47
public class PlanListActivity extends AppCompatActivity implements View.OnClickListener, PlanListRecyclerAdatper.OnListItemLongSelectedInterface, PlanListRecyclerAdatper.OnListItemSelectedInterface{

    private static final String TAG = "PlanListActivity";
    private static PhotosLibraryClient photosLibraryClient ;
    private static final int RC_GET_PLAN_TITLE=1003;
    private static final int RC_CREATE_PLAN=1005;
    private static final int RC_PLAN_MAIN=1007;
    private static final int RC_GET_SETTING=1009;
    private int POS_planList = 0;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private static FloatingActionsMenu menuMultipleActions;

    private ArrayList<PlanItem> planItemList;
    private PlanListRecyclerAdatper PlanListRecyclerAdatper;

    private DatabaseReferenceData dbReference;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planlist);


        //구글 인증
        // dynamiclink로 들어왔을때 이것도 미완성
        checkDynamicLink();
        // Firebase database
        dbReference=(DatabaseReferenceData) getApplication();
        dbReference.setContext(this);

        //floating 버튼 setonClickListener
        menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.floatingmenu_planlist);
        FloatingActionButton createPlanBtn = (FloatingActionButton) findViewById(R.id.floatingbtn_createplan);
        FloatingActionButton joinPlanBtn = (FloatingActionButton) findViewById(R.id.floatingbtn_joinplan);
        FloatingActionButton getSetBtn = (FloatingActionButton) findViewById(R.id.floatingbtn_setting);
        menuMultipleActions.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {

            }

            @Override
            public void onMenuCollapsed() {

            }
        });
        createPlanBtn.setOnClickListener(this);
        joinPlanBtn.setOnClickListener(this);
        getSetBtn.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_planlist);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        planItemList=new ArrayList<PlanItem>();
        //Database 에서 plan목록 받아옴
        searchPlanList();

        PlanListRecyclerAdatper = new PlanListRecyclerAdatper(this, planItemList,this,this) ;
        recyclerView.setAdapter(PlanListRecyclerAdatper) ;

        // 각 아이템 간격 주기
        recyclerView.addItemDecoration(new RecyclerDecoration(this));

    }

    // floating button을누른경우
    public void onClick (View view){
        switch (view.getId()) {

            case R.id.floatingbtn_createplan :
                getAlbumTitle();
                break;
            //Join the plan
            // 직접 link 대신 비밀번호처럼 이용
            case R.id.floatingbtn_joinplan :
                joinPlanItem();
                break;

            case R.id.floatingbtn_setting :
                getSettingWindow();
                break;

        }
    }

    public void getSettingWindow(){
        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
        startActivityForResult(intent,RC_GET_SETTING);

    }


    // plan을 만든다 CreatePlanActivity로 이동
    public void getAlbumTitle() {
            Intent intent = new Intent(getApplicationContext(), CreatePlanActivity.class);
            intent.putExtra("CheckEdit",0);
            startActivityForResult(intent, RC_GET_PLAN_TITLE);
    }

    // 만든다음
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_PLAN_TITLE) {
            switch (resultCode) {

                case RC_CREATE_PLAN:

                    /* 데이터가 중복되서 생성되서 끔
                    PlanItem planItem = data.getParcelableExtra("planItem");

                    planItemList.add(planItem);
                    //마지막 날짜를 기준으로 최신으로 정렬
                    Collections.sort(planItemList, new Comparator<PlanItem>() {
                        @Override
                        public int compare(PlanItem o1, PlanItem o2) {
                            return ~o1.getEndDates().compareTo(o2.getEndDates());
                        }
                    });
                    PlanListRecyclerAdatper.notifyDataSetChanged();
*/
                    break;

                default:

                    break;

            }
        }

    }

    @Override
    public void onEditItemSelected(View v, int position){
        //팝업창 생성후 menu를 inflate
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
        getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        POS_planList = position;
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_edit:
                        Intent intent = new Intent(getApplicationContext(), CreatePlanActivity.class);
                        intent.putExtra("planItem", planItemList.get(position));
                        intent.putExtra("CheckEdit",1);
                        intent.putExtra("CheckEdit",1);
                        startActivityForResult(intent, RC_GET_PLAN_TITLE);
                        break;

                    case R.id.menu_delete:
                        String title = planItemList.get(position).getPlanTitle();
                        new AlertDialog.Builder(PlanListActivity.this).setTitle(title + " > 앨범 삭제").setMessage("삭제하시겠습니까?")
                                .setIcon(R.drawable.ic_baseline_delete_24)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dbReference.getDbUserPlansRef().child(planItemList.get(position).getKey()).removeValue();
                                        dbReference.getDbPlanUsersRef().child(planItemList.get(position).getKey()).removeValue();
                                        planItemList.remove(position);
                                        PlanListRecyclerAdatper.notifyDataSetChanged();
                                        Log.i(TAG,"---- onClick 예 ----");
                                        //searchPlanList();
                                        //위에 메소드를 실행하면 이것부터 진행되는듯 + 아래의 log도 나옴 확인도 안눌렀는데
                                        //아마 우선순위가 있는건가
                                        Log.i(TAG,"---- searchPlanList is done ----");
                                        Toast.makeText(getApplication(), "삭제를 완료했습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplication(),"취소하였습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                }).show();
                        break;
                    default: break;
                }

                return false;
            }

        });
        popupMenu.show();
    }



    @Override
    public void onItemSelected(View v, int position) {
        //AlbumRecyclerAdatper.ViewHolder viewHolder = (AlbumRecyclerAdatper.ViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        Intent intent = new Intent(getApplicationContext(), PlanMainActivity.class);
        intent.putExtra("planItem", planItemList.get(position));
        startActivityForResult(intent,RC_PLAN_MAIN);
    }

    @Override
    public void onItemLongSelected(View v, int position) {
        Toast.makeText(this, position + " long clicked", Toast.LENGTH_SHORT).show();
    }

    //firebase my plan search
    // 대기 사용해야됌
    // 한번만? 지속적인 업데이트?
    public void searchPlanList(){

        ChildEventListener childEventListener= new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PlanItem planItem = dataSnapshot.getValue(PlanItem.class);
                planItem.setKey(dataSnapshot.getKey());
                planItem.setTimestamptoCalendarDates();
                planItemList.add(planItem);
                PlanListRecyclerAdatper.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG,"----ChildChanged----");
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.i(TAG,"----ChildRemoved----");
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.i(TAG,"----ChildMoved----");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG,"----Cancelled----");
            }
        };

        dbReference.getDbUserPlansRef().addChildEventListener(childEventListener);
    }

    //Join other plan by dynamic link
    public void joinPlanItem(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("초대 받은 계획의 동적링크를 입력하시오");

        final EditText editText=new EditText(this);
        dialog.setView(editText);

        dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton("Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String dynamiclink=editText.getText().toString();
                addUserData(dynamiclink);
            }
        });
        dialog.show();
    }

    // plan data중 같은 dynamic link를 가지는 것을 찾고 각각추가해준다.
    //+ 이미 있는 경우 생각해야함
    // Userinfo중 Name만 ? Email은 굳이?
    public void addUserData(final String link){
        dbReference.getDbPlansRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot child:dataSnapshot.getChildren()) {
                    if (child.getValue(PlanItem.class).getDynamicLink() != null) {
                        if (child.getValue(PlanItem.class).getDynamicLink().equals(link)) {
                            PlanItem planItem = child.getValue(PlanItem.class);
                            planItem.setTimestamptoCalendarDates();
                            //planItem.setKey(child.getKey());
                            //Log.i(TAG, planItem.getKey());

                            // album id가 같은 album이라도 사람마다 다름
                            // plans 에는 owner의 albumid UserPlan에 사람마다 album id를 넣어주는 것이 좋아보임
                            planItem.setAlbumId(GooglePhotoReference.joinAlbum(planItem.getAlbumSharedToken()));
                            dbReference.getDbPlanUsersRef().child(child.getKey()).child(LoginInfoProvider.getUserUID(PlanListActivity.this))
                                    .setValue(LoginInfoProvider.getUserInfoMap(PlanListActivity.this));
                            dbReference.getDbUserPlansRef().child(child.getKey()).setValue(planItem);

                            break;

                        }
                    }
                }
                Log.i(TAG,link);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    //Link를 타고 앱을 실행했을떄 의 경우
    public void checkDynamicLink(){

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener( this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            pendingDynamicLinkData.getExtensions();
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.i(TAG, String.valueOf(deepLink));
                            addUserDatabyKey(deepLink.getQueryParameter("planKey"));
                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    public void addUserDatabyKey(final String planKey) {
        dbReference.getDbPlansRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.i(TAG, planKey + child.getKey());
                    if (child.getKey().equals(planKey)) {
                        PlanItem planItem = child.getValue(PlanItem.class);
                        planItem.setTimestamptoCalendarDates();
                        //planItem.setKey(child.getKey());
                        //Log.i(TAG, planItem.getKey());

                        // album id가 같은 album이라도 사람마다 다름
                        // plans 에는 owner의 albumid UserPlan에 사람마다 album id를 넣어주는 것이 좋아보임
                        planItem.setAlbumId(GooglePhotoReference.joinAlbum(planItem.getAlbumSharedToken()));
                        dbReference.getDbPlanUsersRef().child(child.getKey()).child(LoginInfoProvider.getUserUID(PlanListActivity.this))
                                .setValue(LoginInfoProvider.getUserInfoMap(PlanListActivity.this));
                        dbReference.getDbUserPlansRef().child(child.getKey()).setValue(planItem);
                        Log.i(TAG, "update");

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
