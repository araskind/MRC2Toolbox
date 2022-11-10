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

package edu.umich.med.mrc2.datoolbox.gui.annotation.editors;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DocumentAnnotationDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -1841223875857617686L;

	private static final Icon attachDocumentIcon = GuiUtils.getIcon("attachDocument", 32);
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.DocumentAnnotationDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private JButton saveButton, cancelButton;
	private ObjectAnnotation currentAnnotation;
	private AnnotatedObject currentObject;
	private JPanel panel_1;
	private JLabel lblTitle;
	private JTextField documentTitleTextField;
	private JButton btnBrowse;
	private JLabel lblFile;
	private JTextField sourceFileTextField;
//	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private JLabel fileTypeLabel;

	public DocumentAnnotationDialog(ActionListener listener) {
		super();
		setTitle("Document-based annotation");
		setIconImage(((ImageIcon) attachDocumentIcon).getImage());
		setSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(600, 150));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);

		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		lblFile = new JLabel("File ");
		GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.anchor = GridBagConstraints.EAST;
		gbc_lblFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblFile.gridx = 0;
		gbc_lblFile.gridy = 0;
		panel_1.add(lblFile, gbc_lblFile);

		sourceFileTextField = new JTextField();
		sourceFileTextField.setEditable(false);
		GridBagConstraints gbc_sourceFileTextField = new GridBagConstraints();
		gbc_sourceFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_sourceFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sourceFileTextField.gridx = 1;
		gbc_sourceFileTextField.gridy = 0;
		panel_1.add(sourceFileTextField, gbc_sourceFileTextField);
		sourceFileTextField.setColumns(10);

		btnBrowse = new JButton("Browse ...");
		btnBrowse.setActionCommand(BROWSE_COMMAND);
		btnBrowse.addActionListener(this);
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 5);
		gbc_btnBrowse.gridx = 2;
		gbc_btnBrowse.gridy = 0;
		panel_1.add(btnBrowse, gbc_btnBrowse);
		
		fileTypeLabel = new JLabel("");
		GridBagConstraints gbc_fileTypeLabel = new GridBagConstraints();
		gbc_fileTypeLabel.gridheight = 2;
		gbc_fileTypeLabel.insets = new Insets(0, 0, 5, 0);
		gbc_fileTypeLabel.gridx = 3;
		gbc_fileTypeLabel.gridy = 0;
		panel_1.add(fileTypeLabel, gbc_fileTypeLabel);

		lblTitle = new JLabel("Title ");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 0, 5);
		gbc_lblTitle.anchor = GridBagConstraints.EAST;
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 1;
		panel_1.add(lblTitle, gbc_lblTitle);

		documentTitleTextField = new JTextField();
		GridBagConstraints gbc_documentTitleTextField = new GridBagConstraints();
		gbc_documentTitleTextField.insets = new Insets(0, 0, 0, 5);
		gbc_documentTitleTextField.gridwidth = 2;
		gbc_documentTitleTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_documentTitleTextField.gridx = 1;
		gbc_documentTitleTextField.gridy = 1;
		panel_1.add(documentTitleTextField, gbc_documentTitleTextField);
		documentTitleTextField.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Attach document annotation");
		saveButton.addActionListener(listener);
		saveButton.setActionCommand(
				MainActionCommands.SAVE_OBJECT_DOCUMENT_ANNOTATION_COMMAND.getName());
		panel.add(saveButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		loadPreferences();
//		initChooser();
		pack();
	}

//	private void initChooser() {
//
//		chooser = new ImprovedFileChooser();
//		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
//		chooser.addActionListener(this);
//		chooser.setAcceptAllFileFilterUsed(true);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setCurrentDirectory(baseDirectory);
//		chooser.setApproveButtonText("Attach document");
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Word files", "doc", "docx"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("PowerPoint files", "ppt", "pptx"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xls", "xlsx"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "tiff", "gif", "bmp"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("SDF structure files", "sdf"));
//		chooser.addChoosableFileFilter(new FileNameExtensionFilter("MOL files", "mol"));
//	}

	public void loadAnnotation(ObjectAnnotation annotation) {

		currentAnnotation = annotation;
		if(currentAnnotation == null)
			return;

		documentTitleTextField.setText(currentAnnotation.getLinkedDocumentName());
		if(currentAnnotation.getLinkedDocumentId() == null)
			saveButton.setText("Attach document annotation");
		else {
			setTitle("Edit document title for \"" + currentAnnotation.getLinkedDocumentName() + "\"");
			Icon ftIcon = GuiUtils.getDocumentFormatIcon(currentAnnotation.getLinkedDocumentFormat(), 64);
			fileTypeLabel.setIcon(ftIcon);
			saveButton.setText("Update document title");
			btnBrowse.setEnabled(false);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectLinkedFile();
			
//			chooser.showOpenDialog(this);
//
//		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
//
//			File inputFile = chooser.getSelectedFile();
//			baseDirectory = inputFile.getParentFile();
//			sourceFileTextField.setText(chooser.getSelectedFile().getAbsolutePath());
//			documentTitleTextField.setText(FilenameUtils.getBaseName(inputFile.getName()));
//			
//			Icon ftIcon = GuiUtils.getDocumentFormatIcon(
//					DocumentFormat.getFormatByFileExtension(
//							FilenameUtils.getExtension(inputFile.getName())), 64);
//			fileTypeLabel.setIcon(ftIcon);
//			savePreferences();
//		}
	}
	
	private void selectLinkedFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Word files", "doc", "docx");
		fc.addFilter("PowerPoint files", "ppt", "pptx");
		fc.addFilter("Excel files", "xls", "xlsx");
		fc.addFilter("PDF files", "pdf");
		fc.addFilter("Image files", "png", "jpg", "jpeg", "tiff", "gif", "bmp");
		fc.addFilter("SDF structure files", "sdf");
		fc.addFilter("MOL files", "mol");
		fc.setTitle("Attach document");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			sourceFileTextField.setText(fc.getSelectedFile().getAbsolutePath());
			documentTitleTextField.setText(FilenameUtils.getBaseName(inputFile.getName()));
			
			Icon ftIcon = GuiUtils.getDocumentFormatIcon(
					DocumentFormat.getFormatByFileExtension(
							FilenameUtils.getExtension(inputFile.getName())), 64);
			fileTypeLabel.setIcon(ftIcon);
			savePreferences();
		}
	}

	public String getDocumentTitle() {
		return documentTitleTextField.getText().trim();
	}

	public File getDocumentSourceFile() {

		String docPath = sourceFileTextField.getText().trim();
		if(docPath.isEmpty())
			return null;

		Path path =  null;
		try {
			path =  Paths.get(docPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(path == null)
			return null;

		if(!path.toFile().exists())
			return null;

		return path.toFile();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
					MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	/**
	 * @return the currentAnnotation
	 */
	public ObjectAnnotation getAnnotation() {
		return currentAnnotation;
	}
}


















