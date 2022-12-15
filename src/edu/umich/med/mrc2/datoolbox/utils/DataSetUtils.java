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
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileTimeStampComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataSetUtils {

	public static Matrix subsetDataMatrix(
			DataAnalysisProject currentProject,
			DataPipeline acivePipeline,
			ExperimentDesignSubset activeDesign,
			MsFeatureSet activeFeatures) {

		if(currentProject == null || acivePipeline == null)
			return null;

		Matrix source = currentProject.getDataMatrixForDataPipeline(acivePipeline);
		Matrix featureMatrix = source.getMetaDataDimensionMatrix(0);
		Matrix fileMatrix = source.getMetaDataDimensionMatrix(1);

		Collection<Long>features = 
				activeFeatures.getFeatures().
				stream().map(f -> source.getColumnForLabel(f)).
				sorted().collect(Collectors.toList());
		Matrix newFeatureMatrix = featureMatrix.selectColumns(Ret.NEW, features);

		DataFile[] activeFiles = 
				sortFiles(acivePipeline, FileSortingOrder.NAME, activeDesign);
		Collection<Long>files = 
				Arrays.asList(activeFiles).stream().
				map(file -> source.getRowForLabel(file)).
				collect(Collectors.toList());
		Matrix newFileMatrix = fileMatrix.selectRows(Ret.NEW, files);

		Matrix subset = source.selectColumns(Ret.LINK, features).selectRows(Ret.NEW, files);
		subset.setMetaDataDimensionMatrix(0, newFeatureMatrix);
		subset.setMetaDataDimensionMatrix(1, newFileMatrix);
		return subset;
	}

	public static Matrix getFeatureSubsetMatrix(
			Matrix source,
			Collection<MsFeature>activeFeatures) {

		Matrix featureMatrix = source.getMetaDataDimensionMatrix(0);
		Collection<Long>features = activeFeatures.stream().
				map(f -> source.getColumnForLabel(f)).sorted().collect(Collectors.toList());
		Matrix newFeatureMatrix = featureMatrix.selectColumns(Ret.NEW, features);

		Matrix subset = source.selectColumns(Ret.NEW, features);
		subset.setMetaDataDimensionMatrix(0, newFeatureMatrix);
		subset.setMetaDataDimensionMatrix(1, source.getMetaDataDimensionMatrix(1));
		return subset;
	}

	public static ExperimentDesignSubset getSamplesOnlyDesignSubset(DataAnalysisProject currentProject) {

		if(currentProject.getExperimentDesign() == null)
			return null;

		ExperimentDesignSubset samplesOnly = 
				new ExperimentDesignSubset(GlobalDefaults.SAMPLES_ONLY.getName());
		samplesOnly.addLevel(ReferenceSamplesManager.sampleLevel);
		return samplesOnly;
	}

	public static DataFile[] sortFiles(
			DataPipeline dataPipeline,
			FileSortingOrder order,
			ExperimentDesignSubset activeDesign) {

		if (MRC2ToolBoxCore.getCurrentProject() == null)
			return null;

		if (MRC2ToolBoxCore.getCurrentProject().getExperimentDesign() == null)
			return null;

		if (MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getSamples().isEmpty())
			return new DataFile[0];

		Collection<ExperimentalSample> samples =
				MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().
				getSamplesForDesignSubset(activeDesign);

		if (order.equals(FileSortingOrder.NAME))
			return samples.stream().
					flatMap(s -> s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).
					stream()).filter(s -> s.isEnabled()).
					sorted().toArray(size -> new DataFile[size]);

		if (order.equals(FileSortingOrder.TIMESTAMP))
			return samples.stream().flatMap(s -> s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).
					stream()).filter(s -> s.isEnabled()).
					sorted(new DataFileTimeStampComparator()).
					toArray(size -> new DataFile[size]);

		if (order.equals(FileSortingOrder.SAMPLE_ID)) {

			return samples.stream().
					sorted(new ExperimentalSampleComparator(SortProperty.ID)).
					flatMap(s -> s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).
					stream()).filter(s -> s.isEnabled()).toArray(size -> new DataFile[size]);
		}
		if (order.equals(FileSortingOrder.SAMPLE_NAME))
			return samples.stream().sorted(new ExperimentalSampleComparator(SortProperty.Name)).
					flatMap(s -> s.getDataFilesForMethod(dataPipeline.getAcquisitionMethod()).stream()).
					filter(s -> s.isEnabled()).toArray(size -> new DataFile[size]);

		ArrayList<DataFile>sortedFiles = new ArrayList<DataFile>();
		return sortedFiles.toArray(new DataFile[sortedFiles.size()]) ;
	}

}
