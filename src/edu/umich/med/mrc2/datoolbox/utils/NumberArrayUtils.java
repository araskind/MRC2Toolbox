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
 * This code is modified from EBI JMZML library BinaryDataArray class
 * uk.ac.ebi.jmzml.model.mzml.BinaryDataArray
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class NumberArrayUtils {
	
    public static final int BYTES_64_PRECISION = 8;
    public static final int BYTES_32_PRECISION = 4;
    private static final Base64 base64 = new Base64();
    
    public enum Precision {
        /**
         * Corresponds to the PSI-MS ontology term "MS:1000521" / "32-bit float"
         * and binary data will be represented in the Java primitive: float
         */
        FLOAT32BIT,

        /**
         * Corresponds to the PSI-MS ontology term "MS:1000523" / "64-bit float"
         * and binary data will be represented in the Java primitive: double
         */
        FLOAT64BIT,

        /**
         * Corresponds to the PSI-MS ontology term "MS:1000519" / "32-bit integer"
         * and binary data will be represented in the Java primitive: int
         */
        INT32BIT,

        /**
         * Corresponds to the PSI-MS ontology term "MS:1000522" / "64-bit integer"
         * and binary data will be represented in the Java primitive: long
         */
        INT64BIT,

        /**
         * Corresponds to the PSI-MS ontology term "MS:1001479" / "null-terminated ASCII string"
         * and binary data will be represented in the Java type: String
         */
        NTSTRING
    }
    
    public static String encodeNumberArray(double[] value) throws UnsupportedEncodingException {
    	
        ByteBuffer buffer = ByteBuffer.allocate(value.length * BYTES_64_PRECISION);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (double aDoubleArray : value)
            buffer.putDouble(aDoubleArray);
        
        byte[] data = buffer.array();
        byte[] compressed = gzipCompress(data);
        byte[] binArray = null;

		binArray = new Base64().encode(compressed);
        return new String(binArray, StandardCharsets.US_ASCII);
    }
    
    public static double[] decodeNumberArray(String encodedValues) throws UnsupportedEncodingException {
    	
    	byte[] compressed = encodedValues.getBytes("ASCII");
    	byte[]decoded = new Base64().decode(compressed); 	
    	byte[] uncompressed = gzipUncompress(decoded);   	
    	Number[]dataArray = convertData(uncompressed, Precision.FLOAT64BIT);
        return Arrays.asList(dataArray).stream().
        		mapToDouble(v -> v.doubleValue()).toArray();
    }
    
    private static Number[] convertData(byte[] data, Precision prec) {
        int step;
        switch (prec) {
            case FLOAT64BIT: // fall through
            case INT64BIT:
                step = 8;
                break;
            case FLOAT32BIT: // fall through
            case INT32BIT:
                step = 4;
                break;
            default:
                step = -1;
        }
        // create a Number array of sufficient size
        Number[] resultArray = new Number[data.length / step];
        // create a buffer around the data array for easier retrieval
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN); // the order is always LITTLE_ENDIAN
        // progress in steps of 4/8 bytes according to the set step
        for (int indexOut = 0; indexOut < data.length; indexOut += step) {
            // Note that the 'getFloat(index)' and getInt(index) methods read the next 4 bytes
            // and the 'getDouble(index)' and getLong(index) methods read the next 8 bytes.
            Number num;
            switch (prec) {
                case FLOAT64BIT:
                    num = bb.getDouble(indexOut);
                    break;
                case INT64BIT:
                    num = bb.getLong(indexOut);
                    break;
                case FLOAT32BIT:
                    num = bb.getFloat(indexOut);
                    break;
                case INT32BIT:
                    num = bb.getInt(indexOut);
                    break;
                default:
                    num = null;
            }
            resultArray[indexOut / step] = num;
        }
        return resultArray;
    }
    
	private static byte[] gzipCompress(byte[] uncompressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(uncompressedData.length);
             GZIPOutputStream gzipOS = new GZIPOutputStream(bos)) {
            gzipOS.write(uncompressedData);
            // You need to close it before using bos
            gzipOS.close();
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

	private static byte[] gzipUncompress(byte[] compressedData) {
        byte[] result = new byte[]{};
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPInputStream gzipIS = new GZIPInputStream(bis)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
	
    private static byte[] compress(byte[] uncompressedData) {
        byte[] data; // Decompress the data

        // create a temporary byte array big enough to hold the compressed data
        // with the worst compression (the length of the initial (uncompressed) data)
        // EDIT: if it turns out this byte array was not big enough, then double its size and try again.
        byte[] temp = new byte[uncompressedData.length / 2];
        int compressedBytes = temp.length;
        while (compressedBytes == temp.length) {
            // compress
            temp = new byte[temp.length * 2];
            Deflater compresser = new Deflater();
            compresser.setInput(uncompressedData);
            compresser.finish();
            compressedBytes = compresser.deflate(temp);
        }      
        
        // create a new array with the size of the compressed data (compressedBytes)        
        data = new byte[compressedBytes];
        System.arraycopy(temp, 0, data, 0, compressedBytes);

        return data;
    }
    
    private static byte[] decompress(byte[] compressedData) {
    	
        byte[] decompressedData;

        // using a ByteArrayOutputStream to not having to define the result array size beforehand
        Inflater decompressor = new Inflater();

        decompressor.setInput(compressedData);
        // Create an expandable byte array to hold the decompressed data
        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);
        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                if (count == 0 && decompressor.needsInput()) {
                    break;
                }
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                throw new IllegalStateException("Encountered wrong data format " +
                        "while trying to decompress binary data!", e);
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            // ToDo: add logging
            e.printStackTrace();
        }
        // Get the decompressed data
        decompressedData = bos.toByteArray();

        if (decompressedData == null) {
            throw new IllegalStateException("Decompression of binary data produced no result (null)!");
        }
        return decompressedData;
    }
    
	public static String encodeStatValues(DescriptiveStatistics stats) {
		
		String valueString = null;
		if(stats != null && stats.getN() > 0) {
			
			try {
				valueString = encodeNumberArray(stats.getValues());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return valueString;
	}
	
	public static double[] decodeValueString(String encodedValues) {
		
		if(encodedValues == null || encodedValues.isBlank())
			return null;
		
		double[] values = null;
		try {
			values = NumberArrayUtils.decodeNumberArray(encodedValues);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return values;
	}
}










