package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class ColorComboRender extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;
	public Color m_color = Color.black;
	public Color m_focusColor = (Color) UIManager.get("List.selectionBackground");
	public Color m_nonFocusColor = Color.white;

	public Component getListCellRendererComponent(JList list, Object obj, int row, boolean sel, boolean hasFocus) {
		if (hasFocus || sel) {
			this.setBorder(
					new CompoundBorder(new MatteBorder(2, 10, 2, 10, this.m_focusColor), new LineBorder(Color.black)));
		} else {
			this.setBorder(new CompoundBorder(new MatteBorder(2, 10, 2, 10, this.m_nonFocusColor),
					new LineBorder(Color.black)));
		}
		if (obj instanceof Color) {
			this.m_color = (Color) obj;
		}
		return this;
	}

	@Override
	public void paintComponent(Graphics g) {
		this.setBackground(this.m_color);
		super.paintComponent(g);
	}
}