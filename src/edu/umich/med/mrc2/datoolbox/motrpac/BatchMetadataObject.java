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

package edu.umich.med.mrc2.datoolbox.motrpac;

import java.util.Date;

public class BatchMetadataObject {

	private String experimentId;
	private String assay;
	private String batchName;
	private Date runStart;
	private Date runEnd;
	
	public BatchMetadataObject() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public BatchMetadataObject(String experimentId, String assay, String batchName, Date runStart, Date runEnd) {
		super();
		this.experimentId = experimentId;
		this.assay = assay;
		this.batchName = batchName;
		this.runStart = runStart;
		this.runEnd = runEnd;
	}
	public String getExperimentId() {
		return experimentId;
	}

	public void setExperimentId(String experimentId) {
		this.experimentId = experimentId;
	}

	public String getAssay() {
		return assay;
	}

	public void setAssay(String assay) {
		this.assay = assay;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public Date getRunStart() {
		return runStart;
	}

	public void setRunStart(Date runStart) {
		this.runStart = runStart;
	}

	public Date getRunEnd() {
		return runEnd;
	}

	public void setRunEnd(Date runEnd) {
		this.runEnd = runEnd;
	}
	
	
}
