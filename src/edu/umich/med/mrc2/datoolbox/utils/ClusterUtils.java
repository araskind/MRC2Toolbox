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
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ClusterUtils {

	public static MsFeature getMostIntensiveFeature(Collection<MsFeature> features) {
		return features.stream().
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public static MsFeature getMostIntensiveFeature(MsFeatureCluster fcluster) {
		return ClusterUtils.getMostIntensiveFeature(fcluster.getFeatures());
	}
	
	public static MsFeature getMostIntensiveMsmsFeature(MsFeatureCluster fcluster) {
		
		if(fcluster.getFeatures().size() == 1)
			return fcluster.getFeatures().iterator().next();
		else
			return fcluster.getFeatures().stream().
				filter(f -> Objects.nonNull(f.getSpectrum())).
				filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
				sorted(new MsFeatureComparator(SortProperty.msmsIntensity, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public static Matrix createClusterCorrelationMatrix(MsFeatureCluster fcluster, boolean activeOnly) {

		Matrix dataMatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment()
				.getDataMatrixForDataPipeline(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline());

		if(dataMatrix == null)
			return null;

		Collection<MsFeature> sorted = null;
		Matrix corrMatrix = null;

		if (activeOnly)
			sorted = fcluster.getActiveFeatures();
		else
			sorted = fcluster.getFeatures().stream().
				sorted(new MsFeatureComparator(SortProperty.Name)).
				collect(Collectors.toList());
		long[] columnIndex =
				sorted.stream().
				map(f -> dataMatrix.getColumnForLabel(f)).
				mapToLong(i -> i).
				toArray();

		MsFeature[] sortedFeatures = sorted.toArray(new MsFeature[sorted.size()]);

		//	TODO	Select active samples - this is a temporary fix untill design subsets are implemented
		ArrayList<Long>rowList = new ArrayList<Long>();
		for(DataFile file : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getDataFilesForAcquisitionMethod(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline().getAcquisitionMethod())) {

			if(file.isEnabled())
				rowList.add(dataMatrix.getRowForLabel(file));
		}
		long[] rowIndex = ArrayUtils.toPrimitive(rowList.toArray(new Long[rowList.size()]));
		Matrix raw = dataMatrix.select(Ret.LINK, rowIndex, columnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
		corrMatrix = raw.corrcoef(Ret.LINK, true, false).replace(Ret.NEW, Double.NaN, 0.0d);
		corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatures));
		corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray((Object[])sortedFeatures).transpose(Ret.NEW));
		return corrMatrix;
	}
}
