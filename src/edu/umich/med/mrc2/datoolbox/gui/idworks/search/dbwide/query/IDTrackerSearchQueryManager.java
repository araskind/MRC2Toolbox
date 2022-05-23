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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.IDTSearchQuery;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTSearchQueryUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class IDTrackerSearchQueryManager extends JDialog implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8863863842858961944L;
	private static final Icon dialogIcon = GuiUtils.getIcon("findList", 32);
	private IDTrackerSearchQueryManagerToolbar toolbar;
	private IDTrackerSearchQueryListingTable idTrackerSearchQueryListingTable;

	public IDTrackerSearchQueryManager(ActionListener listener) {
		
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("IDTracker search query manager");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		
		setSize(new Dimension(640, 480));
		setPreferredSize(new Dimension(640, 480));
		
		toolbar = new IDTrackerSearchQueryManagerToolbar(this);	
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		idTrackerSearchQueryListingTable = new IDTrackerSearchQueryListingTable();
		Collection<IDTSearchQuery>queryList = new ArrayList<IDTSearchQuery>();
		try {
			queryList = IDTSearchQueryUtils.getSearchQueryList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		idTrackerSearchQueryListingTable.setTableModelFromQueryList(queryList);
		idTrackerSearchQueryListingTable.addMouseListener(

		        new MouseAdapter(){

		          public void mouseClicked(MouseEvent e){

		            if (e.getClickCount() == 2) {
		            	IDTSearchQuery query = idTrackerSearchQueryListingTable.getSelectedQuery();
		            	if(query != null) {
		            		((IDTrackerDataSearchDialog)listener).loadQueryParameters(query);
		            		dispose();
		            	}
		            }
		          }
	        });
		JScrollPane scroll = new JScrollPane(idTrackerSearchQueryListingTable);
	
		getContentPane().add(scroll, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		btnCancel.addActionListener(al);

		JButton searchButton = new JButton("Load selected query into search form");
		searchButton.addActionListener(listener);
		searchButton.setActionCommand(MainActionCommands.LOAD_ID_TRACKER_SAVED_QUERY_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ID_TRACKER_DELETE_QUERY_COMMAND.getName()))
			deleteSelectedQuery();
	}

	private void deleteSelectedQuery() {

		IDTSearchQuery query = getSelectedQuery();
		if(query == null)
			return;
		
		if(!query.getAuthor().equals(MRC2ToolBoxCore.getIdTrackerUser())) {
			
			MessageDialog.showErrorMsg("You can not delete queries created by other users!", this);
			return;
		}
		if(MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete selected query?", this) == JOptionPane.NO_OPTION)
			return;
		
		try {
			IDTSearchQueryUtils.deleteQuery(query);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		idTrackerSearchQueryListingTable.removeQuery(query);
	}
	
	public IDTSearchQuery getSelectedQuery() {
		return idTrackerSearchQueryListingTable.getSelectedQuery();
	}

}
