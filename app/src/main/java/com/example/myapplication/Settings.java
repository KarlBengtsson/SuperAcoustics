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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        readPreferences();

        /**
         * Ok button dismiss settings.
         */
        Button okButton=(Button)findViewById(R.id.settingsOkButton);
        Button.OnClickListener okBtnListener =
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        Settings.this.setPreferences();
                        finish();
                    }
                };
        okButton.setOnClickListener(okBtnListener);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.Default:
                if (checked)
                    mAudioSource = 0;
                    break;
            case R.id.Mic:
                if (checked)
                    mAudioSource = 1;
                    break;
            case R.id.Voice_Communication:
                if (checked)
                    mAudioSource = 7;
                break;
            case R.id.Voice_Recognition:
                if (checked)
                    mAudioSource = 6;
                break;
            case R.id.unprocessed:
                if (checked)
                    mAudioSource = 9;
                break;
        }
    }

    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mAudioSource = preferences.getInt("AudioSource", 0);

    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("AudioSource", mAudioSource);
        editor.commit();
    }
}