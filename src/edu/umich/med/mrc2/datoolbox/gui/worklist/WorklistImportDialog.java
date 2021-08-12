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

package edu.umich.med.mrc2.datoolbox.gui.worklist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class WorklistImportDialog extends JDialog 
		implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8716421349210019279L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("editCollection", 32);
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private WorklistPanel worklistPanel;
	private boolean appendWorklist;

	public WorklistImportDialog(WorklistPanel worklistPanel, boolean appendWorklist) {
		super();
		
		setTitle("Import worklist from file");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(640, 480));
		setResizable(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		this.appendWorklist = appendWorklist;
		this.worklistPanel = worklistPanel;

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		initChooser();
		panel.add(chooser, BorderLayout.CENTER);
		
		//	TODO load/save last opened folder
		loadPreferences();
		pack();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);
		
		FileNameExtensionFilter wklFilter = 
				new FileNameExtensionFilter("Agilent worklist files", "wkl");
		chooser.addChoosableFileFilter(wklFilter);

		FileNameExtensionFilter txtFilter = 
				new FileNameExtensionFilter("Text files", "txt", "tsv");
		chooser.addChoosableFileFilter(txtFilter);
	}
	

	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(chooser)) {

			if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
				
				File inputFile = chooser.getSelectedFile();
				baseDirectory = inputFile.getParentFile();
				worklistPanel.loadWorklistFromFile(inputFile, appendWorklist);
			}
			if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
				dispose();
		}		
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}

}
