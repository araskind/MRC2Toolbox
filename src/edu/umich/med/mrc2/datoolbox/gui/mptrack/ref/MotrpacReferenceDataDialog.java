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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay.DockableMotrpacAssayManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.sampletype.DockableMoTrPACSampleTypeManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.tcode.DockableMoTrPACTissueCodeManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MotrpacReferenceDataDialog extends JDialog {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4857760627417308661L;

	private static final Icon dialogIcon = GuiUtils.getIcon("metadata", 32);
	private DockableMotrpacAssayManagerPanel assayManagerPanel;
	private DockableMoTrPACSampleTypeManagerPanel sampleTypeManagerPanel;
	private DockableMoTrPACTissueCodeManagerPanel tissueCodeManagerPanel;
	
	public MotrpacReferenceDataDialog() {
		super(MRC2ToolBoxCore.getMainWindow());
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setTitle("MoTrPAC metadata reference");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.MODELESS);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		setLayout(new BorderLayout(0, 0));
		CControl control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		CGrid grid = new CGrid(control);

		assayManagerPanel = new DockableMotrpacAssayManagerPanel();
		sampleTypeManagerPanel = new DockableMoTrPACSampleTypeManagerPanel();
		tissueCodeManagerPanel = new DockableMoTrPACTissueCodeManagerPanel();
		
		grid.add(0, 0, 100, 100, assayManagerPanel,
				sampleTypeManagerPanel, tissueCodeManagerPanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
				
		refreshPanels();
		pack();
	}
	
	private void refreshPanels() {

		assayManagerPanel.loadAssays();
		sampleTypeManagerPanel.loadSampleTypes();
		tissueCodeManagerPanel.loadTissueCodes();		
	}
}
