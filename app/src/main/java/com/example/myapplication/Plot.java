package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Plot  extends AppCompatActivity {
    private LineGraphSeries<DataPoint> series1;
    private int mSignLength;
    private double mSampleRate;
    private int Room;
    private ArrayList<Integer> y;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Spartest";
    private String FILE_NAME = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_layout);
        readPreferences();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            /*y = extras.getIntegerArrayList("plotData");*/
            mSignLength = extras.getInt("SignalLength",0);
            //The key argument here must match that used in the other activity
        }
        File dir = new File(path);
        dir.mkdirs();
        File file = new File(path + "/"+FILE_NAME+Integer.toString(Room)+".txt");
        String [] saveText = Load(file);
        grapher(saveText);



    }




    public void grapher(String[] yString) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series1 = new LineGraphSeries<>();
        double x, t = 0;

        for (int q = 0; q < mSignLength; q++) {
            double Y = Double.parseDouble(yString[q]);
            t += 1;
            x = t/mSampleRate;
            series1.appendData(new DataPoint(x,Y), true, mSignLength);
        }

        graph.addSeries(series1);



    }













    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        Room = preferences.getInt("ROOM" , 0);
        FILE_NAME = preferences.getString("filename", "");
    }

/*    private void setPreferences() {
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
    }*/


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
