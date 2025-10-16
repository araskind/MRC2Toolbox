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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;

public class MapUtils {

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> sortEntriesByValues(
			Map<K, V> map, SortDirection direction) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {

				if(direction.equals(SortDirection.DESC))
					return e2.getValue().compareTo(e1.getValue());
				else
					return e1.getValue().compareTo(e2.getValue());
			}
		});
		return sortedEntries;
	}

	public static <K, V extends Comparable<? super V>>Entry<K, V> getTopEntryByValue(
			Map<K, V> map, SortDirection direction) {

		return sortEntriesByValues(map, direction).get(0);
	}

	public static <K, V extends Comparable<? super V>>Map<K,V>sortMapByValue(Map<K, V> unSortedMap, SortDirection direction){

		LinkedHashMap<K, V> sortedMap = new LinkedHashMap<>();
		if(direction.equals(SortDirection.DESC)) {

			unSortedMap.entrySet()
			    .stream()
			    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		}
		else {
			unSortedMap.entrySet()
			    .stream()
			    .sorted(Map.Entry.comparingByValue())
			    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
		}
		return sortedMap;
	}
}
