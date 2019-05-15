

package com.example.SuperAcoustics;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    private int source;
    private int process;

    private int mAudioSource;
    private int processing;
    private int mAudioSourceBack;
    private int processingBack;
    private int Calibrate;
    private int Room;

    private RadioGroup WindowingGroup;
    private RadioGroup audioSourceGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        readPreferences();
        if (Calibrate == 1) {
            source = mAudioSource;
            process = processing;
        } else {
            source = mAudioSourceBack;
            process = processingBack;
        }
        audioSourceGroup = (RadioGroup) findViewById(R.id.RadioGroup1);
        setSourceSelection(source);
        WindowingGroup = (RadioGroup) findViewById(R.id.RadioGroup2);
        setProcessSelection(process);

        Button okButton=(Button)findViewById(R.id.settingsOkButton);
        Button.OnClickListener okBtnListener =
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        source = getAudiosource();
                        process = getProcessing();
                        if (Calibrate == 1) {
                            mAudioSource = source;
                            processing = process;
                        } else {
                            mAudioSourceBack = source;
                            processingBack = process;
                        }
                        Settings.this.setPreferences();
                        finish();
                    }
                };
        okButton.setOnClickListener(okBtnListener);

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

    private void setProcessSelection(int process) {
        switch (process) {
            case 1:
                WindowingGroup.check(R.id.case1);
                break;
            case 2:
                WindowingGroup.check(R.id.case2);
                break;
            case 3:
                WindowingGroup.check(R.id.case3);
                break;
        }
    }

    private void setSourceSelection(int source) {
        switch (source) {
            case 7:
                audioSourceGroup.check(R.id.sju);
                break;
            case 6:
                audioSourceGroup.check(R.id.sex);
                break;
            case 0:
                audioSourceGroup.check(R.id.noll);
                break;
            case 1:
                audioSourceGroup.check(R.id.ett);
                break;
            case 9:
                audioSourceGroup.check(R.id.nio);
                break;
        }
    }

    private int getProcessing() {
        final String answer2 = ((RadioButton) findViewById(WindowingGroup.getCheckedRadioButtonId())).getText().toString();
        switch (answer2) {
            case "Hann Window":
                process = 1;
                break;
            case "Uniform Window":
                process = 2;
                break;
            case "Flat Top Window":
                process = 3;
                break;
        }
        return process;
    }


    private int getAudiosource() {
        final String answer = ((RadioButton) findViewById(audioSourceGroup.getCheckedRadioButtonId())).getText().toString();
        switch(answer) {
            case "Default":
                source = 0;
                break;
            case "Mic":
                source = 1;
                break;
            case "Voice Communication":
                source = 7;
                break;
            case "Voice Recognition":
                source = 6;
                break;
            case "Unprocessed":
                source = 9;
                break;
        }
            return source;
    }


    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mAudioSource = preferences.getInt("AudioSource", 6);
        processing = preferences.getInt("window", 1);
        mAudioSourceBack = preferences.getInt("AudioSourceBack", 6);
        processingBack = preferences.getInt("windowBack", 1);
        Calibrate = preferences.getInt("CALIBRATE", 1);
    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("window", processing);
        editor.putInt("AudioSource", mAudioSource);
        editor.putInt("windowBack", processingBack);
        editor.putInt("AudioSourceBack", mAudioSourceBack);
        editor.putInt("CALIBRATE", Calibrate);
        editor.commit();
    }
}