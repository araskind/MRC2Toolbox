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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.renderer.xy.XYSplineRenderer;

public class DefaultSplineRenderer extends XYSplineRenderer{

	/**
	 *
	 */
	private static final long serialVersionUID = -3596655359770128594L;
	public static final Shape dataPointsShape = new Ellipse2D.Double(-2, -2, 5, 5);

	public DefaultSplineRenderer() {

		super(5, FillType.TO_ZERO);
		setDefaultPaint(Color.BLUE);
		setDefaultFillPaint(Color.BLUE);
		setUseFillPaint(true);

		// Set shape properties
		setDefaultShape(dataPointsShape);
		setDefaultShapesFilled(true);
		setDefaultShapesVisible(true);
		setDrawOutlines(false);

//		setSeriesItemLabelsVisible(0, false);
//		setSeriesShapesVisible(0, true);
//		setSeriesPaint(0, Color.red);
//		setSeriesStroke(0, new BasicStroke(1.5f));
	}
}
