package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.util;

public interface CycleState {

	public void apply();
	public void halt();
	public boolean isActive();
	public boolean alwaysActivate();
	public String getDesc();

}
