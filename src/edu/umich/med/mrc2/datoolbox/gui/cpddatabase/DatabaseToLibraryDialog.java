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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DatabaseToLibraryDialog  extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -1398197472444571634L;
	private JButton addButton;
	private JButton cancelButton;
	private JComboBox libraryComboBox;
	private Collection<CompoundIdentity>newCompounds;

	private static final Icon createLibIcon = GuiUtils.getIcon("newLibrary", 32);
	private static final Icon addToLibIcon = GuiUtils.getIcon("databaseToLibrary", 32);

	public DatabaseToLibraryDialog() {

		super(MRC2ToolBoxCore.getMainWindow());
		setIconImage(((ImageIcon) addToLibIcon).getImage());

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(450, 140));
		setPreferredSize(new Dimension(450, 140));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblLibrary = new JLabel("Library");
		GridBagConstraints gbc_lblLibrary = new GridBagConstraints();
		gbc_lblLibrary.insets = new Insets(0, 0, 5, 5);
		gbc_lblLibrary.anchor = GridBagConstraints.EAST;
		gbc_lblLibrary.gridx = 0;
		gbc_lblLibrary.gridy = 0;
		panel.add(lblLibrary, gbc_lblLibrary);

		libraryComboBox = new JComboBox<CompoundLibrary>();
		GridBagConstraints gbc_libraryComboBox = new GridBagConstraints();
		gbc_libraryComboBox.gridwidth = 2;
		gbc_libraryComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_libraryComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_libraryComboBox.gridx = 1;
		gbc_libraryComboBox.gridy = 0;
		panel.add(libraryComboBox, gbc_libraryComboBox);

		cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.EAST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 1;
		panel.add(cancelButton, gbc_cancelButton);

		addButton = new JButton("Add to library");
		addButton.addActionListener(this);
		GridBagConstraints gbc_addButton = new GridBagConstraints();
		gbc_addButton.anchor = GridBagConstraints.ABOVE_BASELINE;
		gbc_addButton.gridx = 2;
		gbc_addButton.gridy = 1;
		panel.add(addButton, gbc_addButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(addButton);
		rootPane.setDefaultButton(addButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();

		newCompounds = new ArrayList<CompoundIdentity>();
	}

	public void updateDialog(String command, CompoundIdentity id) {

		newCompounds.clear();
		newCompounds.add(id);
		updateDialogFromCommand(command);
	}

	public void updateDialog(String command, Collection<CompoundIdentity>compounds) {

		newCompounds.clear();
		newCompounds.addAll(compounds);
		updateDialogFromCommand(command);
	}

	private void updateDialogFromCommand(String command) {

		if(command.equals(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND.getName())) {

			setTitle(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_COMMAND.getName());
			addButton.setActionCommand(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_COMMAND.getName());
			addButton.setText(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_COMMAND.getName());
			setIconImage(((ImageIcon) addToLibIcon).getImage());

			libraryComboBox.setModel(new SortedComboBoxModel<CompoundLibrary>(MRC2ToolBoxCore.getActiveMsLibraries()));
			CompoundLibrary selected = null;
			if(libraryComboBox.getSelectedItem() != null)
				selected = (CompoundLibrary) libraryComboBox.getSelectedItem();

			if(selected != null) {

				if(MRC2ToolBoxCore.getActiveMsLibraries().contains(selected))
					libraryComboBox.setSelectedItem(selected);
			}
		}
		if(command.equals(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_COMMAND.getName())) {

			setTitle(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_COMMAND.getName());
			addButton.setActionCommand(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_COMMAND.getName());
			addButton.setText(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_COMMAND.getName());
			setIconImage(((ImageIcon) createLibIcon).getImage());
		}
		if(command.equals(MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_COMMAND.getName())) {

			setTitle(MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_COMMAND.getName());
			addButton.setActionCommand(MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_COMMAND.getName());
			addButton.setText(MainActionCommands.ADD_COMPOUND_LIST_TO_LIBRARY_COMMAND.getName());
			setIconImage(((ImageIcon) addToLibIcon).getImage());
			libraryComboBox.setModel(new SortedComboBoxModel<CompoundLibrary>(MRC2ToolBoxCore.getActiveMsLibraries()));
			CompoundLibrary selected = null;
			if(libraryComboBox.getSelectedItem() != null)
				selected = (CompoundLibrary) libraryComboBox.getSelectedItem();

			if(selected != null) {

				if(MRC2ToolBoxCore.getActiveMsLibraries().contains(selected))
					libraryComboBox.setSelectedItem(selected);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.ADD_TO_LIBRARY_FROM_DATABASE_COMMAND.getName()))
			addMultipleCompoundsFromDatabaseToLibrary();

		if (command.equals(MainActionCommands.CREATE_LIBRARY_FROM_DATABASE_COMMAND.getName())){


			setVisible(false);
		}
	}

	private void addMultipleCompoundsFromDatabaseToLibrary() {

		if(newCompounds.isEmpty())
			return;

		CompoundLibrary library = (CompoundLibrary) libraryComboBox.getSelectedItem();
		if(library == null)
			return;

		if(newCompounds.size() == 1) {
			addSingleNewCompound(newCompounds.iterator().next(), library);
			return;
		}
		//	TODO add multiple compounds
		ArrayList<String>log = new ArrayList<String>();
		for(CompoundIdentity id : newCompounds) {

			LibraryMsFeature match =
					library.getFeatures().stream().
					filter(f -> f.getPrimaryIdentity().getCompoundIdentity().equals(id)).findFirst().get();


		}
	}

	private void addSingleNewCompound(CompoundIdentity id, CompoundLibrary library) {

		LibraryMsFeature match =
				library.getFeatures().stream().
				filter(f -> f.getPrimaryIdentity().getCompoundIdentity().equals(id)).
				findFirst().orElse(null);

		if(match == null) {
			addCompoundToLibrary(id, library, null);
			setVisible(false);
			return;
		}
		//	Depending on wheather ID may be ambigous, for now only lipids are supported
		if(id.getPrimaryDatabase().equals(CompoundDatabaseEnum.LIPIDMAPS_BULK)) {

			String yesNoQuestion = id.getName() + " already present in the library " + library.getLibraryName()
				+ ".\nDo you want to insert additional entry for it with a different retention time?";

			if(MessageDialog.showChoiceMsg(yesNoQuestion , this) == JOptionPane.YES_OPTION) {

				long existingEntries = library.getFeatures().stream().
					filter(f -> f.getPrimaryIdentity().getCompoundIdentity().equals(id)).count();
				String newName = id.getName() + " #" + Integer.toString((int) (existingEntries + 1));
				addCompoundToLibrary(id, library, newName);
				return;
			}
			else
				return;
		}
		else {
			MessageDialog.showWarningMsg(
				id.getName() + " already present in the library " + library.getLibraryName(), this);
			setVisible(false);
			switchToLibraryFeature(match, library);
			return;
		}
	}

	private void addCompoundToLibrary(CompoundIdentity newCompound2, CompoundLibrary library, String newName) {

		MsFeatureIdentity msid =
				new MsFeatureIdentity(newCompound2, CompoundIdentificationConfidence.ACCURATE_MASS);
		String compoundName = newName;
		if(compoundName == null)
			compoundName = newCompound2.getName();

		LibraryMsFeature lf = new LibraryMsFeature(compoundName, newCompound2.getExactMass(), 0.0d);
		lf.setPrimaryIdentity(msid);
		boolean added = false;
		try {
			MSRTLibraryUtils.loadLibraryFeature(lf, library.getLibraryId());
			added = true;
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(added) {

			setVisible(false);
			library.addFeature(lf);
			switchToLibraryFeature(lf, library);
		}
		else {
			MessageDialog.showErrorMsg("Inserting new library entry failed!", this);
		}
	}

	private void switchToLibraryFeature(LibraryMsFeature lf, CompoundLibrary library) {

		MsLibraryPanel libPanel  = (MsLibraryPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.MS_LIBRARY);
		libPanel.loadLibrary(library);
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.MS_LIBRARY);
		libPanel.selectFeature(lf);
	}
}














