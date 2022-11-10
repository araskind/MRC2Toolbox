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
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ChemicalModificationsParser {

	private static final String lineSeparator = System.getProperty("line.separator");
	private static char columnSeparator;
	
	public static final String ORDER_COLUMN = "##";
	public static final String ENABLED_COLUMN = "Active";
	public static final String TYPE_COLUMN = "Type";
	public static final String CHEM_MOD_COLUMN = "Name";
	public static final String CEF_NOTATION_COLUMN = "CEF notation";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String CHARGE_COLUMN = "Charge";
	public static final String OLIGOMER_COLUMN = "Oligomer";
	public static final String ADDED_GROUP_COLUMN = "Added group";
	public static final String REMOVED_GROUP_COLUMN = "Removed group";
	public static final String MASS_CORRECTION_COLUMN = "Mass correction";
	public static final String MASS_CORRECTION_ABS_COLUMN = "Mass correction abs.";
	public static final String KMD_COLUMN = "KMD";
	public static final String POOLED_MEDIAN_COLUMN = "Pooled median";

	public static void writeChemicalModificationsToFile(File exportFile) throws Exception {

		FileWriter fWriter = new FileWriter(exportFile);
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();

		// Create header
		writer.append(ENABLED_COLUMN);
		writer.append(columnSeparator);
		writer.append(TYPE_COLUMN);
		writer.append(columnSeparator);
		writer.append(CHEM_MOD_COLUMN);
		writer.append(columnSeparator);
		writer.append(DESCRIPTION_COLUMN);
		writer.append(columnSeparator);
		writer.append(CHARGE_COLUMN);
		writer.append(columnSeparator);
		writer.append(OLIGOMER_COLUMN);
		writer.append(columnSeparator);
		writer.append(ADDED_GROUP_COLUMN);
		writer.append(columnSeparator);
		writer.append(REMOVED_GROUP_COLUMN);
		writer.append(columnSeparator);
		writer.append(MASS_CORRECTION_COLUMN);
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
