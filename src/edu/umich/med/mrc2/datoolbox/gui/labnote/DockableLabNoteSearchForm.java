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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.github.lgooddatepicker.components.DatePicker;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleComboboxRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class DockableLabNoteSearchForm extends DefaultSingleCDockable
	implements ActionListener, ItemListener {

	private static final Icon componentIcon = GuiUtils.getIcon("dbLookup", 16);
	public static final String CLEAR_FORM = "Clear form";

	private JComboBox categoryComboBox;
	private JComboBox experimentComboBox;
	private JComboBox sampleComboBox;
	private JComboBox userComboBox;
	private JComboBox assayComboBox;
	private DatePicker datePickerFrom;
	private DatePicker datePickerTo;
	private JButton clearButton;
	private JButton searchButton;
	private ActionListener parentPanel;

	private IndeterminateProgressDialog idp;
	private JLabel lblInstrument;
	private JComboBox instrumentComboBox;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DockableLabNoteSearchForm(ActionListener parentPanel) {

		super("DockableLabNoteSearchForm", componentIcon, "Search notes database", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		this.parentPanel = parentPanel;

		setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 158, 52, 33, 82, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel categoryLabel = new JLabel("Category: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(categoryLabel, gbc_lblNewLabel);

		categoryComboBox = new JComboBox();
		categoryComboBox.setMinimumSize(new Dimension(70, 25));
		categoryComboBox.setMaximumSize(new Dimension(120, 25));
		categoryComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		categoryComboBox.setModel(new DefaultComboBoxModel<QcEventType>( QcEventType.values()));
		categoryComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_categoryComboBox = new GridBagConstraints();
		gbc_categoryComboBox.gridwidth = 2;
		gbc_categoryComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_categoryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_categoryComboBox.gridx = 1;
		gbc_categoryComboBox.gridy = 0;
		panel.add(categoryComboBox, gbc_categoryComboBox);

		lblInstrument = new JLabel("Instrument: ");
		GridBagConstraints gbc_lblInstrument = new GridBagConstraints();
		gbc_lblInstrument.anchor = GridBagConstraints.EAST;
		gbc_lblInstrument.insets = new Insets(0, 0, 5, 5);
		gbc_lblInstrument.gridx = 0;
		gbc_lblInstrument.gridy = 1;
		panel.add(lblInstrument, gbc_lblInstrument);

		instrumentComboBox = new JComboBox();
		instrumentComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		instrumentComboBox.setMinimumSize(new Dimension(100, 25));
		instrumentComboBox.setMaximumSize(new Dimension(300, 25));
		GridBagConstraints gbc_instrumentComboBox = new GridBagConstraints();
		gbc_instrumentComboBox.gridwidth = 5;
		gbc_instrumentComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_instrumentComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentComboBox.gridx = 1;
		gbc_instrumentComboBox.gridy = 1;
		panel.add(instrumentComboBox, gbc_instrumentComboBox);

		JLabel experimentLabel = new JLabel("Experiment: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel.add(experimentLabel, gbc_lblNewLabel_2);

		experimentComboBox = new JComboBox();
		experimentComboBox.setMinimumSize(new Dimension(200, 25));
		experimentComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		experimentComboBox.setSize(new Dimension(250, 25));
		experimentComboBox.setMaximumSize(new Dimension(250, 25));
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.gridwidth = 5;
		gbc_comboBox_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 2;
		panel.add(experimentComboBox, gbc_comboBox_1);

		Component lblAssay = new JLabel("Assay: ");
		GridBagConstraints gbc_lblAssay = new GridBagConstraints();
		gbc_lblAssay.anchor = GridBagConstraints.EAST;
		gbc_lblAssay.insets = new Insets(0, 0, 5, 5);
		gbc_lblAssay.gridx = 0;
		gbc_lblAssay.gridy = 3;
		panel.add(lblAssay, gbc_lblAssay);

		assayComboBox = new JComboBox();
		assayComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		assayComboBox.setMinimumSize(new Dimension(100, 25));
		assayComboBox.setMaximumSize(new Dimension(400, 25));
		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.gridwidth = 2;
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = 3;
		panel.add(assayComboBox, gbc_assayComboBox);

		JLabel sampleLabel = new JLabel("Sample: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 4;
		panel.add(sampleLabel, gbc_lblNewLabel_1);

		sampleComboBox = new JComboBox();
		sampleComboBox.setRenderer(new ExperimentalSampleComboboxRenderer());
		sampleComboBox.setMinimumSize(new Dimension(70, 25));
		sampleComboBox.setMaximumSize(new Dimension(100, 25));
		sampleComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		sampleComboBox.setSelectedIndex(-1);

		GridBagConstraints gbc_comboBox_2 = new GridBagConstraints();
		gbc_comboBox_2.gridwidth = 3;
		gbc_comboBox_2.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_2.gridx = 1;
		gbc_comboBox_2.gridy = 4;
		panel.add(sampleComboBox, gbc_comboBox_2);

		JLabel dateLabel = new JLabel("Created between ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 5;
		panel.add(dateLabel, gbc_lblNewLabel_3);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, -1);
		LocalDate startDate = c.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		datePickerFrom = new DatePicker();
		datePickerFrom.setMaximumSize(new Dimension(140, 25));
		datePickerFrom.setDate(startDate);
		GridBagConstraints gbc_datePicker = new GridBagConstraints();
		gbc_datePicker.insets = new Insets(0, 0, 5, 5);
		gbc_datePicker.anchor = GridBagConstraints.WEST;
		gbc_datePicker.gridx = 1;
		gbc_datePicker.gridy = 5;
		panel.add(datePickerFrom, gbc_datePicker);

		JLabel lblAnd = new JLabel("and");
		GridBagConstraints gbc_lblAnd = new GridBagConstraints();
		gbc_lblAnd.insets = new Insets(0, 0, 5, 5);
		gbc_lblAnd.gridx = 2;
		gbc_lblAnd.gridy = 5;
		panel.add(lblAnd, gbc_lblAnd);

		datePickerTo = new DatePicker();
		datePickerTo.setDate(currentDate);
		datePickerTo.setMaximumSize(new Dimension(140, 25));
		GridBagConstraints gbc_datePicker_1 = new GridBagConstraints();
		gbc_datePicker_1.anchor = GridBagConstraints.WEST;
		gbc_datePicker_1.insets = new Insets(0, 0, 5, 5);
		gbc_datePicker_1.fill = GridBagConstraints.VERTICAL;
		gbc_datePicker_1.gridx = 3;
		gbc_datePicker_1.gridy = 5;
		panel.add(datePickerTo, gbc_datePicker_1);

		JLabel lblCreatedBy = new JLabel("Created/edited by: ");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.EAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 6;
		panel.add(lblCreatedBy, gbc_lblCreatedBy);

		userComboBox = new JComboBox();
		userComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		userComboBox.setMinimumSize(new Dimension(100, 25));
		userComboBox.setMaximumSize(new Dimension(400, 25));
		GridBagConstraints gbc_userComboBox = new GridBagConstraints();
		gbc_userComboBox.gridwidth = 2;
		gbc_userComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_userComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_userComboBox.gridx = 1;
		gbc_userComboBox.gridy = 6;
		panel.add(userComboBox, gbc_userComboBox);

		searchButton = new JButton("Search notes");
		searchButton.setActionCommand(MainActionCommands.SEARCH_DATABASE_COMMAND.getName());
		searchButton.addActionListener(parentPanel);

		clearButton = new JButton("Clear form");
		clearButton.setActionCommand(CLEAR_FORM);
		clearButton.addActionListener(this);
		clearButton.setPreferredSize(new Dimension(83, 25));
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 2;
		gbc_btnNewButton_1.gridy = 7;
		panel.add(clearButton, gbc_btnNewButton_1);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridwidth = 3;
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 3;
		gbc_btnNewButton.gridy = 7;
		panel.add(searchButton, gbc_btnNewButton);
		
		populateSearchFormFromDatabase();
	}
	
	@SuppressWarnings("unchecked")
	private void populateSearchFormFromDatabase() {
		
		if (!ConnectionManager.connectionDefined()) {
			MainWindow.displayErrorMessage("Connection error", 
					"Database connection not defined!");
			return;
		}		
		LIMSInstrument[] instruments =
				IDTDataCache.getInstrumentList().stream().
				toArray(size -> new LIMSInstrument[size]);
		instrumentComboBox.setModel(
				new DefaultComboBoxModel<LIMSInstrument>(instruments));
		instrumentComboBox.setSelectedIndex(-1);
		
		experimentComboBox.setModel(
				new SortedComboBoxModel<LIMSExperiment>(LIMSDataCache.getExperiments()));
		experimentComboBox.setSelectedIndex(-1);
		experimentComboBox.addItemListener(this);
		
		assayComboBox.setModel(
				new SortedComboBoxModel<Assay>(LIMSDataCache.getAssays()));
		assayComboBox.setSelectedIndex(-1);
		
		List<LIMSUser> staff = LIMSDataCache.getUsers().stream().filter(u -> u.getAffiliation().equals("STAFF"))
				.collect(Collectors.toList());
		SortedComboBoxModel<LIMSUser> userModel = new SortedComboBoxModel<LIMSUser>(staff);
		userComboBox.setModel(userModel);
		userComboBox.setSelectedIndex(-1);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(CLEAR_FORM))
			clearForm();
	}

	public void clearForm() {

		experimentComboBox.removeItemListener(this);

		categoryComboBox.setSelectedIndex(-1);
		experimentComboBox.setSelectedIndex(-1);
		sampleComboBox.setSelectedIndex(-1);
		userComboBox.setSelectedIndex(-1);
		assayComboBox.setSelectedIndex(-1);
		instrumentComboBox.setSelectedIndex(-1);

		experimentComboBox.addItemListener(this);

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.YEAR, -1);
		LocalDate startDate = c.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		datePickerFrom.setDate(startDate);
		datePickerTo.setDate(currentDate);
	}

	public Date getStartDate() {

		if(datePickerFrom.getDate() == null)
			return null;

		return Date.from(datePickerFrom.getDate().
				atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public Date getEndDate() {

		if(datePickerTo.getDate() == null)
			return null;

		return Date.from(datePickerTo.getDate().
				atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public QcEventType getAnnotationCategory() {
		return (QcEventType)categoryComboBox.getSelectedItem();
	}

	public LIMSUser getNoteAuthor() {
		return (LIMSUser)userComboBox.getSelectedItem();
	}

	public LIMSExperiment getExperiment() {
		return (LIMSExperiment)experimentComboBox.getSelectedItem();
	}

	public Assay getAssay() {
		return (Assay)assayComboBox.getSelectedItem();
	}

	public ExperimentalSample getSample() {
		return (ExperimentalSample)sampleComboBox.getSelectedItem();
	}

	public LIMSInstrument getInstrument() {
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














