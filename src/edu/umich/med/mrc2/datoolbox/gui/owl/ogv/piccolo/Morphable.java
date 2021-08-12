package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;

public interface Morphable {

	public PActivity morphTo(PNode node, long duration);
	
	public boolean doDefaultMorph();
}
