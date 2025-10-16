/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.util.ArrayList;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.PrincipalComponents;
import weka.attributeSelection.Ranker;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.packageManagement.Package;

public class WekaTest {

	private static final String USAGE = String.format(
			"This program performs principal components analysis on the given"
			+ "dataset.\nIt will transform the data onto its principal components, "
			+ "optionally performing\ndimensionality reduction by ignoring the "
			+ "principal components with the\nsmallest eigenvalues.\n\n" + "Required options:\n"
			+ "-i [string]     Input dataset to perform PCA on.\n\n" + "Options:\n\n"
			+ "-d [int]    Desired dimensionality of output dataset. If -1,\n"
			+ "            no dimensionality reduction is performed.\n" + "            Default value -1.\n"
			+ "-s          If set, the data will be scaled before running\n"
			+ "            PCA, such that the variance of each feature is 1.");

	public static void main(String[] args) {
		
        int numAtts = 5;

        ArrayList<Attribute> atts = new ArrayList<Attribute>(numAtts);
        
		Instances dataset = new Instances("MetricInstances", atts, 5);
	}
	
	private static void packages() {
		
		WekaPackageManager.loadPackages(false);
		try {
			for (Package p: WekaPackageManager.getAvailablePackages())
				  System.out.println("- " + p.getName() + "/" + p.getPackageMetaData().get("Version"));
			
//			WekaPackageManager.installPackageFromRepository("netlibNativeWindows", null, System.out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void PCATest(String[] args) {
		
		try {
			// Get the data set path.
			String dataset = Utils.getOption('i', args);
			if (dataset.length() == 0)
				throw new IllegalArgumentException();



			// Load input dataset.
			DataSource source = new DataSource(dataset);
			Instances data = source.getDataSet();


			// Find out what dimension we want.
			int k = 0;
			String dimension = Utils.getOption('d', args);
			if (dimension.length() == 0) {
				k = data.numAttributes();
			} else {
				k = Integer.parseInt(dimension);
				// Validate the parameter.
				if (k > data.numAttributes()) {
					System.out.printf("[Fatal] New dimensionality (%d) cannot be greater"
							+ "than existing dimensionality (%d)!'\n", k, data.numAttributes());

					System.exit(-1);
				}
			}
			// Performs a principal components analysis.
			PrincipalComponents pcaEvaluator = new PrincipalComponents();

			// Sets the amount of variance to account for when retaining principal
			// components.
			pcaEvaluator.setVarianceCovered(1.0);
			// Sets maximum number of attributes to include in transformed attribute
			// names.
			pcaEvaluator.setMaximumAttributeNames(-1);

			// Scaled X such that the variance of each feature is 1.
			boolean scale = Utils.getFlag('s', args);
			if (scale) {
				pcaEvaluator.setCenterData(true);
			} else {
				pcaEvaluator.setCenterData(false);
			}

			// Ranking the attributes.
			Ranker ranker = new Ranker();
			// Specify the number of attributes to select from the ranked list.
			ranker.setNumToSelect(k - 1);

			AttributeSelection selector = new AttributeSelection();
			selector.setSearch(ranker);
			selector.setEvaluator(pcaEvaluator);
			selector.SelectAttributes(data);

			// Transform data into eigenvector basis.
			Instances transformedData = selector.reduceDimensionality(data);


		} catch (IllegalArgumentException e) {
			System.err.println(USAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
