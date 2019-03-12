package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class FileName extends Activity {
    public static final String EXTRA_MESSAGE = "Calibration";
    public static final String ROOM_MESSAGE = "Room";
    public String path = "";
    private String FILE_NAME = "";
    private String REPOSITORY_NAME = "";
    private String VOLUME_VALUE = "";
    private String AREA_VALUE = "";
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
    int volume = 0;
    int area = 0;
    int mAudioSource = 0;
    int mSampleRate = 0;
    private TextView nameTextView;
    private TextView volumeTextView;
    private TextView areaTextView;
    private Button okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_name_activity);
        readPreferences();
        nameTextView = (TextView) findViewById(R.id.nameInput);
        volumeTextView = (TextView) findViewById(R.id.volumeText);
        areaTextView = (TextView) findViewById(R.id.areaText);
        okButton = (Button) findViewById(R.id.nameButton);
        Button.OnClickListener okListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Intent intent;
                        intent = new Intent(FileName.this, MainActivity.class);
                        REPOSITORY_NAME = nameTextView.getText().toString();
                        volume = Integer.valueOf(volumeTextView.getText().toString());
                        area = Integer.valueOf(areaTextView.getText().toString());

                        FileName.this.setPreferences();
                        startActivity(intent);
                        finish();

                    }
                };
        okButton.setOnClickListener(okListener);
    }



    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        Room = preferences.getInt("ROOM", 0);
        FILE_NAME = preferences.getString("filename", "");
        REPOSITORY_NAME = preferences.getString("foldername", "");
        volume = preferences.getInt("volume", 0);
        area = preferences.getInt("area", 0);
    }



    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("foldername", REPOSITORY_NAME);
        editor.putInt("volume", volume);
        editor.putInt("area", area);
        editor.apply();
    }




}
