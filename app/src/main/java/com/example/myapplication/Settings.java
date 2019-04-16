

package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class Settings extends Activity {

    private int mAudioSource;
    private int processing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        readPreferences();
        Button okButton=(Button)findViewById(R.id.settingsOkButton);
        Button.OnClickListener okBtnListener =
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAudioSource = getAudiosource();
                        processing = getProcessing();
                        Settings.this.setPreferences();
                        finish();
                    }
                };
        okButton.setOnClickListener(okBtnListener);
    }

    private int getProcessing() {
        RadioGroup WindowingGroup = (RadioGroup) findViewById(R.id.RadioGroup2);
        final String answer2 = ((RadioButton) findViewById(WindowingGroup.getCheckedRadioButtonId())).getText().toString();
        switch (answer2) {
            case "Hann Window":
                processing = 1;
                break;
            case "Uniform Window":
                processing = 2;
                break;
            case "Flat Top Window":
                processing = 3;
                break;
        }
        return processing;
    }


    private int getAudiosource() {
        RadioGroup audioSourceGroup = (RadioGroup) findViewById(R.id.RadioGroup1);
        final String answer = ((RadioButton) findViewById(audioSourceGroup.getCheckedRadioButtonId())).getText().toString();
        switch(answer) {
            case "Default":
                mAudioSource = 0;
                break;
            case "Mic":
                mAudioSource = 1;
                break;
            case "Voice Communication":
                mAudioSource = 7;
                break;
            case "Voice Recognition":
                mAudioSource = 6;
                break;
            case "Unprocessed":
                mAudioSource = 9;
                break;
        }
            return mAudioSource;
    }


    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mAudioSource = preferences.getInt("AudioSource", 6);
        processing = preferences.getInt("window", 1);

    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("window", processing);
        editor.putInt("AudioSource", mAudioSource);
        editor.commit();
    }
}