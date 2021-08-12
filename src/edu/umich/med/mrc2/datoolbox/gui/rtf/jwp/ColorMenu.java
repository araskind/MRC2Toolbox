package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ColorMenu extends JMenu {
	private static final long serialVersionUID = 1L;
	Border _activeBorder = new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.BLUE),
			new MatteBorder(1, 1, 1, 1, this.getBackground()));
	Map<Color, ColorPane> _colorPanes;
	Border _selectedBorder = new CompoundBorder(new MatteBorder(2, 2, 2, 2, Color.RED),
			new MatteBorder(1, 1, 1, 1, this.getBackground()));
	ColorPane _selectedColorPane;
	Border _unselectedBorder = new CompoundBorder(new MatteBorder(1, 1, 1, 1, this.getBackground()),
			new BevelBorder(1, Color.WHITE, Color.GRAY));

	public ColorMenu(String name) {
		super(name);
		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.setLayout(new GridLayout(8, 8));
		this._colorPanes = new HashMap<Color, ColorPane>();
		int[] arrn = new int[4];
		arrn[1] = 128;
		arrn[2] = 192;
		arrn[3] = 255;
		int[] values = arrn;
		int r = 0;
		while (r < values.length) {
			int g = 0;
			while (g < values.length) {
				int b = 0;
				while (b < values.length) {
					Color color = new Color(values[r], values[g], values[b]);
					ColorPane colorPane = new ColorPane(color);
					p.add(colorPane);
					this._colorPanes.put(color, colorPane);
					++b;
				}
				++g;
			}
			++r;
		}
		this.add(p);
	}

	public void setColor(Color c) {
		ColorPane obj = this._colorPanes.get(c);
		if (obj == null) {
			return;
		}
		if (this._selectedColorPane != null) {
			this._selectedColorPane.setSelected(false);
		}
		this._selectedColorPane = obj;
		this._selectedColorPane.setSelected(true);
	}

	public Color getColor() {
		if (this._selectedColorPane == null) {
			return null;
		}
		return this._selectedColorPane.getColor();
	}

	public void doSelection() {
		this.fireActionPerformed(new ActionEvent(this, 1001, this.getActionCommand()));
	}

	class ColorPane extends JPanel implements MouseListener {
		private static final long serialVersionUID = -1234630827742960572L;
		Color _color;
		boolean _isSelected;

		public ColorPane(Color color) {
			this._color = color;
			this.setBackground(color);
			this.setBorder(ColorMenu.this._unselectedBorder);
			String msg = "rgb( " + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + " )";
			this.setToolTipText(msg);
			this.addMouseListener(this);
		}

		public void setSelected(boolean isSelected) {
			this._isSelected = isSelected;
			if (this._isSelected) {
				this.setBorder(ColorMenu.this._selectedBorder);
			} else {
				this.setBorder(ColorMenu.this._unselectedBorder);
			}
		}

		public Color getColor() {
			return this._color;
		}

		@Override
		public Dimension getMaximumSize() {
			return this.getPreferredSize();
		}

		@Override
		public Dimension getMinimumSize() {
			return this.getPreferredSize();
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(15, 15);
		}

		public boolean isSelected() {
			return this._isSelected;
		}

		@Override
		public void mouseClicked(MouseEvent ev) {
		}

		@Override
		public void mouseEntered(MouseEvent ev) {
			this.setBorder(ColorMenu.this._activeBorder);
		}

		@Override
		public void mouseExited(MouseEvent ev) {
			this.setBorder(this._isSelected ? ColorMenu.this._selectedBorder : ColorMenu.this._unselectedBorder);
		}

		@Override
		public void mousePressed(MouseEvent ev) {
		}

		@Override
		public void mouseReleased(MouseEvent ev) {
			ColorMenu.this.setColor(this._color);
			MenuSelectionManager.defaultManager().clearSelectedPath();
			ColorMenu.this.doSelection();
		}
	}

}