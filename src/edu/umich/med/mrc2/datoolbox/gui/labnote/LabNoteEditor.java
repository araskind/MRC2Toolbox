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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;

import com.github.lgooddatepicker.components.DatePicker;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rtf.DockableRTFEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleComboboxRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import rtf.AdvancedRTFEditorKit;

public class LabNoteEditor extends JDialog implements ActionListener, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1481350935164911990L;

	private JButton saveButton, cancelButton;
	private AnalysisQcEventAnnotation currentAnnotation;
	private LabNoteEditorToolbar toolbar;
	private JComboBox categoryComboBox;
	private JComboBox experimentComboBox;
	private JComboBox sampleComboBox;
	private DatePicker datePicker;

	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 32);

	private JPanel panel_1;
	private JLabel lblAssay;
	private JComboBox assayComboBox;

	private IndeterminateProgressDialog idp;
	private CControl control;
	private CGrid grid;
	private DockableLabNotePlainTextViewer noteViewer;
	private DockableRTFEditor rtfEditor;
	private JLabel lblInstrument;
	private JComboBox instrumentComboBox;
	private JCheckBox chckbxIgnoreInstrument;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LabNoteEditor(ActionListener listener) {

		super();
		setTitle("Edit annotation");
		setIconImage(((ImageIcon) editAnnotationIcon).getImage());
		setSize(new Dimension(850, 640));
		setPreferredSize(new Dimension(850, 640));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setModal(true);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		noteViewer = new DockableLabNotePlainTextViewer();
		noteViewer.setEditable(true);
		rtfEditor = new DockableRTFEditor(
				"LabNoteEditorDockableRTFEditor", "Formatted", false);

		grid.add(0, 0, 100, 100, rtfEditor, noteViewer);
		control.getContentArea().deploy(grid);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
		control.getController().setFocusedDockable(rtfEditor.intern(), true);

		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 225, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel categoryLabel = new JLabel("Category: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(categoryLabel, gbc_lblNewLabel);

		categoryComboBox = new JComboBox();
		categoryComboBox.setMinimumSize(new Dimension(70, 25));
		categoryComboBox.setMaximumSize(new Dimension(120, 25));
		categoryComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		categoryComboBox.setModel(new DefaultComboBoxModel<QcEventType>( QcEventType.values()));
		categoryComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(categoryComboBox, gbc_comboBox);

		JLabel dateLabel = new JLabel("Date: ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 0;
		panel_1.add(dateLabel, gbc_lblNewLabel_3);

		datePicker = new DatePicker();
		datePicker.setMaximumSize(new Dimension(120, 25));
		LocalDate localDate = new Date().toInstant().
				atZone(ZoneId.systemDefault()).toLocalDate();
		datePicker.setDate(localDate);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 0;
		panel_1.add(datePicker, gbc_btnNewButton);

		JLabel experimentLabel = new JLabel("Experiment: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_1.add(experimentLabel, gbc_lblNewLabel_2);

		experimentComboBox = new JComboBox();
		experimentComboBox.setMinimumSize(new Dimension(100, 25));
		experimentComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		experimentComboBox.setModel(
			new SortedComboBoxModel<LIMSExperiment>(LIMSDataCache.getExperiments()));
		experimentComboBox.setSize(new Dimension(250, 25));
		experimentComboBox.setMaximumSize(new Dimension(250, 25));
		experimentComboBox.setSelectedIndex(-1);
		experimentComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.gridwidth = 3;
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 1;
		panel_1.add(experimentComboBox, gbc_comboBox_1);

		lblAssay = new JLabel("Assay: ");
		GridBagConstraints gbc_lblAssay = new GridBagConstraints();
		gbc_lblAssay.anchor = GridBagConstraints.EAST;
		gbc_lblAssay.insets = new Insets(0, 0, 5, 5);
		gbc_lblAssay.gridx = 0;
		gbc_lblAssay.gridy = 2;
		panel_1.add(lblAssay, gbc_lblAssay);

		assayComboBox = new JComboBox(
				new SortedComboBoxModel<Assay>(LIMSDataCache.getAssays()));
		assayComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		assayComboBox.setSelectedIndex(-1);
		assayComboBox.setMaximumSize(new Dimension(300, 25));
		assayComboBox.setMinimumSize(new Dimension(100, 25));
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = 2;
		panel_1.add(assayComboBox, gbc_assayComboBox);

		JLabel sampleLabel = new JLabel("Sample: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel_1.add(sampleLabel, gbc_lblNewLabel_1);

		sampleComboBox = new JComboBox();
		sampleComboBox.setRenderer(new ExperimentalSampleComboboxRenderer());
		sampleComboBox.setMinimumSize(new Dimension(70, 25));
		sampleComboBox.setMaximumSize(new Dimension(100, 25));
		sampleComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		sampleComboBox.setSelectedIndex(-1);

		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 1;
		gbc_comboBox_2.gridy = 3;
		panel_1.add(sampleComboBox, gbc_comboBox_2);

		lblInstrument = new JLabel("Instrument: ");
		GridBagConstraints gbc_lblInstrument = new GridBagConstraints();
		gbc_lblInstrument.anchor = GridBagConstraints.EAST;
		gbc_lblInstrument.insets = new Insets(0, 0, 0, 5);
		gbc_lblInstrument.gridx = 0;
		gbc_lblInstrument.gridy = 4;
		panel_1.add(lblInstrument, gbc_lblInstrument);

		instrumentComboBox =
			new JComboBox(new SortedComboBoxModel<LIMSInstrument>(IDTDataCache.getInstrumentList()));
		instrumentComboBox.setSelectedIndex(-1);
		instrumentComboBox.setMinimumSize(new Dimension(70, 25));
		instrumentComboBox.setMaximumSize(new Dimension(100, 25));
		instrumentComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_instrumentComboBox = new GridBagConstraints();
		gbc_instrumentComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_instrumentComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentComboBox.gridx = 1;
		gbc_instrumentComboBox.gridy = 4;
		panel_1.add(instrumentComboBox, gbc_instrumentComboBox);

		chckbxIgnoreInstrument = new JCheckBox("Ignore instrument");
		GridBagConstraints gbc_chckbxIgnoreInstrument = new GridBagConstraints();
		gbc_chckbxIgnoreInstrument.anchor = GridBagConstraints.WEST;
		gbc_chckbxIgnoreInstrument.gridx = 3;
		gbc_chckbxIgnoreInstrument.gridy = 4;
		panel_1.add(chckbxIgnoreInstrument, gbc_chckbxIgnoreInstrument);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		saveButton.setActionCommand(
				MainActionCommands.SAVE_LAB_NOTE_COMMAND.getName());
		panel.add(saveButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public synchronized void clearPanel() {

		noteViewer.clearPanel();
		rtfEditor.clearPanel();

		experimentComboBox.removeItemListener(this);

		categoryComboBox.setSelectedIndex(-1);
		experimentComboBox.setSelectedIndex(-1);
		sampleComboBox.setSelectedIndex(-1);
		assayComboBox.setSelectedIndex(-1);

		experimentComboBox.addItemListener(this);

		LocalDate currentDate = new Date().toInstant().
				atZone(ZoneId.systemDefault()).toLocalDate();
		datePicker.setDate(currentDate);
	}

	public AnalysisQcEventAnnotation getAnnotation() {
		return currentAnnotation;
	}

	public void loadAnnotation(AnalysisQcEventAnnotation annotation, Document doc) {

		clearPanel();
		currentAnnotation = annotation;
		if(currentAnnotation == null)
			return;

		categoryComboBox.setSelectedItem(currentAnnotation.getQcEventType());
		experimentComboBox.setSelectedItem(currentAnnotation.getExperiment());
		sampleComboBox.setSelectedItem(currentAnnotation.getSample());
		assayComboBox.setSelectedItem(currentAnnotation.getAssay());
		noteViewer.setAnnotationText(currentAnnotation.getText());
		instrumentComboBox.setSelectedItem(currentAnnotation.getInstrument());

		if(doc == null)
			return;

		if(doc.getLength() > 0)
			rtfEditor.loadDocument(doc);
	}

	public String getAnnotationText() {
		return noteViewer.getText().trim();
	}

	public Document getDocument() {
		return rtfEditor.getDocument();
	}

	/**
	 * @return the w_kit
	 */
	public AdvancedRTFEditorKit getRTFEditorKit() {
		return rtfEditor.getRTFEditorKit();
	}

	@Override
	public void setVisible(boolean visible) {

		if (visible) {
			if(rtfEditor.getDocument().getLength() > 0) {
				control.getController().setFocusedDockable(rtfEditor.intern(), true);
			}
			else {
				control.getController().setFocusedDockable((Dockable) noteViewer.intern(), true);
				noteViewer.focusOntext();
			}
		}
		super.setVisible(visible);
	}

	public Date getDate() {

		if(datePicker.getDate() == null)
			return null;

		return Date.from(datePicker.getDate().
				atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public QcEventType getAnnotationCategory() {
		return (QcEventType)categoryComboBox.getSelectedItem();
	}

	public LIMSExperiment getExperiment() {
		return (LIMSExperiment)experimentComboBox.getSelectedItem();
	}

	public ExperimentalSample getSample() {
		return (ExperimentalSample)sampleComboBox.getSelectedItem();
	}

	public Assay getAssay() {
		return (Assay)assayComboBox.getSelectedItem();
	}

	public LIMSInstrument getInstrument() {

		if(chckbxIgnoreInstrument.isSelected())
			return null;
		else
			return (LIMSInstrument)instrumentComboBox.getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent event) {
		// TODO Auto-generated method stub
		if (event.getStateChange() == ItemEvent.SELECTED) {

			if (event.getSource().equals(experimentComboBox)) {

				LIMSExperiment experiment = (LIMSExperiment)experimentComboBox.getSelectedItem();
				if(experiment.getExperimentDesign() == null) {

					ExperimentDesignRetrievalTask task = new ExperimentDesignRetrievalTask(experiment);
					idp = new IndeterminateProgressDialog("Getting MetLIMS data ...", this.getContentPane(), task);
					idp.setLocationRelativeTo(this.getContentPane());
					idp.setVisible(true);
				}
				else {
					ExperimentalSample[] samples = experiment.getExperimentDesign().getSamples().
							stream().toArray(size -> new ExperimentalSample[size]);
					sampleComboBox.setModel(new DefaultComboBoxModel<ExperimentalSample>(samples));
					sampleComboBox.setSelectedIndex(-1);
//					AssayMethod[] assays = null;
//					try {
//						assays = LIMSUtils.getAssaysForExperiment(experiment).
//							stream().toArray(size -> new AssayMethod[size]);
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					assayComboBox.setModel(new DefaultComboBoxModel<AssayMethod>(assays));
				}
			}
		}
	}

	class ExperimentDesignRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private LIMSExperiment limsExperiment;

		public ExperimentDesignRetrievalTask(LIMSExperiment limsExperiment) {
			this.limsExperiment = limsExperiment;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			ExperimentDesign design = null;
			try {
				design = LIMSUtils.getDesignForExperiment(limsExperiment.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(design != null) {
				limsExperiment.setDesign(design);
				ExperimentalSample[] samples = limsExperiment.getExperimentDesign().getSamples().
						stream().toArray(size -> new ExperimentalSample[size]);
				DefaultComboBoxModel<ExperimentalSample> aModel =
						new DefaultComboBoxModel<ExperimentalSample>(samples);
				sampleComboBox.setModel(aModel);
				sampleComboBox.setSelectedIndex(-1);
			}
//			AssayMethod[] assays = null;
//			try {
//				assays = LIMSUtils.getAssaysForExperiment(limsExperiment).
//					stream().toArray(size -> new AssayMethod[size]);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			assayComboBox.setModel(new DefaultComboBoxModel<AssayMethod>(assays));
			return null;
		}
	}
}
