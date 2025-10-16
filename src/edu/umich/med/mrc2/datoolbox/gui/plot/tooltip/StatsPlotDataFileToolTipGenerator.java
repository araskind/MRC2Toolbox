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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;

public class StatsPlotDataFileToolTipGenerator extends StandardCategoryToolTipGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String valueSuffix;
	
    public StatsPlotDataFileToolTipGenerator(
    		DateFormat formatter, String valueSuffix) {
		super("", formatter);
		this.valueSuffix = valueSuffix;
	}

	public StatsPlotDataFileToolTipGenerator(
			NumberFormat formatter, String valueSuffix) {
		super("", formatter);
		this.valueSuffix = valueSuffix;
	}

	@Override
    public String generateToolTip(CategoryDataset dataset, int row, int column) { 
		
        Object rowKey = dataset.getRowKey(row);
        String columnKey = dataset.getColumnKey(column).toString();
        Number value = dataset.getValue(row, column);
        StringBuilder sb = new StringBuilder(500);
        sb.append("");
        if(rowKey instanceof DataFile) {
        	
        	DataFile df = (DataFile)rowKey;
        	sb.append("<HTML><B>Data file: </B>");
        	sb.append(df.getName());
        	
        	NavigableMap<ExperimentDesignFactor, ExperimentDesignLevel> desCell = null;
        	if(df.getParentSample() != null) {
        		desCell = df.getParentSample().getDesignCell();
        		sb.append("<BR><B>Sample: </B>");
        		sb.append(df.getParentSample().getName());
        		sb.append(" (");
        		sb.append(df.getParentSample().getId());
        		sb.append(")");
        	}
        	if(desCell != null && !desCell.isEmpty()) {
        		
        		for(Entry<ExperimentDesignFactor, ExperimentDesignLevel>e : desCell.entrySet()) {
        			
        			sb.append("<BR><B>");
        			sb.append(e.getKey().getName());
        			sb.append(": </B>");
        			sb.append(e.getValue().getName());
        		}
        		sb.append("<BR>");
        	}      	
        }
        sb.append("<B>Series: </B>");
        sb.append(columnKey);
        sb.append("<BR>");
        sb.append("<B>Value: </B>");
        sb.append(getNumberFormat().format(value));
        if(valueSuffix != null) {
        	sb.append(" ");
        	sb.append(valueSuffix);
        }
    	return sb.toString();
    }
}
