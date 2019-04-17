package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
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
    private TextView calBackTextView;
    private TextView calTextView;
    private TextView measureText1;
    private TextView measureText2;
    private TextView measureText3;
    private TextView measureRT;
    private static final String TAG = "MainActivity";
    float gain = 0;
    float gainBack = 0;
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
    private double[] SPLmeasure5;
    double SPLaverageRoom1 [] = new double[32];
    double SPLaverageRoom2 [] = new double[32];
    double SPLbackgroundRoom2 [] = new double [32];
    private double SPLRoom1;
    private double SPLRoom2;
    private double Background;
    private float avg;
    private FileOutputStream fos;
    private int fromCheck;
    private boolean room1check;
    private boolean room2check;
    private boolean reverbcheck;
    private int sArea;
    private boolean backgroundcheck;
    private int processing;
    private int mAudioSourceBack;
    private int processingBack;


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

    public void CalibrateSPL (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gain );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 0);
        editor.putInt("CALIBRATE", 1);
        editor.apply();
        startActivity(intent);
    }

    public void CalibrateBackground (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gainBack );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 0);
        editor.putInt("CALIBRATE", 2);
        editor.apply();
        startActivity(intent);
    }

    public void MeasureSPL1 (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gain );
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM" , 1);
        editor.putInt("CALIBRATE", 1);
        editor.apply();
        startActivityForResult(intent, 2);
    }

    public void MeasureBackground (View view) {
        Intent intent = new Intent(this, measuredBA.class);
        intent.putExtra(EXTRA_MESSAGE, gainBack);
        SharedPreferences preferences = getSharedPreferences("LevelMeter" , MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ROOM", 3);
        editor.putInt("CALIBRATE", 2);
        editor.apply();
        startActivityForResult(intent, 4);
    }

    public void MeasureSPL2 (View view) {
        if(backgroundcheck) {
            Intent intent = new Intent(this, measuredBA.class);
            intent.putExtra(EXTRA_MESSAGE, gain);
            SharedPreferences preferences = getSharedPreferences("LevelMeter", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("ROOM", 2);
            editor.putInt("CALIBRATE", 1);
            editor.apply();
            startActivityForResult(intent, 3);
        } else {
            Toast.makeText(this, "Please measure backgound noise in room 2 " +
                    "first!", Toast.LENGTH_LONG).show();
        }
    }

    public void MeasureReverb (View view) {
        Intent launchIntent = new Intent();
        launchIntent.setAction("com.example.cuiyuzhao.acousticmeasurement.action.LAUNCH_IT");
        startActivityForResult(launchIntent, 1);
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
        if (reverbcheck && room1check && room2check && backgroundcheck) {
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

    private void checkBackgroundNoise(double[] SPL, double[] Back) {
        for (int i = 0; i<SPL.length; i++) {
            double diff = SPL[i] - Back[i];
            if (diff < 10 && diff > 6) {
                SPL[i] = 10 * Math.log10((Math.pow(10, SPL[i]/10)) - (Math.pow(10, Back[i]/10)));
            } else if (diff <= 6) {
                SPL[i] -= 1.3;
            }
        }
        SPLaverageRoom2 = SPL;
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
                Log.v(TAG,"Stuff didn't get sent back");
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
                SPLmeasure5 = data.getDoubleArrayExtra("measure5");

                for (int i = 0; i<SPLmeasure1.length; i++) {
                    double sum = Math.pow(10, SPLmeasure1[i] / 10) + Math.pow(10, SPLmeasure2[i] / 10)
                            + Math.pow(10, SPLmeasure3[i] / 10) + Math.pow(10, SPLmeasure4[i] / 10);
                    SPLaverageRoom1[i] = 10* Math.log10(sum);
                }
                saveFile("SPL_Room1", SPLaverageRoom1);

                double [] gainDouble = new double[1];
                gainDouble[0] = gain;
                saveFile("Gain SPL", gainDouble);

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
                SPLmeasure5 = data.getDoubleArrayExtra("measure5");

                for (int i = 0; i<SPLmeasure1.length; i++) {
                    double sum = Math.pow(10, SPLmeasure1[i] / 10) + Math.pow(10, SPLmeasure2[i] / 10)
                            + Math.pow(10, SPLmeasure3[i] / 10) + Math.pow(10, SPLmeasure4[i] / 10) + Math.pow(10, SPLmeasure5[i] / 10);
                    SPLaverageRoom2[i] = 10* Math.log10(sum);
                }
                checkBackgroundNoise(SPLaverageRoom2, SPLbackgroundRoom2);
                saveFile("SPL_Room2", SPLaverageRoom2);
                room2check = true;
                SPLRoom2=data.getDoubleExtra("dBA", 0);
                measureText2.setText(dBformat(SPLRoom2));
            }
        } else if (requestCode == 4) {
            if(resultCode == Activity.RESULT_OK){
                SPLmeasure1 = data.getDoubleArrayExtra("measure1");
                SPLmeasure2 = data.getDoubleArrayExtra("measure2");
                SPLmeasure3 = data.getDoubleArrayExtra("measure3");
                SPLmeasure4 = data.getDoubleArrayExtra("measure4");
                SPLmeasure5 = data.getDoubleArrayExtra("measure5");

                for (int i = 0; i<SPLmeasure1.length; i++) {
                    double sum = Math.pow(10, SPLmeasure1[i] / 10) + Math.pow(10, SPLmeasure2[i] / 10)
                            + Math.pow(10, SPLmeasure3[i] / 10) + Math.pow(10, SPLmeasure4[i] / 10) + Math.pow(10, SPLmeasure5[i] / 10);
                    SPLbackgroundRoom2[i] = 10* Math.log10(sum);
                }
                saveFile("SPL_Background_Room2", SPLbackgroundRoom2);

                double [] gainBackDouble = new double[1];
                gainBackDouble[0] = gainBack;
                saveFile("Gain Background Noise", gainBackDouble);

                backgroundcheck = true;
                Background=data.getDoubleExtra("dBA", 0);
                measureText3.setText(dBformat(Background));
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

    public void getInfo (View view) {
        FragmentManager fm = getSupportFragmentManager();
        InfoFragment infoFragment = InfoFragment.newInstance("InfoFragment");
        infoFragment.show(fm, "fragment_info");
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
        //mSampleRate = preferences.getInt("SampleRate", 8000);
        gain = preferences.getFloat("mGainDif", 0);
        gainBack = preferences.getFloat("mGainBackDif", 0);
        SPLRoom1 = preferences.getFloat("mRoom1", 0);
        SPLRoom2 = preferences.getFloat("mRoom2", 0);
        Room = preferences.getInt("ROOM",0);
        volume = preferences.getInt("volume", 0);
        area = preferences.getInt("area", 0);
        sArea = preferences.getInt("area", 0);
        length = preferences.getInt("length", 0);
        width = preferences.getInt("width", 0);
        height = preferences.getInt("height", 0);
        mAudioSource = preferences.getInt("AudioSource", 6);
        processing = preferences.getInt("window", 1);
        mAudioSourceBack = preferences.getInt("AudioSourceBack", 6);
        processingBack = preferences.getInt("windowBack", 1);
    }
    private void initTextViews() {
        calTextView = (TextView) findViewById(R.id.calibrateText);
        calTextView.setText(Float.toString(gain));
        calBackTextView = (TextView) findViewById(R.id.calibrateBackgroundText);
        calBackTextView.setText(Float.toString(gainBack));
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
        calBackTextView.setText(Float.toString(gainBack));
        Log.d(TAG, "onResume() called");

    }
    @Override
    protected void onRestart() {
        super.onRestart();
        readPreferences();
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
        return String.format(Locale.ENGLISH, "%.3f", dB);
    }

}
