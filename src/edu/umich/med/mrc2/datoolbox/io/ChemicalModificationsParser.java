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

package edu.umich.med.mrc2.datoolbox.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod.ChemModificationTableModel;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ChemicalModificationsParser {

	private static final String lineSeparator = System.getProperty("line.separator");
	private static char columnSeparator;

	public static void writeChemicalModificationsToFile(File exportFile) throws Exception {

		FileWriter fWriter = new FileWriter(exportFile);
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();

		// Create header
		writer.append(ChemModificationTableModel.ENABLED_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.TYPE_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.CHEM_MOD_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.DESCRIPTION_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.CHARGE_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.OLIGOMER_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.ADDED_GROUP_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.REMOVED_GROUP_COLUMN);
		writer.append(columnSeparator);
		writer.append(ChemModificationTableModel.MASS_CORRECTION_COLUMN);
		writer.append(lineSeparator);

		for (Adduct mod : AdductManager.getAdductList()) {

			String active = Boolean.toString(mod.isEnabled());
			writer.append(active);
			writer.append(columnSeparator);
			writer.append(mod.getModificationType().getName());
			writer.append(columnSeparator);
			writer.append(mod.getName());
			writer.append(columnSeparator);
			writer.append(mod.getDescription());
			writer.append(columnSeparator);
			writer.append(Integer.toString(mod.getCharge()));
			writer.append(columnSeparator);
			writer.append(Integer.toString(mod.getOligomericState()));
			writer.append(columnSeparator);
			writer.append(mod.getAddedGroup());
			writer.append(columnSeparator);
			writer.append(mod.getRemovedGroup());
			writer.append(columnSeparator);
			writer.append(MRC2ToolBoxConfiguration.getMzFormat().format(mod.getMassCorrection()));
			writer.append(lineSeparator);
		}
		writer.flush();
		fWriter.close();
		writer.close();
	}
}
