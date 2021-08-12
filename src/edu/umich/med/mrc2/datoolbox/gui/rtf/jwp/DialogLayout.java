package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class DialogLayout implements LayoutManager {
	protected int m_divider = -1;
	protected int m_hGap = 10;
	protected int m_vGap = 5;

	public DialogLayout() {
	}

	public DialogLayout(int hGap, int vGap) {
		this.m_hGap = hGap;
		this.m_vGap = vGap;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int divider = this.getDivider(parent);
		int w = 0;
		int h = 0;
		int k = 1;
		while (k < parent.getComponentCount()) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			w = Math.max(w, d.width);
			h += d.height + this.m_vGap;
			k += 2;
		}
		Insets insets = parent.getInsets();
		return new Dimension(divider + w + insets.left + insets.right, (h -= this.m_vGap) + insets.top + insets.bottom);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return this.preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		int divider = this.getDivider(parent);
		Insets insets = parent.getInsets();
		int w = parent.getWidth() - insets.left - insets.right - divider;
		int x = insets.left;
		int y = insets.top;
		int k = 1;
		while (k < parent.getComponentCount()) {
			Component comp1 = parent.getComponent(k - 1);
			Component comp2 = parent.getComponent(k);
			Dimension d = comp2.getPreferredSize();
			comp1.setBounds(x, y, divider, d.height);
			comp2.setBounds(x + divider, y, w, d.height);
			y += d.height + this.m_vGap;
			k += 2;
		}
	}

	public int getHGap() {
		return this.m_hGap;
	}

	public int getVGap() {
		return this.m_vGap;
	}

	public void setDivider(int divider) {
		if (divider > 0) {
			this.m_divider = divider;
		}
	}

	public int getDivider() {
		return this.m_divider;
	}

	protected int getDivider(Container parent) {
		if (this.m_divider > 0) {
			return this.m_divider;
		}
		int divider = 0;
		int k = 0;
		while (k < parent.getComponentCount()) {
			Component comp = parent.getComponent(k);
			Dimension d = comp.getPreferredSize();
			divider = Math.max(divider, d.width);
			k += 2;
		}
		return divider += this.m_hGap;
	}

	public String toString() {
		return String.valueOf(this.getClass().getName()) + "[hgap=" + this.m_hGap + ",vgap=" + this.m_vGap + ",divider="
				+ this.m_divider + "]";
	}
}