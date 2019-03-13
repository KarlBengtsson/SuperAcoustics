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
    private int mCompLength;
    private int counter4;
    private int plotType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_layout);
        readPreferences();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mSignLength = extras.getInt("SignalLength",0);
            plotType = extras.getInt("plotType",0);
            //The key argument here must match that used in the other activity
        }

        Y = new double[mSignLength];
        x = new double[mSignLength];

        File file = new File(path + "/"+FILE_NAME+Integer.toString(Room)+Integer.toString(counter4)+".txt");
        String [] saveText = Load(file);
        if (plotType == 1)
        {
            grapher(saveText);
        }
        else
            {
            grapher_two(saveText);
        }



    }




    public void grapher(String[] yString) {

        GraphView graph = (GraphView) findViewById(R.id.graph);
        // activate horizontal zooming and scrolling
        graph.getViewport().setScalable(true);

        // activate horizontal scrolling
        graph.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);

        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);

        // change x-label and y-label
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time [s]");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Amplitude [unit?]");
        // set plot title
        graph.setTitle("Time signal");


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

        // set maximum x-value for s
        graph.getViewport().setMaxX(x[x.length-1]);
        graph.addSeries(series1);



    }


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


        // change x-label and y-label
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Frequency [hz]");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Amplitude [unit?]");

        // set plot title
        graph.setTitle("FFT of time Signal");

        // set maximum x-value for hz
        graph.getViewport().setMaxX(mSampleRate/2);


        series1 = new LineGraphSeries<>();
        double  t = 0;
        mCompLength = highestPowerof2(mSignLength);
        for (int q = 0; q < mCompLength; q++) {
            Y[q] = Double.parseDouble(yString[q]);
            yy += Y[q]*Y[q];
            t += 1;
            x[q] = t/mSampleRate;
        }

        // compute the nyquist frequency
        int Fn = (int) (1/(x[2]-x[1])/2);



                double[] input = new double[mCompLength];
                double[] im = new double[mCompLength];


                for (int i = 0; i < mCompLength; i++) {
                    im[i]=0; input[i]=Y[i];
                }
                FFT fft = new FFT(mCompLength);
                fft.fft(input,im);


                int numUniquePoints = (int) Math.ceil(((double) mCompLength+1)/2);
                // throw away second half and take the magnitude at the same time
                double[] Cinput = new double[numUniquePoints];
                for (int r = 0; r<numUniquePoints; r++) {
                    Cinput[r] = Math.sqrt((Math.pow(input[r],2)+Math.pow(im[r],2)))*2/mCompLength;
                }
                // account for endpoint uniqueness
                Cinput[0]=Cinput[0]/2;
                Cinput[Cinput.length-1]=Cinput[Cinput.length-1]/2;
                System.out.println("Length:" + Cinput.length);
                double[] XX;



                XX = linspace(0.0,numUniquePoints,numUniquePoints);

                // frequencies
                for (int d = 0; d<numUniquePoints; d++){
                    // strange that the factor 4 seemed to work
                    XX[d] = XX[d]*2*Fn/mCompLength;
                }


        for (int g = 0; g<numUniquePoints; g++) {
            series1.appendData(new DataPoint(XX[g], Cinput[g]), true, numUniquePoints);
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
        counter4 = preferences.getInt("mCount", 0);
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

    static int highestPowerof2(int n)
    {
        int res = 0;
        for (int i = n; i >= 1; i--)
        {
            // If i is a power of 2
            if ((i & (i - 1)) == 0)
            {
                res = i;
                break;
            }
        }
        return res;
    }


}




