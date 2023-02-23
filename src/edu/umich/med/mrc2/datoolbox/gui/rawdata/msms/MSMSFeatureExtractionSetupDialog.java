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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class MSMSFeatureExtractionSetupDialog extends JDialog  implements ActionListener, BackedByPreferences{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1021679311401746260L;
	private static final Icon extractMSMSFeaturesIcon = GuiUtils.getIcon("findMSMSFeatures", 32);
	
	
	private Preferences preferences;	
	public static final String LAST_USED_METHOD_ID = "LAST_USED_METHOD_ID";

	private MSMSFeatureExtractionParametersPanel parametersPanel;
	private DataExtractionMethod deMethod;
	private MSMSExtractionParameterSet initialParameterSet;
	
	private MSMSFeatureExtractionSetupDialogToolbar toolbar;
	private MSMSFeatureExtractionParameterSelectorDialog msmsFeatureExtractionParameterSelectorDialog;
		
	public MSMSFeatureExtractionSetupDialog(ActionListener listener) {
		super();
		setTitle("MSMS feature extraction settings");
		setIconImage(((ImageIcon) extractMSMSFeaturesIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(640, 750));
		setSize(new Dimension(640, 700));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		toolbar = new MSMSFeatureExtractionSetupDialogToolbar(this);
		toolbar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		parametersPanel = new MSMSFeatureExtractionParametersPanel();
		getContentPane().add(parametersPanel, BorderLayout.CENTER);

		//	Buttons
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
				MainActionCommands.MSMS_FEATURE_EXTRACTION_COMMAND.getName());
		extractButton.setActionCommand(
				MainActionCommands.MSMS_FEATURE_EXTRACTION_COMMAND.getName());
		extractButton.addActionListener(listener);
		panel_2.add(extractButton);	
		JRootPane rootPane = SwingUtilities.getRootPane(extractButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(extractButton);
		loadPreferences();
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SHOW_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_LIST_COMMAND.getName())) {
			showMethodSelector();
		}
		if(command.equals(MainActionCommands.LOAD_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND.getName())) {
			loadSelectedMethodFromDatabase();
		}
//		if(command.equals(MainActionCommands.SHOW_SAVE_MSMS_FEATURE_EXTRACTION_METHOD_DIALOG_COMMAND.getName())) {
//
//		}
//		if(command.equals(MainActionCommands.SAVE_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND.getName())) {
//
//		}
//		if(command.equals(MainActionCommands.DELETE_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND.getName())) {
//
//		}
	}
	
	private void loadSelectedMethodFromDatabase() {

		MSMSExtractionParameterSet parSet = 
				msmsFeatureExtractionParameterSelectorDialog.getSelectedMSMSExtractionParameterSet();
		if(parSet == null)
			return;
		
		initialParameterSet = parSet;
		deMethod = IDTDataCash.getDataExtractionMethodById(parSet.getId());
		parametersPanel.loadParameters(initialParameterSet, deMethod);		
		msmsFeatureExtractionParameterSelectorDialog.dispose();
	}

	private void showMethodSelector() {
		
		msmsFeatureExtractionParameterSelectorDialog = 
				new MSMSFeatureExtractionParameterSelectorDialog(this);
		msmsFeatureExtractionParameterSelectorDialog.setLocationRelativeTo(this);
		msmsFeatureExtractionParameterSelectorDialog.setVisible(true);
	}

	public String getParameterSetName() {
		return parametersPanel.getParameterSetName();
	}
	
	public String getDescription() {
		return parametersPanel.getDescription();
	}
		
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		
		String paramSetId = preferences.get(LAST_USED_METHOD_ID, null);
		if(paramSetId != null && !paramSetId.isEmpty()) {
			
			DataExtractionMethod cashedMethod = 
					IDTDataCash.getDataExtractionMethodById(paramSetId);
			
			if(cashedMethod != null) {
				
				initialParameterSet = IDTDataCash.getMSMSExtractionParameterSetById(cashedMethod.getId());
				
				if(initialParameterSet != null) {					
					deMethod = cashedMethod;
					parametersPanel.loadParameters(initialParameterSet, deMethod);
					return;
				}
			}
		}
		else if(!IDTDataCash.getMsmsExtractionParameters().isEmpty()) {
			initialParameterSet = IDTDataCash.getMsmsExtractionParameters().iterator().next();
			deMethod = IDTDataCash.getDataExtractionMethodById(initialParameterSet.getId());
			parametersPanel.loadParameters(initialParameterSet, deMethod);
			savePreferences();
			return;
		}
		else
			parametersPanel.loadDefaultParameters();
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());	
		if(deMethod != null)
			preferences.put(LAST_USED_METHOD_ID, deMethod.getId());
		else
			preferences.put(LAST_USED_METHOD_ID, "");
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();	
	}

	public MSMSExtractionParameterSet getMSMSExtractionParameterSet() {
		
		Collection<String>errors = parametersPanel.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return null;
		}
		MSMSExtractionParameterSet updatedPs = 
				parametersPanel.createParameterSet();
		return updatedPs;
	}
	
	public DataExtractionMethod getDataExtractionMethod() {
		return deMethod;
	}

	public MSMSExtractionParameterSet getInitialParameterSet() {
		return initialParameterSet;
	}

	public void loadParameters(
			MSMSExtractionParameterSet ps, 
			DataExtractionMethod deMethod2) {
		this.deMethod = deMethod2;
		initialParameterSet = ps;
		parametersPanel.loadParameters(ps, deMethod);
	}
}













