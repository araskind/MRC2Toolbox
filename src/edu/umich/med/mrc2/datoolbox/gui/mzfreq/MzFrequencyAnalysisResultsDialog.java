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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class MzFrequencyAnalysisResultsDialog extends JDialog implements BackedByPreferences, ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2684929912807436860L;

	private static final Icon mzFrequencyIcon = GuiUtils.getIcon("mzFrequency", 32);
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private MzFrequencyDataTable table;
	private MzFequencyResultsToolbar toolBar;
	
	public MzFrequencyAnalysisResultsDialog(
			ActionListener actionListener,
			Collection<MzFrequencyObject>mzFrequencyObjects,
			String binningParameter) {
		super();
		
		setTitle("M/Z frequency analysis results, binning at " + binningParameter);
		setIconImage(((ImageIcon) mzFrequencyIcon).getImage());
		setSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		toolBar = new MzFequencyResultsToolbar(this);
		panel_1.add(toolBar, BorderLayout.NORTH);
		
		table = new MzFrequencyDataTable();
		panel_1.add(new JScrollPane(table), BorderLayout.CENTER);
		table.setTableModelFromMzFrequencyObjectCollection(mzFrequencyObjects);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		if(MessageDialog.showChoiceWithWarningMsg(
				"Unsaved results will be lost.\n"
				+ "Do you want to close the dialog?", this) == JOptionPane.YES_OPTION) {
			savePreferences();
			super.dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		//	TODO
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		//	TODO
	}

}
