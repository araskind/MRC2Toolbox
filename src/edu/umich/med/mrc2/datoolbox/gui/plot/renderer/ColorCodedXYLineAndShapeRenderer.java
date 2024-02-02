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

package edu.umich.med.mrc2.datoolbox.gui.plot.renderer;

import java.awt.Paint;

import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorCodingUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public abstract class ColorCodedXYLineAndShapeRenderer extends XYLineAndShapeRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected XYDataset datasetToRender;
	protected LookupPaintScale lookupPaintScale;
	protected ColorGradient colorGradient; 
	protected ColorScale colorScale;
	protected Range dataRange;
	
	public ColorCodedXYLineAndShapeRenderer(
			XYDataset datasetToRender, 
			ColorGradient colorGradient, 
			ColorScale colorScale) {
		super(false, true);
		this.datasetToRender = datasetToRender;
		this.colorGradient = colorGradient;
		this.colorScale = colorScale;
	}
	
	public void createLookupPaintScale() {
		
		dataRange = createDataRange();
		lookupPaintScale = ColorCodingUtils.createLookupPaintScale(
					dataRange, 
					colorGradient, 
					colorScale,
					256);
	}
	
	protected abstract Range createDataRange();

	public ColorCodedXYLineAndShapeRenderer(XYDataset datasetToRender) {	
		this(datasetToRender, ColorGradient.GREEN_RED, ColorScale.LINEAR);
	}

//	TODO override to get the value of interest
	@Override
    public abstract Paint getItemPaint(int row, int column);
//	  {
//		return lookupPaintScale.getPaint(datasetToRender.getYValue(row, column));        
//    }

	public LookupPaintScale getLookupPaintScale() {
		return lookupPaintScale;
	}

	public Range getDataRange() {
		return dataRange;
	}

	public void setColorGradient(ColorGradient newColorGradient) {
		
		if(!this.colorGradient.equals(newColorGradient)) {
			
			this.colorGradient = newColorGradient;					
			if(dataRange != null)
				lookupPaintScale = ColorCodingUtils.createLookupPaintScale(
						dataRange, 
						colorGradient, 
						colorScale,
						256);
		}
	}

	public void setColorScale(ColorScale newColorScale) {
		
		if(!this.colorScale.equals(newColorScale)){
			
			this.colorScale = newColorScale;
			if(dataRange != null)
				lookupPaintScale = ColorCodingUtils.createLookupPaintScale(
						dataRange, 
						colorGradient, 
						colorScale,
						256);
		}
	}
}














