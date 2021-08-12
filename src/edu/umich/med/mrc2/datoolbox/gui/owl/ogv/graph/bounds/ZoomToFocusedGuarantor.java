package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.bounds;

import java.awt.geom.Rectangle2D;

import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.gui.GraphCanvas;

public class ZoomToFocusedGuarantor extends BoundsGuarantorCycleState {

	public ZoomToFocusedGuarantor(GraphCanvas canvas) {
		setDesc("Zoom to focused node");
		setCanvas(canvas);
	}
	
	@Override
	public Rectangle2D getNewBounds() {
		if (canvas.getFocusedNode() == null)
			return null;
		return canvas.getFocusedNode().getGlobalFullBounds();
	}


}
