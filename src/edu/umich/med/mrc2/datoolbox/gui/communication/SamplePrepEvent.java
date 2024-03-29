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

package edu.umich.med.mrc2.datoolbox.gui.communication;

import java.util.EventObject;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.RDPMetadataDefinitionStage;

public class SamplePrepEvent extends EventObject {

	/**
	 *
	 */
	private static final long serialVersionUID = -69865822456165105L;
	private ParameterSetStatus status;
	private RDPMetadataDefinitionStage defStage;

	public SamplePrepEvent(LIMSSamplePreparation source) {
		super(source);
	}

	public SamplePrepEvent(LIMSSamplePreparation source, ParameterSetStatus status) {

		super(source);
		this.status = status;
	}
	
	public SamplePrepEvent(
			LIMSSamplePreparation source, 
			ParameterSetStatus status, 
			RDPMetadataDefinitionStage defStage) {
		super(source);
		this.status = status;
		this.defStage = defStage;
	}

	public LIMSSamplePreparation getSource() {
		return (LIMSSamplePreparation) this.source;
	}

	public ParameterSetStatus getStatus() {
		return status;
	}

	public RDPMetadataDefinitionStage getDefStage() {
		return defStage;
	}
}
