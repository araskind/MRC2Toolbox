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

package edu.umich.med.mrc2.datoolbox.data;

import edu.umich.med.mrc2.datoolbox.data.enums.AdductNotationType;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public interface Adduct extends Comparable<Adduct>{
	
	void setId(String id);
	
	String getId();

	void finalizeModification();

	double getAbsoluteMassCorrection();

	String getAddedGroup();

	int getCharge();

	String getDescription();

	double getMassCorrection();

	ModificationType getModificationType();

	String getName();

	int getOligomericState();

	Polarity getPolarity();

	String getRemovedGroup();

	boolean isEnabled();

	boolean isHalogenated();

	void setAddedGroup(String addedGroup);

	void setCharge(int charge);

	void setDescription(String description);

	void setEnabled(boolean isEnabled);

	void setMassCorrection(double massCorrection);

	void setModificationType(ModificationType modificationType);

	void setName(String adductName);

	void setOligomericState(int oligomericState);

	void setRemovedGroup(String removedGroup);

	String getSmiles();

	void setSmiles(String smiles);
	
	void setNotationForType(AdductNotationType notationType, String notation);
	
	String getNotationForType(AdductNotationType notationType);
}