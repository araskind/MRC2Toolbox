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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableActivePrepPanel extends DefaultSingleCDockable {
	
	private static final Icon editPrepIcon = GuiUtils.getIcon("editSamplePrep", 16);
	private SamplePrepEditorPanel samplePrepEditorPanel;

	public DockableActivePrepPanel() {

		super("DockableActivePrepPanel", editPrepIcon, 
				"Active sample preparation", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		samplePrepEditorPanel = new SamplePrepEditorPanel(true);
		add(samplePrepEditorPanel, BorderLayout.CENTER);
	}

	public void loadPrepData(LIMSSamplePreparation prep) {
		samplePrepEditorPanel.loadPrepData(prep);
	}

	public void clearPanel() {
		samplePrepEditorPanel.clearPanel();
	}
}




























