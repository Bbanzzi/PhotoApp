package com.example.photoapp.PlanSchedule;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.photoapp.R;

import java.util.List;

public class PlaceWindowFragment extends Fragment {

    public Cell cell_frag;

    public PlaceWindowFragment(@NonNull Cell cell){
        this.cell_frag = cell;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View view = inflater.inflate(R.layout.table_main_fragment, container, false);
        ListView textViewWin = view.findViewById(R.id.plus_plan_frag);
        planFragAdapter adapter = new planFragAdapter();
        adapter.setPlanWinItems(cell_frag);
        textViewWin.setAdapter(adapter);

        return view;
    }

}
