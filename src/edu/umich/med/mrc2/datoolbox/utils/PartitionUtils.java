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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

public class PartitionUtils {

	/**
	 * From here:
	 * https://stackoverflow.com/questions/27857011/how-to-split-a-string-array-into-small-chunk-arrays-in-java
	 */
	public static int[][] splitArray(int[] arrayToSplit, int chunkSize){
		
	    if(chunkSize<=0)
	        return null;
	    
	    // if rest>0 then our last array will have less elements than the others
	    int rest = arrayToSplit.length % chunkSize;
	    
	    int chunks = arrayToSplit.length / chunkSize + (rest > 0 ? 1 : 0);
	    int[][] arrays = new int[chunks][];
	    for(int i = 0; i < (rest > 0 ? chunks - 1 : chunks); i++){
	        // this copies 'chunk' times 'chunkSize' elements into a new array
	        arrays[i] = Arrays.copyOfRange(arrayToSplit, i * chunkSize, i * chunkSize + chunkSize);
	    }
	    if(rest > 0){
	        arrays[chunks - 1] = Arrays.copyOfRange(arrayToSplit, (chunks - 1) * chunkSize, (chunks - 1) * chunkSize + rest);
	    }
	    return arrays;
	}
	
	public static <T> List<List<T>> partition(Collection<T> list, int size){
		
		List<T>inputList = list.stream().collect(Collectors.toList());
		return ListUtils.partition(inputList, size);
	}
}
