package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "Calibration";
    public static final String ROOM_MESSAGE = "Room";
    private Button dbMeter;
    private Button measure1;
    private TextView calTextView;
    private TextView measureText1;
    private TextView measureText2;
    private static final String TAG = "MainActivity";
    double mDifferenceFromNominal = 0.0;
    int splRoom1 = 0;
    int splRoom2 = 0;
    int Room = 0;
    int mAudioSource = 0;
    int mSampleRate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.d(TAG, "OnCreate() called");
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        readPreferences();
        dbMeter = (Button) findViewById(R.id.dbmeterButton);
        measure1 = (Button) findViewById(R.id.measureButton1);
        initTextViews();
        onCheckPerm();

    }


    public void CalibrateSPL (View view) {
        Intent intent = new Intent(this, LevelMeterActivity.class);
        startActivity(intent);
    }

    public void MeasureSPL1 (View view) {
        Intent intent = new Intent(this, MeasureSPL.class);
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM",1);
        editor.commit();
        intent.putExtra(EXTRA_MESSAGE, mDifferenceFromNominal );
        startActivity(intent);
    }
    public void MeasureSPL2 (View view) {
        Intent intent = new Intent(this, MeasureSPL.class);
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM",2);
        editor.commit();
        intent.putExtra(EXTRA_MESSAGE, mDifferenceFromNominal );
        startActivity(intent);
    }

    public void ViewResult (View view) {
        Intent intent = new Intent(this,ViewResult.class);
        startActivity(intent);
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void onCheckPerm() {
        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 3;
        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }
    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mDifferenceFromNominal = preferences.getInt("mGainDif", 0);
        splRoom1 = preferences.getInt("mRoom1", 0);
        splRoom2 = preferences.getInt("mRoom2", 0);
        Room = preferences.getInt("ROOM",0);
    }
    private void initTextViews() {
        calTextView = (TextView) findViewById(R.id.calibrateText);
        calTextView.setText(Double.toString(mDifferenceFromNominal));
        measureText1 = (TextView) findViewById(R.id.measureText1);
        measureText1.setText("Not measured");
        measureText2 = (TextView) findViewById(R.id.measureText2);
        measureText2.setText("Not measured");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "onSaveInstanceState() called");

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "RestoreInstanceState() called");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    //Called when returning to Main Activity from Result Activity
    protected void onResume() {
        super.onResume();
        readPreferences();
        calTextView.setText(Double.toString(mDifferenceFromNominal));
        Log.d(TAG, "onResume() called");

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        readPreferences();
        if (Room==1){
            measureText1.setText(Integer.toString(splRoom1));
        }
        else {
            measureText2.setText(Integer.toString(splRoom2));
        }
        Log.d(TAG, "onRestart() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }
}
