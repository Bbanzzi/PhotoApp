package com.example.photoapp.PlanSchedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evrencoskun.tableview.TableView;
import com.evrencoskun.tableview.listener.ITableViewListener;
import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//Cell에서 place를 setText하는 방법을 수정해야함
//일정 추가했을때 한번에 표시가 안되는점 수정해야함
public class PlanScheduleActivity extends AppCompatActivity{


    public boolean cell_clicked_val = false;
    public final static String[] col_day = {"1일차", "2일차","3일차","4일차","5일차"};
    final static String[] row_time = {"00시","03시","06시","09시","12시","15시","18시","21시"};

    private List<Cell> returnCell = new ArrayList<>();
    private static List<RowHeader> mRowHeaderList;
    private static List<List<Cell>> mCellList;
    private static List<ColumnHeader> mColumnHeaderList;
    private int days_row = 0;
    private List<Cell> rowCell1 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell2 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell3 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell4 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell5 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell6 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell7 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));
    private List<Cell> rowCell8 = new ArrayList<>(Collections.nCopies(5,new Cell("-")));

    private static DatabaseReferenceData dbReference_1;
    private static DatabaseReference dbDataReference_1;

    //private static DatabaseReferenceData
    static PlanScheduleRecyclerAdapter myTableViewAdapter;
    TableView tableView;
    Button btn_to_frag;
    Button btn_to_refresh;
    Button btn_edit_cancel;
    Button btn_edit_accept;
    EditText frag_hour;
    EditText frag_min;
    EditText frag_place;
    EditText frag_memo;
    Toolbar toolbar;
    Animation downUp_animation, upDown_animation;
    static String planitemkey;
    private boolean touch_frag = true;
    public int touch_cell_c = 0;
    public int touch_cell_r = 0;
    public Cell Cell_touch;
    public Cell Cell_edit;
    private static int row_pos = 100;
    private static int col_pos = 100;
    private static int position_cell = 100;
    private static boolean isChanged = false;
    boolean pageopen = false;
    FrameLayout framepage;
    TableView tablepage;
    FrameLayout.LayoutParams params_frame;
    TableView.LayoutParams params_table;
    public static int pos_edit = 0;
    private static final int RC_PlAN_SCH = 1010;

    LinearLayout lin_table;
    LinearLayout lin_frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_first);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initData();
        onCreateView();
        setActionBar_create();



        lin_frag = (LinearLayout) findViewById(R.id.lin_frame);
        lin_table = (LinearLayout) findViewById(R.id.lin_table);

        framepage =(FrameLayout) findViewById(R.id.frame_contain_in);

        downUp_animation = AnimationUtils.loadAnimation(this, R.anim.move_downtoup);
        upDown_animation = AnimationUtils.loadAnimation(this, R.anim.move_uptodown);
        SlidingPageAnimationListener animationListener = new SlidingPageAnimationListener();
        downUp_animation.setAnimationListener(animationListener);
        upDown_animation.setAnimationListener(animationListener);

    }

    private void setActionBar_create() {

        toolbar = findViewById(R.id.toolbar_planSchedule);
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //custom된 actionbar를 add
        actionBar.setDisplayShowTitleEnabled(false); //원래 title을 actionbar에서 제거
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        Intent intent = getIntent();
        PlanItem planItem = intent.getParcelableExtra("planItem");
        String title = planItem.getAlbumTitle();
        ActionBar.LayoutParams parms = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);

        TextView title_planSchedule = (TextView) findViewById(R.id.textview_inPlanSchedule);
        title_planSchedule.setText(title);

        ImageButton btn_backToPlanList = (ImageButton) findViewById(R.id.btn_back_inPlanSchedule);
        btn_backToPlanList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ImageButton btn_refreshSchedule = (ImageButton)findViewById(R.id.btn_refresh_inPlanSchedule);
        btn_refreshSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myTableViewAdapter.setCellItems(mCellList);
            }
        });

    }

    public void onCreateView(){
        tableView = findViewById(R.id.content_container);
        createTableView();
        loadDataAndReceive();
    }

    private void createTableView() {
        myTableViewAdapter = new PlanScheduleRecyclerAdapter();
        tableView.setAdapter(myTableViewAdapter);

        //setting tableview listener
        OTableViewListener oTableViewListener = new OTableViewListener(tableView);
        tableView.setTableViewListener(oTableViewListener);
    }

    private void initData() {

        mColumnHeaderList = new ArrayList<>();
        mRowHeaderList = new ArrayList<>();
        //outer arraylist가 row region, inner arraylist가 col region
        mCellList = new ArrayList<>();

        List<ColumnHeader> columnHeaders = new ArrayList<>();
        for(int i=0; i<5;i++){
            columnHeaders.add(new ColumnHeader(col_day[i]));
        }

        // add data into RowHeaders
        List<RowHeader> rowHeaders= new ArrayList<>();
        for(int i=0; i<8; i++){
            rowHeaders.add(new RowHeader(row_time[i]));
        }

        mColumnHeaderList.addAll(columnHeaders);
        mRowHeaderList.addAll(rowHeaders);

        mCellList.add(rowCell1);mCellList.add(rowCell2);mCellList.add(rowCell3);
        mCellList.add(rowCell4);mCellList.add(rowCell5);mCellList.add(rowCell6);
        mCellList.add(rowCell7);mCellList.add(rowCell8);
    }


    private void loadDataAndReceive(){

        Intent intent = getIntent();
        PlanItem planItem = intent.getParcelableExtra("planItem");
        dbReference_1 = (DatabaseReferenceData) getApplication();
        dbReference_1.setContext(this);
        dbDataReference_1 = dbReference_1.getDbPlanScheduleRef().child(planItem.getKey());
        planitemkey = planItem.getKey();

        readDataFromDB(dbDataReference_1);

        myTableViewAdapter.setColumnHeaderItems(mColumnHeaderList);
        myTableViewAdapter.setRowHeaderItems(mRowHeaderList);
        myTableViewAdapter.setCellItems(mCellList);

    }


    private void readDataFromDB(DatabaseReference refDatabase) {

        readData(refDatabase, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                myTableViewAdapter.setCellItems(mCellList);
            }
            @Override
            public void onStart() {
            }
            @Override
            public void onFailure() {
            }
        });

    }

    public interface OnGetDataListener{
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }


    public void readData(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot_1 : dataSnapshot.getChildren()){
                    for (DataSnapshot snapshot_2 : snapshot_1.getChildren()){
                        RealtimeData dataReal = snapshot_2.getValue(RealtimeData.class);
                        int row_days = dataReal.getDays() - 1;
                        int col_times = dataReal.getTime()/60/3;
                        Cell data_now = mCellList.get(col_times).get(row_days);
                        String before_place = data_now.getData().toString();
                        ArrayList<Integer> before_time_arr = data_now.getTime();
                        ArrayList<String> before_place_arr = data_now.getPlace();
                        int new_time = dataReal.getTime();
                        String new_place = dataReal.getPlace();

                        if (data_now.getData() == "-") {
                            String place = dataReal.getPlace();
                            Cell cellAdd = new Cell(place,place,new_time);
                            mCellList.get(col_times).set(row_days, cellAdd);
                        }
                        else {
                            if(!before_time_arr.contains(new_time)) {
                                String place = before_place + "\n" + new_place;
                                Cell cellAdd = new Cell(place,before_place_arr,before_time_arr);
                                cellAdd.addTime(new_time);
                                cellAdd.addPlace(new_place);
                                mCellList.get(col_times).set(row_days, cellAdd);
                            }
                        }

                    }
                }
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure();
            }
        });

    }


    public class OTableViewListener implements ITableViewListener {
        @NonNull
        private Context mContext;
        @NonNull
        private TableView mTableView;

        public OTableViewListener(@NonNull TableView tableView){
            this.mContext = tableView.getContext();
            this.mTableView = tableView;
        }
        @SuppressLint("ResourceAsColor")
        @Override
        public void onCellClicked(@NonNull RecyclerView.ViewHolder cellView, int columnPosition, int
                rowPosition) {

            FragmentManager manager = getSupportFragmentManager();
            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) lin_table.getLayoutParams();
            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) lin_frag.getLayoutParams();

            if(col_pos == 100 && row_pos == 100){
                col_pos = columnPosition;
                row_pos = rowPosition;
                Cell_touch = mCellList.get(rowPosition).get(columnPosition);
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.move_downtoup,R.anim.move_uptodown)
                        .replace(R.id.frame_contain_in, new PlaceWindowFragment(Cell_touch))
                        .commit();
                framepage.setVisibility(View.VISIBLE);
                params1.weight= 2;
                lin_table.setLayoutParams(params1);
                params2.weight= 1;
                lin_frag.setLayoutParams(params2);


            }else if(col_pos==columnPosition && row_pos == rowPosition) {
                manager.popBackStack();
                framepage.setVisibility(View.GONE);
                params2.weight= 0;
                lin_frag.setLayoutParams(params2);
                col_pos = 100;
                row_pos = 100;
            } else{
                manager.popBackStack();
                col_pos = columnPosition;
                row_pos = rowPosition;
                Cell_touch = mCellList.get(rowPosition).get(columnPosition);
                manager.beginTransaction().replace(R.id.frame_contain_in, new PlaceWindowFragment(Cell_touch))
                        .commit();
            }

        }
        @Override
        public void onCellDoubleClicked(@NonNull RecyclerView.ViewHolder cellView, int columnPosition, int
                rowPosition) {
        }
        @Override
        public void onCellLongPressed(@NonNull RecyclerView.ViewHolder cellView, int column, int row) {
            // Do What you want
        }
        @Override
        public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder columnHeaderView, int
                columnPosition) {
        }
        @Override
        public void onColumnHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder columnHeaderView, int
                columnPosition) {
        }
        @Override
        public void onColumnHeaderLongPressed(@NonNull RecyclerView.ViewHolder columnHeaderView, int
                columnPosition) {
        }
        @Override
        public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder rowHeaderView, int
                rowPosition) {
        }
        @Override
        public void onRowHeaderDoubleClicked(@NonNull RecyclerView.ViewHolder rowHeaderView, int
                rowPosition) {
        }
        @Override
        public void onRowHeaderLongPressed(@NonNull RecyclerView.ViewHolder rowHeaderView, int
                rowPosition) {
        }
    }



    public void clickAddPlan(View view) {
        Intent intent = new Intent(PlanScheduleActivity.this, EditPlanScheduleActivity.class);
        startActivityForResult(intent,RC_PlAN_SCH);
    }




    public void onEditPlan(View view) {
        int id = view.getId();
        int pos = (id - 1000)/4;
        pos_edit = pos;
        ArrayList<Integer> time_val = mCellList.get(row_pos).get(col_pos).getTime();
        ArrayList<String> place_val = mCellList.get(row_pos).get(col_pos).getPlace();
        String place_s = place_val.get(pos);

        if(!place_s.equals("-")) {

            String time_s = time_val.get(pos).toString();

            dbDataReference_1.child(col_day[col_pos]).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.getKey().equals(time_s)) {
                            RealtimeData data_edit = child.getValue(RealtimeData.class);
                            String place = data_edit.getPlace();
                            String memo = data_edit.getMemo();
                            int time_HM = data_edit.getTime();
                            Intent intent = new Intent(PlanScheduleActivity.this, EditPlanScheduleChange.class);
                            intent.putExtra("place", place);
                            intent.putExtra("memo", memo);
                            intent.putExtra("hourmin", time_HM);
                            startActivityForResult(intent, RC_PlAN_SCH);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
        }else{
            Toast.makeText(getApplicationContext(), "plan is empty", Toast.LENGTH_SHORT).show();
            //알림 형식으로 바꾸는게?
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_PlAN_SCH){
            if(resultCode == EditPlanScheduleChange.RC_EDIT_PLAN_CHA){
                Cell_edit = mCellList.get(row_pos).get(col_pos);
                String hourVal = data.getExtras().getString("hourVal");
                String minVal =  data.getExtras().getString("minVal");
                String placeVal = data.getExtras().getString("placeVal");
                String memoVal = data.getExtras().getString("memoVal");
                String spinVal = data.getExtras().getString("spinVal");
                int time_add = 0;
                assert spinVal != null;
                if(spinVal.equals("오전")) {
                    time_add = Integer.parseInt(hourVal) * 60 + Integer.parseInt(minVal);
                }else{
                    time_add = (Integer.parseInt(hourVal)+12) * 60 + Integer.parseInt(minVal);
                }


                //제거하고 추가하는 방법이용
                ArrayList<Integer> time_val_del = mCellList.get(row_pos).get(col_pos).getTime();
                String time_del = String.valueOf(time_val_del.get(pos_edit));
                mCellList.get(row_pos).get(col_pos).delPlace(pos_edit);
                mCellList.get(row_pos).get(col_pos).delTime(pos_edit);

                dbReference_1 = (DatabaseReferenceData) getApplication();
                dbReference_1.setContext(this);
                dbDataReference_1 = dbReference_1.getDbPlanScheduleRef().child(planitemkey);
                HashMap<String, Object> dataN = new HashMap<>();
                RealtimeData data_edit = new RealtimeData(placeVal, memoVal, hourVal, minVal, col_pos + 1);
                String day = col_day[col_pos];
                dataN.put(String.valueOf(time_add), data_edit);
                dbDataReference_1.child(day).child(time_del).removeValue();
                dbDataReference_1.child(day).updateChildren(dataN);

                if ((Cell_edit.getData() == null) || (Cell_edit.getData() == "-")) {//원래는 =="-"
                    Cell cellAdd = new Cell(placeVal, placeVal, time_add);
                    mCellList.get(row_pos).set(col_pos, cellAdd);
                } else {
                    String before_place = Cell_edit.getData().toString();
                    String place = before_place + "\n" + placeVal;
                    Cell cellAdd = new Cell(place, Cell_edit.getPlace(), Cell_edit.getTime());
                    cellAdd.addTime(time_add);
                    cellAdd.sortTime();
                    int inD = cellAdd.getTime().indexOf(time_add);
                    cellAdd.addPlace(placeVal, inD);
                    mCellList.get(row_pos).set(col_pos, cellAdd);
                }

                myTableViewAdapter.setCellItems(mCellList);

                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_contain_in, new PlaceWindowFragment(Cell_edit))
                        .commit();

            }
            if(resultCode == EditPlanScheduleChange.RC_EDIT_PLAN_DEL){
                Log.i("TAG", "----EnterCheck---- : ");
                ArrayList<Integer> time_val = mCellList.get(row_pos).get(col_pos).getTime();
                String time_i = String.valueOf(time_val.get(pos_edit));
                mCellList.get(row_pos).get(col_pos).delPlace(pos_edit);
                mCellList.get(row_pos).get(col_pos).delTime(pos_edit);
                dbDataReference_1.child(col_day[col_pos]).child(time_i).removeValue();

                myTableViewAdapter.setCellItems(mCellList);
                Cell_edit = mCellList.get(row_pos).get(col_pos);
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_contain_in, new PlaceWindowFragment(Cell_edit))
                        .commit();
            }

            if (resultCode == EditPlanScheduleActivity.RC_EDIT_PLAN){
                if(!(data==null)) {
                    Cell_edit = mCellList.get(row_pos).get(col_pos);
                    String hourVal = data.getExtras().getString("hourVal");
                    String minVal =  data.getExtras().getString("minVal");
                    String placeVal = data.getExtras().getString("placeVal");
                    String memoVal = data.getExtras().getString("memoVal");
                    String spinVal = data.getExtras().getString("spinVal");
                    int t=0;
                    assert spinVal != null;
                    if(spinVal.equals("오전")) {
                        t = Integer.parseInt(hourVal) * 60
                                + Integer.parseInt(minVal);
                    }else{
                        t = (Integer.parseInt(hourVal)+12) * 60
                                + Integer.parseInt(minVal);
                    }

                    //upload data to firebase database
                    dbReference_1 = (DatabaseReferenceData) getApplication();
                    dbReference_1.setContext(this);
                    dbDataReference_1 = dbReference_1.getDbPlanScheduleRef().child(planitemkey);
                    HashMap<String, Object> dataN = new HashMap<>();
                    RealtimeData data_edit = new RealtimeData(placeVal, memoVal, hourVal, minVal, col_pos + 1);
                    String day = col_day[col_pos];
                    //String day_time = day + t;
                    dataN.put(String.valueOf(t), data_edit);
                    dbDataReference_1.child(day).updateChildren(dataN);

                    if (Cell_edit.getData() == "-") {
                        Cell cellAdd = new Cell(placeVal, placeVal, t);
                        mCellList.get(row_pos).set(col_pos, cellAdd);
                    } else {
                        String before_place = Cell_edit.getData().toString();
                        String place = before_place + "\n" + placeVal;
                        Cell cellAdd = new Cell(place, Cell_edit.getPlace(), Cell_edit.getTime());
                        cellAdd.addTime(t);
                        cellAdd.sortTime();
                        int inD = cellAdd.getTime().indexOf(t);
                        cellAdd.addPlace(placeVal, inD);
                        mCellList.get(row_pos).set(col_pos, cellAdd);
                    }

                    myTableViewAdapter.setCellItems(mCellList);

                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_contain_in, new PlaceWindowFragment(Cell_edit))
                            .commit();
                }

            }
        }
    }

    private class SlidingPageAnimationListener implements Animation.AnimationListener{

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if(pageopen){
                framepage.setVisibility(View.INVISIBLE);
                pageopen = false;
            }else{
                pageopen=true;
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

}
