package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.util.Collection;

import edu.umd.cs.piccolo.PNode;

public interface NamedChildProvider {
	public Collection<Object> getChildNames(PNode node);
	public PNode getNamedChild(Object name, PNode node);
	public void setNamedChild(Object name, PNode node, PNode value);
}
