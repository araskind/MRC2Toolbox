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

package edu.umich.med.mrc2.datoolbox.gui.plot.dataset;

import org.jfree.data.xy.DefaultXYZDataset;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class CorrelationMapDataSet extends DefaultXYZDataset implements IHeatMapDataSet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2535216261141143315L;
	private Matrix corrMatrix;
	private MsFeatureCluster cluster;
	private MsFeature[][] labels;
	private String[]rowLabels;
	private String[]columnLabels;

	public CorrelationMapDataSet(MsFeatureCluster cluster) {

		super();
		this.cluster = cluster;
		this.corrMatrix = cluster.getCorrelationMatrix();
		addCorrelationData();
	}

	private void addCorrelationData() {		

		if (corrMatrix == null)
			throw new IllegalArgumentException("The 'data' is null.");

		double[] xvalues = new double[(int) corrMatrix.getRowCount() * (int) corrMatrix.getColumnCount()];
		double[] yvalues = new double[(int) corrMatrix.getRowCount() * (int) corrMatrix.getColumnCount()];
		double[] zvalues = new double[(int) corrMatrix.getRowCount() * (int) corrMatrix.getColumnCount()];
		labels = new MsFeature[(int) corrMatrix.getRowCount() * (int) corrMatrix.getColumnCount()][2];
		rowLabels = new String[(int) corrMatrix.getRowCount()];
		columnLabels = new String[(int) corrMatrix.getColumnCount()];
		
		int count = 0;

		for (long i = 0; i < corrMatrix.getRowCount(); i++) {

			for (long j = 0; j < corrMatrix.getColumnCount(); j++) {

				xvalues[count] = (double) i;
				yvalues[count] = (double) j;
				zvalues[count] = corrMatrix.getAsDouble(new long[] { i, j });
				labels[count][0] = (MsFeature) corrMatrix.getDimensionMetaData(0, new long[] { 0, i });
				labels[count][1] = (MsFeature) corrMatrix.getDimensionMetaData(1, new long[] { j, 0 });			
				count++;
			}
			rowLabels[(int)i] = corrMatrix.getRowLabel(i);
		}
		for (long j = 0; j < corrMatrix.getColumnCount(); j++)
			columnLabels[(int)j] = corrMatrix.getColumnLabel(j);
			
		double[][] data = new double[][] { xvalues, yvalues, zvalues };
		super.addSeries(cluster.toString(), data);
	}

	public String[] getRowLabels() {
		return rowLabels;
	}
	
	public String[] getColumnLabels() {
		return columnLabels;
	}

	public int getFeatureNumber() {
		return (int) corrMatrix.getColumnCount();
	}

	public String getItemLabel(int index) {

		String label = "";

		if (corrMatrix.getColumnLabel(index) != null)
			label = corrMatrix.getColumnLabel(index);

		return label;
	}

	public MsFeature[][] getLabels() {
		return labels;
	}
	
	public Range getDataRange() {
		return new Range(corrMatrix.getMinValue(), corrMatrix.getMaxValue());
	}

	@Override
	public void setDataScale(DataScale newScale) {
		// TODO Auto-generated method stub
		
	}
}

















