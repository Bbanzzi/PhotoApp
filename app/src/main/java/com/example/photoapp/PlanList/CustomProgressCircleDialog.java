package com.example.photoapp.PlanList;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.photoapp.R;
import com.rey.material.app.Dialog;

public class CustomProgressCircleDialog extends Dialog {
    public CustomProgressCircleDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_customprogresscircle);

    }
}