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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.ExperimentDesignFields;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class ExperimentDesignParser {

	// TODO add design completeness and validity verification
	public static ExperimentDesign parseExperimentDesign(File designFile) {

		int nameColumn = -1;
		int idColumn = -1;
		int typeColumn = -1;
		boolean isFactor;

		ExperimentDesign experimentDesign = new ExperimentDesign();
		HashMap<ExperimentDesignFactor, Integer> factorMap = new HashMap<ExperimentDesignFactor, Integer>();
		String[][] designData = null;
		try {
			designData = DelimitedTextParser.parseTextFileWithEncoding(designFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (designData != null) {

			// Parse header
			String[] header = designData[0];

			for (int i = 0; i < header.length; i++) {

				isFactor = true;

				for (ExperimentDesignFields af : ExperimentDesignFields.values()) {

					if (header[i].equals(af.getName()))
						isFactor = false;
				}
				if (header[i].equals(StandardFactors.SAMPLE_CONTROL_TYPE.getName()))
					isFactor = false;

				if (header[i].equals(StandardFactors.BATCH.getName()))
					isFactor = false;

				if (isFactor) {

					ExperimentDesignFactor newFactor = new ExperimentDesignFactor(header[i]);
					experimentDesign.addFactor(newFactor,false);
					factorMap.put(newFactor, i);
				}
				if (header[i].equals(ExperimentDesignFields.SAMPLE_NAME.getName()))
					nameColumn = i;

				if (header[i].equals(ExperimentDesignFields.SAMPLE_ID.getName()))
					idColumn = i;

				if (header[i].equals(StandardFactors.SAMPLE_CONTROL_TYPE.getName()))
					typeColumn = i;
			}
			if (nameColumn == -1 || idColumn == -1)
				return null;

			// Add levels
			for (Entry<ExperimentDesignFactor, Integer> entry : factorMap.entrySet()) {

				TreeSet<String> levels = new TreeSet<String>();

				for (int j = 1; j < designData.length; j++)
					levels.add(designData[j][entry.getValue()].trim());

				for (String level : levels) {

					ExperimentDesignLevel newLevel = new ExperimentDesignLevel(level, entry.getKey());
					entry.getKey().addLevel(newLevel);
				}
			}
			// Assign levels to samples
			for (int i = 1; i < designData.length; i++) {

				ExperimentalSample es = new ExperimentalSample(designData[i][idColumn], designData[i][nameColumn]);

				//	Sample type
				if (typeColumn == -1)
					es.addDesignLevel(ReferenceSamplesManager.sampleLevel);

				if(typeColumn >= 0) {

					ExperimentDesignLevel refLevel = 
							ReferenceSamplesManager.getSampleControlTypeFactor().getLevelByName(designData[i][typeColumn].trim());
					if(refLevel != null)
						es.addDesignLevel(ReferenceSamplesManager.sampleLevel);
					else
						es.addDesignLevel(refLevel);
				}
				//	User-specified factors
				String[] line = designData[i];
				factorMap.forEach((k,v) -> es.addDesignLevel(k.getLevelByName(line[v].trim())));
				experimentDesign.addSample(es,false);
			}
		}
		return experimentDesign;
	}
}
