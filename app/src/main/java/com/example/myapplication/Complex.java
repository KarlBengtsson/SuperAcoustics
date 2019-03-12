package com.example.myapplication;

class Complex {
    public final double re;
    public final double im;
    public final double magn;

    public Complex() {
        this(0, 0);
    }

    public Complex(double r, double i) {
        re = r;
        im = i;
        magn = Math.sqrt(Math.pow(r,2)+Math.pow(i,2));
    }

    public Complex add(Complex b) {
        return new Complex(this.re + b.re, this.im + b.im);
    }

    public Complex sub(Complex b) {
        return new Complex(this.re - b.re, this.im - b.im);
    }

    public Complex mult(Complex b) {
        return new Complex(this.re * b.re - this.im * b.im,
                this.re * b.im + this.im * b.re);
    }


    @Override
    public String toString() {
        return String.format("(%f,%f)", re, im);
    }
}
