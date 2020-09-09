package com.example.photoapp.PlanMain.Photo;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.photoapp.PlanMain.PlanFragment;
import com.example.photoapp.PlanMain.PlanPhotoData;
import com.example.photoapp.PlanSchedule.RealtimeData;
import com.example.photoapp.R;

import java.util.ArrayList;
import java.util.Objects;

public class PhotoFragment extends Fragment {

    private static String TAG ="PhotoFragment";


    private PlanPhotoData planPhotoData;
    public static PhotoFragment newInstance(int position, PlanPhotoData planPhotoData){
        Bundle bundle = new Bundle();
        bundle.putParcelable("planPhotoData" , planPhotoData);

        PhotoFragment fragment = new PhotoFragment();
        fragment.setArguments(bundle);
        return  fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extra = getArguments();
        planPhotoData =extra.getParcelable("planPhotoData" );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_photomain, container, false);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onfullScreenListener.onFragmentClick();
            }
        });
        ImageView imageView= (ImageView) rootView.findViewById(R.id.imageview_photomain);

        RequestOptions cropOptions = new RequestOptions()
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(Objects.requireNonNull(getContext()))
                .load(planPhotoData.getImageUrl())
                .apply(cropOptions)
                .into(imageView);

        return rootView;
    }

    public interface onFullScreenListener{
        public void onFragmentClick();
    }

    private static onFullScreenListener onfullScreenListener;

    public static void setOnfullScreenListener(onFullScreenListener lister){
        onfullScreenListener=lister;
    }
}
