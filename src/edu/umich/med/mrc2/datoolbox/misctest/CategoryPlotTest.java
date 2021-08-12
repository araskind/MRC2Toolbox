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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;

public class CategoryPlotTest extends ApplicationFrame{

	public CategoryPlotTest(String title) {
		super(title);
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
 
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
	}
	
	private JPanel createChartPanel() {
		
		String chartTitle = "Programming Languages Trends";
	    String categoryAxisLabel = "Interest over time";
	    String valueAxisLabel = "Popularity";
	 
	    CategoryDataset dataset = createDataset();
	    JFreeChart chart = null;
	    
	    //	chart = ChartFactory.createLineChart(chartTitle, categoryAxisLabel, valueAxisLabel, dataset);	 
	    
	    chart = ChartFactory.createBarChart(chartTitle, categoryAxisLabel, valueAxisLabel, dataset);
	    
	    //chart = ChartFactory.createStackedAreaChart(chartTitle, categoryAxisLabel, valueAxisLabel, dataset);
	    
	    return new ChartPanel(chart);
	}

	private CategoryDataset createDatasetMC() {
		
		DefaultMultiValueCategoryDataset dataset = new DefaultMultiValueCategoryDataset();
		
	    String[] lang = new String[] { "Java", "PHP", "C++", "C#"};
	    String [] year = new String[] { "2005", "2006", "2007", "2008"};

	   
	   for(int i=0; i<4; i++) {
		   
		   List<Integer>seriesData = IntStream.rangeClosed((i+1), (i+1)*3).boxed().collect(Collectors.toList());	   
		   Collections.shuffle(seriesData);
		   dataset.add(seriesData, lang[i], year[i]);
	   }	    
	   return dataset;
	}
	
	
	 private CategoryDataset createDataset() {
		 
		 DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		 
		    String series1 = "Java";
		    String series2 = "PHP";
		    String series3 = "C++";
		    String series4 = "C#";
		 
		    dataset.addValue(5.0, series1, "2005");
		    dataset.addValue(4.8, series1, "2006");
		    dataset.addValue(4.5, series1, "2007");
		    dataset.addValue(4.3, series1, "2008");
		    dataset.addValue(4.0, series1, "2009");
		    dataset.addValue(4.1, series1, "2010");
		    dataset.addValue(4.2, series1, "2011");
		    dataset.addValue(4.2, series1, "2012");
		    dataset.addValue(4.0, series1, "2013");
		 
		    dataset.addValue(4.0, series2, "2005");
		    dataset.addValue(4.2, series2, "2006");
		    dataset.addValue(3.8, series2, "2007");
		    dataset.addValue(3.6, series2, "2008");
		    dataset.addValue(3.4, series2, "2009");
		    dataset.addValue(3.4, series2, "2010");
		    dataset.addValue(3.3, series2, "2011");
		    dataset.addValue(3.1, series2, "2012");
		    dataset.addValue(3.2, series2, "2013");
		 
		    dataset.addValue(3.6, series3, "2005");
		    dataset.addValue(3.4, series3, "2006");
		    dataset.addValue(3.5, series3, "2007");
		    dataset.addValue(3.2, series3, "2008");
		    dataset.addValue(3.2, series3, "2009");
		    dataset.addValue(3.0, series3, "2010");
		    dataset.addValue(2.8, series3, "2011");
		    dataset.addValue(2.8, series3, "2012");
		    dataset.addValue(2.6, series3, "2013");
		 
		    dataset.addValue(3.2, series4, "2005");
		    dataset.addValue(3.2, series4, "2006");
		    dataset.addValue(3.0, series4, "2007");
		    dataset.addValue(3.0, series4, "2008");
		    dataset.addValue(2.8, series4, "2009");
		    dataset.addValue(2.7, series4, "2010");
		    dataset.addValue(2.6, series4, "2011");
		    dataset.addValue(2.6, series4, "2012");
		    dataset.addValue(2.4, series4, "2013");
		 
		    return dataset;
	    }

	public static void main(String[] args) {
		CategoryPlotTest demo = new CategoryPlotTest("Category plot test");
		demo.pack();
		UIUtils.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
}
