package com.example.photoapp.PlanSchedule;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.photoapp.R;

public class EditWindowFragment extends Fragment {

    PlanScheduleActivity planScheduleActivity;

    public EditWindowFragment(){
    }

    @Override
    public void onAttach(@NonNull Context context){
        super.onAttach(context);
        planScheduleActivity  = (PlanScheduleActivity) getActivity();
    }

    @Override
    public void onDetach(){
        super.onDetach();
        planScheduleActivity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.table_edit_fragment,container,false);

        Button btn_edit_cancel = view.findViewById(R.id.btn_edit_cancel);
        btn_edit_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //planScheduleActivity.onCancelEdit(1);
            }
        });

        Button btn_edit_accept = view.findViewById(R.id.btn_edit_accept);
        btn_edit_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //.onAcceptEdit(1);
            }
        });
        return view;
    }


}
