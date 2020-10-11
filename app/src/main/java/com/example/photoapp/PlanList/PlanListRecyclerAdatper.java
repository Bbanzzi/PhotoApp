package com.example.photoapp.PlanList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photoapp.R;

import java.util.List;

public class PlanListRecyclerAdatper extends RecyclerView.Adapter<PlanListRecyclerAdatper.ViewHolder> {

    Context context;
    List<PlanItem> items;

    //클릭 처리 plan을 눌렀을 때https://thepassion.tistory.com/300
    public interface OnListItemLongSelectedInterface {
        void onItemLongSelected(View v, int position);
    }
    public interface OnListItemSelectedInterface {
        void onItemSelected(View v, int position);
        void onEditItemSelected(View v, int position);
    }

    private static OnListItemSelectedInterface planListener;
    private static OnListItemLongSelectedInterface planLongListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlanListRecyclerAdatper(Context context, List<PlanItem> items,
                                   OnListItemSelectedInterface listener,
                                   OnListItemLongSelectedInterface longListener) {
        this.planListener = listener;
        this.planLongListener = longListener;
        this.context=context;
        this.items=items;
    }
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textview_plantitle;
        public TextView textview_dates;
        public ConstraintLayout plan;
        public TextView textview_personnel;
        public ImageButton btn_edit_planList;

        public ViewHolder(View viewitem) {
            super(viewitem);
            textview_plantitle=(TextView)viewitem.findViewById(R.id.textview_plantitle);
            textview_dates=(TextView)viewitem.findViewById(R.id.textview_dates);
            textview_personnel=(TextView)viewitem.findViewById(R.id.textview_personNum);
            plan=(ConstraintLayout)viewitem.findViewById(R.id.plan);
            btn_edit_planList = viewitem.findViewById(R.id.btn_edit_planlist);

            plan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    planListener.onItemSelected(v,getAdapterPosition());
                }
            });

            plan.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    planLongListener.onItemLongSelected(v, getAdapterPosition());
                    return false;
                }
            });

            btn_edit_planList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    planListener.onEditItemSelected(v,getAdapterPosition());
                }
            });

        }
    }


    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public PlanListRecyclerAdatper.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.item_planlist, parent, false) ;
        PlanListRecyclerAdatper.ViewHolder vh = new PlanListRecyclerAdatper.ViewHolder(view) ;
        //View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan,null);

        return vh;
    }
    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull PlanListRecyclerAdatper.ViewHolder holder, int position) {
        final PlanItem item=items.get(position);
        //holder.image.setBackground(drawable);
        holder.textview_plantitle.setText(item.getPlanTitle());
        holder.textview_dates.setText(item.getStartNEndDates());
        holder.textview_personnel.setText("인원 : " +item.getPlanPersonnel());
    }

    @Override
    public int getItemCount() {
        return  items.size();
    }



}
