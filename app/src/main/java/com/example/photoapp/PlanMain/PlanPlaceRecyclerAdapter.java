package com.example.photoapp.PlanMain;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.List;


public class PlanPlaceRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private String TAG="placeRecyclerAdapter";

    private static final int VIEWTYPE_EMPTYPLACE=0;
    private static final int VIEWTYPE_PLACE=1;
    private static final int VIEWTYPE_PHOTO=2;

    private Context context;

    private List<PlanPhotoData> list;

    private static Boolean checkBoxState=false;

    private static int imageWidthPixels;

    public static String place_val = null;
    public static String memo_val = null;
    public static String time_val = null;

    private static int checkPosition;


    // 생성자에서 데이터 리스트 객체를 전달받음.
    PlanPlaceRecyclerAdapter(Context context, List<PlanPhotoData> list
                             //OnPhotoItemSelectedInterface photoListener,
            ,OnPhotoItemLongSelectedInterface photoLongListener
            ,OnPlaceItemClickedInterface placeListener) {
        this.context = context;
        this.list = list;
        this.photoListener=photoListener;
        this.photoLongListener=photoLongListener;
        this.placeListener=placeListener;

        imageWidthPixels = context.getResources().getDisplayMetrics().widthPixels / 4;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.i(TAG, "onCreateViewHolder");
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        if(viewType==VIEWTYPE_PLACE) {
            View view = inflater.inflate(R.layout.item_planplace, parent, false);
            PlaceViewHolder vh = new PlaceViewHolder(view);
            return vh;
        }else{
            View view = inflater.inflate(R.layout.item_planplacephoto, parent, false);
            PhotoViewHolder vh = new PhotoViewHolder(view);
            return vh;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        checkPosition = position;

        if(list.get(position).getPlace()!=null) {
            PlaceViewHolder placeViewHolder = (PlaceViewHolder) holder;
            placeViewHolder.name.setText(list.get(position).getPlace());
            placeViewHolder.time.setText(list.get(position).getTime());

        }else if(list.get(position).getId() != null) {
            PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
            photoViewHolder.bind(position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(list.get(position).getPlace() !=null ) {
            return VIEWTYPE_PLACE;
        }else if(list.get(position).getId() !=null){
            return VIEWTYPE_PHOTO;
        }
        else{
            return VIEWTYPE_EMPTYPLACE;
        }
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "getItemCount()" + list.size());
        return list.size();
    }


    public class PlaceViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView time;
        LinearLayout lin_place_time;
        ImageButton btn_add_plan_item;

        public PlaceViewHolder (View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.textview_planplace);
            time = (TextView)itemView.findViewById(R.id.textview_plantime);
            lin_place_time = (LinearLayout) itemView.findViewById(R.id.lin_place_time);
            btn_add_plan_item = (ImageButton) itemView.findViewById(R.id.btn_add_plan_item);

            lin_place_time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    placeListener.onPlaceItemClicked(v,list.get(getAdapterPosition()));
                    Log.i("TAG","----clickItemPos2---- : " + list.get(getAdapterPosition()).getTime_i());
                }
            });


        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        CheckBox checkBox;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageview_photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_photo);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "On Click");
                    //photoListener.onPhotoItemSelected(v,getAdapterPosition());
                }
            });
            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(checkBoxState){
                        checkBoxState=false;
                    }else{
                        checkBoxState=true;
                    }
                    photoLongListener.onPhotoItemLongSelected(v,checkBoxState,getAdapterPosition());
                    return true;
                }
            });
        }


        public void bind(final int position) {

            PlanPhotoData planPhotoData=list.get(position);

            RequestOptions cropOptions = new RequestOptions().override(imageWidthPixels)
                    .placeholder(R.drawable.item_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(context)
                    .load(planPhotoData.getImageUrl())
                    .apply(cropOptions)
                    .into(imageView);

            if (checkBoxState) {
                checkBox.setVisibility(View.VISIBLE);
            } else {
                checkBox.setVisibility(View.GONE);
            }

            checkBox.setChecked(planPhotoData.getCheck());

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(((CheckBox)v).isChecked()){
                        planPhotoData.setCheck(true);
                    }else{
                        planPhotoData.setCheck(false);
                    }

                }
            });

        }
    }


    //클릭 처리 plan을 눌렀을 때https://thepassion.tistory.com/300
    public interface OnPhotoItemSelectedInterface {
        void onPhotoItemSelected(View v, int position);
    }
    public interface OnPhotoItemLongSelectedInterface {
        void onPhotoItemLongSelected(View v, Boolean checkState, int position);
    }

    public interface OnPlaceItemClickedInterface{
        void onPlaceItemClicked(View v, PlanPhotoData data);
    }
    private static OnPhotoItemSelectedInterface photoListener;
    private static OnPhotoItemLongSelectedInterface photoLongListener;
    private static OnPlaceItemClickedInterface placeListener;

    public void setCheckBoxState(Boolean checkBoxState){
        this.checkBoxState=checkBoxState;
    }
}