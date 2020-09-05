package com.example.photoapp.PlanMain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoapp.PlanList.PlanListRecyclerAdatper;
import com.example.photoapp.PlanSchedule.EditPlanScheduleChange;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.List;


public class PlanFragment  extends Fragment implements PlanPlaceRecyclerAdapter.OnPhotoItemSelectedInterface,
        PlanPlaceRecyclerAdapter.OnPhotoItemLongSelectedInterface,
        PlanPlaceRecyclerAdapter.OnPlaceItemClickedInterface,
        PlanMainActivity.onKeyBackPressedListener{

    private RecyclerView recyclerView;
    private PlanPlaceRecyclerAdapter sectionAdapter;
    PlanMainActivity planMainActivity;

    private String title;
    private static String TAG =" Fragment";
    private ArrayList<RealtimeData> planSchedule;
    private List<PlanPhotoData> items=new ArrayList<>();


    public PlanFragment(){}

    public static PlanFragment newInstance(int position, String title, ArrayList<RealtimeData> realTimeData){
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putParcelableArrayList("planSchedule", realTimeData);

        PlanFragment fragment = new PlanFragment();
        fragment.setArguments(bundle);
        Log.i(TAG,title);
        return  fragment;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Bundle extra = getArguments();
        title = extra.getString("title");
        planSchedule =extra.getParcelableArrayList("planSchedule");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((PlanMainActivity)getActivity()).setOnKeyBackPressedListener(this);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_planplacelist, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_planplacelist);
        // Create an instance of SectionedRecyclerViewAdapter

        for(RealtimeData realTimeData:planSchedule){
            items.add(new PlanPhotoData(realTimeData.getPlace(),realTimeData.getTimeStr(),realTimeData.getMemo(),realTimeData.getTime()));
            //Log.i("TAG","----onCreateView_frag---- : " + realTimeData.getTime());
            if(realTimeData.getPhotoDataList() !=null){
                items.addAll(realTimeData.getPhotoDataList());
            }
        }

        sectionAdapter=new PlanPlaceRecyclerAdapter(getContext(),items, this,this);
        final GridLayoutManager glm = new GridLayoutManager(getContext(), 4);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(final int position) {
                if (sectionAdapter.getItemViewType(position) == 0 || sectionAdapter.getItemViewType(position) == 1 ) {
                    return 4;
                }
                return 1;
            }
        });
        sectionAdapter.setHasStableIds(true);
        recyclerView.setLayoutManager(glm);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setAdapter(sectionAdapter);
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    public String getTitle(){
        return this.title;
    }

    public void update(ArrayList<RealtimeData> items){
        /*
        planSchedule.clear();
        planSchedule.addAll(items);

         */
        this.items.clear();
        for(RealtimeData realTimeData:planSchedule){
            this.items.add(new PlanPhotoData(realTimeData.getPlace(),realTimeData.getTimeStr(),realTimeData.getMemo(),realTimeData.getTime()));
            if(realTimeData.getPhotoDataList() !=null){
                this.items.addAll(realTimeData.getPhotoDataList());
            }
        }
        sectionAdapter.notifyDataSetChanged();


    }

    @Override
    public void onPlaceItemClicked(View v, PlanPhotoData data){
        String place = data.getPlace();
        String memo = data.getMemo();
        int time = data.getTime_i();
        ((PlanMainActivity)getActivity()).startEditPlanWithClick(place,memo,time);
    }

    @Override
    public void onPhotoItemLongSelected(View v, Boolean checkState, int position) {
        ((PlanMainActivity)getActivity()).changeCheckState(checkState);
    }

    @Override
    public void onBack(Boolean checkBoxState) {
        if(checkBoxState){
            sectionAdapter.setCheckBoxState(false);
        }else{
            PlanMainActivity activity=(PlanMainActivity)getActivity();
            activity.setOnKeyBackPressedListener(null);
            activity.onBackPressed();
        }
    }

    @Override
    public void onPhotoItemSelected(View v, int position) {

    }
}
