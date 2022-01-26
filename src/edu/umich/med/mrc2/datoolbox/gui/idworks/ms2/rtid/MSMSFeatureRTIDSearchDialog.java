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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.rtid;

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
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MSMSFeatureRTIDSearchDialog extends JDialog implements ActionListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5562109151082627209L;
	
	private static final Icon findMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 32);
	private Preferences preferences;

	

	public MSMSFeatureRTIDSearchDialog(ActionListener listener) {
		super();
		setTitle("Find MSMS features by RT/identity");
		setIconImage(((ImageIcon)findMSMSFeaturesIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(750, 600));
		setPreferredSize(new Dimension(750, 600));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSearch = new JButton(MainActionCommands.SEARCH_FEATURES_BY_RT_ID_COMMAND.getName());
		btnSearch.setActionCommand(MainActionCommands.SEARCH_FEATURES_BY_RT_ID_COMMAND.getName());
		btnSearch.addActionListener(listener);
		buttonPanel.add(btnSearch);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSearch);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSearch);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		//	TODO

	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(MSMSFeatureRTIDSearchDialog.class.getName());
		loadPreferences(preferences);


	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		preferences = Preferences.userRoot().node(MSMSFeatureRTIDSearchDialog.class.getName());
	}
}
