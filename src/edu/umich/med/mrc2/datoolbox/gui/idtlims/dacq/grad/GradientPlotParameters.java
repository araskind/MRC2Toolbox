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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.util.EnumMap;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.enums.SolventChannel;

public class GradientPlotParameters {

	private boolean showSolventPercent;
	private boolean showFlowRate;
	private final Map<SolventChannel,Boolean>showChannels;
	
	public GradientPlotParameters() {
		super();
		showChannels = new EnumMap<>(SolventChannel.class);
	}

	public boolean isShowSolventPercent() {
		return showSolventPercent;
	}

	public void setShowSolventPercent(boolean showSolventPercent) {
		this.showSolventPercent = showSolventPercent;
	}

	public boolean isShowFlowRate() {
		return showFlowRate;
	}

	public void setShowFlowRate(boolean showFlowRate) {
		this.showFlowRate = showFlowRate;
	}
	
	public void setChannelVisibility(SolventChannel channel, boolean isVisible) {
		showChannels.put(channel, isVisible);
	}
	
	public boolean getChannelVisibility(SolventChannel channel) {
		return showChannels.get(channel);
	}	
}
