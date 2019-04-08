package com.example.myapplication;

/**
 * Created by stefmase on 16/04/2015.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

/**
 * Created by eugeniosoddu on 27/03/15.
 */
public class PlotFFT extends View {

    private Paint paintLines1, paintLabelsY, paintLabelsX, paintLabelsMax,paintLabelsMaxRectFill,paintLabelsMaxRectStroke,paintLinear, paintWeight, paintLines2;
    private Paint paintLegend,paintLegendRectFill,paintLegendRectStroke;
    //private float db1, db2;
    private Path path;
    private float[] inData1, inData2;
    private int BAND_NUMBER;
    private double BAND_WIDTH;

    private String LAeqTimeDisplay,LeqTimeDisplay;

    private float fontSize;

    public PlotFFT(Context context) {
        this(context, null, 0);
    }

    public PlotFFT(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlotFFT(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paintLines1 = new Paint();
//        paintLines1.setColor(getResources().getColor(R.color.plot_grey_light));
        paintLines1.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLines1.setStrokeWidth(1.0f);

        paintLines2 = new Paint();
//        paintLines2.setColor(getResources().getColor(R.color.plot_grey_dark));
        paintLines2.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLines2.setStrokeWidth(1.0f);

        paintLabelsY = new Paint();
//        paintLabelsY.setColor(getResources().getColor(R.color.plot_text));
        paintLabelsY.setTextSize(20);
        paintLabelsY.setTextAlign(Paint.Align.RIGHT);

        paintLabelsX = new Paint();
//        paintLabelsX.setColor(getResources().getColor(R.color.plot_text));
        paintLabelsX.setTextSize(20);
        paintLabelsX.setTextAlign(Paint.Align.CENTER);

        paintLinear = new Paint();
        paintLinear.setColor(getResources().getColor(R.color.plot_cyan));
        paintLinear.setStyle(Paint.Style.STROKE);
        paintLinear.setStrokeWidth(3.0f);

        paintWeight = new Paint();
        paintWeight.setColor(getResources().getColor(R.color.plot_red));
        paintWeight.setStyle(Paint.Style.STROKE);
        paintWeight.setStrokeWidth(3.0f);

        paintLabelsMax = new Paint();
        paintLabelsMax.setTextSize(20);
        paintLabelsMax.setTextAlign(Paint.Align.RIGHT);

        paintLabelsMaxRectFill = new Paint();
//        paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.background_material_light));
        paintLabelsMaxRectFill.setStyle(Paint.Style.FILL);

        paintLabelsMaxRectStroke = new Paint();
        paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.plot_red));
        paintLabelsMaxRectStroke.setStyle(Paint.Style.STROKE);
        paintLabelsMaxRectStroke.setStrokeWidth(2.0f);

        paintLegend = new Paint();
        paintLegend.setTextSize(20);
        paintLegend.setTextAlign(Paint.Align.RIGHT);

        paintLegendRectFill = new Paint();
//        paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.background_material_light));
        paintLegendRectFill.setStyle(Paint.Style.FILL);

        paintLegendRectStroke = new Paint();
        paintLegendRectStroke.setColor(getResources().getColor(R.color.plot_red));
        paintLegendRectStroke.setStyle(Paint.Style.STROKE);
        paintLegendRectStroke.setStrokeWidth(2.0f);


        // color with different style
        /*final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String display_color = preferences.getString("display_color", "2");*/
        String display_color = "1";

        if (display_color.equals("1")){
            paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.app_grey_light_light));
            paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.app_grey_dark));
            paintLegendRectFill.setColor(getResources().getColor(R.color.app_grey_light_light));
            paintLegendRectStroke.setColor(getResources().getColor(R.color.app_grey_dark));
            paintLabelsX.setColor(getResources().getColor(R.color.app_grey_dark));
            paintLabelsY.setColor(getResources().getColor(R.color.app_grey_dark));
            paintLines2.setColor(getResources().getColor(R.color.app_grey_dark));
            paintLines1.setColor(getResources().getColor(R.color.app_grey_light));

        } else if (display_color.equals("2")) {
            paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.app_grey_dark_dark));
            paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.app_grey_light));
            paintLegendRectFill.setColor(getResources().getColor(R.color.app_grey_dark_dark));
            paintLegendRectStroke.setColor(getResources().getColor(R.color.app_grey_light));
            paintLabelsX.setColor(getResources().getColor(R.color.app_grey_light));
            paintLabelsY.setColor(getResources().getColor(R.color.app_grey_light));
            paintLines2.setColor(getResources().getColor(R.color.app_grey_light));
            paintLines1.setColor(getResources().getColor(R.color.app_grey_dark));

        } else if (display_color.equals("3")) {
            paintLabelsMaxRectFill.setColor(getResources().getColor(R.color.app_black));
            paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.app_grey_light));
            paintLegendRectFill.setColor(getResources().getColor(R.color.app_black));
            paintLegendRectStroke.setColor(getResources().getColor(R.color.app_grey_light));
            paintLabelsX.setColor(getResources().getColor(R.color.app_white));
            paintLabelsY.setColor(getResources().getColor(R.color.app_white));
            paintLines2.setColor(getResources().getColor(R.color.app_grey_light));
            paintLines1.setColor(getResources().getColor(R.color.app_grey_dark));
        }

        /*String timeDisplayString = preferences.getString("timeDisplay", "1");*/
        String timeDisplayString = "1";
        if  (timeDisplayString.equals("0.5")) {
            LAeqTimeDisplay = this.getContext().getResources().getString(R.string.LAeqTimeDisplay_label) + "(0.5 s)";
            LeqTimeDisplay = this.getContext().getResources().getString(R.string.LeqTimeDisplay_label) + "(0.5 s)";
        } else if (timeDisplayString.equals("1")) {
            LAeqTimeDisplay = this.getContext().getResources().getString(R.string.LAeqTimeDisplay_label) + "(1 s)";
            LeqTimeDisplay = this.getContext().getResources().getString(R.string.LeqTimeDisplay_label) + "(1 s)";
        } else if (timeDisplayString.equals("2")) {
            LAeqTimeDisplay = this.getContext().getResources().getString(R.string.LAeqTimeDisplay_label) + "(2 s)";
            LeqTimeDisplay = this.getContext().getResources().getString(R.string.LeqTimeDisplay_label) + "(2 s)";
        }


        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();

        fontSize = w * 0.04f;
        paintLabelsX.setTextSize(fontSize);
        paintLabelsY.setTextSize(fontSize);
        paintLabelsMax.setTextSize(fontSize);
        paintLegend.setTextSize(fontSize);

        paintLinear.setStrokeWidth(3.0f);
        paintWeight.setStrokeWidth(3.0f);

        float h = getHeight();
        float w_plot = getWidth() - paintLabelsY.measureText(Float.toString(paintLabelsY.getTextSize()));
        float deltaLabelX = (paintLabelsX.descent() - paintLabelsX.ascent());
        float deltaLabelY = (paintLabelsY.descent() - paintLabelsY.ascent());
        float deltaLabelMax = (paintLabelsMax.descent() - paintLabelsMax.ascent());
        float h_plot = getHeight() - deltaLabelX - deltaLabelY;
        float yMax = 110f;
        float dbyMaxIst = 0;
        float dbyMaxIstA = 0;
        float xMaxIst = 0;
        float xMaxIstA = 0;
        float yMaxIst = 0;
        float yMaxIstA = 0;
        int iMaxIst = 0;
        int iMaxIstA = 0;

        // grafico FFT non pesato
        if (inData1 != null) {

            path.rewind();
            path.moveTo(w - w_plot, deltaLabelY + h_plot);
//            path.moveTo(w - w_plot, deltaLabelY + h_plot - (inData1[0] * h_plot / yMax));
            for (int i = 0; i < inData1.length; i++) {
                float x =  w - w_plot + w_plot
                        * (float) (Math.log(i + 1.0) / Math.log(inData1.length));
                float y = inData1[i] * h_plot / yMax;
                if (Float.isInfinite(y) || Float.isNaN(y))
                    y = 0;

                // parte per 'nascondere' problema i=0 con valore non valido per la parte pesata
                // metto anche la fft non pesata con y=0 così i grafici partono dallo stesso punto
                // vedi sotto parte commentata e main activity
                if (i==0){
                    y = 0;
                }

                y = deltaLabelY + h_plot - y;

                if(dbyMaxIst < inData1[i]){
                    xMaxIst = x;
                    yMaxIst = y;
                    dbyMaxIst = inData1[i];
                    iMaxIst = i;
                }

                path.lineTo(x, y);
            }
            canvas.drawPath(path, paintLinear);
        }

        // grafico FFT pesato
        if (inData2 != null) {

            path.rewind();
//            path.moveTo(w - w_plot, deltaLabelY + h_plot);
            path.moveTo(w - w_plot, deltaLabelY + h_plot);

            for (int i = 0; i < inData2.length; i++) {
                float x = w - w_plot + w_plot
                        * (float) (Math.log(i + 1.0) / Math.log(inData2.length));
                float y = inData2[i] * h_plot / yMax;
                if (Float.isInfinite(y) || Float.isNaN(y))
                    y = 0;
                y = deltaLabelY + h_plot - y;

                if(dbyMaxIstA < inData2[i]){
                    xMaxIstA = x;
                    yMaxIstA = y;
                    dbyMaxIstA = inData2[i];
                    iMaxIstA = i;
                }

                path.lineTo(x, y);
            }
            canvas.drawPath(path, paintWeight);

            // Linee orizzontali
            for (int i = 5; i <= yMax; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMax, w, deltaLabelY + h_plot - i * h_plot / yMax, paintLines1);
            }
            for (int i = 0; i <= yMax; i += 10) {
                canvas.drawLine(w - w_plot, deltaLabelY + h_plot - i * h_plot / yMax, w, deltaLabelY + h_plot - i * h_plot / yMax, paintLines2);
                canvas.drawText("" + i, w - w_plot - 5, deltaLabelY + h_plot - i * h_plot / yMax + paintLabelsX.descent(), paintLabelsY);
            }

            // Linee verticali
            int [] vertical_lines1 = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 600, 700, 800, 900,
                    1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000};

            for (int line = 0; line < vertical_lines1.length; line++) {

                float i = vertical_lines1[line] /  (float) (BAND_WIDTH);

                float x = w - w_plot + w_plot * (float) (Math.log(i + 1) / Math.log(inData2.length));
                canvas.drawLine(x, deltaLabelY + 0, x, deltaLabelY + h_plot, paintLines1);
            }

            int [] vertical_lines2 = {0, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000};
            String [] vertical_lines_label = {"0", "20", "50", "100", "200", "500", "1k", "2k","5k", "10k", "20k"};

            for (int line = 0; line < vertical_lines2.length; line++) {

                float i = vertical_lines2[line] /  (float) (BAND_WIDTH);

                float x = w - w_plot + w_plot * (float) (Math.log(i + 1) / Math.log(inData2.length));
                canvas.drawText("" + vertical_lines_label[line], x, deltaLabelY + h_plot - paintLabelsX.ascent(), paintLabelsX);
                canvas.drawLine(x, deltaLabelY + 0, x, deltaLabelY + h_plot, paintLines2);
            }
            //canvas.drawText("" + paintLabelsX.descent(), w/2, h/2, paintLabelsX);
        }

//        // parte per verificare i valori fft
//        // per i= 0 c'è un problema con i dati fft pesati. Infatti per i=0 il valore non è valido (forse meno infinito)
//        // vedi main activity
//        int p=0;
//        if (inData1 != null) {
//            if (Float.isInfinite(inData1[p]) || Float.isNaN(inData1[p])) {
//                canvas.drawText("1)  0", w / 2, h_plot / 2, paintLabelsMax);
//            } else {
//                canvas.drawText("1)  "+dBformat(inData1[p]), w / 2, h_plot / 2, paintLabelsMax);
//            }
//        }
//        if (inData2 != null) {
//            if (Float.isInfinite(inData2[p]) || Float.isNaN(inData2[p])) {
//                canvas.drawText("2) 0", w / 2 , h_plot / 2+40, paintLabelsMax);
//            } else {
//                canvas.drawText("2)  "+dBformat(inData2[p]), w / 2, h_plot / 2+40, paintLabelsMax);
//            }
//        }
//        // fine parte per verificare i valori fft


//        canvas.drawText((inData2[0])+" ", w/2+20, h_plot/2+30, paintLabelsMax);


        float text_width = paintLegend.measureText("      " + LeqTimeDisplay + "   " + LAeqTimeDisplay + "  ");
        canvas.drawRect(w - text_width - 10, deltaLabelMax, w - 10, 10 + 2 * deltaLabelMax + paintLegend.descent(), paintLegendRectStroke);
        canvas.drawRect(w - text_width - 10, deltaLabelMax, w - 10, 10 + 2 * deltaLabelMax  + paintLegend.descent(), paintLegendRectFill);
        paintLegend.setColor(getResources().getColor(R.color.plot_cyan));
        canvas.drawText("  " + LeqTimeDisplay + " ", w - 10 - paintLegend.measureText("   " + LAeqTimeDisplay + "   "), 2 * deltaLabelMax, paintLegend);
        paintLegend.setColor(getResources().getColor(R.color.plot_red));
        canvas.drawText("  " + LAeqTimeDisplay + "  ", w - 10, 2 * deltaLabelMax, paintLegend);

        Path lineA = new Path();
        lineA.moveTo(xMaxIst-10, yMaxIst-10);
        lineA.lineTo(xMaxIst-30, yMaxIst-25);
        lineA.close();
        paintLinear.setStrokeWidth(1.0f);
        canvas.drawPath(lineA, paintLinear);

        fontSize = w * 0.03f;
        paintLabelsMax.setTextSize(fontSize);
        paintLabelsMax.setColor(getResources().getColor(R.color.plot_cyan));
        deltaLabelMax = (paintLabelsMax.descent() - paintLabelsMax.ascent());
        text_width = paintLabelsMax.measureText(" 200000 Hz ");
        paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.plot_cyan));
//        canvas.drawRect(xMaxIst - 30 - text_width, yMaxIst - 25 - 2 * deltaLabelMax, xMaxIst - 30, yMaxIst - 20, paintLabelsMaxRectStroke);
//        canvas.drawRect(xMaxIst - 30 - text_width, yMaxIst - 25 - 2 * deltaLabelMax, xMaxIst - 30, yMaxIst - 20, paintLabelsMaxRectFill);
//        canvas.drawText(dBformat(dbyMaxIstA) + " dB(A)  ", xMaxIst - 30 , yMaxIst -30 - deltaLabelMax, paintLabelsMax);
//        canvas.drawText(Bandformat(iMaxIstA*BAND_WIDTH) + " Hz  ", xMaxIst - 30, yMaxIst - 30, paintLabelsMax);
        canvas.drawRect(xMaxIst - 30 - text_width, yMaxIst - 25 - (float) 2.5 * deltaLabelMax, xMaxIst - 30, yMaxIst - 25, paintLabelsMaxRectStroke);
        canvas.drawRect(xMaxIst - 30 - text_width, yMaxIst - 25 - (float) 2.5 * deltaLabelMax, xMaxIst - 30, yMaxIst - 25, paintLabelsMaxRectFill);
        canvas.drawText(dBformat(dbyMaxIst) + " dB  ", xMaxIst - 30 , yMaxIst - 25 - (float) 1.5 * deltaLabelMax, paintLabelsMax);
        canvas.drawText(Bandformat(iMaxIst*BAND_WIDTH) + " Hz  ", xMaxIst - 30, yMaxIst - 25 - (float) 0.5 * deltaLabelMax, paintLabelsMax);

        Path line = new Path();
        line.moveTo(xMaxIstA-10, yMaxIstA+10);
        line.lineTo(xMaxIstA-30, yMaxIstA+15);
        line.close();
        paintWeight.setStrokeWidth(1.0f);
        canvas.drawPath(line, paintWeight);

        fontSize = w * 0.03f;
        paintLabelsMax.setTextSize(fontSize);
        paintLabelsMax.setColor(getResources().getColor(R.color.plot_red));
        deltaLabelMax = (paintLabelsMax.descent() - paintLabelsMax.ascent());
        text_width = paintLabelsMax.measureText("  999.9 dB(A) ");
        paintLabelsMaxRectStroke.setColor(getResources().getColor(R.color.plot_red));
        canvas.drawRect(xMaxIstA - 30 - text_width, yMaxIstA + 15 , xMaxIstA - 30, yMaxIstA + 15 + (float) 2.5 * deltaLabelMax, paintLabelsMaxRectStroke);
        canvas.drawRect(xMaxIstA - 30 - text_width, yMaxIstA + 15 , xMaxIstA - 30, yMaxIstA + 15 + (float) 2.5 * deltaLabelMax, paintLabelsMaxRectFill);
        canvas.drawText(dBformat(dbyMaxIstA) + " dB(A)  ", xMaxIstA - 30, yMaxIstA + 15 + (float) 1 * deltaLabelMax, paintLabelsMax);
        canvas.drawText(Bandformat(iMaxIstA*BAND_WIDTH) + " Hz  ", xMaxIstA - 30 , yMaxIstA + 15 + (float) 2 * deltaLabelMax, paintLabelsMax);




    }

    public void setDataPlot(int BLOCK_SIZE_FFT, double BAND_WIDTH, float[] data1, float[] data2) {

        this.BAND_NUMBER = BLOCK_SIZE_FFT / 2;
        this.BAND_WIDTH = BAND_WIDTH;
        //this.db1 = (float) Math.floor(db1 * 10) / 10;
        //this.db2 = (float) Math.floor(db2 * 10) / 10;
        if (inData1 == null || inData1.length != data1.length)
            inData1 = new float[data1.length];
        System.arraycopy(data1, 0, inData1, 0, data1.length);
        if (inData2 == null || inData2.length != data2.length)
            inData2 = new float[data2.length];
        System.arraycopy(data2, 0, inData2, 0, data2.length);
        invalidate();
    }

    private String dBformat(double dB) {
        // stop the recording log file
        return String.format(Locale.ENGLISH, "%.1f", dB);

    }
    private String Bandformat(double dB) {
        // stop the recording log file
        return String.format(Locale.ENGLISH, "%.0f", dB);

    }
}
