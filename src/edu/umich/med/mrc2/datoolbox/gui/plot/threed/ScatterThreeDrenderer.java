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

package edu.umich.med.mrc2.datoolbox.gui.plot.threed;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart3d.axis.Axis3D;
import org.jfree.chart3d.data.xyz.XYZDataset;
import org.jfree.chart3d.data.xyz.XYZItemKey;
import org.jfree.chart3d.graphics3d.Dimension3D;
import org.jfree.chart3d.graphics3d.Object3D;
import org.jfree.chart3d.graphics3d.Offset3D;
import org.jfree.chart3d.graphics3d.World;
import org.jfree.chart3d.plot.XYZPlot;
import org.jfree.chart3d.renderer.xyz.ScatterXYZRenderer;


public class ScatterThreeDrenderer extends ScatterXYZRenderer{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3380995670727927310L;
	private Offset3D  itemLabelOffsetPercent;
	private double size;
	private Map<Integer, PlotShapes>seriesShapeMap;
	private Map<Integer, Color>seriesColorMap;

	private static final PlotShapes defaultShape = PlotShapes.OCTAHEDRON;

    /**
     * Creates a new instance with default attribute values.
     */
    public ScatterThreeDrenderer() {

        super();
        this.size = 0.10;
        this.itemLabelOffsetPercent = new Offset3D(0.0, 1.0, 0.0);

        seriesShapeMap = new HashMap<Integer, PlotShapes>();
        seriesColorMap = new HashMap<Integer, Color>();
    }

    public void populateSeriesColorMap(int seriesCount, PlotShapes defaultShape) {

    	seriesShapeMap.clear();
    	seriesColorMap.clear();
    	for(int i=0; i<seriesCount; i++) {

    		seriesShapeMap.put(i, defaultShape);
    		seriesColorMap.put(i, getColorSource().getColor(i, 0));
    	}
    }

    public void setSeriesAttributes(int series, PlotShapes seriesShape, Color seriesColor) {

		seriesShapeMap.put(series, seriesShape);
		seriesColorMap.put(series, seriesColor);
    }

	/**
     * Constructs and places one item from the specified dataset into the given
     * world.  The {@link XYZPlot} class will iterate over its dataset and
     * and call this method for each item (in other words, you don't need to
     * call this method directly).
     *
     * @param dataset the dataset ({@code null} not permitted).
     * @param series  the series index.
     * @param item  the item index.
     * @param world  the world ({@code null} not permitted).
     * @param dimensions  the dimensions ({@code null} not permitted).
     * @param xOffset  the x-offset.
     * @param yOffset  the y-offset.
     * @param zOffset  the z-offset.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void composeItem(XYZDataset dataset, int series, int item,
        World world, Dimension3D dimensions, double xOffset, double yOffset,
        double zOffset) {

        double x = dataset.getX(series, item);
        double y = dataset.getY(series, item);
        double z = dataset.getZ(series, item);

        XYZPlot plot = getPlot();
        Axis3D xAxis = plot.getXAxis();
        Axis3D yAxis = plot.getYAxis();
        Axis3D zAxis = plot.getZAxis();

        double delta = this.size / 2.0;
        Dimension3D dim = plot.getDimensions();
        double xx = xAxis.translateToWorld(x, dim.getWidth());
        double xmin = Math.max(0.0, xx - delta);
        double xmax = Math.min(dim.getWidth(), xx + delta);
        double yy = yAxis.translateToWorld(y, dim.getHeight());
        double ymin = Math.max(0.0, yy - delta);
        double ymax = Math.min(dim.getHeight(), yy + delta);
        double zz = zAxis.translateToWorld(z, dim.getDepth());
        double zmin = Math.max(0.0, zz - delta);
        double zmax = Math.min(dim.getDepth(), zz + delta);

        if ((xmin >= xmax) || (ymin >= ymax) || (zmin >= zmax))
            return;

        double cx = (xmax + xmin) / 2.0 + xOffset;
        double cy = (ymax + ymin) / 2.0 + yOffset;
        double cz = (zmax + zmin) / 2.0 + zOffset;

        Color color = seriesColorMap.get(series);
        if(color == null)
        	color = getColorSource().getColor(series, item);

        PlotShapes shape = seriesShapeMap.get(series);
        if(shape == null)
        	shape = defaultShape;

        Object3D symbol = createSymbol(cx, xmax, xmin, cy, ymax, ymin, cz, zmax, zmin, color, shape);

        if(symbol != null) {

            Comparable<?> seriesKey = dataset.getSeriesKey(series);
            XYZItemKey itemKey = new XYZItemKey(seriesKey, item);
            symbol.setProperty(Object3D.ITEM_KEY, itemKey);
            world.add(symbol);

            if (getItemLabelGenerator() != null) {
                String label = getItemLabelGenerator().generateItemLabel(dataset, seriesKey, item);
                if (label != null) {
                    double dx = this.itemLabelOffsetPercent.getDX() * this.size;
                    double dy = this.itemLabelOffsetPercent.getDY() * this.size;
                    double dz = this.itemLabelOffsetPercent.getDZ() * this.size;
                    Object3D labelObj = Object3D.createLabelObject(label,
                            getItemLabelFont(), getItemLabelColor(),
                            getItemLabelBackgroundColor(), cx + dx, cy + dy,
                            cz + dz, false, true);
                    labelObj.setProperty(Object3D.ITEM_KEY, itemKey);
                    world.add(labelObj);
                }
            }
        }
    }

    private Object3D createSymbol(
    		double cx, double xmax, double xmin,
    		double cy, double ymax, double ymin,
    		double cz, double zmax, double zmin,
    		Color color, PlotShapes shape) {

    	if(shape.equals(PlotShapes.BOX))
    		return Object3D.createBox(cx, xmax - xmin, cy, ymax - ymin, cz, zmax - zmin, color);

    	if(shape.equals(PlotShapes.SPHERE))
    		return Object3D.createSphere(xmax - xmin, 15, cx, cy, cz, color, color);

    	if(shape.equals(PlotShapes.TETRAHEDRON))
    		return Object3D.createTetrahedron(xmax - xmin, cx, cy, cz, color);

    	if(shape.equals(PlotShapes.OCTAHEDRON))
    		return Object3D.createOctahedron(xmax - xmin, cx, cy, cz, color);

    	return null;
    }
}



































