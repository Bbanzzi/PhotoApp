package com.example.photoapp.PlanList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.photoapp.LoginInfoProvider;
import com.example.photoapp.MainActivity;
import com.example.photoapp.PlanMain.PlanMainActivity;
import com.example.photoapp.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class SettingPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences prefs;
    SwitchPreference useOnlyWIFI;
    SwitchPreference message;
    Preference profile;
    Preference logout;

    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth;
    //Activity NetWorkActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        GoogleSignInOptions googleSignInOptions = MainActivity.gso;
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions).build();
        mAuth = FirebaseAuth.getInstance();

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());//this 대신 getActivity()를 씀?

        useOnlyWIFI = (SwitchPreference) findPreference("useOnlyWIFI");
        message = (SwitchPreference) findPreference("messageNotice");
        profile = (Preference) findPreference("profile");
        logout = (Preference) findPreference("logout");
        String email = LoginInfoProvider.getUserEmail(getContext());
        profile.setSummary(email);


        if (prefs.getBoolean("messageNotice", false)) {
            message.setSummary("사진 업로드 시 알림을 받습니다");
            PlanMainActivity.messageCheck = true;
        }
        if (prefs.getBoolean("useOnlyWIFI", false)) {
            useOnlyWIFI.setSummary("데이터 사용");
            PlanMainActivity.downLoadOnlyWIFI = false;
        }

        logout.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext()).setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            signOut();
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getContext(),"취소",Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        }
                    }).show();

            return true;
        });

    }

    public void signOut(){
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                mAuth.signOut();
                if(mGoogleApiClient.isConnected()){
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if(status.isSuccess()){
                                Toast.makeText(getContext(),"로그아웃",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Toast.makeText(getContext(),"로그아웃 실패",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("messageNotice")) {
            if (prefs.getBoolean("messageNotice", false)) {
                message.setSummary("메시지 알림 허용");
                PlanMainActivity.messageCheck = true;
            } else {
                message.setSummary("사진 업로드 시 알림을 받습니다");
                PlanMainActivity.messageCheck = false;
            }
        }

        if (key.equals("useOnlyWIFI")) {
            if (prefs.getBoolean("useOnlyWIFI", false)) {
                useOnlyWIFI.setSummary("데이터 사용");
                PlanMainActivity.downLoadOnlyWIFI = false;
            } else {
                useOnlyWIFI.setSummary("WIFI가 연결되어있지 않을 때 데이터를 이용해 사진 업로드 합니다");
                PlanMainActivity.downLoadOnlyWIFI = true;

            }

            PlanMainActivity.refreshDisplay = true;
            //((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
        }

    }
}
