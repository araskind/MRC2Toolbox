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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.summary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.MinimalMSOneFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MSMSCLusterDataSetSummaryDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2451814964627936355L;
	private static final Icon summaryIcon = GuiUtils.getIcon("summary", 32);
	
	private static final Icon foundIcon = GuiUtils.getIcon("level", 24);
	private static final Icon notFoundIcon = GuiUtils.getIcon("levelInactive", 24);
	
	private MSMSClusterDataSet dataSet;
	
	private JTextField clusterSetNameTextField;
	private JTextArea clusterSetDescriptionTextArea;
	private JLabel dateCreatedLabel;
	private JLabel numClustersLabel;
	private JLabel numFeaturesLabel;
	private JTextField flSetNameTextField;
	private JTextArea flSetDescriptionTextArea;
	private JLabel flSetDateCreatedLabel;
	private JLabel flSetLastModifiedLabel;
	private JLabel flSetCreatedByLabel;	
	private FoundLookupFeaturesTable foundLookupFeaturesTable;
	private MinimalMSOneFeatureTable notFoundFeaturesTable;
	private JLabel dataSetCreatedByLabel;
	private JLabel lastModifedLabel;
	private JLabel numFoundLookupFesturesLabel;
	private JLabel numNotFoundLookupFesturesLabel;

	public MSMSCLusterDataSetSummaryDialog(MSMSClusterDataSet dataSet) {
		super();
		
		this.dataSet = dataSet;
		
		setSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(800, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		setTitle("Summary for \"" + dataSet.getName() +"\" data set");
		setIconImage(((ImageIcon) summaryIcon).getImage());
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel_1.add(tabbedPane, BorderLayout.CENTER);
		
		foundLookupFeaturesTable = new FoundLookupFeaturesTable();
		tabbedPane.addTab("Found lookup features", foundIcon, 
				new JScrollPane(foundLookupFeaturesTable), null);

		notFoundFeaturesTable = new MinimalMSOneFeatureTable();
		tabbedPane.addTab("NOT found lookup features", notFoundIcon, 
				new JScrollPane(notFoundFeaturesTable), null);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Data set details", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(5, 5, 5, 5))));
		panel_1.add(panel_4, BorderLayout.NORTH);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		JLabel lblNewLabel = new JLabel("Name: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_4.add(lblNewLabel, gbc_lblNewLabel);
		
		clusterSetNameTextField = new JTextField();
		clusterSetNameTextField.setEditable(false);
		GridBagConstraints gbc_clusterSetNameTextField = new GridBagConstraints();
		gbc_clusterSetNameTextField.gridwidth = 5;
		gbc_clusterSetNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_clusterSetNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_clusterSetNameTextField.gridx = 1;
		gbc_clusterSetNameTextField.gridy = 0;
		panel_4.add(clusterSetNameTextField, gbc_clusterSetNameTextField);
		clusterSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Description: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel_4.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		clusterSetDescriptionTextArea = new JTextArea();
		clusterSetDescriptionTextArea.setEditable(false);
		clusterSetDescriptionTextArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		clusterSetDescriptionTextArea.setRows(2);
		clusterSetDescriptionTextArea.setWrapStyleWord(true);
		clusterSetDescriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_clusterSetDescriptionTextArea = new GridBagConstraints();
		gbc_clusterSetDescriptionTextArea.gridwidth = 6;
		gbc_clusterSetDescriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_clusterSetDescriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_clusterSetDescriptionTextArea.gridx = 0;
		gbc_clusterSetDescriptionTextArea.gridy = 2;
		panel_4.add(clusterSetDescriptionTextArea, gbc_clusterSetDescriptionTextArea);
		
		JLabel lblNewLabel_1 = new JLabel("Created: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 3;
		panel_4.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		dateCreatedLabel = new JLabel("");
		dateCreatedLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.anchor = GridBagConstraints.WEST;
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_dateCreatedLabel.gridx = 1;
		gbc_dateCreatedLabel.gridy = 3;
		panel_4.add(dateCreatedLabel, gbc_dateCreatedLabel);
		
		JLabel lblNewLabel_3 = new JLabel("Last modified: ");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 3;
		panel_4.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		lastModifedLabel = new JLabel("");
		lastModifedLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lastModifedLabel = new GridBagConstraints();
		gbc_lastModifedLabel.anchor = GridBagConstraints.WEST;
		gbc_lastModifedLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lastModifedLabel.gridx = 3;
		gbc_lastModifedLabel.gridy = 3;
		panel_4.add(lastModifedLabel, gbc_lastModifedLabel);
		
		JLabel lblNewLabel_5 = new JLabel("Created by: ");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 4;
		gbc_lblNewLabel_5.gridy = 3;
		panel_4.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		dataSetCreatedByLabel = new JLabel("");
		GridBagConstraints gbc_dataSetCreatedByLabel = new GridBagConstraints();
		gbc_dataSetCreatedByLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dataSetCreatedByLabel.anchor = GridBagConstraints.WEST;
		gbc_dataSetCreatedByLabel.gridx = 5;
		gbc_dataSetCreatedByLabel.gridy = 3;
		panel_4.add(dataSetCreatedByLabel, gbc_dataSetCreatedByLabel);
		
		JLabel lblNewLabel_7 = new JLabel("# of clusters: ");
		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_7.gridx = 0;
		gbc_lblNewLabel_7.gridy = 4;
		panel_4.add(lblNewLabel_7, gbc_lblNewLabel_7);
		
		numClustersLabel = new JLabel("");
		numClustersLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numClustersLabel = new GridBagConstraints();
		gbc_numClustersLabel.anchor = GridBagConstraints.WEST;
		gbc_numClustersLabel.insets = new Insets(0, 0, 5, 5);
		gbc_numClustersLabel.gridx = 1;
		gbc_numClustersLabel.gridy = 4;
		panel_4.add(numClustersLabel, gbc_numClustersLabel);
		
		JLabel lblNewLabel_8 = new JLabel("# of features: ");
		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
		gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_8.gridx = 2;
		gbc_lblNewLabel_8.gridy = 4;
		panel_4.add(lblNewLabel_8, gbc_lblNewLabel_8);
		
		numFeaturesLabel = new JLabel("");
		numFeaturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numFeaturesLabel = new GridBagConstraints();
		gbc_numFeaturesLabel.anchor = GridBagConstraints.WEST;
		gbc_numFeaturesLabel.insets = new Insets(0, 0, 5, 5);
		gbc_numFeaturesLabel.gridx = 3;
		gbc_numFeaturesLabel.gridy = 4;
		panel_4.add(numFeaturesLabel, gbc_numFeaturesLabel);
		
		JLabel lblNewLabel_4 = new JLabel("#Found lookup features: ");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 5;
		panel_4.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		numFoundLookupFesturesLabel = new JLabel("");
		numFoundLookupFesturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numFoundLookupFesturesLabel = new GridBagConstraints();
		gbc_numFoundLookupFesturesLabel.anchor = GridBagConstraints.WEST;
		gbc_numFoundLookupFesturesLabel.insets = new Insets(0, 0, 0, 5);
		gbc_numFoundLookupFesturesLabel.gridx = 1;
		gbc_numFoundLookupFesturesLabel.gridy = 5;
		panel_4.add(numFoundLookupFesturesLabel, gbc_numFoundLookupFesturesLabel);
		
		JLabel lblNewLabel_4_1 = new JLabel("#Found lookup features: ");
		GridBagConstraints gbc_lblNewLabel_4_1 = new GridBagConstraints();
		gbc_lblNewLabel_4_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4_1.gridx = 2;
		gbc_lblNewLabel_4_1.gridy = 5;
		panel_4.add(lblNewLabel_4_1, gbc_lblNewLabel_4_1);
		
		numNotFoundLookupFesturesLabel = new JLabel("");
		numNotFoundLookupFesturesLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_numFoundLookupFesturesLabel_1 = new GridBagConstraints();
		gbc_numFoundLookupFesturesLabel_1.anchor = GridBagConstraints.WEST;
		gbc_numFoundLookupFesturesLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_numFoundLookupFesturesLabel_1.gridx = 3;
		gbc_numFoundLookupFesturesLabel_1.gridy = 5;
		panel_4.add(numNotFoundLookupFesturesLabel, gbc_numFoundLookupFesturesLabel_1);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Feature lookup data set details", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(5, 5, 5, 5))));
		panel_1.add(panel_5, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_5.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);
		
		JLabel lblNewLabel_9 = new JLabel("Name: ");
		GridBagConstraints gbc_lblNewLabel_9 = new GridBagConstraints();
		gbc_lblNewLabel_9.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_9.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_9.gridx = 0;
		gbc_lblNewLabel_9.gridy = 0;
		panel_5.add(lblNewLabel_9, gbc_lblNewLabel_9);
		
		flSetNameTextField = new JTextField();
		flSetNameTextField.setEditable(false);
		GridBagConstraints gbc_flSetNameTextField = new GridBagConstraints();
		gbc_flSetNameTextField.gridwidth = 5;
		gbc_flSetNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_flSetNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_flSetNameTextField.gridx = 1;
		gbc_flSetNameTextField.gridy = 0;
		panel_5.add(flSetNameTextField, gbc_flSetNameTextField);
		flSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_10 = new JLabel("Description: ");
		GridBagConstraints gbc_lblNewLabel_10 = new GridBagConstraints();
		gbc_lblNewLabel_10.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_10.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_10.gridx = 0;
		gbc_lblNewLabel_10.gridy = 1;
		panel_5.add(lblNewLabel_10, gbc_lblNewLabel_10);
		
		flSetDescriptionTextArea = new JTextArea();
		flSetDescriptionTextArea.setRows(2);
		flSetDescriptionTextArea.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		flSetDescriptionTextArea.setWrapStyleWord(true);
		flSetDescriptionTextArea.setLineWrap(true);
		flSetDescriptionTextArea.setEditable(false);
		GridBagConstraints gbc_flSetDescriptionTextArea = new GridBagConstraints();
		gbc_flSetDescriptionTextArea.gridwidth = 6;
		gbc_flSetDescriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_flSetDescriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_flSetDescriptionTextArea.gridx = 0;
		gbc_flSetDescriptionTextArea.gridy = 2;
		panel_5.add(flSetDescriptionTextArea, gbc_flSetDescriptionTextArea);
		
		JLabel lblNewLabel_11 = new JLabel("Created: ");
		GridBagConstraints gbc_lblNewLabel_11 = new GridBagConstraints();
		gbc_lblNewLabel_11.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_11.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_11.gridx = 0;
		gbc_lblNewLabel_11.gridy = 3;
		panel_5.add(lblNewLabel_11, gbc_lblNewLabel_11);
		
		flSetDateCreatedLabel = new JLabel("");
		flSetDateCreatedLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_flSetDateCreatedLabel = new GridBagConstraints();
		gbc_flSetDateCreatedLabel.anchor = GridBagConstraints.WEST;
		gbc_flSetDateCreatedLabel.insets = new Insets(0, 0, 0, 5);
		gbc_flSetDateCreatedLabel.gridx = 1;
		gbc_flSetDateCreatedLabel.gridy = 3;
		panel_5.add(flSetDateCreatedLabel, gbc_flSetDateCreatedLabel);
		
		JLabel lblNewLabel_12 = new JLabel("Last modified: ");
		GridBagConstraints gbc_lblNewLabel_12 = new GridBagConstraints();
		gbc_lblNewLabel_12.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_12.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_12.gridx = 2;
		gbc_lblNewLabel_12.gridy = 3;
		panel_5.add(lblNewLabel_12, gbc_lblNewLabel_12);
		
		flSetLastModifiedLabel = new JLabel("");
		flSetLastModifiedLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_flSetLastModifiedLabel = new GridBagConstraints();
		gbc_flSetLastModifiedLabel.anchor = GridBagConstraints.WEST;
		gbc_flSetLastModifiedLabel.insets = new Insets(0, 0, 0, 5);
		gbc_flSetLastModifiedLabel.gridx = 3;
		gbc_flSetLastModifiedLabel.gridy = 3;
		panel_5.add(flSetLastModifiedLabel, gbc_flSetLastModifiedLabel);
		
		JLabel lblNewLabel_13 = new JLabel("Created by: ");
		GridBagConstraints gbc_lblNewLabel_13 = new GridBagConstraints();
		gbc_lblNewLabel_13.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_13.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_13.gridx = 4;
		gbc_lblNewLabel_13.gridy = 3;
		panel_5.add(lblNewLabel_13, gbc_lblNewLabel_13);
		
		flSetCreatedByLabel = new JLabel("");
		GridBagConstraints gbc_flSetCreatedByLabel = new GridBagConstraints();
		gbc_flSetCreatedByLabel.anchor = GridBagConstraints.WEST;
		gbc_flSetCreatedByLabel.gridx = 5;
		gbc_flSetCreatedByLabel.gridy = 3;
		panel_5.add(flSetCreatedByLabel, gbc_flSetCreatedByLabel);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Close");
		panel.add(cancelButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		
		loadDataSet();
		pack();
	}
	
	private void loadDataSet() {
		
		clusterSetNameTextField.setText(dataSet.getName());
		clusterSetDescriptionTextArea.setText(dataSet.getDescription());
		dateCreatedLabel.setText(
				MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(dataSet.getDateCreated()));
		lastModifedLabel.setText(
				MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(dataSet.getLastModified()));
		dataSetCreatedByLabel.setText(dataSet.getCreatedBy().getInfo());
		
		numClustersLabel.setText(Integer.toString(dataSet.getClusters().size()));
		
		long numFeatures = dataSet.getClusters().stream().
				flatMap(c -> c.getComponents().stream()).count();
		numFeaturesLabel.setText(Long.toString(numFeatures));
		
		FeatureLookupDataSet lookupSet = dataSet.getFeatureLookupDataSet();
		if(lookupSet != null) {
			
			flSetNameTextField.setText(lookupSet.getName());
			flSetDescriptionTextArea.setText(lookupSet.getDescription());
			flSetDateCreatedLabel.setText(
					MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(lookupSet.getDateCreated()));
			flSetLastModifiedLabel.setText(
					MRC2ToolBoxConfiguration.defaultTimeStampFormat.format(lookupSet.getLastModified()));
			flSetCreatedByLabel.setText(lookupSet.getCreatedBy().getInfo());
			
			Set<MinimalMSOneFeature> foundLookupFeatures = 
					dataSet.getClusters().stream().
					filter(c -> c.getLookupFeature() != null).
					map(c -> c.getLookupFeature()).
					collect(Collectors.toSet());
			List<MinimalMSOneFeature> notFoundLookupFeatures = 
					lookupSet.getFeatures().stream().
					filter(f -> !foundLookupFeatures.contains(f)).
					sorted().collect(Collectors.toList());
			if(!notFoundLookupFeatures.isEmpty())
				notFoundFeaturesTable.setTableModelFromFeatureCollection(notFoundLookupFeatures);
			
			numFoundLookupFesturesLabel.setText(Integer.toString(foundLookupFeatures.size()));
			numNotFoundLookupFesturesLabel.setText(Integer.toString(notFoundLookupFeatures.size()));
		}
	}
}


















