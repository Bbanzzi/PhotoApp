package com.example.photoapp.PlanList;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//, RecyclerView.ItemAnimator
public class RecyclerDecoration extends RecyclerView.ItemDecoration {

    private int sizeDp10;
    private int sizeDp5;

    public RecyclerDecoration(Context context) {

        sizeDp10 = dpToPx(context, 10);
        sizeDp5 = dpToPx(context, 5);
    }

    // dp -> pixel 단위로 변경
    private int dpToPx(Context context, int dp) {
        //코드를 통해 view 사이즈에 변화를 주거나 여백을 설정해 줄 때는 위와 같이 Pixel 단위로 변환해서 작업을 해줘야 됩니다. 꼭 기억하세요!
        //https://black-jin0427.tistory.com/102
        return (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }


    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);
        int itemCount = state.getItemCount();

        outRect.left=sizeDp10;
        outRect.right=sizeDp10;
        //상하 설정
        if(position == 0) {
            // 첫번 째 줄 아이템
            outRect.top = sizeDp10;
            outRect.bottom = sizeDp10;
        } else {
            outRect.bottom = sizeDp10;
        }

        // spanIndex = 0 -> 왼쪽
        // spanIndex = 1 -> 오른쪽
        // GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        // int spanIndex = lp.getSpanIndex();

    }

}
