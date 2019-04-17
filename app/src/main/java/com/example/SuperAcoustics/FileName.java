package com.example.SuperAcoustics;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    int volume = 0; int height = 0; int length = 0; int width = 0;
    int area = 0;
    int mAudioSource = 0;
    int mSampleRate = 0;
    private TextView nameTextView;
    private TextView lengthTextView, heightTextView, widthTextView;
    private TextView areaTextView;
    private Button okButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_name_activity);
        SharedPreferences preferences = getSharedPreferences("LevelMeter",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        readPreferences();
        nameTextView = (TextView) findViewById(R.id.nameInput);
        lengthTextView = (TextView) findViewById(R.id.lengthText);
        heightTextView = (TextView) findViewById(R.id.HeightText);
        widthTextView = (TextView) findViewById(R.id.widthText);
        areaTextView = (TextView) findViewById(R.id.areaText);
        okButton = (Button) findViewById(R.id.nameButton);


        Button.OnClickListener okListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (nameTextView.getText().length() == 0 || lengthTextView.getText().length() == 0 ||
                                widthTextView.getText().length() == 0 || heightTextView.getText().length() == 0
                                || areaTextView.getText().length() == 0 ) {
                            Toast.makeText(FileName.this, "Please fill out all the information", Toast.LENGTH_LONG).show();
                        }
                        else {
                            // Dismiss this dialog.
                            Intent intent;
                            intent = new Intent(FileName.this, MainActivity.class);
                            REPOSITORY_NAME = nameTextView.getText().toString();
                            height = Integer.valueOf(heightTextView.getText().toString());
                            length = Integer.valueOf(lengthTextView.getText().toString());
                            width = Integer.valueOf(widthTextView.getText().toString());
                            volume = height * length * width;
                            area = Integer.valueOf(areaTextView.getText().toString());
                            FileName.this.setPreferences();
                            startActivity(intent);
                            finish();
                        }

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
        length = preferences.getInt("length", 0);
        width = preferences.getInt("width", 0);
        height = preferences.getInt("height", 0);

    }



    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("foldername", REPOSITORY_NAME);
        editor.putInt("height", height);
        editor.putInt("length", length);
        editor.putInt("width", width);
        editor.putInt("volume", volume);
        editor.putInt("area", area);
        editor.apply();
    }




}
