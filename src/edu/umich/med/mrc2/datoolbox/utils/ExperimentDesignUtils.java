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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;

public class ExperimentDesignUtils {

	public static void AdjustEnabledLevels(ExperimentDesign design) {
		
		//	Get disabled levels in enabled factors
		Set<ExperimentDesignLevel>disabledLevels = 
				design.getFactors().stream().filter(f -> f.isEnabled()).flatMap(f -> f.getLevels().stream()).
				filter(l -> !l.isEnabled()).collect(Collectors.toSet());
		
		if(disabledLevels.isEmpty())
			return;
		
		Set<ExperimentDesignLevel>enabledLevels = design.getSamples().stream().
				filter(s -> Collections.disjoint(s.getDesignCell().values(), disabledLevels)).
				flatMap(s -> s.getDesignCell().values().stream()).collect(Collectors.toCollection(HashSet::new));
		
		design.getFactors().stream().filter(f -> f.isEnabled()).
			flatMap(f -> f.getLevels().stream()).
			forEach(s -> s.setEnabled(enabledLevels.contains(s)));
	}
}
