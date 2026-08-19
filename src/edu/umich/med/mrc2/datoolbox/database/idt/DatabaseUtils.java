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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DatabaseUtils {
	
	private static final Logger logger = LogManager.getLogger(DatabaseUtils.class);
	
	private DatabaseUtils() {
		/* This utility class should not be instantiated */
	}
	
	public static void writeBlobToFile(File destination, InputStream blobStream) {

		try(BufferedInputStream is = new BufferedInputStream(blobStream)){		
			inputToOutput(is, destination);
		} catch (IOException e2) {
			logger.error(String.format("Failed to write data to %s", destination.getAbsolutePath()), e2);
		}
	}
	
	private static void inputToOutput(BufferedInputStream is, File destination) {
		
		try(FileOutputStream fos = new FileOutputStream(destination)){
			writeBufferToFile(fos, is, destination);
		} catch (IOException e1) {
			logger.error(String.format("Failed to write data to %s", destination.getAbsolutePath()), e1);
		}
	}
	
	private static void writeBufferToFile(FileOutputStream fos, BufferedInputStream is, File destination) {
		byte[] buffer = new byte[2048];
		int r = 0;
		try {
			while ((r = is.read(buffer)) != -1) {
				fos.write(buffer, 0, r);
			}
		} catch (IOException e) {
			logger.error(String.format("Failed to write data to %s", destination.getAbsolutePath()), e);
		}
		try {
			fos.flush();
		} catch (IOException e) {
			logger.error(String.format("Failed to write data to %s", destination.getAbsolutePath()), e);
		}
	}
	
	public static FileInputStream compressAndSetBlob(
			File inputFile,			
			PreparedStatement ps,
			int blobPosition) throws IOException, SQLException {
		
		int streamLength = 0;
		FileInputStream fis = null;
		if(inputFile != null && inputFile.exists()) {
			
			File archive = FIOUtils.changeExtension(inputFile, "zip");
			if(inputFile.isDirectory())
				CompressionUtils.zipFolder(inputFile, archive);
			else
				CompressionUtils.zipFile(inputFile, archive);

			if(archive.exists()) {
				fis = new FileInputStream(archive);
				streamLength = (int) archive.length();
			}
			if(fis != null)
				ps.setBinaryStream(blobPosition, fis, streamLength);
			else
				ps.setBinaryStream(blobPosition, null, 0);
		} else {
			ps.setBinaryStream(blobPosition, null, 0);
		}
		return fis;
	}
	
	public static void deleteTemporaryArchive(File inputFile) {
		File archive = FIOUtils.changeExtension(inputFile, "zip");
		if(archive.exists()) {
			Path path = Paths.get(archive.getAbsolutePath());
	        try {
				Files.delete(path);
			} catch (IOException e) {
				logger.error(String.format("Failed to delete temporary archive %s", archive.getAbsolutePath()));
			}
		}
	}
	
	public static FileInputStream setBlob(
			File inputFile,			
			PreparedStatement ps,
			int blobPosition) throws IOException, SQLException {
		
		int streamLength = 0;
		FileInputStream fis = null;
		if(inputFile != null && inputFile.exists()) {
			fis = new FileInputStream(inputFile);
			streamLength = (int) inputFile.length();
			ps.setBinaryStream(blobPosition, fis, streamLength);
		} else {
			ps.setBinaryStream(blobPosition, null, 0);
		}
		return fis;
	}
	
	public static InputStream setBlobFromCompressedString(
			String inputString, 
			PreparedStatement ps,
			int blobPosition) throws IOException, SQLException {
		
		byte[] compressedMethod = CompressionUtils.compressString(inputString);
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(compressedMethod);
		} catch (Exception e) {
			logger.error("Failed to create input stream", e);
		}
		if(is != null)
			ps.setBinaryStream(blobPosition, is, compressedMethod.length);
		else
			ps.setBinaryStream(blobPosition, null, 0);
		
		return is;
	}
	
	public static String decompressStringFromBlob(InputStream blobInputStream) throws IOException {
		
		String outputString = null;
		try(InputStream its = blobInputStream){
			if (its != null) {
				try(BufferedInputStream itbis = new BufferedInputStream(its)){
					outputString = CompressionUtils.decompressString(itbis.readAllBytes());
				}
			}
		}
		return outputString;
	}
}

















