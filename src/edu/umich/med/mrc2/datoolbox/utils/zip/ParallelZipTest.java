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

package edu.umich.med.mrc2.datoolbox.utils.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.Iterator;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;

public class ParallelZipTest {

	public ParallelZipTest() {
		// TODO Auto-generated constructor stub
	}

	public static void compressFolder(String sourceFolder, String absoluteZipfilepath) {
		long start = System.currentTimeMillis();
		try {
			ScatterSample scatterSample = new ScatterSample();
			File srcFolder = new File(sourceFolder);
			if (srcFolder != null && srcFolder.isDirectory()) {
				Iterator<File> i = FileUtils.iterateFiles(srcFolder, new String[] { "xml" }, true);
				File zipFile = new File(absoluteZipfilepath);
				
		        ZipArchiveOutputStream zipArchiveOutputStream =
			        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
			        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, 
			        			new FileOutputStream(zipFile));
		        zipArchiveOutputStream.setUseZip64(Zip64Mode.Always);			        
				while (i.hasNext()) {
					
					File file = i.next();
					ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(file.getName());
					zipArchiveEntry.setMethod(ZipArchiveEntry.DEFLATED);
					scatterSample.addEntry(zipArchiveEntry, 
							new FileInputStreamSupplier(Paths.get(file.getAbsolutePath())));
				}
				scatterSample.writeTo(zipArchiveOutputStream);
				zipArchiveOutputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		System.err.println(Double.toString((end - start)/1000));
	}

	public static void main(String[] args) {

		compressFolder("Y:\\DataAnalysis\\MRC2ToolboxProjects\\RD_9\\xmls",
				"Y:\\DataAnalysis\\MRC2ToolboxProjects\\RD_9\\xmls.zip");
	}
}
