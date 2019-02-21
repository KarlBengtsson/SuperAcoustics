package com.example.myapplication;

/**1) implement the MicrophoneInputListener interface e.g.
 *     class MyAwesomeClass implements MicrophoneInputListener {...}
 * 2) Create the object, e.g.
 *     micInput = new MicrophoneInput(this);
 * 3) Implement processAudioFrame in your MyAwesomeClass
 *     public void processAudioFrame(short[] audioFrame) {...}.
 *
 *  Audio capture runs in a separate thread which is set up when start() is
 *  called and destroyed when stop() is called.
*/

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MeasureSPL extends AppCompatActivity implements
        MicrophoneInputListener{

    MicrophoneInput micInput;
    TextView seconds;
    TextView mdBTextView;
    TextView mdBFractionTextView;
    TextView measuredSPL;
    BarLevelDrawable mBarLevel;
    private TextView mGainTextView;
    private int Room;
    private int one_decimal;
    private double rms;
    private double rmsdB;
    private ArrayList splRoom1 = new ArrayList<Integer>();
    private ArrayList splRoom2 = new ArrayList<Integer>();
    private ArrayList DBmeasure = new ArrayList<Integer>();
    private ArrayList signal = new ArrayList<Integer>();
    private ArrayList signal1 = new ArrayList<Integer>();
    private Button startButton, stopButton, finishMeasure;
    private int counter = 0;
    private int counter3 = 0;
    private int counter4 = 0;
    private int counter1, average1, average2, signalStart;

    // For saving and loading .txt file

    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spartest";


    double mOffsetdB = 10;  // Offset for bar, i.e. 0 lit LEDs at 10 dB.
    // The Google ASR input requirements state that audio input sensitivity
    // should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
    // 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
    double mDifferenceFromNominal = 0.0;
    // For displaying error in calibration.
    double mRmsSmoothed;  // Temporally filtered version of RMS.
    double mAlpha = 0.9;  // Coefficient of IIR smoothing filter for RMS.
    private int mSampleRate;  // The audio sampling rate to use.
    private int mAudioSource;  // The audio source to use.

    // Variables to monitor UI update and check for slow updates.
    private volatile boolean mDrawing;
    private volatile int mDrawingCollided;

    private static final String TAG = "MeasureSPLEVEL";

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate() called");

        // Read the layout and construct.
        setContentView(R.layout.measure_spl_activity);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Read the previous preferences
        readPreferences();
        mGain *= Math.pow(10, mDifferenceFromNominal / 20.0);

        //Retrieves the value set by the calibration
        mGainTextView = (TextView)findViewById(R.id.gain);
        mGainTextView.setText(Double.toString(mDifferenceFromNominal));


        mBarLevel = (BarLevelDrawable)findViewById(R.id.bar_level_drawable_view);
        mdBTextView = (TextView)findViewById(R.id.dBTextView);
        mdBFractionTextView = (TextView)findViewById(R.id.dBFractionTextView);
        measuredSPL = (TextView) findViewById(R.id.measuredSPL);
        seconds = (TextView) findViewById(R.id.textseconds);

        // Here the micInput object is created for audio capture.
        // It is set up to call this object to handle real time audio frames of
        // PCM samples. The incoming frames will be handled by the
        // processAudioFrame method below.
        micInput = new MicrophoneInput(this);

        // Defining a new File for saving later on
        File dir = new File(path);
        dir.mkdirs();

        //StartButton handler
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setEnabled(false);
        //StopButton handler
        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setEnabled(false);
        //Textview to display measured SPL
        measuredSPL = (TextView) findViewById(R.id.measuredSPL);

        // Toggle Button handler.
        final ToggleButton onOffButton=(ToggleButton)findViewById(
                R.id.on_off_toggle_button);
        onOffButton.setChecked(false);
        //start = onOffButton.isChecked();

        ToggleButton.OnClickListener tbListener =
                new ToggleButton.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onOffButton.isChecked()) {
                            startButton.setEnabled(true);
                            finishMeasure.setEnabled(false);
                            readPreferences();
                            micInput.setSampleRate(mSampleRate);
                            micInput.setAudioSource(mAudioSource);
                            micInput.start();
                        } else {
                            startButton.setEnabled(false);
                            finishMeasure.setEnabled(true);
                            micInput.stop();
                            splRoom1 = stopMeasure(splRoom1, counter1);
                        }
                      }
                };
        onOffButton.setOnClickListener(tbListener);
            startButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    stopButton.setEnabled(true);
                    startButton.setEnabled(false);
                    //counter1 = startMeasure();
                    counter1 = counter;
                    signalStart = counter3;


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
                seconds.setText("05");
                if (Room == 1) {
                    File file = new File(path + "/TestFileRoom"+Integer.toString(Room)+".txt");
                    splRoom1 = stopMeasure(splRoom1, counter1);
                    //calculate average SPL
                    int total = 0;
                    for(int i = 0; i < splRoom1.size(); i++) {
                        int a = (int) splRoom1.get(i);
                        total += a;
                    }
                    signal1 = stopSignal(signal1, signalStart);
                    String [] saveText = String.valueOf(signal1).split(" ");
                    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                    Save(file, saveText);
                    signal.clear();

                    average1 = total / splRoom1.size();
                    measuredSPL.setText(Integer.toString(average1));
                }
                else {
                    File file = new File(path + "/TestFileRoom"+Integer.toString(Room)+".txt");
                    splRoom2 = stopMeasure(splRoom2, counter1);
                    //calculate average SPL
                    int total = 0;
                    for(int i = 0; i < splRoom2.size(); i++) {
                        int a = (int) splRoom2.get(i);
                        total += a;
                    }
                    signal1 = stopSignal(signal1, signalStart);
                    String [] saveText = String.valueOf(signal1).split(" ");
                    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                    Save(file,saveText);
                    signal.clear();

                    average2 = total / splRoom2.size();
                    measuredSPL.setText(Integer.toString(average2));
                }
            }
        });

        finishMeasure=(Button)findViewById(R.id.finishButton);
        Button.OnClickListener setFinishBtnListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Dismiss this dialog.
                        MeasureSPL.this.setPreferences();
                        finish();

                    }
                };
        finishMeasure.setOnClickListener(setFinishBtnListener);


    }


    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private int startMeasure() {
        int counter1 = counter;
        return counter1;
    }

    private ArrayList stopMeasure(ArrayList list, int counter1) {
        int stop = DBmeasure.size() - counter1;
        for (int i = 0; i < stop; i++) {
            list.add(i, DBmeasure.get(counter1 + i));
        }
        return list;
    }

    private ArrayList stopSignal(ArrayList saveText, int signalStart) {
        int stop = signal.size() - signalStart;
        for (int i = 0; i < stop; i++) {
            saveText.add(i, signal.get(signalStart + i));
        }
        return saveText;
    }

    /**
     *  This method gets called by the micInput object owned by this activity.
     *  It first computes the RMS value and then it sets up a bit of
     *  code/closure that runs on the UI thread that does the actual drawing.
     */

    @Override
    public void processAudioFrame(short[] audioFrame) {
        if (!mDrawing) {
            mDrawing = true;
            // Compute the RMS value. (Note that this does not remove DC).
            rms = 0;
                for (int i = 0; i < audioFrame.length; i++) {
                    signal.add(audioFrame[i]);
                    rms += audioFrame[i] * audioFrame[i];
                    counter3++;
                }
            rms = Math.sqrt(rms/audioFrame.length);


            // Compute a smoothed version for less flickering of the display.
            mRmsSmoothed = mRmsSmoothed * mAlpha + (1 - mAlpha) * rms;
            rmsdB = 20.0 * Math.log10(mGain * mRmsSmoothed) ;

            //Create Arraylist of dBmeasurements to calculate average SPL
            int a = (int) (rmsdB + 20 );
            DBmeasure.add(a);
            counter++;


            // Set up a method that runs on the UI thread to update of the LED bar
            // and numerical display.
            mBarLevel.post(new Runnable() {
                @Override
                public void run() {
                    // The bar has an input range of [0.0 ; 1.0] and 10 segments.
                    // Each LED corresponds to 6 dB.
                    //mBarLevel.setLevel((mOffsetdB + rmsdB) / 60);

                    DecimalFormat df = new DecimalFormat("##");
                    mdBTextView.setText(df.format(20 + rmsdB));

                    DecimalFormat df_fraction = new DecimalFormat("#");
                    one_decimal = (int) (Math.round(Math.abs(rmsdB * 10))) % 10;
                    mdBFractionTextView.setText(Integer.toString(one_decimal));
                    mDrawing = false;
                }
            });
        } else {
            mDrawingCollided++;
            Log.v(TAG, "Level bar update collision, i.e. update took longer " +
                    "than 20ms. Collision count" + Double.toString(mDrawingCollided));
        }
    }

    /**
     * Method to read the sample rate and audio source preferences.
     */
    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mDifferenceFromNominal = preferences.getInt("mGainDif", 0);
        Room = preferences.getInt("ROOM" , 0);
    }

    private void setPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("mGainDif", (int) mDifferenceFromNominal);
        if (Room == 1) {
            editor.putInt("mRoom1" , average1);
            editor.putInt("ROOM",1);
        }
        else {
            editor.putInt("mRoom2" , average2 );
            editor.putInt("ROOM",2);
        }
        editor.apply();
    }
    private void saveToTxtFile() {

        //Does nothing yet.
    }


    public static void Save(File file, String [] data)
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());
                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }


    public static String[] Load(File file)
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String test;
        int anzahl=0;
        try
        {
            while ((test=br.readLine()) != null)
            {
                anzahl++;
            }
        }
        catch (IOException e) {e.printStackTrace();}

        try
        {
            fis.getChannel().position(0);
        }
        catch (IOException e) {e.printStackTrace();}

        String[] array = new String[anzahl];

        String line;
        int i = 0;
        try
        {
            while((line=br.readLine())!=null)
            {
                array[i] = line;
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }




    // Methods providing information about what happens when going back & forth between activities.
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
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");

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
        micInput.stop();
        Log.d(TAG, "onDestroy() called");
    }
}