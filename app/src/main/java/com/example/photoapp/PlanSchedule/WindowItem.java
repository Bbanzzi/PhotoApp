package com.example.photoapp.PlanSchedule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Bundle;

import com.example.photoapp.R;

class WindowItem extends RelativeLayout {
    TextView text_time;
    TextView text_place;
    ImageButton btn_edit;
    int position = 0;

    public WindowItem(Context context){
        super(context);

        LayoutInflater winInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        winInflater.inflate(R.layout.item_placeonly, this, true);

        text_time = findViewById(R.id.text_time_item);
        text_place = findViewById(R.id.text_place_item);
        btn_edit = findViewById(R.id.btn_edit_item);
        /*
        btn_edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                PlanScheduleActivity.onDeleteItem(position);

            }
        });

         */



    }

    public void setText_time(String item) { text_time.setText(item);}

    public void setText_place(String item) { text_place.setText(item);}

    public void setPosition(int pos) {
        this.position = pos;
        btn_edit.setId(1000+4*position);

    }

}
