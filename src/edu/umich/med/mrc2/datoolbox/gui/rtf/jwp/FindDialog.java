package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class FindDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	public JWordProcessor f_owner;
	public JTabbedPane f_tb;
	public JTextField f_txtFind1;
	public JTextField f_txtFind2;
	public Document f_docFind;
	public Document f_docReplace;
	public ButtonModel f_modelWord;
	public ButtonModel f_modelCase;
	public ButtonModel f_modelUp;
	public ButtonModel f_modelDown;
	public int f_searchIndex = -1;
	public boolean f_searchUp = false;
	public String f_searchData;

	private final static Icon findReplaceIcon = GuiUtils.getIcon("find", 24);

	public FindDialog(JWordProcessor owner, int index) {

		super();
		setTitle("Search/replace");
		setIconImage(((ImageIcon) findReplaceIcon).getImage());
		setModal(false);
		setSize(new Dimension(580, 300));
		setPreferredSize(new Dimension(580, 300));
		setResizable(true);

		this.f_owner = owner;
		this.f_tb = new JTabbedPane();
		JPanel p1 = new JPanel(new BorderLayout());
		JPanel pc1 = new JPanel(new BorderLayout());
		JPanel pf = new JPanel();
		pf.setLayout((LayoutManager) new DialogLayout(20, 5));
		pf.setBorder(new EmptyBorder(8, 5, 8, 0));
		pf.add(new JLabel("Find what:"));
		this.f_txtFind1 = new JTextField();
		this.f_docFind = this.f_txtFind1.getDocument();
		pf.add(this.f_txtFind1);
		pc1.add((Component) pf, "Center");
		JPanel po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(), "Options"));
		JCheckBox chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		this.f_modelWord = chkWord.getModel();
		po.add(chkWord);
		ButtonGroup bg = new ButtonGroup();
		JRadioButton rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		this.f_modelUp = rdUp.getModel();
		bg.add(rdUp);
		po.add(rdUp);
		JCheckBox chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		this.f_modelCase = chkCase.getModel();
		po.add(chkCase);
		JRadioButton rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		this.f_modelDown = rdDown.getModel();
		bg.add(rdDown);
		po.add(rdDown);
		pc1.add((Component) po, "South");
		p1.add((Component) pc1, "Center");
		JPanel p01 = new JPanel(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 1, 2, 8));
		JButton btFind = new JButton("Find Next");
		btFind.addActionListener(new findAction());
		btFind.setMnemonic('f');
		p.add(btFind);
		JButton btClose = new JButton("Close");
		btClose.addActionListener(new closeAction());
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p01.add(p);
		p1.add((Component) p01, "East");
		this.f_tb.addTab("Find", p1);
		JPanel p2 = new JPanel(new BorderLayout());
		JPanel pc2 = new JPanel(new BorderLayout());
		JPanel pc = new JPanel();
		pc.setLayout((LayoutManager) new DialogLayout(20, 5));
		pc.setBorder(new EmptyBorder(8, 5, 8, 0));
		pc.add(new JLabel("Find what:"));
		this.f_txtFind2 = new JTextField();
		this.f_txtFind2.setDocument(this.f_docFind);
		pc.add(this.f_txtFind2);
		pc.add(new JLabel("Replace:"));
		JTextField txtReplace = new JTextField();
		this.f_docReplace = txtReplace.getDocument();
		pc.add(txtReplace);
		pc2.add((Component) pc, "Center");
		po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(), "Options"));
		chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		chkWord.setModel(this.f_modelWord);
		po.add(chkWord);
		bg = new ButtonGroup();
		rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		rdUp.setModel(this.f_modelUp);
		bg.add(rdUp);
		po.add(rdUp);
		chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		chkCase.setModel(this.f_modelCase);
		po.add(chkCase);
		rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		rdDown.setModel(this.f_modelDown);
		bg.add(rdDown);
		po.add(rdDown);
		pc2.add((Component) po, "South");
		p2.add((Component) pc2, "Center");
		JPanel p02 = new JPanel(new FlowLayout());
		p = new JPanel(new GridLayout(3, 1, 2, 8));
		JButton btReplace = new JButton("Replace");
		btReplace.setMnemonic('r');
		p.add(btReplace);
		JButton btReplaceAll = new JButton("Replace All");
		btReplaceAll.addActionListener(new replaceAllAction());
		btReplaceAll.setMnemonic('a');
		p.add(btReplaceAll);
		btClose = new JButton("Close");
		btClose.addActionListener(new closeAction());
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p02.add(p);
		p2.add((Component) p02, "East");
		p01.setPreferredSize(p02.getPreferredSize());
		this.f_tb.addTab("Replace", p2);
		this.f_tb.setSelectedIndex(index);
		this.getContentPane().add((Component) this.f_tb, "Center");
		WindowAdapter flst = new WindowAdapter() {

			@Override
			public void windowActivated(WindowEvent e) {
				FindDialog.this.f_searchIndex = -1;
				if (FindDialog.this.f_tb.getSelectedIndex() == 0) {
					FindDialog.this.f_txtFind1.grabFocus();
				} else {
					FindDialog.this.f_txtFind2.grabFocus();
				}
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				FindDialog.this.f_searchData = null;
			}
		};
		this.addWindowListener(flst);
		this.pack();
	}

	public void setSelectedIndex(int index) {
		this.f_tb.setSelectedIndex(index);
		this.setVisible(true);
		this.f_searchIndex = -1;
	}

	public int findNext(boolean doReplace, boolean showWarnings) {
		int xStart;
		JTextPane monitor;
		int xFinish;
		String replacement;
		block22 : {
			monitor = this.f_owner.getTextPane();
			int pos = monitor.getCaretPosition();
			if (this.f_modelUp.isSelected() != this.f_searchUp) {
				this.f_searchUp = this.f_modelUp.isSelected();
				this.f_searchIndex = -1;
			}
			if (this.f_searchIndex == -1) {
				try {
					DefaultStyledDocument doc = this.f_owner.getDocument();
					this.f_searchData = this.f_searchUp ? doc.getText(0, pos) : doc.getText(pos, doc.getLength() - pos);
					this.f_searchIndex = pos;
				} catch (BadLocationException ex) {
					this.warning(ex.toString());
					return -1;
				}
			}
			String key = "";
			try {
				key = this.f_docFind.getText(0, this.f_docFind.getLength());
			} catch (BadLocationException badLocationException) {
				// empty catch block
			}
			if (key.length() == 0) {
				this.warning("Please enter the target to search");
				return -1;
			}
			if (!this.f_modelCase.isSelected()) {
				this.f_searchData = this.f_searchData.toLowerCase();
				key = key.toLowerCase();
			}
			if (this.f_modelWord.isSelected()) {
				int k = 0;
				while (k < Utils.WORD_SEPARATORS.length) {
					if (key.indexOf(Utils.WORD_SEPARATORS[k]) >= 0) {
						this.warning(
								"The text target contains an illegal character '" + Utils.WORD_SEPARATORS[k] + "'");
						return -1;
					}
					++k;
				}
			}
			replacement = "";
			if (doReplace) {
				try {
					replacement = this.f_docReplace.getText(0, this.f_docReplace.getLength());
				} catch (BadLocationException badLocationException) {
					// empty catch block
				}
			}
			xStart = -1;
			xFinish = -1;
			do {
				boolean b2;
				xStart = this.f_searchUp
						? this.f_searchData.lastIndexOf(key, pos - 1)
						: this.f_searchData.indexOf(key, pos - this.f_searchIndex);
				if (xStart < 0) {
					if (showWarnings) {
						this.warning("Text not found");
					}
					return 0;
				}
				xFinish = xStart + key.length();
				if (!this.f_modelWord.isSelected())
					break block22;
				boolean s1 = xStart > 0;
				boolean b1 = s1 && !Utils.isSeparator((char) this.f_searchData.charAt(xStart - 1));
				boolean s2 = xFinish < this.f_searchData.length();
				boolean bl = b2 = s2 && !Utils.isSeparator((char) this.f_searchData.charAt(xFinish));
				if (!b1 && !b2)
					break block22;
				if (this.f_searchUp && s1) {
					pos = xStart;
					continue;
				}
				if (this.f_searchUp || !s2)
					break;
				pos = xFinish;
			} while (true);
			if (showWarnings) {
				this.warning("Text not found");
			}
			return 0;
		}
		if (!this.f_searchUp) {
			xStart += this.f_searchIndex;
			xFinish += this.f_searchIndex;
		}
		if (doReplace) {
			this.f_owner.setSelection(xStart, xFinish, this.f_searchUp);
			monitor.replaceSelection(replacement);
			this.f_owner.setSelection(xStart, xStart + replacement.length(), this.f_searchUp);
			this.f_searchIndex = -1;
		} else {
			this.f_owner.setSelection(xStart, xFinish, this.f_searchUp);
		}
		return 1;
	}

	protected void warning(String message) {
		JOptionPane.showMessageDialog((Component) this.f_owner, message, "Warning", 1);
	}

	public class closeAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FindDialog.this.setVisible(false);
		}
	}

	public class findAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			FindDialog.this.findNext(false, true);
		}
	}

	public class replaceAllAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int counter = 0;
			do {
				int result;
				if ((result = FindDialog.this.findNext(true, false)) < 0) {
					return;
				}
				if (result == 0)
					break;
				++counter;
			} while (true);
			JOptionPane.showMessageDialog((Component) FindDialog.this.f_owner,
					String.valueOf(counter) + " replacement(s) have been done", "Info", 1);
		}
	}

}
