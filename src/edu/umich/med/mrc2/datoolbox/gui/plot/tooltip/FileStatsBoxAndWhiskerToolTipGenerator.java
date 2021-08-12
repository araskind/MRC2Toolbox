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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;

public class FileStatsBoxAndWhiskerToolTipGenerator extends BoxAndWhiskerToolTipGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1259476315600683899L;
	private static final String QC_TOOL_TIP_FORMAT 
		= "<html>Data file: {0}<BR> Mean: {2}<BR> Median: {3}<BR> Min: {4}<BR> Max: {5}<BR> Q1: {6}<BR> Q3: {7} ";

	public FileStatsBoxAndWhiskerToolTipGenerator() {

		super(QC_TOOL_TIP_FORMAT, NumberFormat.getInstance());
	}

    /**
     * Creates the array of items that can be passed to the
     * {@link MessageFormat} class for creating labels.
     *
     * @param dataset  the dataset ({@code null} not permitted).
     * @param series  the series (zero-based index).
     * @param item  the item (zero-based index).
     *
     * @return The items (never {@code null}).
     */
    @Override
    protected Object[] createItemArray(CategoryDataset dataset, int series, int item) {
    	
        Object[] result = new Object[8];
        result[0] = dataset.getRowKey(series);
        Number y = dataset.getValue(series, item);
        NumberFormat formatter = getNumberFormat();
        result[1] = formatter.format(y);
        
        if (dataset instanceof BoxAndWhiskerCategoryDataset) {
        	
            BoxAndWhiskerCategoryDataset d = (BoxAndWhiskerCategoryDataset) dataset;
            result[2] = formatter.format(d.getMeanValue(series, item));
            result[3] = formatter.format(d.getMedianValue(series, item));
            result[4] = formatter.format(d.getMinRegularValue(series, item));
            result[5] = formatter.format(d.getMaxRegularValue(series, item));
            result[6] = formatter.format(d.getQ1Value(series, item));
            result[7] = formatter.format(d.getQ3Value(series, item));
        }
        return result;
    }
}
