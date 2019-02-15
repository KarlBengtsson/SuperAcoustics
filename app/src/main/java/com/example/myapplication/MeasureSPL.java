package com.example.myapplication;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MeasureSPL extends AppCompatActivity implements
        MicrophoneInputListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measure_spl_activity);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    @Override
    public void processAudioFrame(short[] audioFrame) {

    }
}