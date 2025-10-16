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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.DockableXICSetupPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;

public class XICSetupDialog extends JDialog 
	implements ActionListener, PersistentLayout, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -559817737175901743L;
	
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "XICSetupDialog.layout");
	private static final Icon xicIcon = GuiUtils.getIcon("xic", 24);
	private DockableXICSetupPanel xicSetupPanel;
	private DockableXICMassSelectionPanel massSelectionPanel;
	
	private Preferences preferences;
	private static final String WIDTH = "WIDTH";
	private static final String HEIGTH = "HEIGTH";
	private static final int WIDTH_DEFAULT = 800; 
	private static final int HEIGTH_DEFAULT = 640;
	
	protected CControl control;
	protected CGrid grid;
	
	public XICSetupDialog(ActionListener listener, MSFeatureInfoBundle bundle) {
		
		super();
		setTitle("Set up XIC for the feature");
		setIconImage(((ImageIcon) xicIcon).getImage());
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		xicSetupPanel = new DockableXICSetupPanel(listener);
		xicSetupPanel.loadData(RawDataManager.getRawDataMap().keySet(), false);
		DataFile featueFile = RawDataManager.getDataFileForInjectionId(bundle.getInjectionId());
		if(featueFile != null)
			xicSetupPanel.selectFiles(Collections.singleton(featueFile));
		
		massSelectionPanel = new DockableXICMassSelectionPanel(this);

		grid.add(0, 0, 80, 30, xicSetupPanel, massSelectionPanel);
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
							
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(getContentPane());
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);		
		loadPreferences();
		populateMassSelector(bundle);
		loadLayout(layoutConfigFile);	
		pack();
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(XICSetupDialog.class.getName()));
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {

		this.preferences = preferences;
		xicSetupPanel.getPanel().loadPreferences(preferences);

		int width = preferences.getInt(WIDTH, WIDTH_DEFAULT);
		int height = preferences.getInt(HEIGTH, HEIGTH_DEFAULT);
		setPreferredSize(new Dimension(width, height));
	}

	@Override
	public void savePreferences() {

		if(preferences == null)
			preferences = Preferences.userRoot().node(XICSetupDialog.class.getName());

		xicSetupPanel.getPanel().savePreferences();
		preferences.putInt(WIDTH, getWidth());
		preferences.putInt(HEIGTH, getHeight());	
	}
	
	private void populateMassSelector(MSFeatureInfoBundle bundle) {
		
		MassSpectrum spectrum = bundle.getMsFeature().getSpectrum();
		TandemMassSpectrum msms = spectrum.getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(msms != null) {
			
			Collection<MsPoint> msmsPoints = msms.getSpectrum();
			MsPoint parent = msms.getParent();
			if(parent != null) {
				MsPoint existingParent = msmsPoints.stream().
						filter(p -> p.getMz() == parent.getMz()).
						findFirst().orElse(null);
				if(existingParent == null)
					msmsPoints.add(parent);
			}
			MsPoint selected = msmsPoints.stream().
					sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).
					findFirst().orElse(null);
			massSelectionPanel.setTableModelFromDataPoints(msmsPoints);
			if(selected != null) {
				massSelectionPanel.selectMass(selected.getMz());
				xicSetupPanel.getPanel().setMassList(MRC2ToolBoxConfiguration.getMzFormat().format(selected.getMz()));
			}
		}
	}
	
	@Override
	public void dispose() {
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
	}

	public Collection<String> veryfyParameters() {
		return xicSetupPanel.veryfyParameters();
	}
	
	public ChromatogramExtractionTask createChromatogramExtractionTask() {
		return xicSetupPanel.createChromatogramExtractionTask();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_MASSES_TO_EXTRACT_XIC_COMMAND.getName())) {
			
			Collection<Double> selectedMasses = massSelectionPanel.getSelectedMasses();
			if(selectedMasses.isEmpty())
				return;
			
			List<String> massStringList = selectedMasses.stream().
					map(d -> MRC2ToolBoxConfiguration.getMzFormat().format(d)).
					collect(Collectors.toList());
			
			xicSetupPanel.getPanel().setMassList(StringUtils.join(massStringList, ","));
		}
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















