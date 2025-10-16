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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MultiplexExportSetupDialog extends JDialog 
			implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1591752976832073167L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("saveList", 32);
	
	private Preferences preferences;
	public static final String BASE_DIR = "BASE_DIR";
	public static final String FIELDS_TOEXPORT = "FIELDS_TOEXPORT";
	public static final String PROPERTIES_DELIMITER = "@";
	private File baseDirectory;
	private final String BROWSE_COMMAND = "BROWSE_FOR_OUTPUT";
	private Collection<CompoundMultiplexMixture> multiplexes;
	
	private CCComponentMetadataFieldSelectionTable fieldSelectionTable;
	private JTextField destinationFileTextField;

	public MultiplexExportSetupDialog(
			Collection<CompoundMultiplexMixture> multiplexes,
			ActionListener actionListener) {
		super();
		this.multiplexes = multiplexes;

		setTitle("Export data for selected compound multiplexes");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 800));
		setPreferredSize(new Dimension(800, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));		
		fieldSelectionTable = new CCComponentMetadataFieldSelectionTable();
		
		
		dataPanel.add(new JScrollPane(fieldSelectionTable), BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		dataPanel.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Export file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		destinationFileTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		panel_1.add(destinationFileTextField, gbc_textField);
		destinationFileTextField.setColumns(10);
		
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(BROWSE_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		panel_1.add(browseButton, gbc_btnNewButton);


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

		JButton btnSave = new JButton("Export");
		btnSave.setActionCommand(
				MainActionCommands.EXPORT_SELECTED_MULTIPLEXES_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		
		String filePath = Paths.get(baseDirectory.getAbsolutePath(), 
				"CompoundMultiplex_export_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt").
				toString();
		destinationFileTextField.setText(filePath);
		
		pack();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectOutputFile();
	}
	
	private void selectOutputFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Export Multiplecx data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");
		String defaultFileName = "CompoundMultiplex_export_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile  = fc.getSelectedFile();
			destinationFileTextField.setText(exportFile.getAbsolutePath());
			baseDirectory = exportFile.getParentFile();
		}
	}
	
	public File getOutputFile() {
		
		if(destinationFileTextField.getText().trim().isEmpty())
			return null;
		
		return Paths.get(destinationFileTextField.getText()).toFile();
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).
				getAbsoluteFile();
		
		Collection<CpdMetadataField>fields = new HashSet<CpdMetadataField>();
		try {
			fields = CompoundMultiplexUtils.getCpdMetadataFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		String fieldsToExportString = preferences.get(FIELDS_TOEXPORT, "");
		Map<CpdMetadataField,Boolean>fieldMap = 
				new HashMap<CpdMetadataField,Boolean>();
		fields.stream().forEach(f -> fieldMap.put(f, Boolean.FALSE));

		if(!fieldsToExportString.isEmpty()) {
			
			 String[] fieldsToExportIds =  
					 fieldsToExportString.split(PROPERTIES_DELIMITER);

			 for(String fid : fieldsToExportIds) {
				 
				 CpdMetadataField activeField = fields.stream().
					 filter(f -> f.getId().equals(fid)).
					 findFirst().orElse(null);

				if(activeField != null)
					fieldMap.put(activeField, Boolean.TRUE);
			 }
		}
		fieldSelectionTable.setTableModelFromPropertyMap(fieldMap);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
		Collection<CpdMetadataField>activeFields = 
				fieldSelectionTable.getSelectedProperties();
		List<String> idList = 
				activeFields.stream().map(f -> f.getId()).
				collect(Collectors.toList());
		String idString = "";
		if(!idList.isEmpty())
			idString = StringUtils.join(idList, PROPERTIES_DELIMITER);
		
		preferences.put(FIELDS_TOEXPORT, idString);
	}

	public Collection<CompoundMultiplexMixture> getMultiplexes() {
		return multiplexes;
	}
	
	public Collection<CpdMetadataField> getSelectedProperties() {
		return fieldSelectionTable.getSelectedProperties();
	}
}

































