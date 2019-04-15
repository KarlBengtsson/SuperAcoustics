// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.myapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * This is an example of a simple settings pane with persistence of settings.
 *
 * This class handles the UI interaction for the settings pane.
 * It provides a simple drop-down menu to set the sampling rate.
 * It uses Bundles to persist (i.e. store) the setting between sessions.
 *
 * @author trausti@google.com (Trausti Kristjansson)
 *
 */
public class Settings extends Activity {

    private int mAudioSource;
    private int processing;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        readPreferences();

        RadioGroup audioSourceGroup = (RadioGroup) findViewById(R.id.RadioGroup1);
        final String answer1 = ((RadioButton) findViewById(audioSourceGroup.getCheckedRadioButtonId())).toString();

        RadioGroup WindowingGroup = (RadioGroup) findViewById(R.id.RadioGroup2);
        final String answer2 = ((RadioButton) findViewById(WindowingGroup.getCheckedRadioButtonId())).getText().toString();

        /**
         * Ok button dismiss settings.
         */
        Button okButton=(Button)findViewById(R.id.settingsOkButton);
        Button.OnClickListener okBtnListener =
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAudioSource = getAudiosource(answer1);
                        processing = getProcessing(answer2);
                        Settings.this.setPreferences();
                        finish();
                    }
                };
        okButton.setOnClickListener(okBtnListener);
    }

    private int getProcessing(String answer2) {
        switch (answer2) {
            case "case1":
                processing = 1;
                break;
            case "case2":
                processing = 2;
                break;
            case "case3":
                processing = 3;
                break;
        }
        return processing;
    }


    private int getAudiosource(String answer) {
        switch(answer) {
            case "noll" :
                mAudioSource = 0;
                break;
            case "ett" :
                mAudioSource = 1;
                break;
            case "sju":
                mAudioSource = 7;
                break;
            case "sex" :
                mAudioSource = 6;
                break;
            case "nio" :
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