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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.github.lgooddatepicker.components.DatePicker;

import edu.umich.med.mrc2.datoolbox.data.enums.AnnotatedObjectType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class LabNoteEditorToolbar extends CommonToolbar implements ItemListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 758758438753872786L;
	private JComboBox categoryComboBox;
	private JComboBox experimentComboBox;
	private JComboBox sampleComboBox;
	private DatePicker datePicker;

	public LabNoteEditorToolbar(ActionListener commandListener) {
		super(commandListener);

		JLabel categoryLabel = new JLabel("Category: ");
		categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(categoryLabel);

		categoryComboBox = new JComboBox();
		categoryComboBox.setMaximumSize(new Dimension(120, 25));
		categoryComboBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		DefaultComboBoxModel<AnnotatedObjectType> categoryBoxModel =
			new DefaultComboBoxModel<AnnotatedObjectType>(new AnnotatedObjectType[] {
					AnnotatedObjectType.INSTRUMENT_MAINTENANCE,
					AnnotatedObjectType.SAMPLE,
					AnnotatedObjectType.INJECTION,
					AnnotatedObjectType.EXPERIMENT,
					AnnotatedObjectType.DATA_ANALYSIS,
			});
		categoryComboBox.setModel(categoryBoxModel);
		categoryComboBox.setSelectedIndex(-1);
		categoryComboBox.addItemListener(this);
		categoryComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(categoryComboBox);
		addSeparator(buttonDimension);

		JLabel experimentLabel = new JLabel("Experiment: ");
		experimentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(experimentLabel);

		experimentComboBox = new JComboBox();
		experimentComboBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		experimentComboBox.setModel(
				new SortedComboBoxModel<LIMSExperiment>(LIMSDataCache.getExperiments()));
		experimentComboBox.setMaximumSize(new Dimension(250, 25));
		experimentComboBox.setSelectedIndex(-1);
		experimentComboBox.addItemListener(this);
		experimentComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(experimentComboBox);
		addSeparator(buttonDimension);

		JLabel sampleLabel = new JLabel("Sample: ");
		experimentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(sampleLabel);

		sampleComboBox = new JComboBox();
		sampleComboBox.setMinimumSize(new Dimension(70, 25));
		sampleComboBox.setMaximumSize(new Dimension(100, 25));
		sampleComboBox.setFont(new Font("Tahoma", Font.BOLD, 12));
		sampleComboBox.setSelectedIndex(-1);
		sampleComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(sampleComboBox);
		addSeparator(buttonDimension);

		datePicker = new DatePicker();
		datePicker.setMaximumSize(new Dimension(120, 25));
		LocalDate localDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		datePicker.setDate(localDate);
		datePicker.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(datePicker);
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

}
