package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.collapse;

import java.util.EventListener;


public interface ExpandCollapseListener extends EventListener {
	public void expandStateChanged(ExpansionEvent e);
}
