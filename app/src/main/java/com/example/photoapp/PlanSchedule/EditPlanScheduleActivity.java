package com.example.photoapp.PlanSchedule;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.List;

public class EditPlanScheduleActivity extends AppCompatActivity {

    EditText hour_editPlan;
    EditText min_editPlan;
    EditText place_editPlan;
    EditText memo_editPlan;
    Spinner spin_editPlan;
    public static final int RC_EDIT_PLAN = 1012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan_schedule);

        setActionBar();
        setSpinnerEditPlan();

        PlanMainActivity.FIRST_READ_MAIN = false;
        hour_editPlan = (EditText) findViewById(R.id.hour_editPlan);
        min_editPlan = (EditText) findViewById(R.id.min_editPlan);
        place_editPlan = (EditText) findViewById(R.id.place_editPlan);
        memo_editPlan = (EditText) findViewById(R.id.memo_editPlan);


    }

    private void setActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_editPlan);
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //custom된 actionbar를 add
        actionBar.setDisplayShowTitleEnabled(false); //원래 title을 actionbar에서 제거
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        ImageButton btn_backTOPlanList = (ImageButton) findViewById(R.id.btn_back_inEditPlan);
        btn_backTOPlanList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void setSpinnerEditPlan(){
        List<String> spinArr = new ArrayList<String>();
        spinArr.add("오전");
        spinArr.add("오후");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,spinArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_editPlan = (Spinner) findViewById(R.id.spin_editPlan);
        spin_editPlan.setAdapter(adapter);

    }

    public void closeEditPlan(View v){
        finish();
    }

    public void nextEditPlan(View v){
        String hourVal = hour_editPlan.getText().toString();
        String minVal = min_editPlan.getText().toString();
        String placeVal = place_editPlan.getText().toString();
        String memoVal = memo_editPlan.getText().toString();
        String spinVal = spin_editPlan.getSelectedItem().toString();

        Intent intent = new Intent();
        intent.putExtra("hourVal",hourVal);
        intent.putExtra("minVal",minVal);
        intent.putExtra("placeVal",placeVal);
        intent.putExtra("memoVal",memoVal);
        intent.putExtra("spinVal",spinVal);
        setResult(RC_EDIT_PLAN,intent);

        finish();
    }
}