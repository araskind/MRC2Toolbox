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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

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

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.preferences.SmoothingFilterManager;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.xic.SmothingFilterSelectorPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.FilterClass;

public class SmoothingPreferencesDialog extends JDialog implements BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3488150794809338224L;

	protected static final Icon smoothingPreferencesIcon = GuiUtils.getIcon("smoothingPreferences", 32);
	private Preferences preferences;
	private static final String CLASS_NAME = "clusterfinder.gui.rawdata.SmoothingPreferencesDialog";
	private static final String FILTER_CLASS = "FILTER_CLASS";
	
	private String filterId;	
	private SmothingFilterSelectorPanel smothingFilterSelectorPanel;
	
	public SmoothingPreferencesDialog(ActionListener actionListener, String filterId) {
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Set chromatogram smoothing parameters.");
		setIconImage(((ImageIcon) smoothingPreferencesIcon).getImage());
		setPreferredSize(new Dimension(400, 250));
		setSize(new Dimension(400, 250));
		
		this.filterId = filterId;
		smothingFilterSelectorPanel = new SmothingFilterSelectorPanel();
		getContentPane().add(smothingFilterSelectorPanel, BorderLayout.CENTER);
		
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

		JButton btnSave = new JButton(MainActionCommands.SAVE_SMOOTHING_PREFERENCES_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SAVE_SMOOTHING_PREFERENCES_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		loadPreferences();
	}
	
	public Filter getSmoothingFilter() {		
		return smothingFilterSelectorPanel.getSmoothingFilter();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(CLASS_NAME));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		this.preferences = prefs;
		Filter activeFilter = SmoothingFilterManager.getFilter(filterId);
		if(activeFilter == null) {
			FilterClass fc = FilterClass.getFilterClassByName(
					preferences.get(FILTER_CLASS, FilterClass.SAVITZKY_GOLAY_MZMINE.name()));
			smothingFilterSelectorPanel.setFilterClass(fc);
			SmoothingFilterManager.addFilter(
					filterId, smothingFilterSelectorPanel.getSmoothingFilter());
		}
		else {
			smothingFilterSelectorPanel.setFilter(activeFilter);
		}
	}

	@Override
	public void savePreferences() {
		
		if (preferences == null)
			preferences = Preferences.userRoot().node(CLASS_NAME);
		
//		if(smothingFilterSelectorPanel.getFilterClass() != null)
//			preferences.put(FILTER_CLASS, smothingFilterSelectorPanel.getFilterClass().name());
		
		if(smothingFilterSelectorPanel.getSmoothingFilter() != null)
			SmoothingFilterManager.addFilter(
				filterId, smothingFilterSelectorPanel.getSmoothingFilter());
	}
}


