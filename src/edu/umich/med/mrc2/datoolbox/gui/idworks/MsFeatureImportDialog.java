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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MsFeatureImportDialog extends JDialog  implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -1605053468969470610L;

	private JFileChooser chooser;
	private File baseDirectory, libraryFile;
	private JPanel panel;
	private CompoundLibrary currentLibrary;
	private JComboBox<Assay> assayComboBox;
	private FileNameExtensionFilter txtFilter;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mgfFilter;

	private static final Icon importMultifileIcon = GuiUtils.getIcon("importMultifile", 32);

	public MsFeatureImportDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Import MS features for identification");
		setIconImage(((ImageIcon) importMultifileIcon).getImage());
		setPreferredSize(new Dimension(640, 480));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel main = new JPanel(new BorderLayout(0, 0));
		main.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(main, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		main.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{81, 290, 65, 89, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JLabel lblAssayMethod = new JLabel("Assay method");
		GridBagConstraints gbc_lblAssayMethod = new GridBagConstraints();
		gbc_lblAssayMethod.anchor = GridBagConstraints.EAST;
		gbc_lblAssayMethod.insets = new Insets(0, 0, 0, 5);
		gbc_lblAssayMethod.gridx = 0;
		gbc_lblAssayMethod.gridy = 0;
		panel_1.add(lblAssayMethod, gbc_lblAssayMethod);

		assayComboBox = new JComboBox<Assay>();
		try {
			populateAssayList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assayComboBox.setPreferredSize(new Dimension(28, 25));
		assayComboBox.setMinimumSize(new Dimension(28, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 3;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(assayComboBox, gbc_comboBox);

		JPanel panel_2 = new JPanel();
		main.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel libChooserPanel = new JPanel();
		libChooserPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_libChooserPanel = new GridBagLayout();
		gbl_libChooserPanel.columnWidths = new int[]{0, 0, 0};
		gbl_libChooserPanel.rowHeights = new int[]{0};
		gbl_libChooserPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_libChooserPanel.rowWeights = new double[]{Double.MIN_VALUE};
		libChooserPanel.setLayout(gbl_libChooserPanel);

		panel_2.add(libChooserPanel, BorderLayout.NORTH);

		initChooser();
		panel_2.add(chooser, BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);

		txtFilter = new FileNameExtensionFilter("Text files", "txt", "tsv");
		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "cef", "CEF");
		mgfFilter = new FileNameExtensionFilter("MGF files", "mgf");

		chooser.setFileFilter(xmlFilter);
	}

	private void populateAssayList() throws Exception {

//		Collection<Assay>activeMethods = AssayDatabaseUtils.getAssays(true);
//		SortedComboBoxModel model = new SortedComboBoxModel(activeMethods.toArray(new Assay[activeMethods.size()]));
//		assayComboBox.setModel(model);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(chooser)) {

			if(event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

				libraryFile = chooser.getSelectedFile();
				baseDirectory = libraryFile.getParentFile();
				importData();
			}
			if (event.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
				this.setVisible(false);
		}
	}

	public void setVisible(boolean visible) {

		if(visible)
			clearPanel();

		super.setVisible(visible);
	}

	public synchronized void clearPanel() {
		libraryFile = null;
	}

	private void importData() {

		ArrayList<String>errors = new ArrayList<String>();

		if(libraryFile == null) {
			errors.add("Library file not specified.");
		}
		else {
			if(!libraryFile.exists() || !libraryFile.canRead())
				errors.add("Can not read library file..");
		}
		Assay activeAssay = (Assay) assayComboBox.getSelectedItem();

		if(errors.isEmpty()) {

//			MultiCefImportTask task = new MultiCefImportTask(libraryFile, inputFiles, activeAssay, alignmentType);
//			task.addTaskListener(CefAnalyzerCore.getMainWindow());
//			CefAnalyzerCore.getTaskController().addTask(task);
			this.setVisible(false);
		}
		else {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"));
		}
	}
}






























