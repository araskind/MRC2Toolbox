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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class MSMSFeatureExtractionParameterSelectorDialog extends JDialog implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6884865918670810119L;
	private static final Icon dbLookupIcon = GuiUtils.getIcon("dbLookup", 32);
	private MSMSFeatureExtractionParametersTable msmsFeatureExtractionParametersTable;
	private MSMSFeatureExtractionParametersPanel parametersPanel;
	
	public MSMSFeatureExtractionParameterSelectorDialog(ActionListener listener) {
		super();
		setTitle("Select MSMS feature extraction parameters from database");
		setIconImage(((ImageIcon) dbLookupIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		parametersPanel = new MSMSFeatureExtractionParametersPanel();
		parametersPanel.setEditingEnabled(false);
		JScrollPane dscroll = new JScrollPane(parametersPanel);
		getContentPane().add(dscroll, BorderLayout.EAST);
		
		msmsFeatureExtractionParametersTable = 
				new MSMSFeatureExtractionParametersTable();
		JScrollPane scroll = new JScrollPane(msmsFeatureExtractionParametersTable);
		getContentPane().add(scroll, BorderLayout.CENTER);
		msmsFeatureExtractionParametersTable.setModelFromParametersList(
				IDTDataCache.getMsmsExtractionParameters());
		msmsFeatureExtractionParametersTable.getSelectionModel().addListSelectionListener(this);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_2, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);
		panel_2.add(cancelButton);
		JButton extractButton = new JButton(
				MainActionCommands.LOAD_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND.getName());
		extractButton.setActionCommand(
				MainActionCommands.LOAD_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND.getName());
		extractButton.addActionListener(listener);
		panel_2.add(extractButton);	
		JRootPane rootPane = SwingUtilities.getRootPane(extractButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(extractButton);

		pack();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {
			
			MSMSExtractionParameterSet parSet = 
					msmsFeatureExtractionParametersTable.getSelectedMSMSExtractionParameterSet();
			if(parSet != null) {
				DataExtractionMethod deMethod = 
						IDTDataCache.getDataExtractionMethodById(parSet.getId());
				parametersPanel.loadParameters(parSet, deMethod);
			}
		}
	}
	
	public MSMSExtractionParameterSet getSelectedMSMSExtractionParameterSet(){
		return msmsFeatureExtractionParametersTable.getSelectedMSMSExtractionParameterSet();
	}
}
