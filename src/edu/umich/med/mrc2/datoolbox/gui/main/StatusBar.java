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

package edu.umich.med.mrc2.datoolbox.gui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.LabeledProgressBar;

public class StatusBar extends JPanel implements 
	Runnable, MouseListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6583476429299020779L;
	// frequency in milliseconds how often to update free memory label
	public static final int MEMORY_LABEL_UPDATE_FREQUENCY = 1000;
	public static final int STATUS_BAR_HEIGHT = 20;
	public static final Font statusBarFont = new Font("SansSerif", Font.PLAIN, 12);
	private static final Icon garbageIcon = GuiUtils.getIcon("trashcan_full", 16);
	
	public static final String GC_COMMAND = "GC_COMMAND";

	private JPanel statusTextPanel, memoryPanel;
	private JLabel statusTextLabel;
	private LabeledProgressBar memoryLabel;
	private JButton gcButton;
	private static JLabel projectNameLabel, featureCollectionLabel, msmsClusterLabel;

	public StatusBar() {

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EtchedBorder());

		statusTextPanel = new JPanel();
		statusTextPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		GridBagLayout gbl_statusTextPanel = new GridBagLayout();
		gbl_statusTextPanel.columnWidths = new int[]{0, 0, 74, 0, 0, 0, 0};
		gbl_statusTextPanel.rowHeights = new int[]{14, 0, 0};
		gbl_statusTextPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 1.0};
		gbl_statusTextPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		statusTextPanel.setLayout(gbl_statusTextPanel);
		add(statusTextPanel);
		
		int fieldCount = 0;
		
		JLabel lblNewLabel = new JLabel("Project: ");
		lblNewLabel.setForeground(Color.BLUE);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = fieldCount;
		gbc_lblNewLabel.gridy = 0;
		statusTextPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		fieldCount++;
		
		projectNameLabel = new JLabel("");
		projectNameLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_projectNameLabel = new GridBagConstraints();
		gbc_projectNameLabel.anchor = GridBagConstraints.WEST;
		gbc_projectNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_projectNameLabel.gridx = fieldCount;
		gbc_projectNameLabel.gridy = 0;
		statusTextPanel.add(projectNameLabel, gbc_projectNameLabel);
		
		fieldCount++;
		
		JLabel lblFeatureCollection = new JLabel("Feature collection:");
		lblFeatureCollection.setForeground(Color.BLUE);
		lblFeatureCollection.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblFeatureCollection = new GridBagConstraints();
		gbc_lblFeatureCollection.anchor = GridBagConstraints.EAST;
		gbc_lblFeatureCollection.insets = new Insets(0, 0, 5, 5);
		gbc_lblFeatureCollection.gridx = fieldCount;
		gbc_lblFeatureCollection.gridy = 0;
		statusTextPanel.add(lblFeatureCollection, gbc_lblFeatureCollection);
		
		fieldCount++;
		
		featureCollectionLabel = new JLabel("");
		featureCollectionLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_featureCollectionLabel = new GridBagConstraints();
		gbc_featureCollectionLabel.anchor = GridBagConstraints.WEST;
		gbc_featureCollectionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_featureCollectionLabel.gridx = fieldCount;
		gbc_featureCollectionLabel.gridy = 0;
		statusTextPanel.add(featureCollectionLabel, gbc_featureCollectionLabel);
		
		fieldCount++;
		
		JLabel lblNewLabel_1 = new JLabel("MSMS cluster data set:");
		lblNewLabel_1.setForeground(Color.BLUE);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = fieldCount;
		gbc_lblNewLabel_1.gridy = 0;
		statusTextPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		fieldCount++;
		
		msmsClusterLabel = new JLabel("");
		msmsClusterLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_msmsClusterLabel = new GridBagConstraints();
		gbc_msmsClusterLabel.anchor = GridBagConstraints.WEST;
		gbc_msmsClusterLabel.insets = new Insets(0, 0, 5, 5);
		gbc_msmsClusterLabel.gridx = fieldCount;
		gbc_msmsClusterLabel.gridy = 0;
		statusTextPanel.add(msmsClusterLabel, gbc_msmsClusterLabel);
				
		memoryLabel = new LabeledProgressBar();
		memoryPanel = new JPanel();
		memoryPanel.setLayout(new BoxLayout(memoryPanel, BoxLayout.X_AXIS));
		memoryPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		memoryPanel.add(Box
				.createRigidArea(new Dimension(10, STATUS_BAR_HEIGHT)));
		memoryPanel.add(memoryLabel);
		memoryPanel.add(Box
				.createRigidArea(new Dimension(10, STATUS_BAR_HEIGHT)));

		memoryLabel.addMouseListener(this);
		
		gcButton = new JButton("");
		gcButton.setIcon(garbageIcon);
		gcButton.setToolTipText("Force garbage collection");
		gcButton.setActionCommand(GC_COMMAND);
		gcButton.addActionListener(this);
		
		statusTextLabel = new JLabel();
		add(statusTextLabel);
		statusTextLabel.setFont(statusBarFont);
		statusTextLabel.setMinimumSize(new Dimension(100, STATUS_BAR_HEIGHT));
		statusTextLabel.setPreferredSize(new Dimension(3200, STATUS_BAR_HEIGHT));
				
		gcButton.setMaximumSize(new Dimension(20, 20));
		gcButton.setMinimumSize(new Dimension(20, 20));
		gcButton.setSize(new Dimension(20, 20));
		gcButton.setPreferredSize(new Dimension(20, 20));
		add(gcButton);

		add(memoryPanel);

		Thread memoryLabelUpdaterThread = new Thread(this,
				"Memory label updater thread");
		memoryLabelUpdaterThread.start();
	}
	
	public static void setActiveFeatureCollection(MsFeatureInfoBundleCollection newCollection) {
		
		if(newCollection == null)
			featureCollectionLabel.setText("");
		else
			featureCollectionLabel.setText(newCollection.getName());
	}

	public static void setActiveMSMSClusterDataSet(MSMSClusterDataSet dataSet) {
		
		if(dataSet == null)
			msmsClusterLabel.setText("");
		else
			msmsClusterLabel.setText(msmsClusterLabel.getName());
	}
	
	public static void clearProjectData() {	
		projectNameLabel.setText("");	
		featureCollectionLabel.setText("");
		msmsClusterLabel.setText("");
	}
	
	public static void setProjectName(String projectName) {
		projectNameLabel.setText(projectName);
	}

	/**
	 * Set the text displayed in status bar
	 * 
	 * @param statusText
	 *            Text for status bar
	 * @param textColor
	 *            Text color
	 */
	public void setStatusText(String statusText, Color textColor) {
		statusTextLabel.setText(statusText);
		statusTextLabel.setForeground(textColor);
	}

	/**
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent event) {
		// do nothing

	}

	/**
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent event) {
		// do nothing

	}

	/**
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent event) {
		// do nothing

	}

	/**
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent event) {
		// do nothing

	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public synchronized void run() {

		while (true) {

			// get free memory in megabytes
			long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
			long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
			double fullMem = ((double) (totalMem - freeMem)) / totalMem;

			memoryLabel.setValue(fullMem, freeMem + "MB free");
			memoryLabel.setToolTipText("JVM memory: " + freeMem + "MB, "
					+ totalMem + "MB total");

			try {
				wait(MEMORY_LABEL_UPDATE_FREQUENCY);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public void mouseClicked(MouseEvent arg0) {
		System.gc();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(GC_COMMAND))
			System.gc();
	}

	public static void switchDataPipeline(DataAnalysisProject project, DataPipeline pipeline) {
		
		clearProjectData();	
		if(project != null) {
			projectNameLabel.setText(project.getName());
			//	TODO
		}
	}
	
	public static void showRawDataAnalysisProjectData(RawDataAnalysisProject project) {
		
		clearProjectData();	
		if(project != null) {
			projectNameLabel.setText(project.getName());
			//	TODO
		}
	}
}












