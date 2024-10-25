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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class FIOUtils {

	public static File changeExtension(File f, String newExtension) {

		if (FilenameUtils.getExtension(f.getPath()).equals(newExtension))
			return f;

		String newFileName = FilenameUtils.getFullPath(f.getAbsolutePath()) + FilenameUtils.getBaseName(f.getName())
				+ "." + newExtension;

		return new File(newFileName);
	}

	public static String calculateFileChecksum(String filePath) {

		try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
			return DigestUtils.md5Hex(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String calculateFileChecksum(File file) {

		try (InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
			return DigestUtils.md5Hex(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getTimestamp() {
		return MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
	}

	public static File getFileForLocation(String location) {

		if (location == null || location.trim().isEmpty())
			return null;

		Path filePath = null;
		try {
			filePath = Paths.get(location);
		} catch (Exception e) {
			System.out.println("File at " + location + " was not found.");
		}
		if (filePath != null) {

			File fileToReturn = filePath.toFile();
			if (fileToReturn.exists())
				return fileToReturn;
		}
		return null;
	}

	public static List<Path> findFilesByExtension(Path path, String fileExtension) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		String ext = fileExtension.toLowerCase();
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> !Files.isDirectory(p)).filter(p -> p.toString().toLowerCase().endsWith(ext))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> findDirectoriesByName(Path path, String dirName) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> Files.isDirectory(p)).filter(p -> p.getFileName().toString().equals(dirName))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> findDirectoriesByExtension(Path path, String fileExtension) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		String ext = fileExtension.toLowerCase();
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> Files.isDirectory(p)).filter(p -> p.toString().toLowerCase().endsWith(ext))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> findFilesByName(Path path, String fileName) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> !Files.isDirectory(p))
					.filter(p -> p.getName(p.getNameCount() - 1).toString().toLowerCase().equalsIgnoreCase(fileName))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> findFilesByNameStartingWith(Path path, String nameStart) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		String nameStartSearch = nameStart.toLowerCase();
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> !Files.isDirectory(p))
					.filter(p -> p.getName(p.getNameCount() - 1).toString().toLowerCase().startsWith(nameStartSearch))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Path> findDirectoriesByNameStartingWith(Path path, String nameStart) {

		if (!Files.isDirectory(path)) {
			throw new IllegalArgumentException("Path must be a directory!");
		}
		String nameStartSearch = nameStart.toLowerCase();
		List<Path> result = null;
		try (Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(p -> Files.isDirectory(p))
					.filter(p -> p.getName(p.getNameCount() - 1).toString().toLowerCase().startsWith(nameStartSearch))
					.collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

//	try (Stream<Path> walkStream = Files.walk(Paths.get("your search directory"))) {
//	    walkStream.filter(p -> p.toFile().isFile()).forEach(f -> {
//	        if (f.toString().endsWith("file to be searched")) {
//	            System.out.println(f + " found!");
//	        }
//	    });
//	}

	public static void findFilesByNameRecursively(File sourceDir, String fileName, Set<File> results) {

		if (!sourceDir.isDirectory())
			throw new IllegalArgumentException("Source must be a directory!");

		if (sourceDir.canRead()) {

			for (File temp : sourceDir.listFiles()) {
				if (temp.isDirectory()) {
					findFilesByNameRecursively(temp, fileName, results);
				} else {
					if (fileName.equals(temp.getName())) {
						results.add(temp);
					}
				}
			}
		} else {
			throw new IllegalArgumentException(sourceDir.getAbsoluteFile() + "Permission Denied");
		}
	}

	public static String createFileNameForDataExportType(MainActionCommands type) {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		String typeString = "_DATA_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4R_COMMAND))
			typeString = "_4R_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND))
			typeString = "_4MPP_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND))
			typeString = "_4BINNER_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4METSCAPE_COMMAND))
			typeString = "_4MetScape_";

		if (type.equals(MainActionCommands.EXPORT_DUPLICATES_COMMAND))
			typeString = "_DUPLICATES_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND))
			typeString = "_4MWB_";

		if (type.equals(MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND))
			typeString = "_FEATURE_MZ_RT_STATS_";

		if (type.equals(MainActionCommands.EXPORT_PEAK_WIDTH_STATISTICS_COMMAND))
			typeString = "_FEATURE_PEAK_WIDTH_STATS_";

		if (type.equals(MainActionCommands.EXPORT_FEATURE_STATISTICS_COMMAND))
			typeString = "_FEATURE_MULTIPLE_QC_STATS_";

		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		DataPipeline dataPipeline = currentProject.getActiveDataPipeline();
		String fileName = currentProject.getName();
		if (currentProject.getLimsExperiment() != null)
			fileName = currentProject.getLimsExperiment().getId();

		fileName += "_" + dataPipeline.getName();
		MsFeatureSet fSet = currentProject.getActiveFeatureSetForDataPipeline(dataPipeline);
		if (!fSet.getName().equals(GlobalDefaults.ALL_FEATURES.getName()))
			fileName += "_" + currentProject.getActiveFeatureSetForDataPipeline(dataPipeline).getName();

		fileName += typeString + timestamp + ".txt";

		return fileName;
	}

}
