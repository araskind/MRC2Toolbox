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

package edu.umich.med.mrc2.datoolbox.rawdata;

import java.util.ArrayList;

public class PeakFinder {

	private double[] xx;
	private double[] yy;
	
	private int nPoints = 0;
    private ArrayList<Double> almin = new ArrayList<Double>();
    private ArrayList<Double> almax = new ArrayList<Double>();
    private double[][] minimaCoordinates = null;
    private double[][] maximaCoordinates = null;
    private int nMin = 0;
    private int nMax = 0;
	
    public PeakFinder(double[] xx, double[] yy) {
		super();
		if(xx.length != yy.length)
			throw new IllegalArgumentException("Y and Y coordinate arrays should be of the same length!");
		
		this.xx = xx;
		this.yy = yy;
		nPoints = this.xx.length;
	}

	public void findMinAndMax(){
        
		minimaCoordinates = new double[2][];
		maximaCoordinates = new double[2][];
		
        boolean plateau = false;
        int iPlat = 0;
         
        this.almin.clear();
        this.almax.clear();
        
        int nnMin = 0;
        int nnMax = 0;
        
        // minima
        for(int i=2; i<this.nPoints-1; i++){
            if(plateau){
                if(yy[i]<yy[i-1]){
                    plateau = false;
                    if(yy[i]<yy[i+1]){
                        nnMin++;
                        this.almin.add(xx[i]);
                        this.almin.add(yy[i]); 
                    }
                    else{
                        if(yy[i]==yy[i+1]){
                            plateau = true;
                            iPlat = i;
                        }
                    }
                }
                else{
                    if(yy[i]==yy[i-1]){
                        if(yy[i]<yy[i+1]){
                            plateau = false;
                            nnMin++;
                            double xxp = (xx[i]+xx[iPlat])/2.0;
                            this.almin.add(xxp);
                            this.almin.add(yy[i]);
                        }
                        else{
                           if(yy[i]>yy[i+1]){
                               plateau = false;
                           }
                        }
                    }
                }
            }
            else{
                if(yy[i]<yy[i-1] && yy[i]<yy[i+1]){
                    nnMin++;
                    this.almin.add(xx[i]);
                    this.almin.add(yy[i]);
                }
                else{
                    if(yy[i]<yy[i-1] && yy[i]==yy[i+1]){
                        plateau = true;
                        iPlat = i;
                    }
                }
            }
        }   
        this.nMin = nnMin;
        double[] holdx = new double[nnMin];
        double[] holdy = new double[nnMin];
        int k = 0;
        for(int i=0; i<nnMin; i++){
           holdx[i] = this.almin.get(k++);
           holdy[i] = this.almin.get(k++);
        }
        this.minimaCoordinates[0] = holdx;
        this.minimaCoordinates[1] = holdy; 
       
        // maxima
        plateau = false;
        iPlat = 0;
        for(int i=2; i<this.nPoints-1; i++){
            if(plateau){
                if(yy[i]>yy[i-1]){
                    plateau = false;
                    if(yy[i]>yy[i+1]){
                        nnMax++;
                        this.almax.add(xx[i]);
                        this.almax.add(yy[i]); 
                    }
                    else{
                        if(yy[i]==yy[i+1]){
                            plateau = true;
                            iPlat = i;
                        }
                    }
                }
                else{
                    if(yy[i]==yy[i-1]){
                        if(yy[i]>yy[i+1]){
                            plateau = false;
                            nnMax++;
                            double xxp = (xx[i]+xx[iPlat])/2.0;
                            this.almax.add(xxp);
                            this.almax.add(yy[i]);
                        }
                        else{
                           if(yy[i]<yy[i+1]){
                               plateau = false;
                           }
                        }
                    }
                }
            }
            else{
                if(yy[i]>yy[i-1] && yy[i]>yy[i+1]){
                    nnMax++;
                    this.almax.add(xx[i]);
                    this.almax.add(yy[i]);
                }
                else{
                    if(yy[i]>yy[i-1] && yy[i]==yy[i+1]){
                        plateau = true;
                        iPlat = i;
                    }
                }
            }
        }    
        this.nMax = nnMax;
        holdx = new double[nnMax];
        holdy = new double[nnMax];
        k = 0;
        for(int i=0; i<nnMax; i++){
           holdx[i] = this.almax.get(k++);
           holdy[i] = this.almax.get(k++);
        }
        this.maximaCoordinates[0] = holdx;
        this.maximaCoordinates[1] = holdy;        
    }

	/**
	 * @return
	 * double[][] array of minima points 
	 * minima[0] - x coordinates 
	 * minima[1] - y coordinates
	 */
	public double[][] getMinimaCoordinates() {
		return minimaCoordinates;
	}

	/**
	 * @return
	 * double[][] array of maxima points 
	 * maxima[0] - x coordinates
	 * maxima[1] - y coordinates
	 */
	public double[][] getMaximaCoordinates() {
		return maximaCoordinates;
	}

	/**
	 * @return
	 * Number of minima
	 */
	public int getnMinimaCount() {
		return nMin;
	}

	/**
	 * @return
	 * Number of maxima
	 */
	public int getnMaximaCount() {
		return nMax;
	}
	
}
