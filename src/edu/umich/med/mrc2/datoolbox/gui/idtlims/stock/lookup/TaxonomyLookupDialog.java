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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class TaxonomyLookupDialog extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -100375709746615684L;

	private static final Icon lookupIcon = GuiUtils.getIcon("searchDatabase", 32);
	private JButton btnSelect;
	private JTextField textField;
	private SpeciesTable speciesTable;

	private IndeterminateProgressDialog idp;

	public TaxonomyLookupDialog(ActionListener listener) {
		super();
		setPreferredSize(new Dimension(500, 350));
		setSize(new Dimension(500, 350));
		setTitle("Lookup species in taxonomy database");
		setIconImage(((ImageIcon) lookupIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 0;
		panel_2.add(textField, gbc_textField);
		textField.setColumns(10);

		JButton searchButton = new JButton("Search");
		searchButton.setActionCommand(MainActionCommands.SEARCH_SPECIES_COMMAND.getName());
		searchButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		panel_2.add(searchButton, gbc_btnNewButton);

		speciesTable = new SpeciesTable();
		speciesTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							btnSelect.doClick();
						}
					}
				});
		JScrollPane scrollPane = new JScrollPane(speciesTable);
		panel_1.add(scrollPane, BorderLayout.CENTER);

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

		btnSelect = new JButton(MainActionCommands.SELECT_SPECIES_COMMAND.getName());
		btnSelect.setActionCommand(MainActionCommands.SELECT_SPECIES_COMMAND.getName());
		btnSelect.addActionListener(listener);
		panel.add(btnSelect);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSelect);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSelect);
		
		loadExistingSpecies();
		pack();		
	}
	
	private void loadExistingSpecies() {
		
		Collection<LIMSBioSpecies>species = new ArrayList<LIMSBioSpecies>();
		try {
			species = IDTUtils.getAvailableSpecies();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		speciesTable.setModelFromSpeciesList(species);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SEARCH_SPECIES_COMMAND.getName())) {
			searchTaxonomyDatabase();
		}
	}

	public LIMSBioSpecies getSelectedSpecies() {
		return speciesTable.getSelectedSpecies();
	}

	private void searchTaxonomyDatabase() {

		String searchString = textField.getText().trim();
		if(searchString.length() < 4) {
			MessageDialog.showErrorMsg("Search string too short (please use 4 letters or more).", this);
			return;
		}
		TaxonomyLookupTask task = new TaxonomyLookupTask();
		idp = new IndeterminateProgressDialog("Searching taxonomy database ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class TaxonomyLookupTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */

		public TaxonomyLookupTask() {
			super();
		}

		@Override
		public Void doInBackground() {

			Collection<LIMSBioSpecies>species = new ArrayList<LIMSBioSpecies>();
			try {
				species = IDTUtils.lookupSpeciesInTaxonomyDatabase(textField.getText().trim());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			speciesTable.setModelFromSpeciesList(species);
			return null;
		}

	    @Override
	    public void done() {
	        try {
	            if (!isCancelled()) get();
	        } catch (ExecutionException e) {
	            // Exception occurred, deal with it
	            System.out.println("Exception: " + e.getCause());
	        } catch (InterruptedException e) {
	            // Shouldn't happen, we're invoked when computation is finished
	            throw new AssertionError(e);
	        }
	        super.done();
	    }
	}
}






