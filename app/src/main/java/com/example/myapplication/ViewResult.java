package com.example.myapplication;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ViewResult extends AppCompatActivity {

    // For saving and loading .txt file

    public String path;
    //private String [] THIRD_OCTAVE_LABEL = {"16", "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500",
            //"630", "800", "1000", "1250", "1600", "2000", "2500", "3150", "4000", "5000", "6300", "8000", "10000", "12500", "16000", "20000"};
    private String [] THIRD_OCTAVE_LABEL = {"125", "250", "500", "1000", "2000", "4000"};
    // number 10, 13, 16, 19, 22, 25
    // indices 9, 12, 15, 18, 21, 24
    private int indices[] = {9, 12, 15, 18, 21, 24};
    private TextView resultSPL1;
    private TextView resultSPL2;
    private TextView resultCal;
    private TextView resultreverb;
    private TextView resultSRI;

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
    private float result1;
    private float result2;
    private float reverb;
    private double area;
    private double sArea;
    private double volume;
    private String roomName;
    private File file;
    private int fromCheck;
    private TextView result2SPL1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        resultSPL1 = (TextView)findViewById(R.id.resultRoom1TextView);
        resultSPL2 = (TextView)findViewById(R.id.resultRoom2TextView);
        resultCal = (TextView)findViewById(R.id.resultTextCal);
        resultreverb = (TextView)findViewById(R.id.resultRTTextView);
        resultSRI = (TextView)findViewById(R.id.resultSRITextView);
        readPreferences();
        if (fromCheck == 1) {
            // Coming from welcomeActivity
            readPreferences();
            roomTwoResult();
            roomOneResult();
            reverbResult();
        }
        else {
            // Coming from "View Results Button"
            DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
            path = String.format(Environment.getExternalStorageDirectory() + File.separator + "SuperAcoustics" + File.separator + roomName + "_%s" ,df.format(new Date()));
        }
        roomOneResult();
        roomTwoResult();
        reverbResult();
        calcResult();
    }

    private void roomOneResult() {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String filename = "SPL_Room1" + ".txt";
        file = new File(path + "/"+filename);
        if (file.exists()) {
            String [] saveText = Load(file);
            String stringen = "Frequency:    Sound Pressure Level:  \n";
            for (int i = 0; i<THIRD_OCTAVE_LABEL.length; i++){
            //for (int i = 0; i<saveText.length-1; i++){
                stringen += THIRD_OCTAVE_LABEL[i] +" Hz                \t\t\t\t" + saveText[i] + "  dB   \n";
            }
            resultSPL1.setText(stringen);
        }

    }

    private void roomTwoResult() {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String filename = "SPL_Room2" + ".txt";
        file = new File(path + "/"+filename);
        if (file.exists()) {
            String [] saveText = Load(file);
            String stringen = "Frequency:    Sound Pressure Level:  \n";
            for (int i = 0; i<THIRD_OCTAVE_LABEL.length; i++){
                //for (int i = 0; i<saveText.length-1; i++){
                stringen += THIRD_OCTAVE_LABEL[i] +" Hz                \t\t\t\t" + saveText[i] + "  dB   \n";
            }
            resultSPL2.setText(stringen);
        }
    }

    private void reverbResult() {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String filename = "Reverberation_time" + ".txt";
        file = new File(path + "/"+filename);
        if (file.exists()){
        String [] saveText = Load(file);
            String stringen = "Frequency:                        Time:  \n";
            for (int i = 0; i<THIRD_OCTAVE_LABEL.length; i++){
                stringen += THIRD_OCTAVE_LABEL[i] +" Hz                \t\t\t\t" + saveText[i] + "  seconds   \n";
            }
            resultreverb.setText(stringen);
        }
    }

    private void calcResult() {
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd");
        String filename = "SRI" + ".txt";
        file = new File(path + "/"+filename);
        if (file.exists()){
            String [] saveText = Load(file);
            String stringen = "Frequency:    Sound Reduction index:  \n";
            for (int i = 0; i<THIRD_OCTAVE_LABEL.length; i++){
                stringen += THIRD_OCTAVE_LABEL[i] +" Hz                \t\t\t\t" + saveText[i] + "  dB   \n";
            }
            resultSRI.setText(stringen);
        }

    }


    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
        gain = preferences.getFloat("mGainDif", 0);
        result1 = preferences.getFloat("mRoom1",0);
        result2 = preferences.getFloat("mRoom2",0);
        volume = preferences.getInt("volume", 0);
        sArea = preferences.getInt("area", 0);
        reverb = preferences.getFloat("reverb", 0);
        path = preferences.getString("loadpath",null);
        roomName = preferences.getString("foldername", null);
        fromCheck = preferences.getInt("fromCheck", 0);
        resultSPL1.setText(dBformat(result1) + " dB");
        resultSPL2.setText(dBformat(result2) + " dB");
        resultreverb.setText(Float.toString(reverb) + " seconds");
    }

    private String dBformat(double dB) {
        // stop the recording log file
        return String.format(Locale.ENGLISH, "%.1f", dB);
    }

/*    public void readFileFromTxtRoom1() {
        File file = new File(path + "TestFileRoom1.txt");
        String[] loadText = Load(file);
        String finalString = "";
        for (int i = 0; i < loadText.length; i++)
        {
            finalString += loadText[i] + System.getProperty("line.separator");
        }
        resultSPL1.setText(finalString + " dB");
    }
    public void readFileFromTxtRoom2() {
        File file = new File(path + "TestFileRoom2.txt");
        String[] loadText = Load(file);
        String finalString = "";
        for (int i = 0; i < loadText.length; i++)
        {
            finalString += loadText[i] + System.getProperty("line.separator");
        }
        resultSPL2.setText(finalString + " dB");
    }*/

    public static void Save(File file, String[] data)
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
        int anzahl=1;
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
                String str = line;
                array[i] = str.replace("[","").replace(",","").replace("]","");
                i++;
            }
        }
        catch (IOException e) {e.printStackTrace();}
        return array;
    }
}
