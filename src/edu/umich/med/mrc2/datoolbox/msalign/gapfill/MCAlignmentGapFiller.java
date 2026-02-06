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

package edu.umich.med.mrc2.datoolbox.msalign.gapfill;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.compress.utils.FileNameUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.MetabCombinerParametersObject;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.rqc.MetabCombinerAlignmentScriptGenerator;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MCAlignmentGapFiller {

	private File mcAlignmentProjectDir;
	private Set<PairwiseBatchAlignment>pairwiseBatchAlignmentSet;
	private MetabCombinerParametersObject alignmentProjectSettings;
	
	public MCAlignmentGapFiller(File mcAlignmentProjectDir) {
		super();
		
		this.mcAlignmentProjectDir = mcAlignmentProjectDir;
		pairwiseBatchAlignmentSet = new TreeSet<>();
	}
	
	public void readCummulativeMetaDataWithGaps() {
		
		Path cummulativeMetaDataPath = Paths.get(mcAlignmentProjectDir.getAbsolutePath(),
				MetabCombinerAlignmentScriptGenerator.EXTENDED_CUMMULATIVE_METADATA_FILE_NAME);
		String[][]alignmentData  = new String[0][0];
		try {
			alignmentData = DelimitedTextParser.parseTextFileWithEncoding(
					cummulativeMetaDataPath.toFile(), MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(alignmentData.length > 1) {
			
		}
	}
	
	public void parseAlignmentProjectSettings() {
		
		File xmlFile = Paths.get(mcAlignmentProjectDir.getAbsolutePath(),
				MetabCombinerAlignmentScriptGenerator.ALIGNMENT_SETTINGS_FILE).toFile();
		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {
			doc = sax.build(xmlFile);
		} catch (JDOMException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;						
		}
		if(doc != null)
			alignmentProjectSettings = 
				new MetabCombinerParametersObject(doc.getRootElement());
	}

	public void readPairwiseBatchAlignments() {
		
		Path alignmentFolderPath = Paths.get(mcAlignmentProjectDir.getAbsolutePath(),
				MetabCombinerAlignmentScriptGenerator.McAlignmentProjectSubfolders.AlignmentReports.name());		
		List<Path> alignmentReportList = 
				FIOUtils.findFilesByNameStartingWith(alignmentFolderPath, 
				MetabCombinerAlignmentScriptGenerator.ALIGNMENT_REPORT_NAME_PREFIX);
		for(Path alignmentReportPath : alignmentReportList) {
			
			PairwiseBatchAlignment pba = readPairwiseBatchAlignment(alignmentReportPath.toFile());
			if(pba != null)
				pairwiseBatchAlignmentSet.add(pba);
		}
	}

	private PairwiseBatchAlignment readPairwiseBatchAlignment(File sourceFile) {
		
		if(sourceFile == null || !sourceFile.exists())
			return null;

		String[]parts = FileNameUtils.getBaseName(sourceFile.getPath()).split("-");
		PairwiseBatchAlignment pba = new PairwiseBatchAlignment(
				sourceFile,
				parts[1],
				parts[2],
				parts[3],
				parts[4]);
		String[][]alignmentData  = new String[0][0];
		try {
			alignmentData = DelimitedTextParser.parseTextFileWithEncoding(
					sourceFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(alignmentData.length > 1) {
			
			int idx = -1;
			int idy = -1;
			for(int i=0; i<alignmentData[0].length; i++) {
				
				if(alignmentData[0][i].equals("idx"))
					idx = i;
				
				if(alignmentData[0][i].equals("idy"))
					idy = i;
			}
			Map<String,String>featureAlignmentMap = new TreeMap<>();
			for(int i=1; i<alignmentData.length; i++)
				featureAlignmentMap.put(alignmentData[i][idx], alignmentData[i][idy]);
			
			pba.getFeatureAlignmentMap().putAll(featureAlignmentMap);
		}		
		return pba;
	}
}
