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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
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

	public static Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> createSampleFileMap(
									DataAnalysisProject currentExperiment, 
									ExperimentDesignSubset design, 
									Set<DataPipeline> dataPipelines,
									DataExportFields exportFieldNaming) {

		ExperimentalSampleComparator sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.ID, SortDirection.ASC);

		if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_NAME))
			sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.Name, SortDirection.ASC);

		Map<ExperimentalSample, Map<DataPipeline, Set<DataFile>>> sampleFileMap = new TreeMap<>();
		TreeSet<ExperimentalSample> activeSamples =
				currentExperiment.getExperimentDesign().getActiveSamplesForDesignSubset(design);

		for (ExperimentalSample s : activeSamples) {

			if (sampleFileMap.get(s) == null)
				sampleFileMap.put(s, new TreeMap<>());

			for (DataPipeline m : dataPipelines) {

				if (sampleFileMap.get(s).get(m) == null)
					sampleFileMap.get(s).put(m, new TreeSet<>());
				
				NavigableSet<DataFile> sampleFiles = s.getDataFilesForMethod(m.getAcquisitionMethod());
				if (sampleFiles != null) {
					
					sampleFileMap.get(s).get(m).addAll(
							sampleFiles.stream().filter(f -> f.isEnabled()).
							collect(Collectors.toSet()));
				}
			}
		}
		Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> arrayMap = new TreeMap<>(sampleComparator);
		for (Entry<ExperimentalSample, Map<DataPipeline, Set<DataFile>>> entry : sampleFileMap.entrySet()) {

			arrayMap.put(entry.getKey(), new TreeMap<>());

			for (Entry<DataPipeline, Set<DataFile>> fam : entry.getValue().entrySet())
				arrayMap.get(entry.getKey()).put(fam.getKey(),
						fam.getValue().toArray(new DataFile[fam.getValue().size()]));
		}
		return arrayMap;
	}

	public static Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> createSampleFileMapForDataPipeline(
			DataAnalysisProject currentExperiment, 
			ExperimentDesignSubset designSubset, 
			DataPipeline pipeline,
			DataExportFields exportFieldNaming) {

		ExperimentalSampleComparator sampleComparator = 
				new ExperimentalSampleComparator(SortProperty.ID);

		if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_NAME))
			sampleComparator = new ExperimentalSampleComparator(SortProperty.Name);

		Map<ExperimentalSample, Map<DataPipeline, Set<DataFile>>> sampleFileMap = new TreeMap<>();
		Set<ExperimentalSample> activeSamples =
				currentExperiment.getExperimentDesign().getActiveSamplesForDesignSubset(designSubset);
		Set<DataPipeline> pipelines = new HashSet<>();
		pipelines.add(pipeline);

		for (ExperimentalSample s : activeSamples) {

			if (sampleFileMap.get(s) == null)
				sampleFileMap.put(s, new TreeMap<>());
		

			for (DataPipeline m : pipelines) {

				if (sampleFileMap.get(s).get(m) == null)
					sampleFileMap.get(s).put(m, new TreeSet<>());

				NavigableSet<DataFile> sampleFiles = s.getDataFilesForMethod(m.getAcquisitionMethod());
				if (sampleFiles != null) {
					
					sampleFileMap.get(s).get(m).addAll(
							sampleFiles.stream().filter(f -> f.isEnabled()).
							collect(Collectors.toSet()));
				}
			}
		}
		Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> arrayMap = new TreeMap<>(sampleComparator);
		for (Entry<ExperimentalSample, Map<DataPipeline, Set<DataFile>>> entry : sampleFileMap.entrySet()) {

			arrayMap.put(entry.getKey(), new TreeMap<>());

			for (Entry<DataPipeline, Set<DataFile>> fam : entry.getValue().entrySet())
				arrayMap.get(entry.getKey()).put(fam.getKey(),
						fam.getValue().toArray(new DataFile[fam.getValue().size()]));
		}
		return arrayMap;
	}

	public static Map<DataFile, Integer> createFileColumnMap(
			Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> sampleFileMap, 
			int startColumn) {

		Map<DataFile, Integer> fileColumnMap = new HashMap<>();
		int columnCount = startColumn;

		for (Entry<ExperimentalSample, Map<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

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
			Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> sampleFileMap,
			DataExportFields exportFieldNaming) {

		ArrayList<String> columnList = new ArrayList<>();

		for (Entry<ExperimentalSample, Map<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

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
	
	public static Map<DataPipeline,Map<DataFile,String>>createDataFileColumnNameMap(
			Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> sampleFileMap,
			DataExportFields exportFieldNaming) {
		
		Map<DataPipeline,Map<DataFile,String>>dataFileColumnNameMap = new TreeMap<>();
		Set<DataPipeline> pipelineSet = sampleFileMap.values().stream().
				flatMap(v -> v.keySet().stream()).collect(Collectors.toSet());
		pipelineSet.stream().forEach(p -> dataFileColumnNameMap.put(p, new TreeMap<>()));
		
		for (Entry<ExperimentalSample, Map<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

			int maxReps = 0;

			for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {

				if (fmap.getValue().length > maxReps)
					maxReps = fmap.getValue().length;
			}
			for (Entry<DataPipeline, DataFile[]> fmap : entry.getValue().entrySet()) {
				
				if (maxReps == 1) {
					
					String colName = entry.getKey().getName();
					if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
						colName = entry.getKey().getId();
					
					dataFileColumnNameMap.get(fmap.getKey()).put(fmap.getValue()[0], colName);
				}
				if (maxReps > 1) {

					for (int rep = 1; rep <= fmap.getValue().length; rep++) {
						
						String numRep = StringUtils.leftPad(Integer.toString(rep), 2, "0");
						String colName = entry.getKey().getName() + "-" + numRep;
						if (exportFieldNaming.equals(DataExportFields.SAMPLE_EXPORT_ID))
							colName = entry.getKey().getId() + "-" + numRep;

						dataFileColumnNameMap.get(fmap.getKey()).put(fmap.getValue()[rep-1], colName);
					}
				}
			}
		}		
		return dataFileColumnNameMap;
	}

	public static String[] createSampleColumnNameArrayForDataPipeline(
			Map<ExperimentalSample, Map<DataPipeline, DataFile[]>> sampleFileMap,
			DataExportFields exportFieldNaming, 
			DataPipeline activePipeline) {

		ArrayList<String> columnList = new ArrayList<>();
		for (Entry<ExperimentalSample, Map<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

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
