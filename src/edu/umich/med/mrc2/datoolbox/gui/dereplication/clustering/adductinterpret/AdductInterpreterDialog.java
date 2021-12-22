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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.CorrelationResultsPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GlassPane;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.AdductAssignmentTask;

public class AdductInterpreterDialog extends JDialog implements ActionListener, TaskListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -7779359509116726189L;
	public static final String ADD_REPEAT_COMMAND = "Add repetitive unit (adduct/loss)";
	public static final String ADD_EXCHANGE_COMMAND = "Add group exchange";
	public static final String RECALCULATE_COMMAND = "Assign annotations";
	public static final String ACCEPT_RESULTS_COMMAND = "Accept annotations and close the dialog";
	public static final String CLEAR_MODS_COMMAND = "Clear annotations";
	public static final String DELETE_MOD_COMMAND = "Delete modification selector";

	private AdductInterpreterToolbar toolbar;
	private JSplitPane splitPane;
	private boolean painted;
	private JPanel modsPanel;
	private MsFeatureCluster currentCluster;
	private AdductInterpreterTable adductInterpreterTable;
	private JScrollPane tableScrollPane;
	private JPanel panel;
	private JLabel lblNewLabel;
	@SuppressWarnings("rawtypes")
	private JComboBox adductComboBox;

	private CorrelationResultsPanel parentPanel;
	private JLabel lblNewLabel_1;
	private JFormattedTextField massErrorTextField;
	private JPanel panel_1;
	private JCheckBox generateAdductsCheckBox;
	private JLabel lblMaxCharge;
	private JSpinner chargeSpinner;
	private JLabel lblMaxOligomer;
	private JSpinner oligomerSpinner;
	private MsFeature selectedFeature;
	private Adduct selectedModification;
	private Component defaultGlassPane;

	public AdductInterpreterDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Interpret modifications for feature cluster");
		painted = false;

		setModalityType(ModalityType.MODELESS);
		setSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		defaultGlassPane = this.getGlassPane();

		toolbar = new AdductInterpreterToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		splitPane = new JSplitPane();
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		modsPanel = new JPanel();
		modsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		modsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		splitPane.setRightComponent(modsPanel);
		modsPanel.setLayout(new BoxLayout(modsPanel, BoxLayout.Y_AXIS));

		panel = new JPanel();
		modsPanel.add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 72, 300, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 26, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblNewLabel = new JLabel("Select adduct: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);

		adductComboBox = new JComboBox<Adduct>();
		adductComboBox.setPreferredSize(new Dimension(300, 26));
		adductComboBox.setMinimumSize(new Dimension(300, 26));
		GridBagConstraints gbc_adductComboBox = new GridBagConstraints();
		gbc_adductComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_adductComboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_adductComboBox.gridx = 1;
		gbc_adductComboBox.gridy = 0;
		panel.add(adductComboBox, gbc_adductComboBox);

		lblNewLabel_1 = new JLabel("Mass error for annotation, ppm");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		massErrorTextField = new JFormattedTextField();
		massErrorTextField.setMinimumSize(new Dimension(70, 20));
		massErrorTextField.setPreferredSize(new Dimension(70, 20));
		GridBagConstraints gbc_massErrorTextField = new GridBagConstraints();
		gbc_massErrorTextField.insets = new Insets(0, 0, 5, 0);
		gbc_massErrorTextField.anchor = GridBagConstraints.WEST;
		gbc_massErrorTextField.gridx = 3;
		gbc_massErrorTextField.gridy = 0;
		panel.add(massErrorTextField, gbc_massErrorTextField);

		panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.setBorder(
				new TitledBorder(null, "Auto-generate adducts", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 4;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);

		generateAdductsCheckBox = new JCheckBox("Generate adducts");
		panel_1.add(generateAdductsCheckBox);

		lblMaxCharge = new JLabel("Max charge");
		panel_1.add(lblMaxCharge);

		chargeSpinner = new JSpinner();
		chargeSpinner.setToolTipText("Maximum absolute charge");
		chargeSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		panel_1.add(chargeSpinner);

		lblMaxOligomer = new JLabel("Max oligomer");
		panel_1.add(lblMaxOligomer);

		oligomerSpinner = new JSpinner();
		oligomerSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		panel_1.add(oligomerSpinner);

		tableScrollPane = new JScrollPane();
		adductInterpreterTable = new AdductInterpreterTable();

		tableScrollPane.add(adductInterpreterTable);
		tableScrollPane.setViewportView(adductInterpreterTable);
		tableScrollPane.setPreferredSize(adductInterpreterTable.getPreferredScrollableViewportSize());
		//	tableScrollPane.addComponentListener(new ResizeTableAdjuster());
		splitPane.setLeftComponent(tableScrollPane);

		populateAdductSelector();

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(toolbar);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		addWindowFocusListener(new WindowAdapter() {

			// To check window gained focus
			public void windowGainedFocus(WindowEvent e) {

				if (massErrorTextField.getText().isEmpty()) {

					double error = MRC2ToolBoxConfiguration.getMassAccuracy();
					massErrorTextField.setText(Double.toString(error));
				}
				if (parentPanel == null)
					parentPanel = (CorrelationResultsPanel) MRC2ToolBoxCore.getMainWindow()
							.getPanel(PanelList.CORRELATIONS);

				adductInterpreterTable.getSelectionModel().addListSelectionListener(parentPanel);
			}

			// To check window lost focus
			public void windowLostFocus(WindowEvent e) {

				if (parentPanel == null)
					parentPanel = (CorrelationResultsPanel) MRC2ToolBoxCore.getMainWindow()
							.getPanel(PanelList.CORRELATIONS);

				adductInterpreterTable.getSelectionModel().removeListSelectionListener(parentPanel);
			}
		});

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	private void acceptInterpretationResults() {

		AdductInterpreterTableModel model = 
				((AdductInterpreterTableModel) adductInterpreterTable.getModel());

		for (int i = 0; i < adductInterpreterTable.getRowCount(); i++) {

			int fcolumn = adductInterpreterTable.getColumnIndex(
					AdductInterpreterTableModel.FEATURE_COLUMN);
			int acceptCol = adductInterpreterTable.getColumnIndex(
					AdductInterpreterTableModel.ACCEPT_CHEM_MOD_COLUMN);
			MsFeature feature = (MsFeature) model.getValueAt(i, fcolumn);

			boolean accepted = (boolean) model.getValueAt(i, acceptCol);

			if (accepted)
				feature.setDefaultChemicalModification(feature.getSuggestedModification());
			else
				feature.setDefaultChemicalModification(null);
		}
		adductInterpreterTable.setTableModelFromFeatureCluster(currentCluster);
		parentPanel.refreshClusterData();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(ADD_REPEAT_COMMAND))
			addRepeatUnitBlock();

		if (command.equals(ADD_EXCHANGE_COMMAND))
			addExchangeUnitBlock();

		if (command.equals(RECALCULATE_COMMAND))
			createInterpretation();

		if (command.equals(ACCEPT_RESULTS_COMMAND))
			acceptInterpretationResults();

		if (command.equals(CLEAR_MODS_COMMAND))
			clearInterpretationResults();

		if (command.equals(DELETE_MOD_COMMAND))
			deleteModificationSelector(event);
	}

	private void addExchangeUnitBlock() {
		// TODO Auto-generated method stub

	}

	private void addRepeatUnitBlock() {

		ModificationSelectionPanel repeatPanel = 
				new ModificationSelectionPanel(ModificationType.REPEAT, this);
		modsPanel.add(repeatPanel);
		modsPanel.revalidate();
		modsPanel.repaint();
	}

	private void clearInterpretationResults() {

		currentCluster.setAnnotationMap(null);

		for (MsFeature f : currentCluster.getFeatures())
			f.setDefaultChemicalModification(null);

		adductInterpreterTable.setTableModelFromFeatureCluster(currentCluster);
		parentPanel.refreshClusterData();
	}

	public synchronized void clearPanel() {

		adductInterpreterTable.clearTable();
		adductComboBox.setSelectedIndex(-1);
	}

	private void createInterpretation() {

		if (adductInterpreterTable.getSelectedRow() == -1) {

			MessageDialog.showErrorMsg("Please select the feature in the table!");
			return;
		}
		if (adductComboBox.getSelectedIndex() == -1) {

			MessageDialog.showErrorMsg("Please select an adduct from dropdown!");
			return;
		}
		AdductInterpreterTableModel model = 
				((AdductInterpreterTableModel) adductInterpreterTable.getModel());

		int column = model.getColumnIndex(
				AdductInterpreterTableModel.FEATURE_COLUMN);
		int row = adductInterpreterTable.convertRowIndexToModel(
				adductInterpreterTable.getSelectedRow());

		selectedFeature = (MsFeature) model.getValueAt(row, column);
		selectedModification = (Adduct) adductComboBox.getSelectedItem();

		double masError = Double.valueOf(massErrorTextField.getText());

		AdductAssignmentTask ast = new AdductAssignmentTask(
						currentCluster,
						selectedFeature,
						selectedModification,
						masError);

		if (generateAdductsCheckBox.isSelected()) {

			int maxCharge = (int) chargeSpinner.getValue();
			int maxOligomer = (int) oligomerSpinner.getValue();
			ast = new AdductAssignmentTask(
					currentCluster,
					selectedFeature,
					selectedModification,
					masError,
					true,
					maxCharge,
					maxOligomer);
		}
		ast.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(ast);
	}

	private void deleteModificationSelector(ActionEvent event) {

		Object source = event.getSource();

		if (source instanceof Component) {

			Container parent = ((Component) source).getParent();

			if (parent instanceof ModificationSelectionPanel) {

				modsPanel.remove(parent);
				modsPanel.revalidate();
				modsPanel.repaint();
			}
		}
	}

	public void highlightFeatures(Collection<MsFeature> selectedFeatures) {

		this.setGlassPane(new GlassPane());
		int row;
		int column = adductInterpreterTable.getColumnModel().
				getColumnIndex(AdductInterpreterTableModel.FEATURE_COLUMN);
		for (MsFeature f : selectedFeatures) {

			row = adductInterpreterTable.getFeatureRow(f);
			Component c = adductInterpreterTable.getCellRenderer(row, column)
					.getTableCellRendererComponent(adductInterpreterTable, f, false, false, row, column);

			c.setBackground(Color.cyan);
		}
		adductInterpreterTable.repaint();
		this.setGlassPane(defaultGlassPane);
	}

	public void loadCluster(MsFeatureCluster cluster) {

		currentCluster = cluster;
		adductInterpreterTable.setTableModelFromFeatureCluster(cluster);
		populateAdductSelector();

		selectedFeature = null;
		selectedModification = null;

		int featureColumn = adductInterpreterTable.getColumnIndex(
				AdductInterpreterTableModel.FEATURE_COLUMN);
		int adductColumn = adductInterpreterTable.getColumnIndex(
				AdductInterpreterTableModel.CHEM_MOD_COLUMN);

		for (int i = 0; i < adductInterpreterTable.getRowCount(); i++) {

			MsFeature f = (MsFeature) adductInterpreterTable.getValueAt(i, featureColumn);

			if (f.equals(cluster.getPrimaryFeature())) {

				adductInterpreterTable.setRowSelectionInterval(i, i);
				selectedFeature = f;

				Adduct mod = 
						(Adduct) adductInterpreterTable.getValueAt(i, adductColumn);

				if (mod != null) {

					@SuppressWarnings("unchecked")
					DefaultComboBoxModel<Adduct> model = 
					((DefaultComboBoxModel<Adduct>) adductComboBox.getModel());
					for (int j = 0; j < model.getSize(); j++) {

						if (((Adduct) model.getElementAt(j)).getName().equals(mod.getName())) {

							adductComboBox.setSelectedIndex(j);
							selectedModification = (Adduct) model.getElementAt(j);
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (!painted) {

			painted = true;

			splitPane.setDividerLocation(0.8);
			splitPane.setResizeWeight(0.8);
		}
	}

	@SuppressWarnings("unchecked")
	private void populateAdductSelector() {

		if (MRC2ToolBoxCore.getCurrentProject() == null ||
				MRC2ToolBoxCore.getCurrentProject().getActiveDataPipeline() == null) 			
			return;

		ArrayList<Adduct> modeAdducts = new ArrayList<Adduct>();
		Polarity pol = MRC2ToolBoxCore.getCurrentProject().
				getActiveDataPipeline().getAcquisitionMethod().getPolarity();

		for (Adduct cm : AdductManager.getAdductsForType(ModificationType.ADDUCT)) {

			if (cm.getPolarity().equals(pol) && cm.isEnabled())
				modeAdducts.add(cm);
		}
		SortedComboBoxModel<Adduct> adductSelectorModel = 
				new SortedComboBoxModel<Adduct>(
				modeAdducts.toArray(new Adduct[modeAdducts.size()]));
		adductComboBox.setModel(adductSelectorModel);
		adductComboBox.setSelectedIndex(-1);		
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(AdductAssignmentTask.class)) {

				AdductAssignmentTask task = (AdductAssignmentTask) e.getSource();
				currentCluster = task.getClusterSet().iterator().next();

				adductInterpreterTable.setTableModelFromFeatureCluster(
						currentCluster,
						selectedFeature,
						selectedModification);
			}
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}
}


































