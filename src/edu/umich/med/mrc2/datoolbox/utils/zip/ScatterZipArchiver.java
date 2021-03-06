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
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator;
import org.apache.commons.compress.archivers.zip.ScatterZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.parallel.InputStreamSupplier;
import org.apache.commons.io.FileUtils;

public class ScatterZipArchiver { 

    private ParallelScatterZipCreator scatterZipCreator;
    private ScatterZipOutputStream dirs; 
    private File tempDir;

    public ScatterZipArchiver() throws IOException { 
    	scatterZipCreator = new ParallelScatterZipCreator(); 
    	tempDir = File.createTempFile("scatter-dirs", "tmp");
    	dirs = ScatterZipOutputStream.fileBased(tempDir, 
        				Deflater.BEST_COMPRESSION);
    } 

    public ScatterZipArchiver(File tmpDirParent) throws IOException { 
    	scatterZipCreator = new ParallelScatterZipCreator(); 
    	tempDir = File.createTempFile("scatter-dirs", "tmp", tmpDirParent);
    	dirs = ScatterZipOutputStream.fileBased(tempDir, 
        				Deflater.BEST_COMPRESSION);
    } 
    
    public void addEntry(
    		ZipArchiveEntry zipArchiveEntry, 
    		InputStreamSupplier streamSupplier) throws IOException { 
        if (zipArchiveEntry.isDirectory() && !zipArchiveEntry.isUnixSymlink()) 
            dirs.addArchiveEntry(ZipArchiveEntryRequest.createZipArchiveEntryRequest(zipArchiveEntry, streamSupplier)); 
        else 
            scatterZipCreator.addArchiveEntry( zipArchiveEntry, streamSupplier); 
    } 

    public void writeTo(ZipArchiveOutputStream zipArchiveOutputStream) 
            throws IOException, ExecutionException, InterruptedException { 
        dirs.writeTo(zipArchiveOutputStream); 
        dirs.close(); 
        scatterZipCreator.writeTo(zipArchiveOutputStream); 
    }

	public void cleanup() {
		
		if(tempDir != null) {
			try {
				FileUtils.deleteDirectory(tempDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	} 
}
