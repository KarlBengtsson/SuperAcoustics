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
    private String FILE_NAME = "";
    double[] Y;
    double[] x;
    double yy;
    private String REPOSITORY_NAME;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_layout);
        readPreferences();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSignLength = extras.getInt("SignalLength",0);
            //The key argument here must match that used in the other activity
        }

        Y = new double[mSignLength];
        x = new double[mSignLength];

        File file = new File(path + "/"+FILE_NAME+Integer.toString(Room)+".txt");
        String [] saveText = Load(file);
        grapher_two(saveText);



    }




/*    public void grapher(String[] yString) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);

// activate horizontal scrolling
        graph.getViewport().setScrollable(true);

// activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);

// activate vertical scrolling
        graph.getViewport().setScrollableY(true);


        series1 = new LineGraphSeries<>();
        double  t = 0;
        for (int q = 0; q < mSignLength; q++) {
            Y[q] = Double.parseDouble(yString[q]);
            yy += Y[q]*Y[q];
            t += 1;
            x[q] = t/mSampleRate;
        }
        // compute normalized value
        yy = Math.sqrt(yy);
        for (int q = 0; q < mSignLength; q++) {
        Y[q] = Y[q]/yy;
        }

        for (int g = 0; g<mSignLength; g++) {
            series1.appendData(new DataPoint(x[g], Y[g]), true, mSignLength);
        }
        graph.addSeries(series1);



    }*/


    public void grapher_two(String[] yString) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);

// activate horizontal scrolling
        graph.getViewport().setScrollable(true);

// activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);

// activate vertical scrolling
        graph.getViewport().setScrollableY(true);

        int numUniquePoints = (int) Math.ceil(((double) mSignLength+1)/2);

        series1 = new LineGraphSeries<>();
        double  t = 0;
        for (int q = 0; q < mSignLength; q++) {
            Y[q] = Double.parseDouble(yString[q]);
            yy += Y[q]*Y[q];
            t += 1;
            x[q] = t/mSampleRate;
        }

        // compute the nyquist frequency
        double Fn = 1/(x[2]-x[1])/2;


        // compute normalized value
        yy = Math.sqrt(yy);
        for (int q = 0; q < mSignLength; q++) {
            Y[q] = Y[q]/yy;
        }

                double[] input = Y;

                Complex[] cinput = new Complex[input.length];
                for (int i = 0; i < input.length; i++)
                    cinput[i] = new Complex(input[i], 0.0);

                FFT.fft(cinput);

                double[] YY = new double[mSignLength];
                double[] XX = new double[mSignLength];
                for (int l = 0 ; l < input.length; l++) {
                    System.out.println(cinput[l]);
                   YY[l] = Math.pow(cinput[l].im,2.0)+Math.pow(cinput[l].re,2.0);
                }
                XX = linspace(0.0,mSampleRate/2,mSignLength-1);

        for (int g = 0; g<mSignLength; g++) {
            series1.appendData(new DataPoint(XX[g], YY[g]), true, mSignLength);
        }
        graph.addSeries(series1);



    }

    public static double[] linspace(double min, double max, int points) {
        double[] d = new double[points];
        for (int i = 0; i < points; i++){
            d[i] = min + i * (max - min) / (points - 1);
        }
        return d;
    }













    private void readPreferences() {
        SharedPreferences preferences = getSharedPreferences("LevelMeter",
                MODE_PRIVATE);
        mSampleRate = preferences.getInt("SampleRate", 8000);
        Room = preferences.getInt("ROOM" , 0);
        FILE_NAME = preferences.getString("filename", "");
        REPOSITORY_NAME = preferences.getString("foldername", "");
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/LevelMeter/" + REPOSITORY_NAME;
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

