/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;

/*
*   Class   Stat
*
*   USAGE:  Statistical functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002 as part of Fmath
*   AMENDED: 12 May 2003 Statistics separated out from Fmath as a new class
*   DATE:    18 June 2005, 5 January 2006, 25 April 2006, 12, 21 November 2006
*            4 December 2006 (renaming of cfd and pdf methods - older version also retained)
*            31 December 2006, March 2007, 14 April 2007, 19 October 2007, 27 February 2008
*            29 march 2008, 7 April 2008, 29 April 2008 - 13 May 2008, 22-31 May 2008,
*            4-10 June 2008, 27 June 2008, 2-5 July 2008, 23 July 2008, 31 July 2008,
*            2-4 August 2008,  20 August 2008, 5-10 September 2008, 19 September 2008, 28 September 2008
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) 2002 - 2008 Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
* Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
* provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
* and associated documentation or publications.
*
* Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice, this list of conditions
* and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
* Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
* the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission from the Michael Thomas Flanagan:
*
* Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
* Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
* or its derivatives.
*
***************************************************************************************/

public class StatUtils {

        protected ArrayList<Object> array = null;    // internal array

        // STATIC VARIABLES

        // A small number close to the smallest representable floating point number
        public static final double FPMIN = 1e-300;

        // PRIVATE MEMBERS FOR USE IN GAMMA FUNCTION METHODS AND HISTOGRAM CONSTRUCTION METHODS

        // GAMMA FUNCTIONS
        //  Lanczos Gamma Function approximation - N (number of coefficients -1)
        private static final int LGFN = 6;
        //  Lanczos Gamma Function approximation - Coefficients
        private static final double[] LGFCOEFF = {1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
        //  Lanczos Gamma Function approximation - small gamma
        private static final double LGFGAMMA = 5.0;
        //  Maximum number of iterations allowed in Incomplete Gamma Function calculations
        private static final int IGFITER = 1000;
        //  Tolerance used in terminating series in Incomplete Gamma Function calculations
        private static final double IGFEPS = 1e-8;

        // HISTOGRAM CONSTRUCTION
        //  Tolerance used in including an upper point in last histogram bin when it is outside due to rounding erors
        private static final double HISTOL = 1.0001D;

        // CONSTRUCTORS
        public StatUtils(){
             this.array = new ArrayList<Object>();
        }

        // Chi-Square Cumulative Distribution Function
        // probability that an observed chi-square value for a correct model should be less than chiSquare
        // nu  =  the degrees of freedom
        public static double chiSquareCDF(double chiSquare, int nu){
                if(nu<=0)throw new IllegalArgumentException("The degrees of freedom [nu], " + nu + ", must be greater than zero");
                return incompleteGamma((double)nu/2.0D, chiSquare/2.0D);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Retained for backward compatibility
        public static double incompleteGamma(double a, double x){
            return regularisedGammaFunction(a, x);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double regularizedGammaFunction(double a, double x){
            return regularisedGammaFunction(a, x);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double regularisedGammaFunction(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf;

                if(x < a+1.0D){
                        // Series representation
                        igf = incompleteGammaSer(a, x);
                }
                else{
                        // Continued fraction representation
                        igf = incompleteGammaFract(a, x);
                }
                return igf;
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Series representation of the function - valid for x < a + 1
        public static double incompleteGammaSer(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x>=a+1) throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");

                int i = 0;
                double igf = 0.0D;
                boolean check = true;

                double acopy = a;
                double sum = 1.0/a;
                double incr = sum;
                double loggamma = logGamma(a);

                while(check){
                        ++i;
                        ++a;
                        incr *= x/a;
                        sum += incr;
                        if(Math.abs(incr) < Math.abs(sum)*IGFEPS){
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                check = false;
                        }
                        if(i>=IGFITER){
                                check=false;
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                //System.out.println("\nMaximum number of iterations were exceeded in incompleteGammaSer().\nCurrent value returned.\nIncrement = "+String.valueOf(incr)+".\nSum = "+String.valueOf(sum)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                return igf;
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Continued Fraction representation of the function - valid for x >= a + 1
        // This method follows the general procedure used in Numerical Recipes for C,
        // The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/
        public static double incompleteGammaFract(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x<a+1) throw new IllegalArgumentException("\nx < a+1   Use Series Representation");

                int i = 0;
                double ii;
                double igf;
                boolean check = true;

                double loggamma = logGamma(a);
                double numer;
                double incr;
                double denom = x - a + 1.0D;
                double first = 1.0D/denom;
                double term = 1.0D/FPMIN;
                double prod = first;

                while(check){
                        ++i;
                        ii = (double)i;
                        numer = -ii*(ii - a);
                        denom += 2.0D;
                        first = numer*first + denom;
                        if(Math.abs(first) < FPMIN){
                            first = FPMIN;
                        }
                        term = denom + numer/term;
                        if(Math.abs(term) < FPMIN){
                            term = FPMIN;
                         }
                        first = 1.0D/first;
                        incr = first*term;
                        prod *= incr;
                        if(Math.abs(incr - 1.0D) < IGFEPS)check = false;
                        if(i>=IGFITER){
                                check=false;
                                //System.out.println("\nMaximum number of iterations were exceeded in incompleteGammaFract().\nCurrent value returned.\nIncrement - 1 = "+String.valueOf(incr-1)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                igf = 1.0D - Math.exp(-x+a*Math.log(x)-loggamma)*prod;
                return igf;
        }

        // log to base e of the Gamma function
        // Lanczos approximation (6 terms)
        // Retained for backward compatibility
        public static double logGamma(double x){
                double xcopy = x;
                double fg;
                double first = x + LGFGAMMA + 0.5;
                double second = LGFCOEFF[0];

                if(x>=0.0){
                        if(x>=1.0 && x-(int)x==0.0){
                                fg = logFactorial(x)-Math.log(x);
                        }
                        else{
                                first -= (x + 0.5)*Math.log(first);
                                for(int i=1; i<=LGFN; i++)second += LGFCOEFF[i]/++xcopy;
                                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
                        }
                }
                else{
                        fg = Math.PI/(gamma(1.0D-x)*Math.sin(Math.PI*x));

                        if(fg!=1.0/0.0 && fg!=-1.0/0.0){
                                if(fg<0){
                                         throw new IllegalArgumentException("\nThe gamma function is negative");
                                }
                                else{
                                        fg = Math.log(fg);
                                }
                        }
                }
                return fg;
        }

        // Gamma function
        // Lanczos approximation (6 terms)
        // retained for backward compatibity
        public static double gamma(double x){

                double xcopy = x;
                double first = x + LGFGAMMA + 0.5;
                double second = LGFCOEFF[0];
                double fg;

                if(x>=0.0){
                        if(x>=1.0D && x-(int)x==0.0D){
                                fg = factorial(x)/x;
                        }
                        else{
                                first = Math.pow(first, x + 0.5)*Math.exp(-first);
                                for(int i=1; i<=LGFN; i++)second += LGFCOEFF[i]/++xcopy;
                                fg = first*Math.sqrt(2.0*Math.PI)*second/x;
                        }
                }
                else{
                         fg = -Math.PI/(x*gamma(-x)*Math.sin(Math.PI*x));
                }
                return fg;
        }

        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
            if(n<0)throw new IllegalArgumentException("\nn, " + n + ", must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            for(int i=2; i<=n; i++)f+=Math.log(i);
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(long n){
            if(n<0)throw new IllegalArgumentException("\nn, " + n + ", must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            long iCount = 2L;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1L;
            }
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1.0D;
            }
            return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 1.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f*=iCount;
                iCount += 1.0D;
            }
            return f;
        }

         // Returns a binomial mass probabilty function
        public static double binomialPDF(double p, double n, double k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(logFactorial(n) - logFactorial(k) - logFactorial(n-k)))*Math.pow(p, k)*Math.pow(1.0D - p, n - k);
        }

        // HISTOGRAMS

        // Distribute data into bins to obtain histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                //	System.out.println((m-n) + " data points, above histogram upper limit, excluded in histogramBins");
                return histogramBins(newData, binWidth, binZero);
            }
            else{
                 return histogramBins(data, binWidth, binZero);

            }
        }

        // Distribute data into bins to obtain histogram
        // zero bin position provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero){
            double dmax = maximum(data);
            int nBins = (int) Math.ceil((dmax - binZero)/binWidth);
            if(binZero+nBins*binWidth>dmax)nBins++;
            int nPoints = data.length;
            int[] dataCheck = new int[nPoints];
            for(int i=0; i<nPoints; i++)dataCheck[i]=0;
            double[]binWall = new double[nBins+1];
            binWall[0]=binZero;
            for(int i=1; i<=nBins; i++){
                binWall[i] = binWall[i-1] + binWidth;
            }
            double[][] binFreq = new double[2][nBins];
            for(int i=0; i<nBins; i++){
                binFreq[0][i]= (binWall[i]+binWall[i+1])/2.0D;
                binFreq[1][i]= 0.0D;
            }
            boolean test;

            for(int i=0; i<nPoints; i++){
                test=true;
                int j=0;
                while(test){
                    if(j==nBins-1){
                        if(data[i]>=binWall[j] && data[i]<=binWall[j+1]*(1.0D + HISTOL)){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    else{
                        if(data[i]>=binWall[j] && data[i]<binWall[j+1]){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    if(test){
                        if(j==nBins-1){
                            test=false;
                        }
                        else{
                            j++;
                        }
                    }
                }
            }
            int nMissed=0;
            for(int i=0; i<nPoints; i++)if(dataCheck[i]==0){
                nMissed++;
                //System.out.println("p " + i + " " + data[i] + " " + binWall[0] + " " + binWall[nBins]);
            }
            // if(nMissed>0)System.out.println(nMissed+" data points, outside histogram limits, excluded in histogramBins");
            return binFreq;
        }

        // Distribute data into bins to obtain histogram
        // zero bin position calculated
        public static double[][] histogramBins(double[] data, double binWidth){

            double dmin = minimum(data);
            double dmax = maximum(data);
            double span = dmax - dmin;
            double binZero = dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double histoSpan = ((double)nBins)*binWidth;
            double rem = histoSpan - span;
            if(rem>=0){
                binZero -= rem/2.0D;
            }
            else{
                if(Math.abs(rem)/span>HISTOL){
                    // readjust binWidth
                    boolean testBw = true;
                    double incr = HISTOL/nBins;
                    int iTest = 0;
                    while(testBw){
                       binWidth += incr;
                       histoSpan = ((double)nBins)*binWidth;
                        rem = histoSpan - span;
                        if(rem<0){
                            iTest++;
                            if(iTest>1000){
                                testBw = false;
                                System.out.println("histogram method could not encompass all data within histogram\nContact Michael thomas Flanagan");
                            }
                        }
                        else{
                            testBw = false;
                        }
                    }
                }
            }

            return histogramBins(data, binWidth, binZero);
        }

        // ARRAY MAXIMUM  (deprecated - see ArryMaths class)
        // Maximum of a 1D array of doubles, aa
        public static double maximum(double[] aa){
            int n = aa.length;
            double aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of floats, aa
        public static float maximum(float[] aa){
            int n = aa.length;
            float aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of ints, aa
        public static int maximum(int[] aa){
            int n = aa.length;
            int aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Maximum of a 1D array of longs, aa
        public static long maximum(long[] aa){
            long n = aa.length;
            long aamax=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]>aamax)aamax=aa[i];
            }
            return aamax;
        }

        // Minimum of a 1D array of doubles, aa
        public static double minimum(double[] aa){
            int n = aa.length;
            double aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of floats, aa
        public static float minimum(float[] aa){
            int n = aa.length;
            float aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // ARRAY MINIMUM (deprecated - see ArryMaths class)
        // Minimum of a 1D array of ints, aa
        public static int minimum(int[] aa){
            int n = aa.length;
            int aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // Minimum of a 1D array of longs, aa
        public static long minimum(long[] aa){
            long n = aa.length;
            long aamin=aa[0];
            for(int i=1; i<n; i++){
                if(aa[i]<aamin)aamin=aa[i];
            }
            return aamin;
        }

        // SUM OF ALL ELEMENTS  (deprecated - see ArryMaths class)
        // Sum of all array elements - double array
        public static double arraySum(double[]array){
            double sum = 0.0D;
            for(double i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - float array
        public static float arraySum(float[]array){
            float sum = 0.0F;
            for(float i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - int array
        public static int arraySum(int[]array){
            int sum = 0;
            for(int i:array)sum += i;
            return sum;
        }

        // Sum of all array elements - long array
        public static long arraySum(long[]array){
            long sum = 0L;
            for(long i:array)sum += i;
            return sum;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double normalCDF(double mean, double sd, double upperlimit){
            double prob;
            if(upperlimit==Double.POSITIVE_INFINITY){
                prob = 1.0;
            }
            else{
                if(upperlimit==Double.NEGATIVE_INFINITY){
                    prob = 0.0;
                }
                else{
                    double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                    prob = (1.0D + erf(arg))/2.0D;
                }
            }
            if(isNaN(prob)){
                if(upperlimit>mean){
                    prob = 1.0;
                }
                else{
                    prob = 0.0;
                }
            }
            return prob;
        }

        // Returns true if x is 'Not a Number' (NaN)
        // x is double
        public static boolean isNaN(double x){
            boolean test=false;
            if(x!=x)test=true;
            return test;
        }

        // Error Function
        public static double erf(double x){
                double erf = 0.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erf = 1.0D;
                        }
                        else{
                                if(x>=0){
                                        erf = incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erf = -incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erf;
        }

        // Gaussian (normal) probability density function
        // mean  =  the mean, sd = standard deviation
        public static double normalPDF(double mean, double sd, double x){
                return Math.exp(-square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

        // Gaussian (normal) probability density function
        // mean  =  the mean, sd = standard deviation
        public static double normal(double mean, double sd, double x){
                return Math.exp(-square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

         // Gaussian (normal) probability  density function
        // mean  =  the mean, sd = standard deviation
        public static double gaussianPDF(double mean, double sd, double x){
                return Math.exp(-square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }
        // Gaussian (normal) probability density function
        // mean  =  the mean, sd = standard deviation
        public static double gaussian(double mean, double sd, double x){
                return Math.exp(-square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

        // SQUARES
        // Square of a double number
        public static double square(double a){
            return a*a;
        }

        // Square of a float number
        public static float square(float a){
            return a*a;
        }

        // Square of an int number
        public static int square(int a){
            return a*a;
        }

        // Square of a long number
        public static long square(long a){
            return a*a;
        }

        // Variance of a 1D array of doubles, aa
        public static double variance(double[] aa, double mean){
                int n = aa.length;
                double sum;
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=square(aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Arithmetic mean of a 1D array of doubles, aa
        public static double mean(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((double)n);
        }

}