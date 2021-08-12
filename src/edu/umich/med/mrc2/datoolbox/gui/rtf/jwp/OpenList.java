package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;


import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class OpenList extends JPanel implements ListSelectionListener, ActionListener {
	private static final long serialVersionUID = 1L;
	public JLabel m_title;
	public JTextField m_text;
	public JList m_list;
	public JScrollPane m_scroll;

	public OpenList(String[] data, String title) {
		this.setLayout(null);
		this.m_title = new JLabel(title, 2);
		this.add(this.m_title);
		this.m_text = new JTextField();
		this.m_text.addActionListener(this);
		this.add(this.m_text);
		this.m_list = new JList<String>(data);
		this.m_list.setVisibleRowCount(4);
		this.m_list.addListSelectionListener(this);
		this.m_scroll = new JScrollPane(this.m_list);
		this.add(this.m_scroll);
	}

	public void setSelected(String sel) {
		this.m_list.setSelectedValue(sel, true);
		this.m_text.setText(sel);
	}

	public String getSelected() {
		return this.m_text.getText();
	}

	public void setSelectedInt(int value) {
		this.setSelected(Integer.toString(value));
	}

	public int getSelectedInt() {
		try {
			return Integer.parseInt(this.getSelected());
		} catch (NumberFormatException ex) {
			return -1;
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		Object obj = this.m_list.getSelectedValue();
		if (obj != null) {
			this.m_text.setText(obj.toString());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ListModel model = this.m_list.getModel();
		String key = this.m_text.getText().toLowerCase();
		int k = 0;
		while (k < model.getSize()) {
			String data = (String) model.getElementAt(k);
			if (data.toLowerCase().startsWith(key)) {
				this.m_list.setSelectedValue(data, true);
				break;
			}
			++k;
		}
	}

	public void addListSelectionListener(ListSelectionListener lst) {
		this.m_list.addListSelectionListener(lst);
	}

	@Override
	public Dimension getPreferredSize() {
		Insets ins = this.getInsets();
		Dimension d1 = this.m_title.getPreferredSize();
		Dimension d2 = this.m_text.getPreferredSize();
		Dimension d3 = this.m_scroll.getPreferredSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
	}

	@Override
	public Dimension getMaximumSize() {
		Insets ins = this.getInsets();
		Dimension d1 = this.m_title.getMaximumSize();
		Dimension d2 = this.m_text.getMaximumSize();
		Dimension d3 = this.m_scroll.getMaximumSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
	}

	@Override
	public Dimension getMinimumSize() {
		Insets ins = this.getInsets();
		Dimension d1 = this.m_title.getMinimumSize();
		Dimension d2 = this.m_text.getMinimumSize();
		Dimension d3 = this.m_scroll.getMinimumSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w + ins.left + ins.right, h + ins.top + ins.bottom);
	}

	@Override
	public void doLayout() {
		Insets ins = this.getInsets();
		Dimension d = this.getSize();
		int x = ins.left;
		int y = ins.top;
		int w = d.width - ins.left - ins.right;
		int h = d.height - ins.top - ins.bottom;
		Dimension d1 = this.m_title.getPreferredSize();
		this.m_title.setBounds(x, y, w, d1.height);
		Dimension d2 = this.m_text.getPreferredSize();
		this.m_text.setBounds(x, y += d1.height, w, d2.height);
		this.m_scroll.setBounds(x, y, w, h - (y += d2.height));
	}
}

