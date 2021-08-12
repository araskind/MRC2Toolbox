package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ParagraphDialog extends JDialog {

	private final static Icon alignLeftIcon = GuiUtils.getIcon("format-justify-left", 24);
	private final static Icon alignCenterIcon = GuiUtils.getIcon("format-justify-center", 24);
	private final static Icon alignRightIcon = GuiUtils.getIcon("format-justify-right", 24);
	private final static Icon justifyIcon = GuiUtils.getIcon("format-justify-fill", 24);
	private final static Icon paragraphIcon = GuiUtils.getIcon("format-paragraph", 24);

	private static final long serialVersionUID = 1L;
	public JTextField w_lineSpacing;
	public JTextField w_spaceAbove;
	public JTextField w_spaceBelow;
	public JTextField w_firstIndent;
	public JTextField w_leftIndent;
	public JTextField w_rightIndent;
	public JToggleButton w_btLeft;
	public JToggleButton w_btCenter;
	public JToggleButton w_btRight;
	public JToggleButton w_btJustified;
	public ParagraphPreview m_preview;
	public int f_option;
	public MutableAttributeSet m_attributes;

	public ParagraphDialog() {
		super();
		setTitle("Paragraph style");
		setIconImage(((ImageIcon) paragraphIcon).getImage());
		setModal(true);
		setSize(new Dimension(450, 350));
		setPreferredSize(new Dimension(450, 350));
		setResizable(true);

		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), 1));
		JPanel p = new JPanel(new GridLayout(1, 2, 5, 2));
		JPanel ps = new JPanel(new GridLayout(3, 2, 10, 2));
		ps.setBorder(new TitledBorder(new EtchedBorder(), "Space"));
		ps.add(new JLabel("Line spacing:"));
		this.w_lineSpacing = new JTextField();
		ps.add(this.w_lineSpacing);
		ps.add(new JLabel("Space above:"));
		this.w_spaceAbove = new JTextField();
		ps.add(this.w_spaceAbove);
		ps.add(new JLabel("Space below:"));
		this.w_spaceBelow = new JTextField();
		ps.add(this.w_spaceBelow);
		p.add(ps);
		JPanel pi = new JPanel(new GridLayout(3, 2, 10, 2));
		pi.setBorder(new TitledBorder(new EtchedBorder(), "Indent"));
		pi.add(new JLabel("First indent:"));
		this.w_firstIndent = new JTextField();
		pi.add(this.w_firstIndent);
		pi.add(new JLabel("Left indent:"));
		this.w_leftIndent = new JTextField();
		pi.add(this.w_leftIndent);
		pi.add(new JLabel("Right indent:"));
		this.w_rightIndent = new JTextField();
		pi.add(this.w_rightIndent);
		p.add(pi);
		this.getContentPane().add(p);
		this.getContentPane().add(Box.createVerticalStrut(5));
		p = new JPanel();
		p.setLayout(new BoxLayout(p, 0));
		p.add(Box.createHorizontalStrut(10));
		p.add(new JLabel("Alignment:"));
		p.add(Box.createHorizontalStrut(20));
		ButtonGroup bg = new ButtonGroup();
		this.w_btLeft = new JToggleButton(alignLeftIcon);
		bg.add(this.w_btLeft);
		p.add(this.w_btLeft);
		this.w_btCenter = new JToggleButton(alignCenterIcon);
		bg.add(this.w_btCenter);
		p.add(this.w_btCenter);
		this.w_btRight = new JToggleButton(alignRightIcon);
		bg.add(this.w_btRight);
		p.add(this.w_btRight);
		this.w_btJustified = new JToggleButton(justifyIcon);
		bg.add(this.w_btJustified);
		p.add(this.w_btJustified);
		this.getContentPane().add(p);
		p = new JPanel(new BorderLayout());
		p.setBorder(new TitledBorder(new EtchedBorder(), "Preview"));
		this.m_preview = new ParagraphPreview();
		p.add((Component) this.m_preview, "Center");
		this.getContentPane().add(p);
		p = new JPanel(new FlowLayout());
		JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 2));
		JButton btOK = new JButton("OK");
		btOK.addActionListener(new confirmpgh());
		p1.add(btOK);
		JButton btCancel = new JButton("Cancel");
		btCancel.addActionListener(new cancelpgh());
		p1.add(btCancel);
		p.add(p1);
		this.getContentPane().add(p);
		this.pack();
		FocusListener flst = new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				ParagraphDialog.this.updatePreview();
			}
		};
		this.w_lineSpacing.addFocusListener(flst);
		this.w_spaceAbove.addFocusListener(flst);
		this.w_spaceBelow.addFocusListener(flst);
		this.w_firstIndent.addFocusListener(flst);
		this.w_leftIndent.addFocusListener(flst);
		this.w_rightIndent.addFocusListener(flst);
		this.w_btLeft.addActionListener(new updatepreview());
		this.w_btCenter.addActionListener(new updatepreview());
		this.w_btRight.addActionListener(new updatepreview());
		this.w_btJustified.addActionListener(new updatepreview());
	}

	public void setAttributesph(AttributeSet a) {
		this.m_attributes = new SimpleAttributeSet(a);
		this.w_lineSpacing.setText(Float.toString(StyleConstants.getLineSpacing(a)));
		this.w_spaceAbove.setText(Float.toString(StyleConstants.getSpaceAbove(a)));
		this.w_spaceBelow.setText(Float.toString(StyleConstants.getSpaceBelow(a)));
		this.w_firstIndent.setText(Float.toString(StyleConstants.getFirstLineIndent(a)));
		this.w_leftIndent.setText(Float.toString(StyleConstants.getLeftIndent(a)));
		this.w_rightIndent.setText(Float.toString(StyleConstants.getRightIndent(a)));
		int alignment = StyleConstants.getAlignment(a);
		if (alignment == 0) {
			this.w_btLeft.setSelected(true);
		} else if (alignment == 1) {
			this.w_btCenter.setSelected(true);
		} else if (alignment == 2) {
			this.w_btRight.setSelected(true);
		} else if (alignment == 3) {
			this.w_btJustified.setSelected(true);
		}
		this.updatePreview();
	}

	public AttributeSet getAttributesph() {
		float value;
		if (this.m_attributes == null) {
			return null;
		}
		try {
			value = Float.parseFloat(this.w_lineSpacing.getText());
			StyleConstants.setLineSpacing(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		try {
			value = Float.parseFloat(this.w_spaceAbove.getText());
			StyleConstants.setSpaceAbove(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		try {
			value = Float.parseFloat(this.w_spaceBelow.getText());
			StyleConstants.setSpaceBelow(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		try {
			value = Float.parseFloat(this.w_firstIndent.getText());
			StyleConstants.setFirstLineIndent(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		try {
			value = Float.parseFloat(this.w_leftIndent.getText());
			StyleConstants.setLeftIndent(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		try {
			value = Float.parseFloat(this.w_rightIndent.getText());
			StyleConstants.setRightIndent(this.m_attributes, value);
		} catch (NumberFormatException numberFormatException) {
			// empty catch block
		}
		StyleConstants.setAlignment(this.m_attributes, this.getAlignment());
		return this.m_attributes;
	}

	public int getOption() {
		return this.f_option;
	}

	protected void updatePreview() {
		this.m_preview.repaint();
	}

	protected int getAlignment() {
		if (this.w_btLeft.isSelected()) {
			return 0;
		}
		if (this.w_btCenter.isSelected()) {
			return 1;
		}
		if (this.w_btRight.isSelected()) {
			return 2;
		}
		return 3;
	}

	public class ParagraphPreview extends JPanel {
		private static final long serialVersionUID = 1L;
		public Font m_fn = new Font("Monospace", 0, 6);
		public String m_dummy = "abcdefghjklm";
		public float m_scaleX = 0.25f;
		public float m_scaleY = 0.25f;
		public Random m_random = new Random();

		public ParagraphPreview() {
			this.setBackground(Color.white);
			this.setForeground(Color.black);
			this.setOpaque(true);
			this.setBorder(new LineBorder(Color.black));
			this.setPreferredSize(new Dimension(120, 56));
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			float lineSpacing = 0.0f;
			float spaceAbove = 0.0f;
			float spaceBelow = 0.0f;
			float firstIndent = 0.0f;
			float leftIndent = 0.0f;
			float rightIndent = 0.0f;
			try {
				lineSpacing = Float.parseFloat(ParagraphDialog.this.w_lineSpacing.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			try {
				spaceAbove = Float.parseFloat(ParagraphDialog.this.w_spaceAbove.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			try {
				spaceBelow = Float.parseFloat(ParagraphDialog.this.w_spaceBelow.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			try {
				firstIndent = Float.parseFloat(ParagraphDialog.this.w_firstIndent.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			try {
				leftIndent = Float.parseFloat(ParagraphDialog.this.w_leftIndent.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			try {
				rightIndent = Float.parseFloat(ParagraphDialog.this.w_rightIndent.getText());
			} catch (NumberFormatException numberFormatException) {
				// empty catch block
			}
			this.m_random.setSeed(1959L);
			g.setFont(this.m_fn);
			FontMetrics fm = g.getFontMetrics();
			int h = fm.getAscent();
			int s = Math.max((int) (lineSpacing * this.m_scaleY), 1);
			int s1 = Math.max((int) (spaceAbove * this.m_scaleY), 0) + s;
			int s2 = Math.max((int) (spaceBelow * this.m_scaleY), 0) + s;
			int y = 5 + h;
			int xMarg = 20;
			int x0 = Math.max((int) (firstIndent * this.m_scaleX) + xMarg, 3);
			int x1 = Math.max((int) (leftIndent * this.m_scaleX) + xMarg, 3);
			int x2 = Math.max((int) (rightIndent * this.m_scaleX) + xMarg, 3);
			int xm0 = this.getWidth() - xMarg;
			int xm1 = this.getWidth() - x2;
			int n = (this.getHeight() - (2 * h + s1 + s2 - s + 10)) / (h + s);
			n = Math.max(n, 1);
			g.setColor(Color.lightGray);
			int x = xMarg;
			this.drawLine(g, x, y, xm0, xm0, fm, 0);
			y += h + s1;
			g.setColor(Color.gray);
			int alignment = ParagraphDialog.this.getAlignment();
			int k = 0;
			while (k < n) {
				int xLen;
				x = k == 0 ? x0 : x1;
				int n2 = xLen = k == n - 1 ? xm1 / 2 : xm1;
				if (k == n - 1 && alignment == 3) {
					alignment = 0;
				}
				this.drawLine(g, x, y, xm1, xLen, fm, alignment);
				y += h + s;
				++k;
			}
			x = xMarg;
			g.setColor(Color.lightGray);
			this.drawLine(g, x, y += s2 - s, xm0, xm0, fm, 0);
		}

		public void drawLine(Graphics g, int x, int y, int xMax, int xLen, FontMetrics fm, int alignment) {
			int m;
			int len;
			String str1;
			if (y > this.getHeight() - 3) {
				return;
			}
			StringBuffer s = new StringBuffer();
			int xx = x;
			while (xx + (len = fm
					.stringWidth(str1 = String.valueOf(this.m_dummy.substring(0, m = this.m_random.nextInt(10) + 1))
							+ " ")) < xLen) {
				xx += len;
				s.append(str1);
			}
			String str = s.toString();
			switch (alignment) {
				case 0 : {
					g.drawString(str, x, y);
					break;
				}
				case 1 : {
					xx = (xMax + x - fm.stringWidth(str)) / 2;
					g.drawString(str, xx, y);
					break;
				}
				case 2 : {
					xx = xMax - fm.stringWidth(str);
					g.drawString(str, xx, y);
					break;
				}
				case 3 : {
					while (x + fm.stringWidth(str) < xMax) {
						str = String.valueOf(str) + "a";
					}
					g.drawString(str, x, y);
				}
			}
		}
	}

	public class cancelpgh implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ParagraphDialog.this.f_option = 2;
			ParagraphDialog.this.setVisible(false);
		}
	}

	public class confirmpgh implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ParagraphDialog.this.f_option = 0;
			ParagraphDialog.this.setVisible(false);
		}
	}

	public class updatepreview implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ParagraphDialog.this.updatePreview();
		}
	}

}
