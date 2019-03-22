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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

/**
 * Detta är ett försök till att mäta dBA istället för db. görs på samma sätt som i
 * openoise-meter appen
 *
 */
public class measuredBA extends AppCompatActivity {
        //implements MicrophoneInputListener {

    //-------------------------------- Calibrate -----------------------------------------------------
    TextView mdBTextView;
    TextView mdBFractionTextView;
    BarLevelDrawable mBarLevel;
    private TextView mGainTextView;

    //-------------------------------- Measure SPL -----------------------------------------------
    TextView seconds;
    TextView measuredSPL1;
    TextView measuredSPL2;
    TextView measuredSPL3;
    TextView measuredSPL4;
    private ArrayList<Integer> signal1 = new ArrayList<>(); //används i plot functionen
    private Button startButton, stopButton, finishMeasure, plotT, plotFFT;
    private int counter4 = 0;
    private int average1, average2, average11, average12, average13, average14;
    public String path = "";
    private String FILE_NAME = "TestRoom";
    private String REPOSITORY_NAME;
    private int tOrFFT;

    //--------------------------------------------------------------------------------------------
    double mOffsetdB = 10;  // Offset for bar, i.e. 0 lit LEDs at 10 dB.
    // For displaying error in calibration.
    private int mSampleRate;  // The audio sampling rate to use.
    // Variables to monitor UI update and check for slow updates.
    private volatile boolean mDrawing;
    private volatile int mDrawingCollided;

    private static final String TAG = "LevelMeterActivity";
    private AudioRecord recorder;
    private int Room;

    private final static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private final static int RECORDER_SAMPLERATE = 44100;
    private final static int BYTES_PER_ELEMENT = 2;
    private final static int BLOCK_SIZE = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)
            / BYTES_PER_ELEMENT;
    private final static int BLOCK_SIZE_FFT = 1764;
    private final static int NUMBER_OF_FFT_PER_SECOND = RECORDER_SAMPLERATE
            / BLOCK_SIZE_FFT;

    private final static double FREQRESOLUTION = ((double) RECORDER_SAMPLERATE)
            / BLOCK_SIZE_FFT;

    private double[] weightedA = new double[BLOCK_SIZE_FFT];
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private DoubleFFT_1D fft = null;

    private float [] THIRD_OCTAVE = {16, 20, 25, 31.5f, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500,
            630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000};
    String [] THIRD_OCTAVE_LABEL = {"16", "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500",
            "630", "800", "1000", "1250", "1600", "2000", "2500", "3150", "4000", "5000", "6300", "8000", "10000", "12500", "16000", "20000"};
    private double filter;
    private double dbATimeDisplay; //Final Result
    private float gain = 0; //är detta samma som mdifference from nominal kanske??


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        readPreferences();

        if (Room == 0) {

        // Read the layout and construct.
        setContentView(R.layout.level_meter_activity);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get a handle that will be used in async thread post to update the
        // display.
        mBarLevel = (BarLevelDrawable)findViewById(R.id.bar_level_drawable_view);
        mdBTextView = (TextView)findViewById(R.id.dBTextView);
        mdBFractionTextView = (TextView)findViewById(R.id.dBFractionTextView);
        mGainTextView = (TextView)findViewById(R.id.gain);
        mGainTextView.setText(Double.toString(gain));
        // Toggle Button handler.

        //final int finalCountTimeDisplay = (int) (timeDisplay * NUMBER_OF_FFT_PER_SECOND);
        final int finalCountTimeDisplay = (int) (1 * NUMBER_OF_FFT_PER_SECOND);

        //final int finalCountTimeLog = (int) (timeLog * NUMBER_OF_FFT_PER_SECOND);
        final int finalCountTimeLog = (int) (1 * NUMBER_OF_FFT_PER_SECOND);

        final ToggleButton onOffButton=(ToggleButton)findViewById(
                R.id.on_off_toggle_button);

        ToggleButton.OnClickListener tbListener =
                new ToggleButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onOffButton.isChecked()) {
                            precalculateWeightedA();
                            startRecording((Float) gain, (Integer) finalCountTimeDisplay, (Integer) finalCountTimeLog);
                        } else {
                            stopRecording();
                        }
                    }
                };
        onOffButton.setOnClickListener(tbListener);

        // Call for the method to activate the calibration buttons
        onClickLevelAdjustment();

        //Todo: Behöver vi ha settings? Ska inte samplerate alltid vara 44100?????
        // Settings button, launches the settings dialog.
        Button settingsButton=(Button)findViewById(R.id.settingsButton);
        Button.OnClickListener settingsBtnListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        final ToggleButton onOffButton=(ToggleButton)findViewById(
                                R.id.on_off_toggle_button);
                        onOffButton.setChecked(false);
                        //LevelMeterActivity2.this.micInput.stop();
                        measuredBA.this.setPreferences();
                        Intent settingsIntent = new Intent(measuredBA.this,
                                Settings.class);
                        measuredBA.this.startActivity(settingsIntent);
                    }
                };
        settingsButton.setOnClickListener(settingsBtnListener);

        Button setCalButton=(Button)findViewById(R.id.setCalibrationButton);
        Button.OnClickListener setCalBtnListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        measuredBA.this.setPreferences();
                        stopRecording();
                        finish();

                    }
                };
        setCalButton.setOnClickListener(setCalBtnListener);
        //-----------------------------------------------------------------------------------------
        } else {
            // Read the layout and construct.
            setContentView(R.layout.measure_spl_activity);
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            // Read the previous preferences

            //Retrieves the value set by the calibration
            mGainTextView = (TextView)findViewById(R.id.gain);
            mGainTextView.setText(Float.toString(gain));

            mBarLevel = (BarLevelDrawable)findViewById(R.id.bar_level_drawable_view);
            mdBTextView = (TextView)findViewById(R.id.dBTextView);
            mdBFractionTextView = (TextView)findViewById(R.id.dBFractionTextView);
            measuredSPL1 = (TextView) findViewById(R.id.measuredSPL1);
            measuredSPL2 = (TextView) findViewById(R.id.measuredSPL2);
            measuredSPL3 = (TextView) findViewById(R.id.measuredSPL3);
            measuredSPL4 = (TextView) findViewById(R.id.measuredSPL4);
            seconds = (TextView) findViewById(R.id.textseconds);

            // Defining a new File for saving later on
            //File dir = new File(path);
            //dir.mkdirs();

            //StartButton handler
            startButton = (Button) findViewById(R.id.startButton);
            startButton.setEnabled(false);
            //StopButton handler
            stopButton = (Button) findViewById(R.id.stopButton);
            stopButton.setEnabled(false);

            // Toggle Button handler.
            final ToggleButton onOffButton=(ToggleButton)findViewById(
                    R.id.on_off_toggle_button);
            onOffButton.setChecked(false);

            final int finalCountTimeDisplay = (int) (1 * NUMBER_OF_FFT_PER_SECOND);

            final int finalCountTimeLog = (int) (1 * NUMBER_OF_FFT_PER_SECOND);

            ToggleButton.OnClickListener tbListener =
                    new ToggleButton.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onOffButton.isChecked()) {
                                //plotFFT.setEnabled(false);
                                //plotT.setEnabled(false);
                                startButton.setEnabled(true);
                                finishMeasure.setEnabled(false);
                                readPreferences();
                                precalculateWeightedA();
                                startRecording((Float) gain, (Integer) finalCountTimeDisplay, (Integer) finalCountTimeLog);
                            } else {
                                startButton.setEnabled(false);
                                finishMeasure.setEnabled(true);
                                //plotFFT.setEnabled(true);
                                //plotT.setEnabled(true);
                                stopRecording();
                                //splRoom1 = stopMeasure(splRoom1, counter1);
                            }
                        }
                    };
            onOffButton.setOnClickListener(tbListener);
            startButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    counter4 ++;
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);


                    new CountDownTimer(5000, 1000) {
                        int time=5;
                        public void onTick(long millisUntilFinished) {
                            seconds.setText(checkDigit(time));
                            time--;
                        }

                        public void onFinish() {
                            seconds.setText("0");
                        }

                    }.start();
                }
            });

            stopButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                    plotFFT.setEnabled(true);
                    plotT.setEnabled(true);
                    seconds.setText("05");

                    // if (Room == 1) {
                }
            });

            finishMeasure=(Button)findViewById(R.id.finishButton);
            Button.OnClickListener setFinishBtnListener =
                    new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // Dismiss this dialog.
                            if (counter4 < 4) {
                                Toast.makeText(getApplicationContext(), "Make 4 measurements in each room before finishing", Toast.LENGTH_LONG).show();
                            } else {
                                switch (Room) {
                                    case 1:
                                        average1 = (average11 + average12 + average13 + average14) / 4;
                                        break;

                                    case 2:
                                        average2 = (average11 + average12 + average13 + average14) / 4;
                                        break;
                                }
                            }

                            measuredBA.this.setPreferences();
                            finish();

                        }
                    };
            finishMeasure.setOnClickListener(setFinishBtnListener);

            /*plotT=(Button)findViewById(R.id.plotT);
            plotT.setEnabled(false);
            Button.OnClickListener setPlotTListener =
                    new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            tOrFFT = 1;
                            measuredBA.this.setPreferences();
                            measuredBA.this.plot();
                        }
                    };
            plotT.setOnClickListener(setPlotTListener);

            plotFFT=(Button)findViewById(R.id.plotFFT);
            plotFFT.setEnabled(false);
            Button.OnClickListener setPlotFFTListener =
                    new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            tOrFFT = 2;
                            measuredBA.this.setPreferences();
                            measuredBA.this.plot();
                        }
                    };
            plotFFT.setOnClickListener(setPlotFFTListener);*/

        }

    }

    public void plot () {
        Intent plotIntent = new Intent(measuredBA.this, Plot.class);
        //plotIntent.putIntegerArrayListExtra("plotData", signal1);
        plotIntent.putExtra("SignalLength",signal1.size());
        plotIntent.putExtra("plotType", tOrFFT);
        startActivity(plotIntent);
    }

    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    /**
     * Inner class to handle press of gain adjustment buttons.
     */
    private class DbClickListener implements Button.OnClickListener {
        private float gainIncrement;

        public DbClickListener(float gainIncrement) {
            this.gainIncrement = gainIncrement;
        }

        @Override
        public void onClick(View v) {
            gain += gainIncrement;
            DecimalFormat df = new DecimalFormat("##.# dB");
            mGainTextView.setText(df.format(gain));
        }
    }

    /**
     * Handler for adjusting the calibration
     */

    private void onClickLevelAdjustment() {
        // Level adjustment buttons.

        // Minus 5 dB button event handler.
        Button minus5dbButton = (Button)findViewById(R.id.minus_5_db_button);
        DbClickListener minus5dBButtonListener = new DbClickListener((float) -5.0);
        minus5dbButton.setOnClickListener(minus5dBButtonListener);

        // Minus 1 dB button event handler.
        Button minus1dbButton = (Button)findViewById(R.id.minus_1_db_button);
        DbClickListener minus1dBButtonListener = new DbClickListener((float) -1.0);
        minus1dbButton.setOnClickListener(minus1dBButtonListener);

        // Plus 1 dB button event handler.
        Button plus1dbButton = (Button)findViewById(R.id.plus_1_db_button);
        DbClickListener plus1dBButtonListener = new DbClickListener((float) 1.0);
        plus1dbButton.setOnClickListener(plus1dBButtonListener);

        // Plus 5 dB button event handler.
        Button plus5dbButton = (Button)findViewById(R.id.plus_5_db_button);
        DbClickListener plus5dBButtonListener = new DbClickListener((float) 5.0);
        plus5dbButton.setOnClickListener(plus5dBButtonListener);
    }


    private void precalculateWeightedA() {
        for (int i = 0; i < BLOCK_SIZE_FFT; i++) {
            double actualFreq = FREQRESOLUTION * i;
            double actualFreqSQ = actualFreq * actualFreq;
            double actualFreqFour = actualFreqSQ * actualFreqSQ;
            double actualFreqEight = actualFreqFour * actualFreqFour;

            double t1 = 20.598997 * 20.598997 + actualFreqSQ;
            t1 = t1 * t1;
            double t2 = 107.65265 * 107.65265 + actualFreqSQ;
            double t3 = 737.86223 * 737.86223 + actualFreqSQ;
            double t4 = 12194.217 * 12194.217 + actualFreqSQ;
            t4 = t4 * t4;

            double weightFormula = (3.5041384e16 * actualFreqEight)
                    / (t1 * t2 * t3 * t4);

            weightedA[i] = weightFormula;
        }
    }

    private void startRecording(final float gain, final int finalCountTimeDisplay, final int finalCountTimeLog) {

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BLOCK_SIZE * BYTES_PER_ELEMENT);


        recorder.startRecording();
        isRecording = true;

        fft = new DoubleFFT_1D(BLOCK_SIZE_FFT);

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {

                short rawData[] = new short[BLOCK_SIZE_FFT];


                final float dbFft[] = new float[BLOCK_SIZE_FFT / 2];


                final float dbFftA[] = new float[BLOCK_SIZE_FFT / 2];

                float normalizedRawData;


                double[] audioDataForFFT = new double[BLOCK_SIZE_FFT * 2];


                float amplitudeRef = 0.00002f;


                final float[] dbBand = new float[THIRD_OCTAVE.length];

                final float[] linearBand = new float[THIRD_OCTAVE.length];
                final float[] linearBandCount = new float[THIRD_OCTAVE.length];

                int indexTimeDisplay = 1;
                double linearATimeDisplay = 0;

                int initial_delay = 0;


                while (isRecording) {

                    recorder.read(rawData, 0, BLOCK_SIZE_FFT);

                    initial_delay++;

                    if (initial_delay > 20) {

                        for (int i = 0, j = 0; i < BLOCK_SIZE_FFT; i++, j += 2) {

                            // Range [-1,1]
                            normalizedRawData = (float) rawData[i]
                                    / (float) Short.MAX_VALUE;

                            filter = normalizedRawData;

                            double x = (2 * Math.PI * i) / (BLOCK_SIZE_FFT - 1);
                            double winValue = (1 - Math.cos(x)) * 0.5d;

                            audioDataForFFT[j] = filter * winValue;

                            audioDataForFFT[j + 1] = 0.0;
                        }

                        // FFT
                        fft.complexForward(audioDataForFFT);

                        // Magsum non pesati
                        double linearFftGlobal = 0;

                        double linearFftAGlobal = 0;

                        for (int ki = 0; ki < THIRD_OCTAVE.length; ki++) {
                            linearBandCount[ki] = 0;
                            linearBand[ki] = 0;
                            dbBand[ki] = 0;
                        }

                        // Leggo fino a BLOCK_SIZE_FFT/2 perchè in tot ho BLOCK_SIZE_FFT/2
                        // bande utili
                        for (int i = 0, j = 0; i < BLOCK_SIZE_FFT / 2; i++, j += 2) {

                            double re = audioDataForFFT[j];
                            double im = audioDataForFFT[j + 1];

                            // Magnitudo
                            double mag = Math.sqrt((re * re) + (im * im));

                            double weightFormula = weightedA[i];

                            dbFft[i] = (float) (10 * Math.log10(mag * mag
                                    / amplitudeRef))
                                    + (float) gain;
                            dbFftA[i] = (float) (10 * Math.log10(mag * mag
                                    * weightFormula
                                    / amplitudeRef))
                                    + (float) gain;

                            linearFftGlobal += Math.pow(10, (float) dbFft[i] / 10f);
                            linearFftAGlobal += Math.pow(10, (float) dbFftA[i] / 10f);

                            float linearFft = (float) Math.pow(10, (float) dbFft[i] / 10f);


                            if ((0 <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17.8f)) {
                                linearBandCount[0] += 1;
                                linearBand[0] += linearFft;
                                dbBand[0] = (float) (10 * Math.log10(linearBand[0]));
                            }
                            if ((17.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22.4f)) {
                                linearBandCount[1] += 1;
                                linearBand[1] += linearFft;
                                dbBand[1] = (float) (10 * Math.log10(linearBand[1]));
                            }
                            if ((22.4f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 28.2f)) {
                                linearBandCount[2] += 1;
                                linearBand[2] += linearFft;
                                dbBand[2] = (float) (10 * Math.log10(linearBand[2]));
                            }
                            if ((28.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 35.5f)) {
                                linearBandCount[3] += 1;
                                linearBand[3] += linearFft;
                                dbBand[3] = (float) (10 * Math.log10(linearBand[3]));
                            }
                            if ((35.5f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 44.7f)) {
                                linearBandCount[4] += 1;
                                linearBand[4] += linearFft;
                                dbBand[4] = (float) (10 * Math.log10(linearBand[4]));
                            }
                            if ((44.7f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 56.2f)) {
                                linearBandCount[5] += 1;
                                linearBand[5] += linearFft;
                                dbBand[5] = (float) (10 * Math.log10(linearBand[5]));
                            }
                            if ((56.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 70.8f)) {
                                linearBandCount[6] += 1;
                                linearBand[6] += linearFft;
                                dbBand[6] = (float) (10 * Math.log10(linearBand[6]));
                            }
                            if ((70.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 89.1f)) {
                                linearBandCount[7] += 1;
                                linearBand[7] += linearFft;
                                dbBand[7] = (float) (10 * Math.log10(linearBand[7]));
                            }
                            if ((89.1f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 112f)) {
                                linearBandCount[8] += 1;
                                linearBand[8] += linearFft;
                                dbBand[8] = (float) (10 * Math.log10(linearBand[8]));
                            }
                            if ((112f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 141f)) {
                                linearBandCount[9] += 1;
                                linearBand[9] += linearFft;
                                dbBand[9] = (float) (10 * Math.log10(linearBand[9]));
                            }
                            if ((141f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 178f)) {
                                linearBandCount[10] += 1;
                                linearBand[10] += linearFft;
                                dbBand[10] = (float) (10 * Math.log10(linearBand[10]));
                            }
                            if ((178f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 224f)) {
                                linearBandCount[11] += 1;
                                linearBand[11] += linearFft;
                                dbBand[11] = (float) (10 * Math.log10(linearBand[11]));
                            }
                            if ((224f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 282f)) {
                                linearBandCount[12] += 1;
                                linearBand[12] += linearFft;
                                dbBand[12] = (float) (10 * Math.log10(linearBand[12]));
                            }
                            if ((282f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 355f)) {
                                linearBandCount[13] += 1;
                                linearBand[13] += linearFft;
                                dbBand[13] = (float) (10 * Math.log10(linearBand[13]));
                            }
                            if ((355f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 447f)) {
                                linearBandCount[14] += 1;
                                linearBand[14] += linearFft;
                                dbBand[14] = (float) (10 * Math.log10(linearBand[14]));
                            }
                            if ((447f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 562f)) {
                                linearBandCount[15] += 1;
                                linearBand[15] += linearFft;
                                dbBand[15] = (float) (10 * Math.log10(linearBand[15]));
                            }
                            if ((562f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 708f)) {
                                linearBandCount[16] += 1;
                                linearBand[16] += linearFft;
                                dbBand[16] = (float) (10 * Math.log10(linearBand[16]));
                            }
                            if ((708f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 891f)) {
                                linearBandCount[17] += 1;
                                linearBand[17] += linearFft;
                                dbBand[17] = (float) (10 * Math.log10(linearBand[17]));
                            }
                            if ((891f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1122f)) {
                                linearBandCount[18] += 1;
                                linearBand[18] += linearFft;
                                dbBand[18] = (float) (10 * Math.log10(linearBand[18]));
                            }
                            if ((1122f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1413f)) {
                                linearBandCount[19] += 1;
                                linearBand[19] += linearFft;
                                dbBand[19] = (float) (10 * Math.log10(linearBand[19]));
                            }
                            if ((1413f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1778f)) {
                                linearBandCount[20] += 1;
                                linearBand[20] += linearFft;
                                dbBand[20] = (float) (10 * Math.log10(linearBand[20]));
                            }
                            if ((1778f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2239f)) {
                                linearBandCount[21] += 1;
                                linearBand[21] += linearFft;
                                dbBand[21] = (float) (10 * Math.log10(linearBand[21]));
                            }
                            if ((2239f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2818f)) {
                                linearBandCount[22] += 1;
                                linearBand[22] += linearFft;
                                dbBand[22] = (float) (10 * Math.log10(linearBand[22]));
                            }
                            if ((2818f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 3548f)) {
                                linearBandCount[23] += 1;
                                linearBand[23] += linearFft;
                                dbBand[23] = (float) (10 * Math.log10(linearBand[23]));
                            }
                            if ((3548f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 4467f)) {
                                linearBandCount[24] += 1;
                                linearBand[24] += linearFft;
                                dbBand[24] = (float) (10 * Math.log10(linearBand[24]));
                            }
                            if ((4467f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 5623f)) {
                                linearBandCount[25] += 1;
                                linearBand[25] += linearFft;
                                dbBand[25] = (float) (10 * Math.log10(linearBand[25]));
                            }
                            if ((5623f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 7079f)) {
                                linearBandCount[26] += 1;
                                linearBand[26] += linearFft;
                                dbBand[26] = (float) (10 * Math.log10(linearBand[26]));
                            }
                            if ((7079f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 8913f)) {
                                linearBandCount[27] += 1;
                                linearBand[27] += linearFft;
                                dbBand[27] = (float) (10 * Math.log10(linearBand[27]));
                            }
                            if ((8913f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 11220f)) {
                                linearBandCount[28] += 1;
                                linearBand[28] += linearFft;
                                dbBand[28] = (float) (10 * Math.log10(linearBand[28]));
                            }
                            if ((11220f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 14130f)) {
                                linearBandCount[29] += 1;
                                linearBand[29] += linearFft;
                                dbBand[29] = (float) (10 * Math.log10(linearBand[29]));
                            }
                            if ((14130f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17780f)) {
                                linearBandCount[30] += 1;
                                linearBand[30] += linearFft;
                                dbBand[30] = (float) (10 * Math.log10(linearBand[30]));
                            }
                            if ((17780f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22390f)) {
                                linearBandCount[31] += 1;
                                linearBand[31] += linearFft;
                                dbBand[31] = (float) (10 * Math.log10(linearBand[31]));
                            }

                        }

                        linearATimeDisplay += linearFftAGlobal;

                        if (indexTimeDisplay < finalCountTimeDisplay) {
                            indexTimeDisplay++;
                        } else {

                            // dbATimeDISPLAY is our result!!!!!
                            dbATimeDisplay = 10 * Math.log10(linearATimeDisplay / finalCountTimeDisplay);
                            indexTimeDisplay = 1;
                            linearATimeDisplay = 0;

                            //update textviews
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    DecimalFormat df = new DecimalFormat("##");
                                    mdBTextView.setText(df.format(dbATimeDisplay));

                                    DecimalFormat df_fraction = new DecimalFormat("#");
                                    int one_decimal = (int) (Math.round(Math.abs(dbATimeDisplay * 10))) % 10;
                                    mdBFractionTextView.setText(Integer.toString(one_decimal));
                                }
                            });

                        }

                    }
                } // while
            }
        }, "AudioRecorder Thread");
        recordingThread.start();

    }

    private void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;
            try {
                recordingThread.join();
                //fos.close();
            } catch (Exception e) {
                Log.d("nostro log",
                        "Il Thread principale non può attendere la chiusura del thread secondario dell'audio");
            }
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    //------------------------------------------------------------------------------------------


    @Override
    //Called when returning to Main Activity from Result Activity
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }


    //------------------------------------------------------------------------------------------

    /**
     * Method to read the sample rate and audio source preferences.
     */
    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        gain = preferences.getFloat("mGainDif", 0);
        Room = preferences.getInt("ROOM" , 0);
        REPOSITORY_NAME = preferences.getString("foldername", "");
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LevelMeter/" + REPOSITORY_NAME;
        if (Room == 1) {
            average1 = preferences.getInt("mRoom1", 0);
        }
        else {
            average2 = preferences.getInt("mRoom2", 0);
        }
    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("SampleRate", mSampleRate);
        editor.putFloat("mGainDif", gain);
        editor.putInt("mCount", counter4);
        if (Room == 1) {
            editor.putInt("mRoom1" , average1);
            editor.putInt("ROOM",1);
        }
        else {
            editor.putInt("mRoom2" , average2);
            editor.putInt("ROOM",2);
        }
        editor.apply();
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
        readPreferences();
        Log.d(TAG, "onStart() called");
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
        stopRecording();
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


}

