package com.example.photoapp.PlanSchedule;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.List;

public class EditPlanScheduleChange extends AppCompatActivity {

    EditText hour_editPlan2;
    EditText min_editPlan2;
    EditText place_editPlan2;
    EditText memo_editPlan2;
    Spinner spin_editPlan2;
    int time_orgin;
    public static final int RC_EDIT_PLAN_CHA = 1014;
    public static final int RC_EDIT_PLAN_DEL = 1015;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan_schedule_change);

        PlanMainActivity.FIRST_READ_MAIN = false;
        setActionBar();

        hour_editPlan2 = (EditText) findViewById(R.id.hour_editPlan2);
        min_editPlan2 = (EditText) findViewById(R.id.min_editPlan2);
        place_editPlan2 = (EditText) findViewById(R.id.place_editPlan2);
        memo_editPlan2 = (EditText) findViewById(R.id.memo_editPlan2);

        Intent intent = getIntent();
        String place = intent.getExtras().getString("place");
        String memo = intent.getExtras().getString("memo");
        int time = intent.getExtras().getInt("hourmin");
        time_orgin = time;
        int hour = (time/60);
        setSpinnerEditPlan(hour);
        if(hour>12){
            hour = hour-12;
        }
        int min = time%60;

        place_editPlan2.setText(place);
        memo_editPlan2.setText(memo);
        hour_editPlan2.setText(String.valueOf(hour));
        min_editPlan2.setText(String.valueOf(min));
    }

    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_editPlan2);
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //custom된 actionbar를 add
        actionBar.setDisplayShowTitleEnabled(false); //원래 title을 actionbar에서 제거
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        ImageButton btn_backTOPlanList = (ImageButton) findViewById(R.id.btn_back_inEditPlan2);
        btn_backTOPlanList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setSpinnerEditPlan(int hour){
        List<String> spinArr = new ArrayList<String>();
        spinArr.add("오전");
        spinArr.add("오후");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,spinArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_editPlan2 = (Spinner) findViewById(R.id.spin_editPlan2);
        spin_editPlan2.setAdapter(adapter);
        if(hour>12){
            spin_editPlan2.setSelection(1);
        }else{
            spin_editPlan2.setSelection(0);
        }

    }

    public void closeEditPlan2(View v){
        finish();
    }

    public void nextEditPlan2(View v){
        Intent intent = new Intent();
        intent.putExtra("placeVal",place_editPlan2.getText().toString());
        intent.putExtra("memoVal",memo_editPlan2.getText().toString());
        intent.putExtra("hourVal",hour_editPlan2.getText().toString());
        intent.putExtra("minVal",min_editPlan2.getText().toString());
        intent.putExtra("spinVal",spin_editPlan2.getSelectedItem().toString());
        intent.putExtra("time",time_orgin);
        setResult(RC_EDIT_PLAN_CHA,intent);
        finish();
    }

    public void onDeletePlanChange(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("계획 삭제").setMessage("계획을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("time",time_orgin);
                        setResult(RC_EDIT_PLAN_DEL,intent);
                        finish();
                        Toast.makeText(getApplicationContext(), "삭제하셨습니다", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("최소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "취소하였습니다", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}