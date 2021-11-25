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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;

public class CompressionUtils {

	/**
	 * Decompress the zlib-compressed bytes and return an array of decompressed
	 * bytes
	 *
	 */
	public static byte[] decompress(byte compressedBytes[]) throws DataFormatException {

		Inflater decompresser = new Inflater();
		decompresser.setInput(compressedBytes);

		byte[] resultBuffer = new byte[compressedBytes.length * 2];
		byte[] resultTotal = new byte[0];
		int resultLength = decompresser.inflate(resultBuffer);

		while (resultLength > 0) {

			byte previousResult[] = resultTotal;
			resultTotal = new byte[resultTotal.length + resultLength];
			System.arraycopy(previousResult, 0, resultTotal, 0, previousResult.length);
			System.arraycopy(resultBuffer, 0, resultTotal, previousResult.length, resultLength);
			resultLength = decompresser.inflate(resultBuffer);
		}
		decompresser.end();
		return resultTotal;
	}

	/**
     * Add all files from the source directory to the destination zip file
     *
     * @param source      the directory with files to add
     * @param destination the zip file that should contain the files
     * @throws IOException      if the io fails
     * @throws ArchiveException if creating or adding to the archive fails
     */
    public static void zipFolder(File source, File destination) throws IOException, ArchiveException {

    	if (source == null || !source.exists())
            return;

    	if(!source.isDirectory())
    		return;

        OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
        Collection<File> fileList = FileUtils.listFiles(source, null, true);

        for (File file : fileList) {

            String entryName =
            		source.getName() + File.separator +
            		file.getCanonicalPath().substring(source.getAbsolutePath().length() + 1);

            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            archive.putArchiveEntry(entry);
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
            org.apache.commons.io.IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        }
        archive.finish();
        archive.close();
        archiveStream.close();
    }

    public static void zipFile(File source, File destination) 
    		throws IOException, ArchiveException {

    	if (source == null || !source.exists())
            return;

    	if(source.isDirectory())
    		return;

        OutputStream archiveStream = 
        		new FileOutputStream(destination);
        ArchiveOutputStream archive = 
        		new ArchiveStreamFactory().createArchiveOutputStream(
        				ArchiveStreamFactory.ZIP, archiveStream);
        ZipArchiveEntry entry = new ZipArchiveEntry(source.getName());
        archive.putArchiveEntry(entry);
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
        org.apache.commons.io.IOUtils.copy(input, archive);
        input.close();
        archive.closeArchiveEntry();
        archive.finish();
        archive.close();
        archiveStream.close();
    }

	public static File createOrRetrieveDirectory(final String pathToDir) throws IOException{

	    final Path path = Paths.get(pathToDir);

	    if(Files.notExists(path))
	        return Files.createDirectories(path).toFile();

	    return path.toFile();
	}
}




































