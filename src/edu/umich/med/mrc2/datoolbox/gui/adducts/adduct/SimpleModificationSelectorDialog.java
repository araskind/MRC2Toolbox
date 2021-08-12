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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class SimpleModificationSelectorDialog extends JDialog 
	implements ListSelectionListener, PersistentLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6532806418845095432L;
	
	public static final Icon dialogIcon = GuiUtils.getIcon("calculateAnnotation", 32);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "SimpleModificationSelectorDialog.layout");
	
	private CControl control;
	private CGrid grid;
	private DockableSimpleModificationsTable adductsPanel;
	private DockableMolStructurePanel structurePanel;
	private JComboBox numUnitsComboBox;	
	
	public SimpleModificationSelectorDialog(ActionListener listener, ModificationType modType) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		if(modType.equals(ModificationType.LOSS))
			setTitle("Select neutral loss");
		if(modType.equals(ModificationType.REPEAT))
			setTitle("Select neutral adduct");
		
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 450));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.getController().setTheme(new EclipseTheme());
		getContentPane().add( control.getContentArea() );
		grid = new CGrid(control);

		adductsPanel = new DockableSimpleModificationsTable(modType);
		adductsPanel.getAdductTable().getSelectionModel().addListSelectionListener(this);
		structurePanel = new DockableMolStructurePanel(
				"SimpleModificationSelectorDialogFrameDockableMolStructurePanel");

		grid.add(0, 0, 1, 1,
				adductsPanel,
				structurePanel);

		control.getController().setFocusedDockable(adductsPanel.intern(), true);
		control.getContentArea().deploy(grid);
				
		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[]{95, 76, 30, 0, 0, 0};
		gbl_buttonPanel.rowHeights = new int[]{23, 0};
		gbl_buttonPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_buttonPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		buttonPanel.setLayout(gbl_buttonPanel);
		
		JLabel lblNewLabel = new JLabel("Number of units");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		buttonPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);	
		
		numUnitsComboBox = new JComboBox<Integer>(
				new DefaultComboBoxModel<Integer>(new Integer[] {1,2,3,4,5,6,7,8}));
		GridBagConstraints gbc_numUnitsComboBox = new GridBagConstraints();
		gbc_numUnitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_numUnitsComboBox.anchor = GridBagConstraints.NORTH;
		gbc_numUnitsComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_numUnitsComboBox.gridx = 1;
		gbc_numUnitsComboBox.gridy = 0;
		buttonPanel.add(numUnitsComboBox, gbc_numUnitsComboBox);
		
		JLabel lblNewLabel_1 = new JLabel(" ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		buttonPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_cancelButton.insets = new Insets(0, 0, 0, 5);
		gbc_cancelButton.gridx = 3;
		gbc_cancelButton.gridy = 0;
		buttonPanel.add(cancelButton, gbc_cancelButton);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_saveButton.gridx = 4;
		gbc_saveButton.gridy = 0;
		buttonPanel.add(saveButton, gbc_saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);
		if(modType.equals(ModificationType.LOSS)) {
			saveButton.setText(
					MainActionCommands.ADD_NEUTRAL_LOSS_COMMAND.getName());
			saveButton.setActionCommand(
					MainActionCommands.ADD_NEUTRAL_LOSS_COMMAND.getName());
		}
		if(modType.equals(ModificationType.REPEAT)) {
			saveButton.setText(
					MainActionCommands.ADD_NEUTRAL_ADDUCT_COMMAND.getName());
			saveButton.setActionCommand(
					MainActionCommands.ADD_NEUTRAL_ADDUCT_COMMAND.getName());
		}
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		loadLayout(layoutConfigFile);		
		pack();
	}

	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);
		super.dispose();
	}
	
	public SimpleAdduct getSelectedNeutralModification() {
		return adductsPanel.getAdductTable().getSelectedModification();
	}
	
	public int getNumberOfUnits() {
		return (int)numUnitsComboBox.getSelectedItem();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		structurePanel.clearPanel();
		Adduct mod = adductsPanel.getAdductTable().getSelectedModification();
		if(mod == null || mod.getSmiles() == null)
			return;
		
		structurePanel.showStructure(mod.getSmiles());
	}

	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}
