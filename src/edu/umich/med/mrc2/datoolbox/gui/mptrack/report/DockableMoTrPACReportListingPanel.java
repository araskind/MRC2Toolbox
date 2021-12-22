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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.database.idt.DocumentUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.UserUtils;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDbUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.IdTrackerPasswordActionUnlockDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.MoTrPACDataTrackingPanel;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.tables.XTableColumnModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableMoTrPACReportListingPanel extends DefaultSingleCDockable 
		implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("acqMethod", 16);
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.DockableMoTrPACReportListingPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private MoTrPACReportTable reportsTable;
	private ReportTableToolbar toolbar;
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private MotrpacReportUploadDialog motrpacReportUploadDialog;
	private MoTrPACDataTrackingPanel parentPanel;
	private IdTrackerPasswordActionUnlockDialog confirmActionDialog;
	
	public DockableMoTrPACReportListingPanel(MoTrPACDataTrackingPanel parentPanel) {

		super("DockableMoTrPACReportListingPanel", componentIcon, "Reports", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		this.parentPanel = parentPanel;
		
		toolbar = new ReportTableToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		reportsTable =  new MoTrPACReportTable();
		reportsTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						Point p = e.getPoint();
						XTableColumnModel cModel = (XTableColumnModel) reportsTable.getColumnModel();
						if (cModel.isColumnVisible(cModel.getColumnById(MoTrPACReportTableModel.FILE_DOWNLOAD_COLUMN))
								&& reportsTable.columnAtPoint(p) == cModel.getColumnIndex(MoTrPACReportTableModel.FILE_DOWNLOAD_COLUMN)
								&& e.getClickCount() == 1) {

							MoTrPACReport report = reportsTable.getSelectedReport();
							if (report == null || report.getLinkedDocumentId() == null)
								return;

							downloadReport(report);
						}
					}
				});
		JScrollPane designScrollPane = new JScrollPane(reportsTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		loadPreferences();
		initChooser();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		try {
			chooser.setCurrentDirectory(baseDirectory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
	
	public void loadReports(Collection<MoTrPACReport>reports) {
		reportsTable.setTableModelFromReports(reports);
	}

	private void downloadReport(MoTrPACReport report) {
		
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setDialogTitle("Save report file \"" + report.getLinkedDocumentName() + "\" to local drive");
		chooser.setSelectedFile(null);
		if(chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION){
			
			File destination = chooser.getSelectedFile();
			baseDirectory = destination;
			try {
				DocumentUtils.saveDocumentToFile(report.getLinkedDocumentId(), destination);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			savePreferences();
		}
	}

	public synchronized void clearPanel() {
		reportsTable.clearTable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (MRC2ToolBoxCore.getIdTrackerUser() == null)
			return;
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND.getName()))
			showReportUploader();	
		
		if(command.equals(MainActionCommands.UPLOAD_MOTRPAC_REPORT_COMMAND.getName()))
			uploadReport();
		
		if(command.equals(MainActionCommands.EDIT_MOTRPAC_REPORT_METADATA_COMMAND.getName()))
			editReportMetadata();
		
		if(command.equals(MainActionCommands.SAVE_MOTRPAC_REPORT_METADATA_COMMAND.getName()))
			saveReportMetadata();
			
		if(command.equals(MainActionCommands.CONFIRM_DELETE_MOTRPAC_REPORT_COMMAND.getName()))
			deleteReport();
		
		if(command.equals(MainActionCommands.VERIFY_TRACKER_PASSWORD_COMMAND.getName()))
			verifyPasswordAndPerformAction();
	}
	
	private void editReportMetadata() {
		
		MoTrPACReport report = reportsTable.getSelectedReport();
		if(report == null)
			return;
			
		motrpacReportUploadDialog = new MotrpacReportUploadDialog(this);
		motrpacReportUploadDialog.loadReportData(report);
		motrpacReportUploadDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		motrpacReportUploadDialog.setVisible(true);
	}
	
	private void saveReportMetadata() {
		
		MoTrPACReport report = reportsTable.getSelectedReport();
		if(report == null)
			return;
		
		Collection<String>errors = motrpacReportUploadDialog.validateReportData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), motrpacReportUploadDialog);
			return;
		}
		report.setStudy(motrpacReportUploadDialog.getMotrpacStudy());
		report.setExperiment(motrpacReportUploadDialog.getLIMSExperiment());
		report.setAssay(motrpacReportUploadDialog.getMoTrPACAssay());
		report.setTissueCode(motrpacReportUploadDialog.getMoTrPACTissueCode());
		
		int versionNumber = calculateReportVersion();
		report.setVersionNumber(versionNumber);
		for(Entry<MoTrPACReportCodeBlock, MoTrPACReportCode> stage : 
			motrpacReportUploadDialog.getReportStageDefinition().entrySet())
			report.setReportStageBlock(stage.getKey(), stage.getValue());
		
		try {
			MoTrPACDbUtils.updateMotrpacReportMetadata(report);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		MoTrPACDatabaseCash.refreshReportList();
		parentPanel.showReportListing();
		motrpacReportUploadDialog.dispose();
	}
	
	private void deleteReport() {
		
		if(reportsTable.getSelectedReport() == null)
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete selected report?", this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		confirmActionDialog = 
				new IdTrackerPasswordActionUnlockDialog(this, 
						MainActionCommands.DELETE_MOTRPAC_REPORT_COMMAND.getName());
		confirmActionDialog.setUser(MRC2ToolBoxCore.getIdTrackerUser());
		confirmActionDialog.setLocationRelativeTo(this.getContentPane());
		confirmActionDialog.setVisible(true);
	}
	
	private void verifyPasswordAndPerformAction() {
		
		MoTrPACReport report = reportsTable.getSelectedReport();
		if(report == null)
			return;
		
		LIMSUser user = null;	
		try {
			user = UserUtils.getUserLogon(
					MRC2ToolBoxCore.getIdTrackerUser().getUserName(), 
					confirmActionDialog.getPassword());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(user == null) {
			MessageDialog.showErrorMsg("Password incorrect!", confirmActionDialog);
			return;
		}
		String actionToConfirm = confirmActionDialog.getActionCommand2confirm();
		if(actionToConfirm.equals(MainActionCommands.DELETE_MOTRPAC_REPORT_COMMAND.getName())) {
			try {
				MoTrPACDbUtils.deleteMoTrPACReport(report);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			MoTrPACDatabaseCash.refreshReportList();
			parentPanel.showReportListing();
		}
		confirmActionDialog.dispose();
	}
	
	private void uploadReport() {
		
		File reportFile = motrpacReportUploadDialog.getReportFile();
		if(checkForExistingReport(reportFile))
			return;
		
		Collection<String>errors = motrpacReportUploadDialog.validateReportData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), motrpacReportUploadDialog);
			return;
		}
		int versionNumber = calculateReportVersion();			
		try {
			MoTrPACDbUtils.insertNewMotrpacReport(
					reportFile, 
					versionNumber,
					motrpacReportUploadDialog.getMotrpacStudy(),
					motrpacReportUploadDialog.getLIMSExperiment(),
					motrpacReportUploadDialog.getMoTrPACAssay(),
					motrpacReportUploadDialog.getMoTrPACTissueCode(),
					motrpacReportUploadDialog.getReportStageDefinition(),
					MRC2ToolBoxCore.getIdTrackerUser());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MoTrPACDatabaseCash.refreshReportList();
		parentPanel.showReportListing();
		motrpacReportUploadDialog.dispose();
	}
	
	private int calculateReportVersion() {
		
		String reportDefinitionKey =
				motrpacReportUploadDialog.getMotrpacStudy().getId() + 
				motrpacReportUploadDialog.getLIMSExperiment().getId() + 
				motrpacReportUploadDialog.getMoTrPACAssay().getAssayId() + 
				motrpacReportUploadDialog.getMoTrPACTissueCode().getCode();
		for(Entry<MoTrPACReportCodeBlock, MoTrPACReportCode> stage : motrpacReportUploadDialog.getReportStageDefinition().entrySet()) 
			reportDefinitionKey += stage.getKey().getBlockId() + stage.getValue().getOptionName();
		
		final String rdk = reportDefinitionKey;
		List<MoTrPACReport> reportGroup = MoTrPACDatabaseCash.getMoTrPACReports().stream().
			filter(r -> r.getReportDefinitionKey().equals(rdk)).
			collect(Collectors.toList());
		
		int versionNumber = 1;
		if(reportGroup.size() > 0)
			versionNumber = reportGroup.stream().mapToInt(r -> r.getVersionNumber()).max().getAsInt() + 1;
		
		return versionNumber;
	}
	
	private boolean checkForExistingReport(File reportFile) {
		
		if(reportFile != null && reportFile.exists()) {
			String reportId = null;
			try {
				reportId = DocumentUtils.getDocumentIdByFileHash(reportFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(reportId != null) {
				String documentFileName =  null;
				try {
					documentFileName =  DocumentUtils.getDocumentFileNameIdById(reportId);
					MessageDialog.showErrorMsg("Identical file with the name \"" + 
							documentFileName + "\" is already uploaded.", motrpacReportUploadDialog);
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	private void showReportUploader() {
		
		if (MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in!", this.getContentPane());
			return;
		}		
		if(!parentPanel.isLimsDataLoaded()) {
			MessageDialog.showErrorMsg("Please refresh LIMS data first.", this.getContentPane());
			return;
		}
		motrpacReportUploadDialog = new MotrpacReportUploadDialog(this);
		motrpacReportUploadDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		motrpacReportUploadDialog.setVisible(true);
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
