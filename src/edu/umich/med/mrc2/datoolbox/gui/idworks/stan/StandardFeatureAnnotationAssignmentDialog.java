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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedListModel;

public class StandardFeatureAnnotationAssignmentDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -6105883973403855121L;
	private static final Icon addStandardFeatureAnnotationIcon = GuiUtils.getIcon("addCollection", 32);
	private static final Icon editStandardFeatureAnnotationIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon deleteStandardFeatureAnnotationIcon = GuiUtils.getIcon("deleteCollection", 32);	

	private JButton addButton, removeButton;
	private JList availableStandardFeatureAnnotationList;
	private JList usedStandardFeatureAnnotationList;
	
	private JButton btnSaveChanges;
	private ActionListener listener;

	public StandardFeatureAnnotationAssignmentDialog(
			ActionListener parent, MSFeatureInfoBundle bundle) {

		super();
		this.listener = parent;
		initGui();
		populateAnnotationListsFromFeatureInfoBundle(bundle);
		pack();
	}

	@SuppressWarnings("unchecked")
	private void initGui() {

		setTitle("Edit standard annotations for selected feature");
		setIconImage(((ImageIcon) editStandardFeatureAnnotationIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(600, 400));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{200, 40, 100, 100};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.2, 1.0, 1.0};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 0.0};
		panel.setLayout(gbl_panel);

		JLabel lblAvailableReferenceSamples = new JLabel("Available standard annotations");
		GridBagConstraints gbc_lblAvailableReferenceSamples = new GridBagConstraints();
		gbc_lblAvailableReferenceSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableReferenceSamples.insets = new Insets(0, 0, 5, 5);
		gbc_lblAvailableReferenceSamples.gridx = 0;
		gbc_lblAvailableReferenceSamples.gridy = 0;
		panel.add(lblAvailableReferenceSamples, gbc_lblAvailableReferenceSamples);

		JLabel lblReferenceSamplesPresent = new JLabel("Assigned standard annotations");
		GridBagConstraints gbc_lblReferenceSamplesPresent = new GridBagConstraints();
		gbc_lblReferenceSamplesPresent.gridwidth = 2;
		gbc_lblReferenceSamplesPresent.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblReferenceSamplesPresent.insets = new Insets(0, 0, 5, 0);
		gbc_lblReferenceSamplesPresent.gridx = 2;
		gbc_lblReferenceSamplesPresent.gridy = 0;
		panel.add(lblReferenceSamplesPresent, gbc_lblReferenceSamplesPresent);

		availableStandardFeatureAnnotationList = new JList<StandardFeatureAnnotation>();
		availableStandardFeatureAnnotationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		availableStandardFeatureAnnotationList.setCellRenderer(new StandardFeatureAnnotationListRenderer());
		JScrollPane scrollPane = new JScrollPane(availableStandardFeatureAnnotationList);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);

		usedStandardFeatureAnnotationList = new JList<StandardFeatureAnnotation>();
		usedStandardFeatureAnnotationList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		usedStandardFeatureAnnotationList.setCellRenderer(new StandardFeatureAnnotationListRenderer());
		JScrollPane scrollPane_1 = new JScrollPane(usedStandardFeatureAnnotationList);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridheight = 4;
		gbc_scrollPane_1.gridwidth = 2;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);

		addButton = new JButton(addStandardFeatureAnnotationIcon);
		addButton.setActionCommand(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_COMMAND.getName());
		addButton.addActionListener(this);
		addButton.setSize(CommonToolbar.buttonDimension);
		addButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_addButton = new GridBagConstraints();
		gbc_addButton.anchor = GridBagConstraints.SOUTH;
		gbc_addButton.insets = new Insets(0, 0, 5, 5);
		gbc_addButton.gridx = 1;
		gbc_addButton.gridy = 2;
		panel.add(addButton, gbc_addButton);

		removeButton = new JButton(deleteStandardFeatureAnnotationIcon);
		removeButton.setActionCommand(MainActionCommands.DELETE_STANDARD_FEATURE_ANNOTATION_COMMAND.getName());
		removeButton.addActionListener(this);
		removeButton.setSize(CommonToolbar.buttonDimension);
		removeButton.setPreferredSize(CommonToolbar.buttonDimension);
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.anchor = GridBagConstraints.NORTH;
		gbc_removeButton.insets = new Insets(0, 0, 5, 5);
		gbc_removeButton.gridx = 1;
		gbc_removeButton.gridy = 3;
		panel.add(removeButton, gbc_removeButton);

		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 5;
		panel.add(btnCancel, gbc_btnCancel);

		btnSaveChanges = new JButton("Save changes");
		btnSaveChanges.setActionCommand(MainActionCommands.SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND.getName());
		btnSaveChanges.addActionListener(listener);
		GridBagConstraints gbc_btnSaveChanges = new GridBagConstraints();
		gbc_btnSaveChanges.gridx = 3;
		gbc_btnSaveChanges.gridy = 5;
		panel.add(btnSaveChanges, gbc_btnSaveChanges);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSaveChanges);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSaveChanges);
	}
	

	@SuppressWarnings("unchecked")
	private void populateAnnotationListsFromFeatureInfoBundle(MSFeatureInfoBundle bundle) {

		Collection<StandardFeatureAnnotation> usedSteps = bundle.getStandadAnnotations();
		Collection<StandardFeatureAnnotation> availableSteps =  
				IDTDataCache.getStandardFeatureAnnotationList().stream().
				filter(s -> !usedSteps.contains(s)).collect(Collectors.toSet());
		
		availableStandardFeatureAnnotationList.setModel(
				new SortedListModel<StandardFeatureAnnotation>(availableSteps));
		usedStandardFeatureAnnotationList.setModel(
				new SortedListModel<StandardFeatureAnnotation>(usedSteps));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_COMMAND.getName())) {

			List<StandardFeatureAnnotation>selected = availableStandardFeatureAnnotationList.getSelectedValuesList();
			((SortedListModel)usedStandardFeatureAnnotationList.getModel()).addAll(selected);

			for(StandardFeatureAnnotation ref : selected)
				((SortedListModel)availableStandardFeatureAnnotationList.getModel()).removeElement(ref);
		}
		if(e.getActionCommand().equals(MainActionCommands.DELETE_STANDARD_FEATURE_ANNOTATION_COMMAND.getName())) {

			List<StandardFeatureAnnotation>selected = usedStandardFeatureAnnotationList.getSelectedValuesList();
			((SortedListModel)availableStandardFeatureAnnotationList.getModel()).addAll(selected);

			for(StandardFeatureAnnotation ref : selected)
				((SortedListModel)usedStandardFeatureAnnotationList.getModel()).removeElement(ref);
		}
	}

	public Collection<StandardFeatureAnnotation>getAvailableAnnotations(){

		@SuppressWarnings("rawtypes")
		SortedListModel model = ((SortedListModel)availableStandardFeatureAnnotationList.getModel());
        TreeSet<StandardFeatureAnnotation> items = new TreeSet<StandardFeatureAnnotation>();
		int size = model.getSize();
		for (int i = 0; i < size; i++)
			items.add((StandardFeatureAnnotation) model.getElementAt(i));

		return items;
	}

	public Collection<StandardFeatureAnnotation>getUsedAnnotations(){

		@SuppressWarnings("rawtypes")
		SortedListModel model = ((SortedListModel)usedStandardFeatureAnnotationList.getModel());
        TreeSet<StandardFeatureAnnotation> items = new TreeSet<StandardFeatureAnnotation>();
		int size = model.getSize();
		for (int i = 0; i < size; i++)
			items.add((StandardFeatureAnnotation) model.getElementAt(i));

		return items;
	}
}





































