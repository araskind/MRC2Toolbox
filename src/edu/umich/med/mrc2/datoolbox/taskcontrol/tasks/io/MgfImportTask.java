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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MgfImportTask extends AbstractTask {

	private final static String BEGIN_BLOCK = "BEGIN IONS";
	private final static String PEPMASS = "PEPMASS";
	private final static String CHARGE = "CHARGE";
	private final static String TITLE = "TITLE";
	private final static String RTINSECONDS = "RTINSECONDS";
	private final static String END_BLOCK = "END IONS";

	private File mgfFile;
	private ArrayList<SimpleMsMs> mgfFeatures;
	private ArrayList<MsMsCluster> featureClusters;

	private double rtWindow;
	private double massErrorPpm;
	private Range rtRange, massRange;

	public MgfImportTask(File mgfFile) {

		this.mgfFile = mgfFile;
		total = 0;
		processed = 0;
		mgfFeatures = new ArrayList<SimpleMsMs>();
		featureClusters = new ArrayList<MsMsCluster>();

		rtWindow = MRC2ToolBoxConfiguration.getRtWindow();
		massErrorPpm = MRC2ToolBoxConfiguration.getMassAccuracy();
		
		taskDescription = "Importing data from " + mgfFile.getName();
	}

	private boolean belongsToCluster(SimpleMsMs msms, MsMsCluster cluster) {

		boolean belongs = false;

		rtRange = new Range(cluster.getRt() - rtWindow, cluster.getRt() + rtWindow);
		massRange = MsUtils.createMassRange(cluster.getParentMass(), massErrorPpm);

		if (rtRange.contains(msms.getRetention()) && massRange.contains(msms.getParentMass().getMz()))
			belongs = true;

		return belongs;
	}

	@Override
	public Task cloneTask() {

		return new MgfImportTask(mgfFile);
	}

	private void createFeatureClusters() {

		processed = 0;
		total = mgfFeatures.size();

		for (SimpleMsMs msms : mgfFeatures) {

			if (!msms.isAssignedToCluster()) {

				for (MsMsCluster cluster : featureClusters) {

					if (belongsToCluster(msms, cluster))
						cluster.addMsMs(msms);
				}
				if (!msms.isAssignedToCluster()) {

					MsMsCluster newCluster = new MsMsCluster();
					newCluster.addMsMs(msms);
					featureClusters.add(newCluster);
				}
			}
			processed++;
		}
		processed = 0;

		for (MsMsCluster cluster : featureClusters) {

			cluster.calculateAverageMsMs(massErrorPpm);
			processed++;
		}
	}

	public MsMsCluster[] getFeatureClusterss() {

		return featureClusters.toArray(new MsMsCluster[featureClusters.size()]);
	}

	private void parseMgf() {

		String[][] mgfData = null;
		try {
			mgfData = DelimitedTextParser.parseTextFileWithEncoding(
					mgfFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mgfData == null)
			return;

		total = mgfData.length;

		boolean blockEnded = false;
		int charge = 0;
		double rt = 0.0d;
		String pepMass, chargeString, rtString;
		String title = "";
		MsPoint parentMass = null;
		ArrayList<MsPoint> msms = new ArrayList<MsPoint>();
		SimpleMsMs msmsFeature;

		for (int i = 1; i < mgfData.length; i++) {

			if (mgfData[i][0].trim().equals(END_BLOCK)) {

				blockEnded = true;

				// Add new MSMS
				if (msms.size() > 0) {

					msmsFeature = new SimpleMsMs(parentMass, msms, rt, charge);
					msmsFeature.setTitle(title);
					mgfFeatures.add(msmsFeature);
				}
				msms = new ArrayList<MsPoint>();
			}
			if (mgfData[i][0].trim().equals(BEGIN_BLOCK))
				blockEnded = false;

			if (!blockEnded && !mgfData[i][0].trim().equals(BEGIN_BLOCK)) {

				if (mgfData[i][0].trim().startsWith(PEPMASS)) {

					pepMass = mgfData[i][0].trim().replace(PEPMASS + "=", "");
					parentMass = new MsPoint(Double.valueOf(pepMass), 100.0d);
				} else if (mgfData[i][0].trim().startsWith(CHARGE)) {

					chargeString = mgfData[i][0].trim().replace(CHARGE + "=", "");

					if (chargeString.charAt(1) == '+')
						charge = Integer.valueOf(chargeString.charAt(0));

					if (chargeString.charAt(1) == '-')
						charge = Integer.valueOf("-" + chargeString.charAt(0));
				} else if (mgfData[i][0].trim().startsWith(RTINSECONDS)) {

					rtString = mgfData[i][0].trim().replace(RTINSECONDS + "=", "");
					rt = Double.valueOf(rtString) / 60;
				} else if (mgfData[i][0].trim().startsWith(TITLE)) {

					title = mgfData[i][0].trim().replace(TITLE + "=", "");
				} else {
					if (Character.isDigit(mgfData[i][0].charAt(0))) {

						msms.add(new MsPoint(Double.valueOf(mgfData[i][0].trim()),
								Double.valueOf(mgfData[i][1].trim())));
					}
				}
			}
			processed++;
		}
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		parseMgf();

		createFeatureClusters();

		setStatus(TaskStatus.FINISHED);
	}
}
