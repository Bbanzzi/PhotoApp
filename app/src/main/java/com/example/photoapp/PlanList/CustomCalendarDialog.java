package com.example.photoapp.PlanList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OrientationHelper;

import com.applikeysolutions.cosmocalendar.utils.SelectionType;
import com.applikeysolutions.cosmocalendar.view.CalendarView;
import com.example.photoapp.R;

import java.util.Calendar;
import java.util.List;

public class CustomCalendarDialog extends Dialog {
    private Button buttonBack;
    private Button buttonNext;

    private View.OnClickListener buttonNextListener;
    private View.OnClickListener buttonBackListener;

    private static List<Calendar> selectedDays;

    private CustomCalendarDialogListener customCalendarDialogListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그 밖의 화면은 흐리게 만들어줌
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        getWindow().setAttributes(layoutParams);

        setContentView(R.layout.dialog_customcalendar);

        final CalendarView calendarView=(CalendarView) findViewById(R.id.calendar_view);
        calendarView.setCalendarOrientation(OrientationHelper.HORIZONTAL);
        calendarView.setSelectionType(SelectionType.RANGE);

        //셋팅
        buttonBack=(Button)findViewById(R.id.button_back);
        buttonNext=(Button)findViewById(R.id.button_next);

        //클릭 리스너 셋팅 (클릭버튼이 동작하도록 만들어줌.)
        buttonBack.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                selectedDays=calendarView.getSelectedDates();
                Intent intent = new Intent(
                        v.getContext().getApplicationContext(), // 현재 화면의 제어권자
                        CreatePlanActivity.class); // 다음 넘어갈 클래스 지정
                int index=selectedDays.size()-1;
                Calendar startDates=selectedDays.get(0);
                Calendar endDates=selectedDays.get(index);

                customCalendarDialogListener.onPositiveClicked(startDates,endDates,selectedDays, index);

                dismiss();
            }
        });
    }

    //생성자 생성
    CustomCalendarDialog(@NonNull Context context) {

        super(context);
    }
    //인터페이스 설정
    interface CustomCalendarDialogListener{
        void onPositiveClicked(Calendar startDates,Calendar endDates,List<Calendar> selectedDays, int index);
        void onNegativeClicked();
    }


    //호출할 리스너 초기화
    void setCustomCalendarDialogListener(CustomCalendarDialogListener customCalendarDialogListener){
        this.customCalendarDialogListener = customCalendarDialogListener;
    }


}
