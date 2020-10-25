package com.example.photoapp.PlanMain.PlanWork;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.photoapp.Data.DatabaseReferenceData;
import com.example.photoapp.PlanList.PlanItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.HashMap;
import java.util.Map;

public class PlanDynamicLink {
    private static final String TAG="PlanDynamic link";

    public static Task<ShortDynamicLink> createDynamicLink(PlanItem planItem){
        // 비밀번호 대용으로 이용
        String url="https://www.example.com/?planKey="+planItem.getKey();
        Uri baseUrl = Uri.parse(url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(baseUrl)
                .setDomainUriPrefix("https://prototypephotoapp.page.link")

                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Set parameters
                // ...
                .buildShortDynamicLink() //ShortDynamicLink.Suffix.SHORT 안에 넣어서 길이 조절가능
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            planItem.setDynamicLink(shortLink.toString());
                        } else {
                            Log.e(TAG,"ERRER : Dynamic link faile");
                            // Error
                            // ...
                        }
                    }
                });

        return shortLinkTask ;
    }
}
