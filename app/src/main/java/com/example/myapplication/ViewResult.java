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

public class ViewResult extends AppCompatActivity {

    // For saving and loading .txt file

    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spartest";
    private TextView resultSPL1;
    private TextView resultSPL2;
    private TextView resultCal;

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
    private int result1;
    private int result2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_results);
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        resultSPL1 = (TextView)findViewById(R.id.resultTextSpl1);
        resultSPL2 = (TextView)findViewById(R.id.resultTextSpl2);
        resultCal = (TextView)findViewById(R.id.resultTextCal);
        readPreferences();

    }



    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        mAudioSource = preferences.getInt("AudioSource",
                MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mDifferenceFromNominal = preferences.getInt("mGainDif", 0);
        result1 = preferences.getInt("mRoom1",0);
        result2 = preferences.getInt("mRoom2",0);
        resultSPL1.setText(Integer.toString(result1) + " dB");
        resultSPL2.setText(Integer.toString(result2) + " dB");
        resultCal.setText(Double.toString(mDifferenceFromNominal) + " dB");
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
}
