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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.plot.DefaultDrawingSupplier;

public class PieChartDrawingSupplier extends DefaultDrawingSupplier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6068319829600243155L;

    public PieChartDrawingSupplier(List<Color>colorList) {

		super(colorList.toArray(new Color[colorList.size()]), 
				colorList.toArray(new Color[colorList.size()]),
				DEFAULT_OUTLINE_PAINT_SEQUENCE, 
				DEFAULT_STROKE_SEQUENCE, 
				DEFAULT_OUTLINE_STROKE_SEQUENCE,
				DEFAULT_SHAPE_SEQUENCE);
    }
    
//    @Override
//    public Paint getNextPaint() {
//        return Color.blue;
//    }
//
//	@Override
//	public Paint getNextOutlinePaint() {
//		// TODO Auto-generated method stub
//		return Color.blue;
//	}
//
//	@Override
//	public Paint getNextFillPaint() {
//		// TODO Auto-generated method stub
//		return Color.blue;
//	}
}
