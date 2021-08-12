package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.tooltip.TooltipFactory;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.util.ShapeUtil;

public class PCNode<T> extends PPath {

	// generated
	private static final long serialVersionUID = -7322644935775027738L;

	public static final Object PATH_NODE = new Object();
	
	protected NamedChildProvider provider;

	protected T lo;
	protected PPath pathNode;

	public void initialize(T pc, NamedChildProvider provider, Shape s) {
		setOffset(0, 0);
		setProvider(provider);
		setObject(pc);
		setPickable(true);
		pathNode = new PPath();
		pathNode.setPickable(false);
		setNamedChild(PATH_NODE, pathNode);
		setPaint(null);
		setStroke(null);
		setStrokePaint(null);
		setShape(s);
	}

	public void setShape(Shape s) {
		GeneralPath newShape = new GeneralPath();
		ShapeUtil.normalize(s, newShape);
		pathNode.setPathTo(newShape);
		setPathTo(newShape);
		setOffset(getXOffset() + s.getBounds2D().getX(), getYOffset()
				+ s.getBounds2D().getY());
	}

	public void setTooltipFactory(TooltipFactory factory) {
		addAttribute(TooltipFactory.KEY, factory);
	}

	public PPath getPathDelegate() {
		return (PPath) getNamedChild(PATH_NODE);
	}

	public void setObject(T lo) {
		this.lo = lo;
	}

	public T getObject() {
		return lo;
	}

	public void setProvider(NamedChildProvider provider) {
		this.provider = provider;
	}

	public PNode getNamedChild(Object key) {
		return provider.getNamedChild(key, this);
	}

	public void setNamedChild(Object name, PNode value) {
		provider.setNamedChild(name, this, value);
	}

	public NamedChildProvider getProvider() {
		return provider;
	}

}
