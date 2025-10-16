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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArrayUtils {
	
	public static int[] concatIntArrays(int[] array1, int[] array2) {
	    return IntStream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray();
	}
	
	public static double[] concatDoubleArrays(double[] array1, double[] array2) {
	    return DoubleStream.concat(Arrays.stream(array1), Arrays.stream(array2)).toArray();
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] concatObjectArrays(T[] array1, T[] array2) {
		
	    return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
	    	      .toArray(size -> (T[]) Array.newInstance(array1.getClass().getComponentType(), size));
	}
}
