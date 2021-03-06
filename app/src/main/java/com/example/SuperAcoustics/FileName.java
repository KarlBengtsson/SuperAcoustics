package com.example.SuperAcoustics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FileName extends AppCompatActivity {
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
    double volume = 0; double height = 0; double length = 0; double width = 0;
    double area = 0;
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
                            height = Double.valueOf(heightTextView.getText().toString());
                            length = Double.valueOf(lengthTextView.getText().toString());
                            width = Double.valueOf(widthTextView.getText().toString());
                            volume = height * length * width;
                            area = Double.valueOf(areaTextView.getText().toString());
                            FileName.this.setPreferences();
                            startActivity(intent);
                            finish();
                        }

                    }
                };
        okButton.setOnClickListener(okListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        updateToolBar();

        ImageButton imageButton = (ImageButton) toolbar.findViewById(R.id.infoButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                InfoFragment infoFragment = InfoFragment.newInstance("InfoFragment");
                infoFragment.show(fm, "fragment_info");
            }
        });
    }

    @SuppressLint("NewApi")
    private void updateToolBar() {
        TextView title = (TextView) findViewById(R.id.toolbarTitle);
        title.setText("SuperAcoustics");
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setAutoSizeTextTypeUniformWithConfiguration(10, 26, 1, TypedValue.COMPLEX_UNIT_DIP);
    }

    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        Room = preferences.getInt("ROOM", 0);
        FILE_NAME = preferences.getString("filename", "");
        REPOSITORY_NAME = preferences.getString("foldername", "");
        volume = preferences.getFloat("volume", 0);
        area = preferences.getFloat("area", 0);
        length = preferences.getFloat("length", 0);
        width = preferences.getFloat("width", 0);
        height = preferences.getFloat("height", 0);

    }



    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("foldername", REPOSITORY_NAME);
        editor.putFloat("height", (float) height);
        editor.putFloat("length", (float) length);
        editor.putFloat("width", (float) width);
        editor.putFloat("volume", (float) volume);
        editor.putFloat("area", (float) area);
        editor.apply();
    }




}
