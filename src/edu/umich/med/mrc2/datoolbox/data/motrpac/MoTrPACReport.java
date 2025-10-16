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

package edu.umich.med.mrc2.datoolbox.data.motrpac;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;

public class MoTrPACReport implements Comparable<MoTrPACReport>{

	private String id;
	private Date dateCreated;
	private LIMSUser createBy;
	private String linkedDocumentId;
	private String linkedDocumentName;
	private DocumentFormat linkedDocumentFormat;
	private Map<MoTrPACReportCodeBlock,MoTrPACReportCode>reportStage;
	private MoTrPACStudy study;
	private LIMSExperiment experiment;
	private MoTrPACAssay assay;
	private MoTrPACTissueCode tissueCode;
	private int versionNumber;
	
	public MoTrPACReport(LIMSUser createBy) {
		super();
		this.createBy = createBy;
		this.dateCreated = new Date();
		reportStage = new TreeMap<MoTrPACReportCodeBlock,MoTrPACReportCode>();
	}

	public MoTrPACReport(
			String id, 
			Date dateCreated, 
			LIMSUser createBy, 
			String linkedDocumentId,
			String linkedDocumentName, 
			DocumentFormat linkedDocumentFormat) {
		super();
		this.id = id;
		this.dateCreated = dateCreated;
		this.createBy = createBy;
		this.linkedDocumentId = linkedDocumentId;
		this.linkedDocumentName = linkedDocumentName;
		this.linkedDocumentFormat = linkedDocumentFormat;
		reportStage = new TreeMap<MoTrPACReportCodeBlock,MoTrPACReportCode>();
	}
		
	public void setReportStageBlock(MoTrPACReportCodeBlock block, MoTrPACReportCode code) {
		reportStage.put(block, code);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public LIMSUser getCreateBy() {
		return createBy;
	}

	public void setCreateBy(LIMSUser createBy) {
		this.createBy = createBy;
	}

	public String getLinkedDocumentId() {
		return linkedDocumentId;
	}

	public void setLinkedDocumentId(String linkedDocumentId) {
		this.linkedDocumentId = linkedDocumentId;
	}

	public String getLinkedDocumentName() {
		return linkedDocumentName;
	}

	public void setLinkedDocumentName(String linkedDocumentName) {
		this.linkedDocumentName = linkedDocumentName;
	}

	public DocumentFormat getLinkedDocumentFormat() {
		return linkedDocumentFormat;
	}

	public void setLinkedDocumentFormat(DocumentFormat linkedDocumentFormat) {
		this.linkedDocumentFormat = linkedDocumentFormat;
	}

	public Map<MoTrPACReportCodeBlock, MoTrPACReportCode> getReportStage() {
		return reportStage;
	}

	@Override
	public int compareTo(MoTrPACReport o) {
		return id.compareTo(o.getId());
	}

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;
        
		if (obj == this)
			return true;

        if (!MoTrPACReport.class.isAssignableFrom(obj.getClass()))
            return false;

        final MoTrPACReport other = (MoTrPACReport) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

	public LIMSExperiment getExperiment() {
		return experiment;
	}

	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}

	public MoTrPACAssay getAssay() {
		return assay;
	}

	public void setAssay(MoTrPACAssay assay) {
		this.assay = assay;
	}

	public MoTrPACTissueCode getTissueCode() {
		return tissueCode;
	}

	public void setTissueCode(MoTrPACTissueCode tissueCode) {
		this.tissueCode = tissueCode;
	}

	public MoTrPACStudy getStudy() {
		return study;
	}

	public void setStudy(MoTrPACStudy study) {
		this.study = study;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	public String getReportDefinitionKey() {
		
		String reportDefinitionKey =
				study.getId() + experiment.getId() + 
				assay.getAssayId() + tissueCode.getCode();
		for(Entry<MoTrPACReportCodeBlock, MoTrPACReportCode> stage : reportStage.entrySet()) 
			reportDefinitionKey += stage.getKey().getBlockId() + stage.getValue().getOptionName();
		
		return reportDefinitionKey;
	}	
}







