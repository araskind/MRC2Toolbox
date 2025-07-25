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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import org.jfree.chart.renderer.AbstractRenderer;

public class JFreeChartUtils {

	public static void resizeSymbolForRenderer(AbstractRenderer renderer, int series, double shapeMult) {
		
		Shape shape = renderer.getSeriesShape(series);
		if(shape == null)
			shape = renderer.lookupSeriesShape(series);
		
		AffineTransform resize=new AffineTransform();
		double xFactor = shape.getBounds().width*shapeMult;
		double yFactor = shape.getBounds().height*shapeMult;
		resize.scale(xFactor,yFactor);
		Shape rescaled=resize.createTransformedShape(shape);
		Shape shape1=new GeneralPath(rescaled);
		renderer.setSeriesShape(series,shape1);
	}
}
