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
import java.util.Locale;

public class ViewResult extends AppCompatActivity {

    // For saving and loading .txt file

    //public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spartest";
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
        file = new File(Environment.getExternalStorageDirectory() + File.separator + "SuperAcoustics" + File.separator + roomName);
        roomOneResult();
        roomTwoResult();
        reverbResult();
        calcResult();
    }

    private void roomOneResult() {
        File file = new File(path + "/"+"TestRoom"+"1"+"1"+".txt");
        String [] saveText = Load(file);
        String stringen = "Frequency:    Sound Pressure Level:  \n";
        for (int i = 0; i<32; i++){
            stringen += saveText[i] +" Hz                \t\t\t\t" + saveText[i+2] + "  dB   \n";
        }
        resultSPL1.setText(stringen);
    }

    private void roomTwoResult() {
        File file = new File(path + "/"+"TestRoom"+"1"+"1"+".txt");
        String [] saveText = Load(file);
        String stringen = "Frequency:    Sound Pressure Level:  \n";
        for (int i = 0; i<32; i++){
            stringen += saveText[i] +" Hz                \t\t\t\t" + saveText[i+2] + "  dB   \n";
        }
        resultSPL2.setText(stringen);
    }

    private void reverbResult() {
        File file = new File(path + "/"+"TestRoom"+"1"+"1"+".txt");
        String [] saveText = Load(file);
        String stringen = "Frequency:    Reverberation time:  \n";
        for (int i = 0; i<6; i++){
            stringen += saveText[i] +" Hz                \t\t\t\t" + saveText[i+2] + "  s   \n";
        }
        resultreverb.setText(stringen);
    }

    private void calcResult() {
        area = (0.163*volume)/reverb;
        double X = 10*Math.log10(sArea/area);
        double R = result1 - result2 + X;
        R = Math.round(R * 10000d) / 10000d;


        // Loading the saved txt file and reading into viewResult window

        File file = new File(path + "/"+"TestRoom"+"1"+"1"+".txt");
        String [] saveText = Load(file);
        String stringen = "Frequency:    Sound Reduction index:  \n";
        for (int i = 0; i<32; i++){
            stringen += saveText[i] +" Hz                \t\t\t\t" + saveText[i+2] + "  dB   \n";
        }
        resultSRI.setText(stringen);
        /*resultSRI.setText(Double.toString(R) + " dB");*/
    }

    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        roomName = preferences.getString("foldername", null);
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

        resultSPL1.setText(dBformat(result1) + " dB");
        resultSPL2.setText(dBformat(result2) + " dB");
        resultreverb.setText(Float.toString(reverb) + " seconds");
    }

    private String dBformat(double dB) {
        // stop the recording log file
        return String.format(Locale.ENGLISH, "%.1f", dB);
    }

    public void readFileFromTxtRoom1() {
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
    }

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
