package com.example.photoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.photoapp.Data.GooglePhotoReference;
import com.example.photoapp.PlanList.PlanListActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectActivity extends AppCompatActivity{

    private static final String TAG = "SharedActivity";

    private static final int REQUEST_CODE_TOKEN_AUTH= 1111;

    private static final ArrayList<String> essentialScope= new ArrayList<String>(
            Arrays.asList("https://www.googleapis.com/auth/photoslibrary.sharing"," https://www.googleapis.com/auth/photoslibrary"));
    private static GoogleSignInAccount googleAccount;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent intent = getIntent();
        googleAccount = (GoogleSignInAccount) intent.getParcelableExtra("Account");

        final GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this, essentialScope);
        credential.setSelectedAccount(googleAccount.getAccount());

        @SuppressLint("StaticFieldLeak") final AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    token = credential.getToken();
                } catch (IOException transientEx) {
                    // Network or server error, try later
                    Log.e(TAG, transientEx.toString());
                } catch (UserRecoverableAuthException e) {
                    // Recover (with e.getIntent())
                    Log.e(TAG, e.toString());
                    Intent recover = e.getIntent();
                    startActivityForResult(recover, REQUEST_CODE_TOKEN_AUTH);
                } catch (GoogleAuthException authEx) {
                    // The call is not ever expected to succeed
                    // assuming you have already verified that
                    // Google Play services is installed.
                    Log.e(TAG, authEx.toString());
                }

                return token;
            }

            @Override
            protected void onPostExecute(String token) {

                Intent intent = new Intent(getApplicationContext(), PlanListActivity.class);
                new GooglePhotoReference(token);
                startActivity(intent);

            }
        };

        task.execute();
    }
}
