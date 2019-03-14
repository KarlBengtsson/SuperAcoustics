package com.example.myapplication;

class Banding {


    private int max;
    double mGain = 2500.0 / Math.pow(10.0, 90.0 / 20.0);
    private final int numUniqPts;
    private final int mSampleRate;
    private final int mCompLength;
    private final double[] xvec;
    private final double[] freqs ={16,31.5,63,125,250,500,1000,2000,4000,8000,16000};

    public Banding () {
        this(0,0,0);

    }

    public Banding(int mSamp, int numUniq, int mComp) {
        numUniqPts = numUniq;
        mSampleRate = mSamp;
        max = 0;
        mCompLength = mComp;
        int Fn = mSampleRate/2;
        xvec = linspace(0.0,numUniq-1,numUniq);

        // frequencies
        for (int d = 0; d<numUniq; d++){
            xvec[d] = xvec[d]*2*Fn/mCompLength;
        }
    }

    public double[] bandAmp (double[] fftVec, int nBands) {
        int fftLength = fftVec.length;
        setMax(mSampleRate);

        switch (nBands) {
            case 8:
                int counter = 0;
                double[] bandsAmp1 = new double[nBands];

            int I = 0;
            for (int i = 0; xvec[i]<=22; i++) {
                I++;
                bandsAmp1[0]+= fftVec[i]*fftVec[i];
                counter = i;
            }
            bandsAmp1[0] = Math.sqrt(bandsAmp1[0]/I);


                int J = 0;
                for (int i = counter; xvec[i]<=44; i++) {
                    J++;
                    bandsAmp1[1]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[1] = Math.sqrt(bandsAmp1[1]/J);

                int K = 0;
                for (int i = counter; xvec[i]<=88; i++) {
                    K++;
                    bandsAmp1[2]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[2] = Math.sqrt(bandsAmp1[2]/K);

                int L = 0;
                for (int i = counter; xvec[i]<=177; i++) {
                    L++;
                    bandsAmp1[3]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[3] = Math.sqrt(bandsAmp1[3]/L);

                int M = 0;
                for (int i = counter; xvec[i]<=355; i++) {
                    M++;
                    bandsAmp1[4]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[4] = Math.sqrt(bandsAmp1[4]/M);

                int N = 0;
                for (int i = counter; xvec[i]<=710; i++) {
                    N++;
                    bandsAmp1[5]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[5] = Math.sqrt(bandsAmp1[5]/N);

                int O = 0;
                for (int i = counter; xvec[i]<=1420; i++) {
                    O++;
                    bandsAmp1[6]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[6] = Math.sqrt(bandsAmp1[6]/O);

                int P = 0;
                for (int i = counter; xvec[i]<=2840; i++) {
                    P++;
                    bandsAmp1[7]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp1[7] = Math.sqrt(bandsAmp1[7]/P);


                return bandsAmp1;

            case 9:

                counter = 0;
                double[] bandsAmp2 = new double[nBands];

                I = 0;
                for (int i = 0; xvec[i]<=22; i++) {
                    I++;
                    bandsAmp2[0]+= fftVec[i]*fftVec[i];
                }
                bandsAmp2[0] = Math.sqrt(bandsAmp2[0]/I);


                J = 0;
                for (int i = I; xvec[i]<=44; i++) {
                    J++;
                    bandsAmp2[1]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[1] = Math.sqrt(bandsAmp2[1]/J);

                K = 0;
                for (int i = counter; xvec[i]<=88; i++) {
                    K++;
                    bandsAmp2[2]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[2] = Math.sqrt(bandsAmp2[2]/K);

                L = 0;
                for (int i = counter; xvec[i]<=177; i++) {
                    L++;
                    bandsAmp2[3]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[3] = Math.sqrt(bandsAmp2[3]/L);

                M = 0;
                for (int i = counter; xvec[i]<=355; i++) {
                    M++;
                    bandsAmp2[4]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[4] = Math.sqrt(bandsAmp2[4]/M);

                N = 0;
                for (int i = counter; xvec[i]<=710; i++) {
                    N++;
                    bandsAmp2[5]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[5] = Math.sqrt(bandsAmp2[5]/N);

                O = 0;
                for (int i = counter; xvec[i]<=1420; i++) {
                    O++;
                    bandsAmp2[6]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[6] = Math.sqrt(bandsAmp2[6]/O);

                P = 0;
                for (int i = counter; xvec[i]<=2840; i++) {
                    P++;
                    bandsAmp2[7]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[7] = Math.sqrt(bandsAmp2[7]/P);

                int Q = 0;
                for (int i = counter; xvec[i]<=5680; i++) {
                    Q++;
                    bandsAmp2[8]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp2[8] = Math.sqrt(bandsAmp2[8]/Q);




                return bandsAmp2;

            case 10:

                counter = 0;
                double[] bandsAmp3 = new double[nBands];

                I = 0;
                for (int i = 0; xvec[i]<=22; i++) {
                    I++;
                    bandsAmp3[0]+= fftVec[i]*fftVec[i];
                }
                bandsAmp3[0] = Math.sqrt(bandsAmp3[0]/I);


                J = 0;
                for (int i = I; xvec[i]<=44; i++) {
                    J++;
                    bandsAmp3[1]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[1] = Math.sqrt(bandsAmp3[1]/J);

                K = 0;
                for (int i = counter; xvec[i]<=88; i++) {
                    K++;
                    bandsAmp3[2]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[2] = Math.sqrt(bandsAmp3[2]/K);

                L = 0;
                for (int i = counter; xvec[i]<=177; i++) {
                    L++;
                    bandsAmp3[3]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[3] = Math.sqrt(bandsAmp3[3]/L);

                M = 0;
                for (int i = counter; xvec[i]<=355; i++) {
                    M++;
                    bandsAmp3[4]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[4] = Math.sqrt(bandsAmp3[4]/M);

                N = 0;
                for (int i = counter; xvec[i]<=710; i++) {
                    N++;
                    bandsAmp3[5]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[5] = Math.sqrt(bandsAmp3[5]/N);

                O = 0;
                for (int i = counter; xvec[i]<=1420; i++) {
                    O++;
                    bandsAmp3[6]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[6] = Math.sqrt(bandsAmp3[6]/O);

                P = 0;
                for (int i = counter; xvec[i]<=2840; i++) {
                    P++;
                    bandsAmp3[7]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[7] = Math.sqrt(bandsAmp3[7]/P);

                Q = 0;
                for (int i = counter; xvec[i]<=5680; i++) {
                    Q++;
                    bandsAmp3[8]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[8] = Math.sqrt(bandsAmp3[8]/Q);


                int R = 0;
                for (int i = counter; xvec[i]<=11360; i++) {
                    R++;
                    bandsAmp3[9]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp3[9] = Math.sqrt(bandsAmp3[9]/R);




                return bandsAmp3;

            case 11:

                counter = 0;
                double[] bandsAmp4 = new double[nBands];

                I = 0;
                for (int i = 0; xvec[i]<=22; i++) {
                    I++;
                    bandsAmp4[0]+= fftVec[i]*fftVec[i];
                }
                bandsAmp4[0] = Math.sqrt(bandsAmp4[0]/I);


                J = 0;
                for (int i = I; xvec[i]<=44; i++) {
                    J++;
                    bandsAmp4[1]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[1] = Math.sqrt(bandsAmp4[1]/J);

                K = 0;
                for (int i = counter; xvec[i]<=88; i++) {
                    K++;
                    bandsAmp4[2]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[2] = Math.sqrt(bandsAmp4[2]/K);

                L = 0;
                for (int i = counter; xvec[i]<=177; i++) {
                    L++;
                    bandsAmp4[3]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[3] = Math.sqrt(bandsAmp4[3]/L);

                M = 0;
                for (int i = counter; xvec[i]<=355; i++) {
                    M++;
                    bandsAmp4[4]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[4] = Math.sqrt(bandsAmp4[4]/M);

                N = 0;
                for (int i = counter; xvec[i]<=710; i++) {
                    N++;
                    bandsAmp4[5]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[5] = Math.sqrt(bandsAmp4[5]/N);

                O = 0;
                for (int i = counter; xvec[i]<=1420; i++) {
                    O++;
                    bandsAmp4[6]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[6] = Math.sqrt(bandsAmp4[6]/O);

                P = 0;
                for (int i = counter; xvec[i]<=2840; i++) {
                    P++;
                    bandsAmp4[7]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[7] = Math.sqrt(bandsAmp4[7]/P);

                Q = 0;
                for (int i = counter; xvec[i]<=5680; i++) {
                    Q++;
                    bandsAmp4[8]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[8] = Math.sqrt(bandsAmp4[8]/Q);


                R = 0;
                for (int i = counter; xvec[i]<=11360; i++) {
                    R++;
                    bandsAmp4[9]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[9] = Math.sqrt(bandsAmp4[9]/R);


                int S = 0;
                for (int i = counter; xvec[i]<=22720; i++) {
                    S++;
                    bandsAmp4[10]+= fftVec[i]*fftVec[i];
                    counter = i;
                }
                bandsAmp4[10] = Math.sqrt(bandsAmp4[10]/S);


                return bandsAmp4;
        }

        return null;
    }


    public double[] getFreqs (int nBands){

        switch (nBands) {

            case 8:

            double[] freqlist1 = new double[nBands];
            for (int i = 0; i < nBands; i++) {
                freqlist1[i] = freqs[i];
            }
            return freqlist1;

            case 9:
            double[] freqlist2 = new double[nBands];
            for (int i = 0; i < nBands; i++) {
                freqlist2[i] = freqs[i];
            }
            return freqlist2;


            case 10:
            double[] freqlist3 = new double[nBands];
            for (int i = 0; i < nBands; i++) {
                freqlist3[i] = freqs[i];
            }
            return freqlist3;


            case 11:
            double[] freqlist4 = new double[nBands];
            for (int i = 0; i < nBands; i++) {
                freqlist4[i] = freqs[i];
            }
            return freqlist4;

        }
        return null;
    }









    public void setMax(int mSampleRate) {
        this.max = mSampleRate/2;
    }





    public int getMax(int mSampleRate) {
        int max = 0;
        max = mSampleRate/2;

        return max;
    }

    public int getNBands(int mSampleRate) {
        int nBands = 0;
        switch (mSampleRate) {
            case 8000 :
                nBands = 8;

                break;
            case 16000 :
                nBands = 9;

                break;
            case 24000 :
                nBands = 10;

                break;
            case 48000 :
                nBands = 11;

                break;
        }





        return nBands;
    }


    public static double[] linspace(double min, double max, int points) {
        double[] d = new double[points];
        for (int i = 0; i < points; i++){
            d[i] = min + i * (max - min) / (points - 1);
        }
        return d;
    }


    public static double[] getdB(double[] amp, double mGain){
        double[] rmsdB = new double[amp.length];
        for (int i = 0; i < amp.length; i++)
        {
        rmsdB[i] = 20.0 * Math.log10(mGain * amp[i]);
        }
        return rmsdB;
    }

}
