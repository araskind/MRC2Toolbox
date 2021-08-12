package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.util.List;

import javax.swing.JMenuItem;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.gui.GraphCanvas;

public interface RightClickMenuFactory {
	public static JMenuItem SEPARATOR_ITEM = new JMenuItem();
	
	public List<JMenuItem> getMenuItems(GraphCanvas canvas, PInputEvent e);
}
