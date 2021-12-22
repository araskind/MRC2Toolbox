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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad.DockableChromatographicGradientTable;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad.DockableGradientChartPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad.DockableMobilePhaseAndParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AcquisitionMethodExtendedEditorDialog extends JDialog implements PersistentLayout {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5430696201670115274L;
	private static final Icon editMethodIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 32);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 32);

	private DataAcquisitionMethod method;
	private JButton btnSave;
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "AcquisitionMethodExtendedEditorDialog.layout");
	
	private CControl control;
	private CGrid grid;
	private DockableGradientChartPanel gradientChartPanel;
	private DockableChromatographicGradientTable gradientTable;
	private DockableAcquisitionMethodDataPanel dataPanel;
	private DockableMobilePhaseAndParametersPanel mobilePhaseAndParametersPanel;

	public AcquisitionMethodExtendedEditorDialog(
			DataAcquisitionMethod method, ActionListener actionListener) {
		super();
		this.method = method;

		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		dataPanel = new DockableAcquisitionMethodDataPanel(this);
		gradientChartPanel = new DockableGradientChartPanel();
		gradientTable = new DockableChromatographicGradientTable();
		mobilePhaseAndParametersPanel = new DockableMobilePhaseAndParametersPanel();
		grid.add(0, 0, 75, 100, dataPanel, gradientChartPanel, gradientTable, mobilePhaseAndParametersPanel);

		control.getContentArea().deploy(grid);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadLayout(layoutConfigFile);
		loadMethodData();
		pack();
	}
	
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	private void loadMethodData() {

		if(method == null) {

			setTitle("Add new data acquisition method");
			setIconImage(((ImageIcon) addMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_ACQUISITION_METHOD_COMMAND.getName());
		}
		else {
			setTitle("Edit information for " + method.getName());
			setIconImage(((ImageIcon) editMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_ACQUISITION_METHOD_COMMAND.getName());
		}
		dataPanel.loadMethodData(method);
		
		//	TODO load gradient
		
		pack();
	}
	
	public void setMethodFile(File methodFile) {
		dataPanel.setMethodFile(methodFile);
	}

	public DataAcquisitionMethod getMethod() {
		return method;
	}
	
	public synchronized void clearPanel() {
		
		gradientChartPanel.clearPanel();
		gradientTable.clearTable();
		mobilePhaseAndParametersPanel.clearPanel();
	}

	public void loadGradientData(ChromatographicGradient gradient) {
		
		gradientChartPanel.showGradient(gradient);
		gradientTable.setTableModelFromGradient(gradient);
		mobilePhaseAndParametersPanel.loadGradientData(gradient);
	}
	
	public Collection<String>validateMethodData(){
		
		Collection<String>errors = new ArrayList<String>();
		errors.addAll(dataPanel.validateMethodData());
		
		//	TODO other panels data validation

		return errors;
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

	/**
	 * @return the dataPanel
	 */
	public DockableAcquisitionMethodDataPanel getDataPanel() {
		return dataPanel;
	}

}
