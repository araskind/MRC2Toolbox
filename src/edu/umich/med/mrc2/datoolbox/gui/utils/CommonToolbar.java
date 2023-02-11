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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public abstract class CommonToolbar extends JToolBar {

	/**
	 *
	 */
	private static final long serialVersionUID = -2082413025910340358L;
	public static final Dimension buttonDimension = new Dimension(35, 35);
	protected ActionListener commandListener;

	public CommonToolbar(ActionListener commandListener) {

		super(JToolBar.HORIZONTAL);
		this.commandListener = commandListener;

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setOpaque(false);
		setFloatable(false);
		setBackground(Color.LIGHT_GRAY);
		setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	public abstract void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline);

}
