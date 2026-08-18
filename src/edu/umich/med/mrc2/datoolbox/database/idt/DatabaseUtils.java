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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DatabaseUtils {
	
	private static final Logger logger = LogManager.getLogger(DatabaseUtils.class);
	
	private DatabaseUtils() {
		/* This utility class should not be instantiated */
	}
	
	public static void writeBlobStreamToFile(File destination, InputStream blobStream) {

		try(BufferedInputStream is = new BufferedInputStream(blobStream)){		
			inputToOutput(is, destination);
		} catch (IOException e2) {
			logger.error(String.format("%s %s", "Failed to write data to", destination.getAbsolutePath()), e2);
		}
	}
	
	private static void inputToOutput(BufferedInputStream is, File destination) {
		
		try(FileOutputStream fos = new FileOutputStream(destination)){
			writeBufferToFile(fos, is, destination);
		} catch (IOException e1) {
			logger.error(String.format("%s %s", "Failed to write data to", destination.getAbsolutePath()), e1);
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
			logger.error(String.format("%s %s", "Failed to write data to", destination.getAbsolutePath()), e);
		}
		try {
			fos.flush();
		} catch (IOException e) {
			logger.error(String.format("%s %s", "Failed to write data to", destination.getAbsolutePath()), e);
		}
	}
}
