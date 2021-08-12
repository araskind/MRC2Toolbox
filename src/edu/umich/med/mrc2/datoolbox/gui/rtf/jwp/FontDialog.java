package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FontDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public int m_option = -1;
	public OpenList m_lstFontName;
	public OpenList m_lstFontSize;
	public MutableAttributeSet m_attributes;
	public JCheckBox m_chkBold;
	public JCheckBox m_chkItalic;
	public JCheckBox m_chkUnderline;
	public JCheckBox m_chkStrikethrough;
	public JCheckBox m_chkSubscript;
	public JCheckBox m_chkSuperscript;
	public JComboBox m_cbColor;
	public JLabel m_preview;

	private final static Icon fontSelectIcon = GuiUtils.getIcon("fontOtf", 24);

	public FontDialog(String[] m_fontNames, String[] m_fontSizes) {

		super();
		setTitle("Font selector");
		setIconImage(((ImageIcon) fontSelectIcon).getImage());
		setModal(true);
		setSize(new Dimension(350, 450));
		setPreferredSize(new Dimension(350, 450));
		setResizable(true);

		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), 1));
		JPanel p = new JPanel(new GridLayout(1, 2, 10, 2));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Font"));
		this.m_lstFontName = new OpenList(m_fontNames, "Name:");
		p.add((Component) this.m_lstFontName);
		this.m_lstFontSize = new OpenList(m_fontSizes, "Size:");
		p.add((Component) this.m_lstFontSize);
		this.getContentPane().add(p);
		p = new JPanel(new GridLayout(2, 3, 10, 5));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Effects"));
		this.m_chkBold = new JCheckBox("Bold");
		p.add(this.m_chkBold);
		this.m_chkItalic = new JCheckBox("Italic");
		p.add(this.m_chkItalic);
		this.m_chkUnderline = new JCheckBox("Underline");
		p.add(this.m_chkUnderline);
		this.m_chkStrikethrough = new JCheckBox("Strikeout");
		p.add(this.m_chkStrikethrough);
		this.m_chkSubscript = new JCheckBox("Subscript");
		p.add(this.m_chkSubscript);
		this.m_chkSuperscript = new JCheckBox("Superscript");
		p.add(this.m_chkSuperscript);
		this.getContentPane().add(p);
		this.getContentPane().add(Box.createVerticalStrut(5));
		p = new JPanel();
		p.setLayout(new BoxLayout(p, 0));
		p.add(Box.createHorizontalStrut(10));
		p.add(new JLabel("Color:"));
		p.add(Box.createHorizontalStrut(20));
		this.m_cbColor = new JComboBox();
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
					Color c = new Color(values[r], values[g], values[b]);
					this.m_cbColor.addItem(c);
					++b;
				}
				++g;
			}
			++r;
		}
		this.m_cbColor.setRenderer(new ColorComboRender());
		p.add(this.m_cbColor);
		p.add(Box.createHorizontalStrut(10));
		this.getContentPane().add(p);
		p = new JPanel(new BorderLayout());
		p.setBorder(new TitledBorder(new EtchedBorder(), "Preview"));
		this.m_preview = new JLabel("Preview Font", 0);
		this.m_preview.setBackground(Color.white);
		this.m_preview.setForeground(Color.black);
		this.m_preview.setOpaque(true);
		this.m_preview.setBorder(new LineBorder(Color.black));
		this.m_preview.setPreferredSize(new Dimension(120, 40));
		p.add((Component) this.m_preview, "Center");
		this.getContentPane().add(p);
		p = new JPanel(new FlowLayout());
		JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 2));
		JButton btOK = new JButton("OK");
		btOK.addActionListener(new confirmconfig());
		p1.add(btOK);
		JButton btCancel = new JButton("Cancel");
		btCancel.addActionListener(new cancelconfig());
		p1.add(btCancel);
		p.add(p1);
		this.getContentPane().add(p);
		this.pack();
		ListSelectionListener lsel = new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				FontDialog.this.updatePreview();
			}
		};
		this.m_lstFontName.addListSelectionListener(lsel);
		this.m_lstFontSize.addListSelectionListener(lsel);
		this.m_chkBold.addActionListener(new updateconfig());
		this.m_chkItalic.addActionListener(new updateconfig());
		this.m_cbColor.addActionListener(new updateconfig());
	}

	public void setAttributesadvf(AttributeSet a) {
		this.m_attributes = new SimpleAttributeSet(a);
		String name = StyleConstants.getFontFamily(a);
		this.m_lstFontName.setSelected(name);
		int size = StyleConstants.getFontSize(a);
		this.m_lstFontSize.setSelectedInt(size);
		this.m_chkBold.setSelected(StyleConstants.isBold(a));
		this.m_chkItalic.setSelected(StyleConstants.isItalic(a));
		this.m_chkUnderline.setSelected(StyleConstants.isUnderline(a));
		this.m_chkStrikethrough.setSelected(StyleConstants.isStrikeThrough(a));
		this.m_chkSubscript.setSelected(StyleConstants.isSubscript(a));
		this.m_chkSuperscript.setSelected(StyleConstants.isSuperscript(a));
		this.m_cbColor.setSelectedItem(StyleConstants.getForeground(a));
		this.updatePreview();
	}

	public AttributeSet getAttributesadvf() {
		if (this.m_attributes == null) {
			return null;
		}
		StyleConstants.setFontFamily(this.m_attributes, this.m_lstFontName.getSelected());
		StyleConstants.setFontSize(this.m_attributes, this.m_lstFontSize.getSelectedInt());
		StyleConstants.setBold(this.m_attributes, this.m_chkBold.isSelected());
		StyleConstants.setItalic(this.m_attributes, this.m_chkItalic.isSelected());
		StyleConstants.setUnderline(this.m_attributes, this.m_chkUnderline.isSelected());
		StyleConstants.setStrikeThrough(this.m_attributes, this.m_chkStrikethrough.isSelected());
		StyleConstants.setSubscript(this.m_attributes, this.m_chkSubscript.isSelected());
		StyleConstants.setSuperscript(this.m_attributes, this.m_chkSuperscript.isSelected());
		StyleConstants.setForeground(this.m_attributes, (Color) this.m_cbColor.getSelectedItem());
		return this.m_attributes;
	}

	public int getOption() {
		return this.m_option;
	}

	public void updatePreview() {
		String name = this.m_lstFontName.getSelected();
		int size = this.m_lstFontSize.getSelectedInt();
		if (size <= 0) {
			return;
		}
		int style = 0;
		if (this.m_chkBold.isSelected()) {
			style |= 1;
		}
		if (this.m_chkItalic.isSelected()) {
			style |= 2;
		}
		Font fn = new Font(name, style, size);
		this.m_preview.setFont(fn);
		Color c = (Color) this.m_cbColor.getSelectedItem();
		this.m_preview.setForeground(c);
		this.m_preview.repaint();
	}

	public class cancelconfig implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FontDialog.this.m_option = 2;
			FontDialog.this.setVisible(false);
		}
	}

	public class confirmconfig implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FontDialog.this.m_option = 0;
			FontDialog.this.setVisible(false);
		}
	}

	public class updateconfig implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FontDialog.this.updatePreview();
		}
	}

}
