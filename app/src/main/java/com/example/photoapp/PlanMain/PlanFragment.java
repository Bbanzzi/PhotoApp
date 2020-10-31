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

import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanList.PlanItem;
import com.example.photoapp.PlanList.PlanListRecyclerAdatper;
import com.example.photoapp.PlanMain.Photo.PhotoMainActivity;
import com.example.photoapp.PlanSchedule.EditPlanScheduleChange;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.List;


public class PlanFragment  extends Fragment implements PlanPlaceRecyclerAdapter.OnPhotoItemSelectedInterface,
        PlanPlaceRecyclerAdapter.OnPhotoItemLongSelectedInterface,
        PlanPlaceRecyclerAdapter.OnPlaceItemClickedInterface,
        onKeyBackPressedListener{

    private RecyclerView recyclerView;
    private PlanPlaceRecyclerAdapter sectionAdapter;

    private String title;
    private static String TAG =" Fragment";
    private ArrayList<RealtimeData> planSchedule;
    private ArrayList<PlanPhotoData> items=new ArrayList<>();
    private PlanItem planItem;


    public PlanFragment(){}

    public static PlanFragment newInstance(int position, PlanItem planItem, String title, ArrayList<RealtimeData> realTimeData){
        Bundle bundle = new Bundle();
        bundle.putParcelable("planItem", planItem);
        bundle.putString("title", title);
        bundle.putParcelableArrayList("planSchedule", realTimeData);

        PlanFragment fragment = new PlanFragment();
        fragment.setArguments(bundle);
        return  fragment;
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Bundle extra = getArguments();
        planItem=extra.getParcelable("planItem");
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

        sectionAdapter=new PlanPlaceRecyclerAdapter(getContext(),items, this,this,this);
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

        this.items.clear();
        for(RealtimeData realTimeData: items){
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

    // check상태로 바꾸는 것
    @Override
    public void onPhotoItemLongSelected(View v, Boolean checkState, int position) {
        ((PlanMainActivity)getActivity()).changeCheckState(checkState);
    }

    //뒤로 가기를 눌럿을 경우
    @Override
    public void onBack(Boolean checkBoxState) {
        if(!checkBoxState){
            sectionAdapter.setCheckBoxState(false);
        }else{
            PlanMainActivity activity=(PlanMainActivity)getActivity();
            activity.setOnKeyBackPressedListener(null);
            activity.onBackPressed();
        }
    }

    @Override
    public void onPhotoItemSelected(View v, Boolean checkBoxState,int position) {

        if(checkBoxState){
            sectionAdapter.setCheckBoxState(false);
        }
        Intent intent = new Intent(getContext(), PhotoMainActivity.class);
        intent.putExtra("planItem", planItem);
        intent.putExtra("title", title);
        intent.putParcelableArrayListExtra("realTimeDataList", items);
        intent.putExtra("position", position);
        startActivity(intent);
    }

}
