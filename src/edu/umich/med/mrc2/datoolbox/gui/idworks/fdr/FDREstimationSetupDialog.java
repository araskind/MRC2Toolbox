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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fdr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchParameterSetTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class FDREstimationSetupDialog extends JDialog 
		implements ActionListener, BackedByPreferences, ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8295171816888284985L;
	private static final Icon fdrIcon = GuiUtils.getIcon("fdr", 32);
	
	private PepSearchSetupDialog pepSearchSetupDialog;
	private PepSearchParameterSetTable pepSearchParameterSetTable;
	private Preferences preferences;
	private NISTPepSearchParameterObject activeNISTPepSearchParameterObject;
		
	public FDREstimationSetupDialog(ActionListener actionListener) {
		super();
		setTitle("Estimate FDR for MSMS identification");
		setIconImage(((ImageIcon) fdrIcon).getImage());
		setPreferredSize(new Dimension(1200, 800));
		setSize(new Dimension(1200, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel dataPanel = new JPanel(new BorderLayout(0,0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		dataPanel.add(tabbedPane, BorderLayout.CENTER);
		
		JPanel pepSearchParameterListingPanel = new JPanel(new BorderLayout(0,0));
		pepSearchParameterSetTable = new PepSearchParameterSetTable();
		pepSearchParameterSetTable.getSelectionModel().addListSelectionListener(this);
		pepSearchParameterListingPanel.add(
				new JScrollPane(pepSearchParameterSetTable), BorderLayout.CENTER);
		tabbedPane.addTab("Select PepSearch Parameter Set", 
				null, pepSearchParameterListingPanel, null);
		
		pepSearchSetupDialog = new PepSearchSetupDialog();
		JPanel searchOptionsPanel = pepSearchSetupDialog.getSearchOptionsPanel();
		tabbedPane.addTab("Decoy library search options", null, searchOptionsPanel, null);
		
		JPanel inputAndLibraryPanel = pepSearchSetupDialog.getInputAndLibraryPanel();
		tabbedPane.addTab("Input data and decoy libraries", null, inputAndLibraryPanel, null);

		JPanel outputOptionsPanel = pepSearchSetupDialog.getOutputOptionsPanel();
		tabbedPane.addTab("Output options", null, outputOptionsPanel, null);
	
		JPanel commandPreviewPanel = pepSearchSetupDialog.getCommandPreviewPanel();
		tabbedPane.addTab("Command preview", null, commandPreviewPanel, null);
				
		pepSearchSetupDialog.loadDefaultDecoySearchparameters();
		
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

		JButton btnSave = new JButton(MainActionCommands.CALCULATE_FDR_FOR_LIBRARY_MATCHES.getName());
		btnSave.setActionCommand(MainActionCommands.CALCULATE_FDR_FOR_LIBRARY_MATCHES.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}
	
	public void loadNISTPepSearchParameterObjects(Map<NISTPepSearchParameterObject, Long>paramCounts) {
		pepSearchParameterSetTable.setModelFromHitCountMap(paramCounts);
	}
	
	public void loadDefaultDecoySearchparameters() {
		pepSearchSetupDialog.loadDefaultDecoySearchparameters();
	}
	
	public void loadNISTPepSearchParameterObject(NISTPepSearchParameterObject parameterSet)  {
		
		activeNISTPepSearchParameterObject = parameterSet;
		pepSearchSetupDialog.loadDefaultDecoySearchparameters();
		if(parameterSet != null)
			pepSearchSetupDialog.adjustDefaultDecoySearchParametersFromParameterSet(parameterSet);
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
		// TODO Auto-generated method stub
		preferences = prefs;

	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		preferences = Preferences.userNodeForPackage(this.getClass());
		
		pepSearchSetupDialog.saveLibraryPreferences();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) 
			loadNISTPepSearchParameterObject(
					pepSearchParameterSetTable.getSelectedNISTPepSearchParameterObject());		
	}

	public Collection<File> getSelectedDecoyLibraries(){
		
		Collection<File>selectedDecoys = new ArrayList<File>();
		Collection<File> libFiles = pepSearchSetupDialog.getEnabledLibraryFiles();	
		if(libFiles.isEmpty())
			return selectedDecoys;
		
		List<String> availableDecoyNames = 
				IDTDataCache.getReferenceMsMsLibraryList().stream().
				filter(l -> l.isDecoy()).map(d -> d.getSearchOutputCode()).
				sorted().collect(Collectors.toList());

		return libFiles.stream().
					filter(f -> availableDecoyNames.contains(FilenameUtils.getBaseName(f.getName()))).
					sorted().collect(Collectors.toList());
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		
		File percolatorBinary = new File(MRC2ToolBoxConfiguration.getPercolatorBinaryPath());
		if(!percolatorBinary.exists() || !percolatorBinary.canExecute())
			errors.add("Please specify the correct location of the Percolator binary in program preferences.");
			
		if(pepSearchParameterSetTable.getSelectedNISTPepSearchParameterObject() == null)
			errors.add("Please select PepSearch parameter set.");
			
		List<ReferenceMsMsLibrary> availableDecoys = 
				IDTDataCache.getReferenceMsMsLibraryList().stream().
				filter(l -> l.isDecoy()).sorted().collect(Collectors.toList());
		
		if(getSelectedDecoyLibraries().isEmpty()) {
			errors.add("No decoy libraries selected.");
			errors.add("The following decoy libraries are registered in the Tracker database:");
			List<String>decoyNames = availableDecoys.stream().
					map(d -> d.getSearchOutputCode()).sorted().
					collect(Collectors.toList());
			errors.addAll(decoyNames);
			errors.add(" ");
		}	
		return errors;
	}

	public NISTPepSearchParameterObject getActiveNISTPepSearchParameterObject() {
		return activeNISTPepSearchParameterObject;
	}
}












