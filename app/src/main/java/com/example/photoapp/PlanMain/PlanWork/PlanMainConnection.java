package com.example.photoapp.PlanMain.PlanWork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;


public class PlanMainConnection {
    private Context context;
    private static final String TAG="PLAnMAINPHOTOLISTING";

    private boolean wifiConnected = false;
    private boolean mobileConnected = false;
    private boolean downLoadOnlyWIFI;

    // 여러번 신호가 오기때문에 필요함
    private boolean firstConnect=false;

    private static NetworkReceiver networkReceiver;
    public static SharedPreferences sharedPreferences;


    private OnConnectionListenerInterface onConnectionListener;
    public interface OnConnectionListenerInterface{
        void onConnected();
        void onFailed(boolean Wifi, boolean data, boolean downloadOnlyWIFI);
    }


    public PlanMainConnection(Context context, OnConnectionListenerInterface onConnectionListener) {
        this.context=context;
        this.onConnectionListener=onConnectionListener;
        networkReceiver=new NetworkReceiver();
        // Gets the user's network preference settings
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        // 참이면 wifi만 사용, 거짓이면 wifi+data
        downLoadOnlyWIFI= !sharedPreferences.getBoolean("useOnlyWIFI", false);
        updateConnectedFlags();
        checkConnect();
    }
    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    //처음 상태를 확인
    private void updateConnectedFlags() {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (conn != null) {
                NetworkCapabilities capabilities = conn.getNetworkCapabilities(conn.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        wifiConnected = true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        mobileConnected = true;
                        Log.i(TAG, "whty04" + mobileConnected );
                    }
                }
            }
        } else {
            NetworkInfo networkInfo = conn.getActiveNetworkInfo();
            if ( networkInfo != null &&  networkInfo.isConnected()) {
                wifiConnected =  networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                mobileConnected =  networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                Log.i(TAG, "whty03" + mobileConnected );
                checkConnect();
            }
        }
    }
    //지속적인 상태를 확인
    // 왠지는 모르겠지만 두번씩 받아져서 first connect가 필요함 => 그래야 한번만 실행됨
    // if(firstconnect) 안에가 한번만 실행됌
    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager conn = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) { //api 29 이하 밑에거 deprecated
                if (conn != null) {
                    NetworkCapabilities capabilities = conn.getNetworkCapabilities(conn.getActiveNetwork());
                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            if (firstConnect) {
                                wifiConnected = true;
                                firstConnect = false;
                                checkConnect();
                            } else {
                                firstConnect = true;
                            }
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            if (firstConnect) {
                                mobileConnected = true;
                                firstConnect = false;
                                checkConnect();
                            }else {
                                firstConnect = true;
                            }
                        }
                        else{
                            firstConnect=true;
                        }
                    }
                }
            } else {
                NetworkInfo networkInfo = conn.getActiveNetworkInfo();
                if (networkInfo != null
                        && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    // If device has its Wi-Fi connection, sets refreshDisplay
                    // to true. This causes the display to be refreshed when the user
                    // returns to the app.
                    if (firstConnect) {
                        wifiConnected = true;
                        firstConnect = false;
                        checkConnect();
                    } else {
                        firstConnect = true;
                    }
                } else if ( networkInfo != null
                        && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    if (firstConnect) {
                        mobileConnected = true;
                        firstConnect = false;
                        checkConnect();
                    }
                } else {
                    firstConnect = true;
                }
            }
        }
    }

    private void checkConnect(){
        //wifi만 사용 wifi 연결
        if(downLoadOnlyWIFI & wifiConnected){
            onConnectionListener.onConnected();
        }// 전체 연결=> 둘중 하나면 됌
        else if(!downLoadOnlyWIFI &(wifiConnected | mobileConnected)){
            onConnectionListener.onConnected();
        }
        else {
            onConnectionListener.onFailed(wifiConnected,mobileConnected,downLoadOnlyWIFI);
        }
    }

    public void registerNetworkReceiver(){

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver( networkReceiver, filter);

    }

    public void unregisterNetworkReceiver(){
        if(networkReceiver!=null)
            context.unregisterReceiver(networkReceiver);
    }

}
