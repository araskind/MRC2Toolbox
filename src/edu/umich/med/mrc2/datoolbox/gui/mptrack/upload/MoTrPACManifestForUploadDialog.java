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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.upload;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.mp.CreateUploadManifestTask;

public class MoTrPACManifestForUploadDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8579406888629573415L;

	private static final Icon md5Icon = GuiUtils.getIcon("hashMd5", 32);	
	public static final String OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";	
	
	private Map<MoTrPACAssay,ManifestForUploadSetupPanel>assayPanelMap;
	
	public MoTrPACManifestForUploadDialog(ActionListener listener) {
		super();
		
		setSize(new Dimension(1000, 250));
		setPreferredSize(new Dimension(1000, 250));
		setIconImage(((ImageIcon) md5Icon).getImage());
		setTitle("Create manifest file (MD5) for upload ");
		setModalityType(ModalityType.APPLICATION_MODAL);		
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		assayPanelMap = new TreeMap<MoTrPACAssay,ManifestForUploadSetupPanel>();
		for(MoTrPACAssay assay : MoTrPACDatabaseCache.getMotrpacAssayList()) {
			
			ManifestForUploadSetupPanel assayPanel = new ManifestForUploadSetupPanel(assay, this);
			assayPanelMap.put(assay, assayPanel);
			String tabName = assay.getDescription() + " (" + assay.getCode() + ")";
			tabbedPane.addTab(tabName, assayPanel);
		}	
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.CREATE_MANIFEST_FOR_BIC_UPLOAD_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.CREATE_MANIFEST_FOR_BIC_UPLOAD_COMMAND.getName());
		btnSave.addActionListener(listener);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}
	
	public Collection<CreateUploadManifestTask>getManifestTasks(){
		
		Collection<CreateUploadManifestTask>tasks = 
				new ArrayList<CreateUploadManifestTask>();
			
		for(ManifestForUploadSetupPanel panel : assayPanelMap.values()) {
			
			CreateUploadManifestTask task = panel.getManifestTask();				
			if(task != null)
				tasks.add(task);
		}		
		return tasks;
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

	}
}


