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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.cpd.CompoundCollectionListingTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class CompoundCollectionSelectorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -6105883973403855121L;
	private static final Icon componentIcon = GuiUtils.getIcon("compoundCollection", 32);
	
	private CompoundCollectionListingTable compoundCollectionListingTable;

	public CompoundCollectionSelectorDialog(ActionListener actionListener) {

		super();
		setTitle("Select compound collection");
		setIconImage(((ImageIcon) componentIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 400));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel tablePanel = new JPanel();
		tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(tablePanel, BorderLayout.CENTER);
		
		compoundCollectionListingTable = 
				new CompoundCollectionListingTable();
		tablePanel.add(new JScrollPane(compoundCollectionListingTable), 
				BorderLayout.CENTER);
		
		Collection<CompoundCollection> collections = null;
		try {
			collections = CompoundMultiplexUtils.getCompoundCollections();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		if(collections != null)
			compoundCollectionListingTable.setTableModelFromCompoundCollections(collections);
				
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

		JButton btnLoad = new JButton(
				MainActionCommands.LOAD_COMPOUND_COLLECTION_COMMAND.getName());
		btnLoad.setActionCommand(
				MainActionCommands.LOAD_COMPOUND_COLLECTION_COMMAND.getName());
		btnLoad.addActionListener(actionListener);
		panel.add(btnLoad);
		JRootPane rootPane = SwingUtilities.getRootPane(btnLoad);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnLoad);
		
		compoundCollectionListingTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							btnLoad.doClick();
						}
					}
				});

		pack();
	}

	public CompoundCollection getSelectedCompoundCollection() {
		return compoundCollectionListingTable.getSelectedCompoundCollection();
	}
}





































