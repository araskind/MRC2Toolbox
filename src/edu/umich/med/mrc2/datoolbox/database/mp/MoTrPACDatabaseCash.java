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

package edu.umich.med.mrc2.datoolbox.database.mp;

import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACTissueCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotracSubjectType;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotrpacSampleType;

public class MoTrPACDatabaseCash {

	@SuppressWarnings("unused")
	private static Collection<MotracSubjectType> motrpacSubjetTypes = 
	new TreeSet<MotracSubjectType>();
	private static Collection<MotrpacSampleType> motrpacSampleTypes = 
			new TreeSet<MotrpacSampleType>();
	private static Collection<MoTrPACTissueCode> motrpacTissueCodes = 
			new TreeSet<MoTrPACTissueCode>();	
	private static Collection<MoTrPACStudy> motrpacStudies = 
			new TreeSet<MoTrPACStudy>();	
	private static Collection<MoTrPACAssay> motrpacAssays = 
			new TreeSet<MoTrPACAssay>();
	private static Collection<MoTrPACReportCodeBlock>motracReportCodeBlocks = 
			new TreeSet<MoTrPACReportCodeBlock>();
	
	private static Collection<MoTrPACReport>reports = new TreeSet<MoTrPACReport>();
	
	public static void refreshMotrpacSubjectTypes() {
		motrpacSubjetTypes.clear();
		getMotrpacSubjectTypeList();
	}
	
	public static Collection<MotracSubjectType> getMotrpacSubjectTypeList() {

		if(motrpacSubjetTypes == null)
			motrpacSubjetTypes = new TreeSet<MotracSubjectType>();
		
		if(motrpacSubjetTypes.isEmpty()) {
			try {
				motrpacSubjetTypes.addAll(MoTrPACDbUtils.getMotrpacSubjectTypes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motrpacSubjetTypes;
	}
	
	public static MotracSubjectType getMotracSubjectTypeByName(String typeName) {
		return getMotrpacSubjectTypeList().stream().
				filter(s -> s.getSubjectType().equals(typeName)).
				findFirst().orElse(null);
	}
	
	public static void refreshMotrpacReportCodes() {
		motrpacAssays.clear();
		getMotrpacAssayList();
	}
	
	public static void refreshMotrpacAssayList() {
		motracReportCodeBlocks.clear();
		getMotrpacReportCodeBlocks();
	}

	public static Collection<MoTrPACReportCodeBlock> getMotrpacReportCodeBlocks() {
		
		if(motracReportCodeBlocks == null)
			motracReportCodeBlocks = new TreeSet<MoTrPACReportCodeBlock>();
		
		if(motracReportCodeBlocks.isEmpty()) {
			try {
				motracReportCodeBlocks.addAll(MoTrPACDbUtils.getMotrpacReportCodeBlocks());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motracReportCodeBlocks;
	}
	
	public static MoTrPACReportCodeBlock getMoTrPACReportCodeBlockById(String name) {
		return getMotrpacReportCodeBlocks().stream().
				filter(b -> b.getBlockId().equals(name)).findFirst().orElse(null);
	}

	public static Collection<MoTrPACAssay> getMotrpacAssayList() {

		if(motrpacAssays == null)
			motrpacAssays = new TreeSet<MoTrPACAssay>();
		
		if(motrpacAssays.isEmpty()) {
			try {
				motrpacAssays.addAll(MoTrPACDbUtils.getMotrpacAssays());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motrpacAssays;
	}
	
	public static MoTrPACAssay getMotrpacAssayById(String id) {
		return getMotrpacAssayList().stream().
				filter(a -> a.getAssayId().equals(id)).
				findFirst().orElse(null);
	}
	
	public static void refreshMotrpacStudyList() {
		motrpacStudies.clear();
		getMotrpacStudyList();
	}

	public static Collection<MoTrPACStudy> getMotrpacStudyList() {

		if(motrpacStudies == null)
			motrpacStudies = new TreeSet<MoTrPACStudy>();
		
		if(motrpacStudies.isEmpty()) {
			try {
				motrpacStudies.addAll(MoTrPACDbUtils.getMotrpacStudies());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motrpacStudies;
	}
	
	public static MoTrPACStudy getMotrpacStudyById(String id) {
		return getMotrpacStudyList().stream().
				filter(s -> s.getId().equals(id)).findFirst().orElse(null);
	}
	
	public static void refreshMotrpacSampleTypeList() {
		motrpacSampleTypes.clear();
		getMotrpacSampleTypeList();
	}

	public static Collection<MotrpacSampleType> getMotrpacSampleTypeList() {

		if(motrpacSampleTypes == null)
			motrpacSampleTypes = new TreeSet<MotrpacSampleType>();
		
		if(motrpacSampleTypes.isEmpty()) {
			try {
				motrpacSampleTypes.addAll(MoTrPACDbUtils.getMotrpacSampleTypes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motrpacSampleTypes;
	}
	
	public static void refreshMotrpacTissueCodeList() {
		motrpacTissueCodes.clear();
		getMotrpacTissueCodeList();
	}

	public static Collection<MoTrPACTissueCode> getMotrpacTissueCodeList() {

		if(motrpacTissueCodes == null)
			motrpacTissueCodes = new TreeSet<MoTrPACTissueCode>();
		
		if(motrpacTissueCodes.isEmpty()) {
			try {
				motrpacTissueCodes.addAll(MoTrPACDbUtils.getMotrpacTissueCodes());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return motrpacTissueCodes;
	}
	
	public static MoTrPACTissueCode getMotrpacTissueCodeById(String id) {
		return getMotrpacTissueCodeList().stream().
				filter(c -> c.getCode().equals(id)).
				findFirst().orElse(null);
	}
	
	public static void refreshReportList() {
		reports.clear();
		getMoTrPACReports();
	}
	
	public static Collection<MoTrPACReport> getMoTrPACReports() {
	
		if(reports == null)
			reports = new TreeSet<MoTrPACReport>();
		
		if(reports.isEmpty()) {
			try {
				reports.addAll(MoTrPACDbUtils.getMoTrPACReports());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		return reports;
	}
	
	public static Collection<MoTrPACReport>getFilteredMoTrPACReports(
			MoTrPACStudy study,
			LIMSExperiment experiment,
			MoTrPACAssay assay,
			MoTrPACTissueCode tissue) {
		
		Collection<MoTrPACReport>filtered = new TreeSet<MoTrPACReport>();
		if(study == null)
			return filtered;
		
		Collection<MoTrPACReport>studyReports = getMoTrPACReports().stream().
				filter(r -> r.getStudy().equals(study)).
				collect(Collectors.toList());
		
		if(experiment != null)
			studyReports = studyReports.stream().
					filter(r -> r.getExperiment().equals(experiment)).
					collect(Collectors.toList());
			
		if(assay != null)
			studyReports = studyReports.stream().
					filter(r -> r.getAssay().equals(assay)).
					collect(Collectors.toList());
		
		if(tissue != null)
			studyReports = studyReports.stream().
					filter(r -> r.getTissueCode().equals(tissue)).
					collect(Collectors.toList());
		
		filtered.addAll(studyReports);
		return filtered;
	}
}















