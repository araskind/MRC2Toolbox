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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataExportUtils {

	public static TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> createSampleFileMap(
									DataAnalysisProject currentExperiment, 
									ExperimentDesignSubset design, 
									Set<DataPipeline> dataPipelines,
									DataExportFields exportFieldNaming) {

		ExperimentalSampleComparator sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.ID, SortDirection.ASC);

		if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_NAME))
			sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.Name, SortDirection.ASC);

		TreeMap<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>> sampleFileMap =
				new TreeMap<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>>();
		TreeSet<ExperimentalSample> activeSamples =
				currentExperiment.getExperimentDesign().getActiveSamplesForDesignSubset(design);

		for (ExperimentalSample s : activeSamples) {

			if (sampleFileMap.get(s) == null)
				sampleFileMap.put(s, new TreeMap<DataPipeline, TreeSet<DataFile>>());

			for (DataPipeline m : dataPipelines) {

				if (sampleFileMap.get(s).get(m) == null)
					sampleFileMap.get(s).put(m, new TreeSet<DataFile>());
				
				TreeSet<DataFile> sampleFiles = s.getDataFilesForMethod(m.getAcquisitionMethod());
				if (sampleFiles != null) {
					
					sampleFileMap.get(s).get(m).addAll(
							sampleFiles.stream().filter(f -> f.isEnabled()).
							collect(Collectors.toSet()));
				}
			}
		}
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> arrayMap =
				new TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>(sampleComparator);
		for (Entry<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>> entry : sampleFileMap.entrySet()) {

			arrayMap.put(entry.getKey(), new TreeMap<DataPipeline, DataFile[]>());

			for (Entry<DataPipeline, TreeSet<DataFile>> fam : entry.getValue().entrySet())
				arrayMap.get(entry.getKey()).put(fam.getKey(),
						fam.getValue().toArray(new DataFile[fam.getValue().size()]));
		}
		return arrayMap;
	}

	public static TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> createSampleFileMapForDataPipeline(
			DataAnalysisProject currentExperiment, 
			ExperimentDesignSubset designSubset, 
			DataPipeline pipeline,
			DataExportFields exportFieldNaming) {

		ExperimentalSampleComparator sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.ID);

		if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_NAME))
			sampleComparator = new ExperimentalSampleComparator(SortProperty.Name);

		TreeMap<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>> sampleFileMap =
				new TreeMap<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>>();
		TreeSet<ExperimentalSample> activeSamples =
				currentExperiment.getExperimentDesign().getActiveSamplesForDesignSubset(designSubset);
		HashSet<DataPipeline> pipelines = new HashSet<DataPipeline>();
		pipelines.add(pipeline);

		for (ExperimentalSample s : activeSamples) {

			if (sampleFileMap.get(s) == null)
				sampleFileMap.put(s, new TreeMap<DataPipeline, TreeSet<DataFile>>());
		

			for (DataPipeline m : pipelines) {

				if (sampleFileMap.get(s).get(m) == null)
					sampleFileMap.get(s).put(m, new TreeSet<DataFile>());

				TreeSet<DataFile> sampleFiles = s.getDataFilesForMethod(m.getAcquisitionMethod());
				if (sampleFiles != null) {
					
					sampleFileMap.get(s).get(m).addAll(
							sampleFiles.stream().filter(f -> f.isEnabled()).
							collect(Collectors.toSet()));
				}
			}
		}
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> arrayMap =
				new TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>(sampleComparator);
		for (Entry<ExperimentalSample, TreeMap<DataPipeline, TreeSet<DataFile>>> entry : sampleFileMap.entrySet()) {

			arrayMap.put(entry.getKey(), new TreeMap<DataPipeline, DataFile[]>());

			for (Entry<DataPipeline, TreeSet<DataFile>> fam : entry.getValue().entrySet())
				arrayMap.get(entry.getKey()).put(fam.getKey(),
						fam.getValue().toArray(new DataFile[fam.getValue().size()]));
		}
		return arrayMap;
	}

	public static HashMap<DataFile, Integer> createFileColumnMap(
			TreeMap<ExperimentalSample, 
			TreeMap<DataPipeline, DataFile[]>> sampleFileMap, 
			int startColumn) {

		HashMap<DataFile, Integer> fileColumnMap = new HashMap<DataFile, Integer>();
		int columnCount = startColumn;

		for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

			int maxReps = 0;

			for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

				if (fmap.getValue().length > maxReps)
					maxReps = fmap.getValue().length;
			}
			if (maxReps == 1) {

				columnCount = columnCount + 1;
				for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

					if (fmap.getValue().length > 0)
						fileColumnMap.put(fmap.getValue()[0], columnCount);
				}
			}
			if (maxReps > 1) {

				for (int rep = 1; rep <= maxReps; rep++) {

					columnCount = columnCount + 1;

					for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

						if (fmap.getValue().length >= rep)
							fileColumnMap.put(fmap.getValue()[rep - 1], columnCount);
					}
				}
			}
		}
		return fileColumnMap;
	}

	public static String[] createSampleColumnNameArray(
			TreeMap<ExperimentalSample, 
			TreeMap<DataPipeline, DataFile[]>> sampleFileMap,
			DataExportFields exportFieldNaming) {

		ArrayList<String> columnList = new ArrayList<String>();

		for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

			int maxReps = 0;

			for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

				if (fmap.getValue().length > maxReps)
					maxReps = fmap.getValue().length;
			}
			if (maxReps == 1) {

				String colName = entry.getKey().getName();
				if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
					colName = entry.getKey().getId();
				
				columnList.add(colName);
			}
			if (maxReps > 1) {

				for (int rep = 1; rep <= maxReps; rep++) {
					
					String numRep = StringUtils.leftPad(Integer.toString(rep), 2, "0");
					String colName = entry.getKey().getName() + "-" + numRep;
					if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
						colName = entry.getKey().getId() + "-" + numRep;

					columnList.add(colName);
				}
			}
		}
		return columnList.toArray(new String[columnList.size()]);
	}

	public static String[] createSampleColumnNameArrayForDataPipeline(
			TreeMap<ExperimentalSample, 
			TreeMap<DataPipeline, DataFile[]>> sampleFileMap,
			DataExportFields exportFieldNaming, 
			DataPipeline activePipeline) {

		ArrayList<String> columnList = new ArrayList<String>();
		for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

			int maxReps = 0;

			for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

				if (fmap.getValue().length > maxReps)
					maxReps = fmap.getValue().length;
			}
			if (maxReps == 1) {

				String colName = entry.getKey().getName();
				if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
					colName = entry.getKey().getId();

				if (exportFieldNaming.equals(DataExportFields.DATA_FILE_EXPORT))
					colName = entry.getValue().get(activePipeline)[0].getName();

				columnList.add(colName);
			}
			if (maxReps > 1) {

				for (int rep = 1; rep <= maxReps; rep++) {
					
					String numRep = StringUtils.leftPad(Integer.toString(rep), 2, "0");
					String colName = entry.getKey().getName() + "-" + numRep;
					if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
						colName = entry.getKey().getId() + "-" + numRep;

					if (exportFieldNaming.equals(DataExportFields.DATA_FILE_EXPORT))
						colName = entry.getValue().get(activePipeline)[rep - 1].getName();

					columnList.add(colName);
				}
			}
		}
		return columnList.toArray(new String[columnList.size()]);
	}
}
