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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureLookupListManager;

public class FeatureLookupListSelectorDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7638441884088309136L;
	private static final Icon editCollectionIcon = GuiUtils.getIcon("editCollection", 32);
	private FeatureLookupListsTable fllTable;
	private JButton btnSave;
	
	public FeatureLookupListSelectorDialog(ActionListener listener) {
		super();
		
		setTitle("Select feature lookup list");
		setIconImage(((ImageIcon) editCollectionIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		fllTable = new FeatureLookupListsTable();
		fllTable.setTableModelFromFeatureLookupListCollection(
				FeatureLookupListManager.getFeatureLookupListCollection());
		fllTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							FeatureLookupList selected = 
									fllTable.getSelectedDataSet();
							if(selected == null)
								return;
							
							btnSave.doClick(); 
						}
					}
				});
		getContentPane().add(new JScrollPane(fllTable), BorderLayout.CENTER);

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

		btnSave = new JButton(
				MainActionCommands.LOAD_FEATURE_LOOKUP_LIST_FROM_DATABASE_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.LOAD_FEATURE_LOOKUP_LIST_FROM_DATABASE_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}
	
	public FeatureLookupList getSelectedDataSet() {
		return fllTable.getSelectedDataSet();
	}
}


