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

import android.content.Intent;
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
    TextView measuredSPL1;
    TextView measuredSPL2;
    TextView measuredSPL3;
    TextView measuredSPL4;
    BarLevelDrawable mBarLevel;
    private TextView mGainTextView;
    private int Room;
    private int one_decimal;
    private double rms;
    private double rmsdB;
    private ArrayList<Integer> splRoom11 = new ArrayList<>(); private ArrayList<Integer> splRoom12 = new ArrayList<>();
    private ArrayList<Integer> splRoom13 = new ArrayList<>(); private ArrayList<Integer> splRoom14 = new ArrayList<>();
    private ArrayList<Integer> splRoom2 = new ArrayList<>();
    private ArrayList<Integer> DBmeasure = new ArrayList<>();
    private ArrayList<Integer> signal = new ArrayList<>();
    private ArrayList<Integer> signal1 = new ArrayList<>(); private ArrayList<Integer> signal2 = new ArrayList<>();
    private ArrayList<Integer> signal3 = new ArrayList<>(); private ArrayList<Integer> signal4 = new ArrayList<>();
    private Button startButton, stopButton, finishMeasure, plotT, plotFFT;
    private int counter = 0;
    private int counter3 = 0;
    private int counter4 = 0;
    private int counter1, average1, average2, average11, average12, average13, average14, average21, average22, average23, average24, signalStart;

    // For saving and loading .txt file




    double mOffsetdB = 10;  // Offset for bar, i.e. 0 lit LEDs at 10 dB.
    // The Google ASR input requirements state that audio input sensitivity
    // should be set such that 90 dB SPL at 1000 Hz yields RMS of 2500 for
    // 16-bit samples, i.e. 20 * log_10(2500 / mGain) = 90.
    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
    float gain = 0;
    // For displaying error in calibration.
    double mRmsSmoothed;  // Temporally filtered version of RMS.
    double mAlpha = 0.9;  // Coefficient of IIR smoothing filter for RMS.
    private int mSampleRate;  // The audio sampling rate to use.
    private int mAudioSource;  // The audio source to use.

    // Variables to monitor UI update and check for slow updates.
    private volatile boolean mDrawing;
    private volatile int mDrawingCollided;

    public String path = "";
    private static final String TAG = "MeasureSPLEVEL";
    private String FILE_NAME = "TestRoom";
    private String REPOSITORY_NAME;
    private int tOrFFT;

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
        mGain *= Math.pow(10, gain / 20.0);

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

        // Here the micInput object is created for audio capture.
        // It is set up to call this object to handle real time audio frames of
        // PCM samples. The incoming frames will be handled by the
        // processAudioFrame method below.
        micInput = new MicrophoneInput(this);

        // Defining a new File for saving later on
        File dir = new File(path);
        dir.mkdirs();

        //StartButton handler
/*        startButton = (Button) findViewById(R.id.startButton);
        startButton.setEnabled(false);
        //StopButton handler
        stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setEnabled(false);*/
        //Textview to display measured SPL
        //measuredSPL1 = (TextView) findViewById(R.id.measuredSPL1);

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
                            plotFFT.setEnabled(false);
                            plotT.setEnabled(false);
                            startButton.setEnabled(true);
                            finishMeasure.setEnabled(false);
                            readPreferences();
                            micInput.setSampleRate(mSampleRate);
                            micInput.setAudioSource(mAudioSource);
                            micInput.start();
                        } else {
                            startButton.setEnabled(false);
                            finishMeasure.setEnabled(true);
                            plotFFT.setEnabled(true);
                            plotT.setEnabled(true);
                            micInput.stop();
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
                plotFFT.setEnabled(true);
                plotT.setEnabled(true);
                seconds.setText("05");

               // if (Room == 1) {
                    switch (counter4) {
                        case 1:
                            File file = new File(path + "/"+FILE_NAME+Integer.toString(Room)+Integer.toString(counter4)+".txt");
                            splRoom11 = stopMeasure(splRoom11, counter1);
                            //calculate average SPL
                            int total = 0;
                            for(int i = 0; i < splRoom11.size(); i++) {
                                double a = (int) splRoom11.get(i);
                                total +=  Math.pow(10,a/10);
                            }
                            signal1 = stopSignal(signal1, signalStart);
                            String [] saveText = String.valueOf(signal1).split(" ");
                            Toast.makeText(getApplicationContext(),"Saved to "+ getFilesDir() + "/"+ FILE_NAME + Integer.toString(Room),Toast.LENGTH_LONG).show();
                            Save(file, saveText);
                            signal.clear();
                            average11 = (int) (10*Math.log10(total / splRoom11.size()));
                            measuredSPL1.setText(Integer.toString(average11));
                            break;

                        case 2:
                            File file2 = new File(path + "/"+FILE_NAME+Integer.toString(Room)+Integer.toString(counter4)+".txt");
                            splRoom12 = stopMeasure(splRoom12, counter1);
                            //calculate average SPL
                            int total2 = 0;
                            for(int i = 0; i < splRoom12.size(); i++) {
                                double a = (int) splRoom12.get(i);
                                total2 +=  Math.pow(10,a/10);
                            }
                            signal2 = stopSignal(signal2, signalStart);
                            String [] saveText2 = String.valueOf(signal2).split(" ");
                            Toast.makeText(getApplicationContext(),"Saved to "+ getFilesDir() + "/"+ FILE_NAME + Integer.toString(Room),Toast.LENGTH_LONG).show();
                            Save(file2, saveText2);
                            signal.clear();
                            average12 = (int) (10*Math.log10(total2 / splRoom12.size()));
                            measuredSPL2.setText(Integer.toString(average12));
                            break;
                        case 3:
                            File file3 = new File(path + "/"+FILE_NAME+Integer.toString(Room)+Integer.toString(counter4)+".txt");
                            splRoom13 = stopMeasure(splRoom13, counter1);
                            //calculate average SPL
                            int total3 = 0;
                            for(int i = 0; i < splRoom13.size(); i++) {
                                double a = (int) splRoom13.get(i);
                                total3 +=  Math.pow(10,a/10);
                            }
                            signal3 = stopSignal(signal3, signalStart);
                            String [] saveText3 = String.valueOf(signal3).split(" ");
                            Toast.makeText(getApplicationContext(),"Saved to "+ getFilesDir() + "/"+ FILE_NAME + Integer.toString(Room),Toast.LENGTH_LONG).show();
                            Save(file3, saveText3);
                            signal.clear();
                            average13 = (int) (10*Math.log10(total3 / splRoom13.size()));
                            measuredSPL3.setText(Integer.toString(average13));
                            break;
                        case 4:
                            File file4 = new File(path + "/"+FILE_NAME+Integer.toString(Room)+Integer.toString(counter4)+".txt");
                            splRoom14 = stopMeasure(splRoom14, counter1);
                            //calculate average SPL
                            int total4 = 0;
                            for(int i = 0; i < splRoom14.size(); i++) {
                                double a = (int) splRoom14.get(i);
                                total4 +=  Math.pow(10,a/10);
                            }
                            signal4 = stopSignal(signal4, signalStart);
                            String [] saveText4 = String.valueOf(signal4).split(" ");
                            Toast.makeText(getApplicationContext(),"Saved to "+ getFilesDir() + "/"+ FILE_NAME + Integer.toString(Room),Toast.LENGTH_LONG).show();
                            Save(file4, saveText4);
                            signal.clear();
                            average14 = (int) (10*Math.log10(total4 / splRoom14.size()));
                            measuredSPL4.setText(Integer.toString(average14));
                            break;


                    }

               // }
                /*else {
                    File file = new File(path + "/"+FILE_NAME+Integer.toString(Room)+".txt");
                    splRoom2 = stopMeasure(splRoom2, counter1);
                    //calculate average SPL
                    int total = 0;
                    for(int i = 0; i < splRoom2.size(); i++) {
                        int a = (int) splRoom2.get(i);
                        total += a;
                    }
                    signal1 = stopSignal(signal1, signalStart);
                    String [] saveText = String.valueOf(signal1).split(",");
                    Toast.makeText(getApplicationContext(),"Saved to "+ getFilesDir() + "/"+ FILE_NAME + Integer.toString(Room),Toast.LENGTH_LONG).show();
                    Save(file,saveText);
                    signal.clear();

                    average21 = (int) (10*Math.log10(total / splRoom2.size()));
                    measuredSPL1.setText(Integer.toString(average21));
                }*/
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

                        MeasureSPL.this.setPreferences();
                        finish();

                    }
                };
        finishMeasure.setOnClickListener(setFinishBtnListener);

/*        plotT=(Button)findViewById(R.id.plotT);
        plotT.setEnabled(false);
        Button.OnClickListener setPlotTListener =
                new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        tOrFFT = 1;
                        MeasureSPL.this.setPreferences();
                        MeasureSPL.this.plot();
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
                        MeasureSPL.this.setPreferences();
                        MeasureSPL.this.plot();
                    }
                };
        plotFFT.setOnClickListener(setPlotFFTListener);*/

    }

    public void plot () {
        Intent plotIntent = new Intent(MeasureSPL.this, Plot.class);
        /*plotIntent.putIntegerArrayListExtra("plotData", signal1);*/
        plotIntent.putExtra("SignalLength",signal1.size());
        plotIntent.putExtra("plotType", tOrFFT);
        startActivity(plotIntent);
    }


    public String checkDigit(int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    private int startMeasure() {
        int counter1 = counter;
        return counter1;
    }

    private ArrayList<Integer> stopMeasure(ArrayList<Integer> list, int counter1) {
        int stop = DBmeasure.size() - counter1;
        for (int i = 0; i < stop; i++) {
            list.add(i, DBmeasure.get(counter1 + i));
        }
        return list;
    }

    private ArrayList<Integer> stopSignal(ArrayList<Integer> saveText, int signalStart) {
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
                signal.add((int) audioFrame[i]);
                rms += audioFrame[i]*audioFrame[i];
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
            /*Log.v(TAG, "Level bar update collision, i.e. update took longer " +
                    "than 20ms. Collision count" + Double.toString(mDrawingCollided));*/
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

        editor.putFloat("mGainDif", gain);
        editor.putString("filename", FILE_NAME);
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
        readPreferences();
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