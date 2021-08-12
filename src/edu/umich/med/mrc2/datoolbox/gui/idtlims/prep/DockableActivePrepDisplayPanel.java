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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableActivePrepDisplayPanel extends DefaultSingleCDockable implements PersistentLayout {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "DockableActivePrepDisplayPanel.layout");
	private static final Icon editPrepIcon = GuiUtils.getIcon("editSamplePrep", 16);

	private JLabel nameValueLabel;
	private JLabel idValueLabel;
	private JLabel prepUserLabel;
	private DockableSopTable sopTable;
	private DockableDocumentsTable documentsPanel;
	private DockablePrepSampleTable prepSampleTable;
	private CControl control;
	private CGrid grid;
	private JLabel prepDateValueLabel;

	public DockableActivePrepDisplayPanel() {

		super("DockableActivePrepDisplayPanel", editPrepIcon, "Active sample preparation", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		idValueLabel.setForeground(Color.BLACK);
		idValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_idValueLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Name");
		lblName.setForeground(Color.BLACK);
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		nameValueLabel = new JLabel();
		GridBagConstraints gbc_nameValueLabel = new GridBagConstraints();
		gbc_nameValueLabel.anchor = GridBagConstraints.WEST;
		gbc_nameValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_nameValueLabel.gridx = 1;
		gbc_nameValueLabel.gridy = 1;
		dataPanel.add(nameValueLabel, gbc_nameValueLabel);

		JLabel lblType = new JLabel("Prepared by");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 2;
		dataPanel.add(lblType, gbc_lblType);

		prepUserLabel = new JLabel("");
		GridBagConstraints gbc_prepUserLabel = new GridBagConstraints();
		gbc_prepUserLabel.anchor = GridBagConstraints.WEST;
		gbc_prepUserLabel.insets = new Insets(0, 0, 5, 0);
		gbc_prepUserLabel.gridx = 1;
		gbc_prepUserLabel.gridy = 2;
		dataPanel.add(prepUserLabel, gbc_prepUserLabel);

		JLabel lblPreparedOn = new JLabel("Prepared on");
		GridBagConstraints gbc_lblPreparedOn = new GridBagConstraints();
		gbc_lblPreparedOn.anchor = GridBagConstraints.EAST;
		gbc_lblPreparedOn.insets = new Insets(0, 0, 5, 5);
		gbc_lblPreparedOn.gridx = 0;
		gbc_lblPreparedOn.gridy = 3;
		dataPanel.add(lblPreparedOn, gbc_lblPreparedOn);
		
		prepDateValueLabel = new JLabel("");
		prepDateValueLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_prepDateValueLabel = new GridBagConstraints();
		gbc_prepDateValueLabel.anchor = GridBagConstraints.WEST;
		gbc_prepDateValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_prepDateValueLabel.gridx = 1;
		gbc_prepDateValueLabel.gridy = 3;
		dataPanel.add(prepDateValueLabel, gbc_prepDateValueLabel);

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 4;
		dataPanel.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		sopTable = new DockableSopTable();
		documentsPanel = new DockableDocumentsTable();
		prepSampleTable =  new DockablePrepSampleTable();

		grid.add(0, 0, 100, 100, sopTable, documentsPanel, prepSampleTable);
		control.getContentArea().deploy(grid);
		panel_1.add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);
	}

	public void loadPrepData(LIMSSamplePreparation prep) {

		clearPanel();
		if(prep == null)
			return;

		idValueLabel.setText(prep.getId());
		nameValueLabel.setText(prep.getName());
		prepUserLabel.setText(prep.getCreator().getInfo());
	
		if (prep.getPrepDate() != null)	
			prepDateValueLabel.setText(dateFormat.format(prep.getPrepDate()));
		
		//	Load samples for prep
		try {
			prepSampleTable.setTableModelFromSamples(IDTUtils.getSamplesForPrep(prep));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//	Load SOPs
		try {
			sopTable.setTableModelFromProtocols(IDTUtils.getSamplePrepSops(prep));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//	Load annotations
		try {
			documentsPanel.setModelFromAnnotations(
					AnnotationUtils.getObjetAnnotations(AnnotatedObjectType.SAMPLE_PREP, prep.getId()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearPanel() {

		idValueLabel.setText("");
		nameValueLabel.setText("");
		prepUserLabel.setText("");
		prepDateValueLabel.setText("");
		prepSampleTable.clearTable();
		sopTable.clearPanel();
		documentsPanel.clearPanel();
	}

	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}




























