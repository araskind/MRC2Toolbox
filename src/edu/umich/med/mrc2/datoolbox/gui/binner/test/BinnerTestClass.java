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

package edu.umich.med.mrc2.datoolbox.gui.binner.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

public class BinnerTestClass {

	private static double[][] distances;
	private static double[][] linkages;
	private static double[][] correlations;
	private static int nFeatures = 10;
	
	private static Matrix distancesMatrix;
	private static Matrix linkagesMatrix;
	private static Matrix correlationsMatrix;
	
	private static final String workingDirPath = 
			"Y:\\DataAnalysis\\MRC2ToolboxProjects\\_CORE_NF\\_CONVERTED\\"
			+ "EX01496\\CO300-Pk1000\\EX01496-BATCH04\\exports";
	
	private static final NumberFormat format = new DecimalFormat("#.####");
	
	public static void main(String[] args) {

//		initCorrelations();
//		initializeDistances();
//		initializeLinkages();
		
		initCorrelationsMatrix();
		initializeDistancesMatrix();
		initializeLinkagesMatrix();
	}
	
	private static void initCorrelationsMatrix() {
		
		correlationsMatrix = Matrix.Factory.rand(new long[] {10,10}).corrcoef(Ret.NEW, true, true);
		correlations = correlationsMatrix.toDoubleArray();
		printDoubleMatrix(correlations, format, "test_correlations_M.txt");
		
		distancesMatrix = correlationsMatrix.euklideanDistance(Ret.NEW, true);
		
		correlationsMatrix.euklideanDistanceTo(correlationsMatrix, true);
		
		printDoubleMatrix(distancesMatrix.toDoubleArray(), format, "test_distances_M.txt");
		
		initializeDistances();
	}
	
	private static void initializeDistancesMatrix() {
		
		
	}
	
	private static void initializeLinkagesMatrix() {
		
		
	}

	private static void initCorrelations() {
		
		correlations = new double[nFeatures][nFeatures];
		long size = (long)nFeatures * nFeatures;
		double[]fillValues = new Random().doubles(size, -1.0d, 1.0d).toArray();
		int count = 0;
		for (int i = 0; i < nFeatures; i++) {

			for (int j = 0; j < nFeatures; j++) {
				correlations[i][j] = fillValues[count];	
				count++;
			}
		}
		printDoubleMatrix(correlations, format, "test_correlations.txt");
	}

	private static void initializeDistances() {

		distances = new double[nFeatures - 1][];
		for (int i = 0; i < nFeatures - 1; i++) {
			double[] row = new double[nFeatures - i - 1];
			for (int j = i + 1; j < nFeatures; j++)
				row[j - i - 1] = calculateEuclidianDistance(correlations[i], correlations[j]);
			distances[i] = row;
		}
		printDoubleMatrix(distances, format, "test_distances.txt");
	}

	private static void initializeLinkages() {

		linkages = new double[nFeatures][];
		for (int i = 0; i < nFeatures - 1; i++) {
			double[] linkRow = new double[nFeatures - i - 1];
			double[] distRow = distances[i];
			for (int j = 0; j < nFeatures - i - 1; j++)
				linkRow[j] = distRow[j];
			linkages[i] = linkRow;
		}
		printDoubleMatrix(linkages, format, "test_linkages.txt");
	}
	
	private static void printDoubleMatrix(double[][]matrix, NumberFormat format, String fileName) {

		List<String> data = new ArrayList<String>();
		List<String> line = new ArrayList<String>();
		for (int i = 0; i < matrix.length; i++) {
			
			line.clear();
			if(matrix[i] != null) {
				
				for (int j = 0; j < matrix[i].length; j++)
					line.add(format.format(matrix[i][j]));
			}
			data.add(org.apache.commons.lang3.StringUtils.join(line, '\t'));
		}
		Path outputPath = Paths.get(workingDirPath, fileName);
		try {
		    Files.write(outputPath, 
		    		data,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private static double calculateEuclidianDistance(double[] array1, double[] array2) {
		double distance = 0.0;

		for (int i = 0; i < array1.length; i++) {
			double val = array1[i] - array2[i];
			distance += val * val;
		}

		return Math.sqrt(distance);
	}
}
