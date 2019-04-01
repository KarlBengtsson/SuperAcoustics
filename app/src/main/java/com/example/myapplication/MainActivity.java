package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ReverbFragment.ReverbDialogListener {
    public static final String EXTRA_MESSAGE = "Calibration";
    public static final String ROOM_MESSAGE = "Room";
    private Button dbMeter;
    private Button measure1;
    private Button reverb;
    private TextView calTextView;
    private TextView measureText1;
    private TextView measureText2;
    private TextView measureText3;
    private TextView measureRT;
    private static final String TAG = "MainActivity";
    float gain = 0;
    int Room = 0; int volume = 0; int area = 0; int length = 0; int width = 0; int height = 0;
    String roomName;
    int mAudioSource = 0;
    int mSampleRate = 0;
    private int indices[] = {9, 12, 15, 18, 21, 24};
    private float[] reverbResult = {0, 0, 0, 0, 0, 0};
    private double[] SPLmeasure1;
    private double[] SPLmeasure2;
    private double[] SPLmeasure3;
    private double[] SPLmeasure4;
    double SPLaverageRoom1 [] = new double[32];
    double SPLaverageRoom2 [] = new double[32];
    private double SPLRoom1;
    private double SPLRoom2;
    private float avg;
    private FileOutputStream fos;
    private int fromCheck;
    private boolean room1check;
    private boolean room2check;
    private boolean reverbcheck;
    private int sArea;


    //Todo Measure background noise and explore new way to measure Reverberation time.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Log.d(TAG, "OnCreate() called");
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        SharedPreferences preferences = getSharedPreferences("LevelMeter",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("mRoom1",0);
        editor.putFloat("mRoom2",0);
        editor.putFloat("reverb", 0);
        editor.apply();
        readPreferences();
        dbMeter = (Button) findViewById(R.id.dbmeterButton);
        measure1 = (Button) findViewById(R.id.measureButton1);
        reverb = (Button) findViewById(R.id.reverbButton);
        initTextViews();
        onCheckPerm();
    }

    public void measuredB (View view) {
        Intent intent = new Intent(this, LevelMeterActivity.class);
        startActivity(intent);
    }

    public void CalibrateSPL (View view) {
        //Intent intent = new Intent(this, LevelMeterActivity.class);
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gain );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 0);
        editor.commit();
        startActivity(intent);
    }

    public void MeasureSPL1 (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gain );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 1);
        editor.commit();
        startActivityForResult(intent, 2);
    }
    public void MeasureSPL2 (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gain );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 3);
        editor.commit();
        startActivityForResult(intent, 3);
    }

    public void enterReverb (View view) {
        FragmentManager fm = getSupportFragmentManager();
        ReverbFragment reverbFragment = ReverbFragment.newInstance("ReverbFragment");
        reverbFragment.show(fm, "fragment_reverb");
    }

    public void onFinishEditDialog(String a1, String b, String c, String d, String e, String f) {
        reverbResult[0] = Float.parseFloat(a1);
        reverbResult[1] = Float.parseFloat(b);
        reverbResult[2] = Float.parseFloat(c);
        reverbResult[3] = Float.parseFloat(d);
        reverbResult[4] = Float.parseFloat(e);
        reverbResult[5] = Float.parseFloat(f);
        double reverbResultDouble [] = new double [reverbResult.length];
        for ( int i = 0; i < reverbResult.length; i++) {
            reverbResultDouble[i] = reverbResult[i];
        }
        saveFile("Reverberation_time", reverbResultDouble);
        reverbcheck = true;
        float total = 0;
        for (double a:reverbResult) {
            total += a;
        }
        avg = total/6;
        avg = (float)(Math.round(avg * 10000d) / 10000d);
        measureRT.setText(String.valueOf(avg));
        setPreferences();
    }

    public void ViewResult (View view) {
        if (reverbcheck && room1check && room2check) {
            double[] Area = new double[indices.length];
            double[] R = new double[indices.length];
            for (int i = 0; i<indices.length; i++){
                Area[i] = (0.163*volume)/ reverbResult[i];
                double X = 10*Math.log10(sArea/Area[i]);
                R[i] = SPLaverageRoom1[i] - SPLaverageRoom2[i] + X;
                R[i] = Math.round(R[i] * 10000d) / 10000d;
                saveFile("SRI", R);
            }

            Intent intent = new Intent(this,ViewResult.class);
            fromCheck = 2;
            setPreferences();
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Please perform all the measurements first!", Toast.LENGTH_LONG).show();
        }

    }

    public void MeasureReverb (View view) {
        Intent launchIntent = new Intent();
        launchIntent.setAction("com.example.cuiyuzhao.acousticmeasurement.action.LAUNCH_IT");
        startActivityForResult(launchIntent, 1);

        //Set EP till 1 och MP til 4

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //return result from MeasureReverb
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                reverbResult = data.getFloatArrayExtra("result");
                double reverbResultDouble [] = new double [reverbResult.length];
                for ( int i = 0; i < reverbResult.length; i++) {
                    reverbResultDouble[i] = reverbResult[i];
                }
                saveFile("Reverberation_time", reverbResultDouble);
                reverbcheck = true;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                Log.v(TAG,"Stuff didnt get sent back");
            }
            float total = 0;
            for (double a:reverbResult) {
                total += a;
            }
            avg = total/6;
            avg = (float)(Math.round(avg * 10000d) / 10000d);
            measureRT.setText(String.valueOf(avg));
            setPreferences();
            //return result from MeasureSPL1
        } else if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                SPLmeasure1 = data.getDoubleArrayExtra("measure1");
                SPLmeasure2 = data.getDoubleArrayExtra("measure2");
                SPLmeasure3 = data.getDoubleArrayExtra("measure3");
                SPLmeasure4 = data.getDoubleArrayExtra("measure4");

                for (int i = 0; i<SPLmeasure1.length; i++) {
                    double sum = Math.pow(10, SPLmeasure1[i] / 10) + Math.pow(10, SPLmeasure2[i] / 10)
                            + Math.pow(10, SPLmeasure3[i] / 10) + Math.pow(10, SPLmeasure4[i] / 10);
                    SPLaverageRoom1[i] = 10* Math.log10(sum);
                }
                saveFile("SPL_Room1", SPLaverageRoom1);
                room1check = true;
                SPLRoom1=data.getDoubleExtra("dBA", 0);
                measureText1.setText(dBformat(SPLRoom1));
            }
        } else if (requestCode == 3) {
            if(resultCode == Activity.RESULT_OK){
                SPLmeasure1 = data.getDoubleArrayExtra("measure1");
                SPLmeasure2 = data.getDoubleArrayExtra("measure2");
                SPLmeasure3 = data.getDoubleArrayExtra("measure3");
                SPLmeasure4 = data.getDoubleArrayExtra("measure4");

                for (int i = 0; i<SPLmeasure1.length; i++) {
                    double sum = Math.pow(10, SPLmeasure1[i] / 10) + Math.pow(10, SPLmeasure2[i] / 10)
                            + Math.pow(10, SPLmeasure3[i] / 10) + Math.pow(10, SPLmeasure4[i] / 10);
                    SPLaverageRoom2[i] = 10* Math.log10(sum);
                }
                saveFile("SPL_Room2", SPLaverageRoom2);
                room2check = true;
                SPLRoom2=data.getDoubleExtra("dBA", 0);
                measureText2.setText(dBformat(SPLRoom2));
            }
        }
    }//onActivityResult

    private void saveFile(String number, double[] values ) {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String filename = String.format(number + ".txt");
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + "SuperAcoustics" + File.separator + roomName + "_" + df.format(new Date()));
        if (!path.exists()) {
            Log.d("My oh my...", "The path doesn't exist, create one? : " + path.mkdirs());
        }
        try {
            File file = new File(path, filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            for (int i = 0; i < values.length; i++) {
                fos.write(("  " + dBformat(values[i]) + "\n").getBytes());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("mRoom1", (float) SPLRoom1);
        editor.putFloat("mRoom2", (float) SPLRoom2);
        editor.putFloat("reverb" , avg);
        editor.putInt("fromCheck",fromCheck);
        editor.apply();
    }

    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        roomName = preferences.getString("foldername", null);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
        gain = preferences.getFloat("mGainDif", 0);
        SPLRoom1 = preferences.getFloat("mRoom1", 0);
        SPLRoom2 = preferences.getFloat("mRoom2", 0);
        Room = preferences.getInt("ROOM",0);
        volume = preferences.getInt("volume", 0);
        area = preferences.getInt("area", 0);
        sArea = preferences.getInt("area", 0);
        length = preferences.getInt("length", 0);
        width = preferences.getInt("width", 0);
        height = preferences.getInt("height", 0);
    }
    private void initTextViews() {
        calTextView = (TextView) findViewById(R.id.calibrateText);
        calTextView.setText(Float.toString(gain));
        measureText1 = (TextView) findViewById(R.id.measureText1);
        measureText1.setText("Not measured");
        measureText2 = (TextView) findViewById(R.id.measureText2);
        measureText2.setText("Not measured");
        measureText3 = (TextView) findViewById(R.id.measureText3);
        measureText3.setText("Not measured");
        measureRT = (TextView) findViewById(R.id.measureTextRT);
        measureRT.setText("Not measured");
        room1check = false;
        room2check = false;
        reverbcheck = false;

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
        calTextView.setText(Float.toString(gain));
        Log.d(TAG, "onResume() called");

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        readPreferences();
        if (Room==1){
            measureText1.setText(Integer.toString((int) SPLRoom1));
        }
        else {
            measureText2.setText(Integer.toString((int) SPLRoom2));
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
    private String dBformat(double dB) {
        // stop the recording log file
        return String.format(Locale.ENGLISH, "%.1f", dB);
    }

}
