package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.inet.jortho.FileUserDictionary;
import com.inet.jortho.SpellChecker;
import com.inet.jortho.UserDictionaryProvider;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import rtf.AdvancedRTFDocument;
import rtf.AdvancedRTFEditorKit;

@SuppressWarnings("serial")
public class JWordProcessor extends JPanel {

	private static final Dimension preferredSize = new Dimension(80, 20);

	private static final Icon saveFileIcon = GuiUtils.getIcon("save", 24);
	private static final Icon openFileIcon = GuiUtils.getIcon("open", 24);
	private static final Icon printIcon = GuiUtils.getIcon("print", 24);
	private final static Icon undoIcon = GuiUtils.getIcon("edit-undo", 24);
	private final static Icon redoIcon = GuiUtils.getIcon("edit-redo", 24);
	private final static Icon copyIcon = GuiUtils.getIcon("copy", 24);
	private final static Icon cutIcon = GuiUtils.getIcon("cut", 24);
	private final static Icon pasteIcon = GuiUtils.getIcon("paste", 24);
	private final static Icon findIcon = GuiUtils.getIcon("find", 24);
	private final static Icon findReplaceIcon = GuiUtils.getIcon("rename", 24);
	private final static Icon alignLeftIcon = GuiUtils.getIcon("format-justify-left", 24);
	private final static Icon alignCenterIcon = GuiUtils.getIcon("format-justify-center", 24);
	private final static Icon alignRightIcon = GuiUtils.getIcon("format-justify-right", 24);
	private final static Icon justifyIcon = GuiUtils.getIcon("format-justify-fill", 24);
	private final static Icon indentLessIcon = GuiUtils.getIcon("format-indent-less", 24);
	private final static Icon indentMoreIcon = GuiUtils.getIcon("format-indent-more", 24);
	private final static Icon boldIcon = GuiUtils.getIcon("format-text-bold", 24);
	private final static Icon italicIcon = GuiUtils.getIcon("format-text-italic", 24);
	private final static Icon strikethroughIcon = GuiUtils.getIcon("format-text-strikethrough", 24);
	private final static Icon underlineIcon = GuiUtils.getIcon("format-text-underline", 24);
	private final static Icon colorPickerIcon = GuiUtils.getIcon("colorPicker", 24);
	private final static Icon fontSelectIcon = GuiUtils.getIcon("fontOtf", 24);
	private final static Icon paragraphIcon = GuiUtils.getIcon("format-paragraph", 24);
	private final static Icon pictureIcon = GuiUtils.getIcon("insertPicture", 24);
	private final static Icon spellingIcon = GuiUtils.getIcon("checkSpelling", 24);

	private JTextPane textArea;
	private StyleContext w_context;
	private AdvancedRTFDocument w_doc;
	private AdvancedRTFEditorKit w_kit;
	private JFileChooser f_chooser;
	private JFileChooser m_chooser;
	private JToolBar f_toolBar;
	private JMenuBar menubar;
	private JComboBox f_cbFonts;
	private JComboBox f_cbSizes;
	private JToggleButton f_bBold;
	private JToggleButton f_bItalic;
	private JToggleButton f_underline;
	private JToggleButton f_strike;
	private JToggleButton w_btLeft;
	private JToggleButton w_btCenter;
	private JToggleButton w_btRight;
	private JToggleButton w_btJustified;
	private String f_fontName;
	private int f_fontSize;
	private boolean f_skipUpdate;
	private int f_xStart = -1;
	private int f_xFinish = -1;
	private ColorMenu w_foreground;
	private ColorMenu w_background;
	private MenuListener ml;
	private MenuListener ml2;
	private JComboBox w_cbStyles;
	private Action _undoAction;
	private Action _redoAction;
	private Action _copyAction;
	private Action _cutAction;
	private Action _pasteAction;
	private Action _findAction;
	private Action _replaceAction;
	private UndoManager m_undo;
	private FontDialog w_fontDialog;
	private String[] m_fontSizes;
	private String[] m_fontNames;
	private ParagraphDialog m_paragraphDialog;
	private FindDialog w_findDialog;

	static GraphicsEnvironment ge;

	CaretListener lsts = new CaretListener() {

		@Override
		public void caretUpdate(CaretEvent e) {
			showAttributes(e.getDot());
		}
	};
	FocusListener flst = new FocusListener() {

		@Override
		public void focusGained(FocusEvent e) {
			if (f_xStart >= 0 && f_xFinish >= 0) {
				if (textArea.getCaretPosition() == f_xStart) {
					textArea.setCaretPosition(f_xFinish);
					textArea.moveCaretPosition(f_xStart);
				} else {
					textArea.select(f_xStart, f_xFinish);
				}
			}
		}
		@Override
		public void focusLost(FocusEvent e) {
			f_xStart = textArea.getSelectionStart();
			f_xFinish = textArea.getSelectionEnd();
		}
	};

	public JWordProcessor(boolean displayOnly) {

		super(new BorderLayout());
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		f_fontName = "";
		f_fontSize = 0;
		createPanel(displayOnly);
		initValues();
		if(!displayOnly) {

			createMenu();
			createToolbar();
			//	showStyles();

			SpellChecker.setUserDictionaryProvider((UserDictionaryProvider) new FileUserDictionary());
			SpellChecker.registerDictionaries(null, null);
			SpellChecker.register((JTextComponent) textArea);
			w_doc.addUndoableEditListener(new Undoer());

			w_fontDialog = new FontDialog(m_fontNames, m_fontSizes);
			m_paragraphDialog = new ParagraphDialog();
			w_findDialog = new FindDialog(JWordProcessor.this, 0);
		}
	}

	private void initValues() {

		m_fontNames = ge.getAvailableFontFamilyNames();
		m_fontSizes = new String[]{
			"8", "9", "10", "11", "12", "14", "16", "18",
			"20", "22", "24", "26", "28", "36", "48", "72"};

		m_undo = new UndoManager();
		_undoAction = new AbstractAction("Undo", undoIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					m_undo.undo();
				} catch (CannotUndoException ex) {
					System.err.println("Unable to undo: " + ex);
				}
				updateUndo();
			}
		};
		_redoAction = new AbstractAction("Redo", redoIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					m_undo.redo();
				} catch (CannotUndoException ex) {
					System.err.println("Unable to undo: " + ex);
				}
				updateUndo();
			}
		};
		_copyAction = new AbstractAction("Copy", copyIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.copy();
			}
		};
		_cutAction = new AbstractAction("Cut", cutIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.cut();
			}
		};
		_pasteAction = new AbstractAction("Paste", pasteIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {

				Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
				if (transferable == null)
					return;

				if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
					pasteImage();
				else
					textArea.paste();
			}
		};
		w_foreground = new ColorMenu("Color Character");
		w_foreground.setColor(textArea.getForeground());
		w_foreground.addActionListener((ActionListener) new lstcolorsfg());
		ml = new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent e) { }

			@Override
			public void menuDeselected(MenuEvent e) { }

			@Override
			public void menuSelected(MenuEvent e) {
				int p = textArea.getCaretPosition();
				AttributeSet a = w_doc.getCharacterElement(p).getAttributes();
				Color c = StyleConstants.getForeground(a);
				w_foreground.setColor(c);
			}
		};
		w_foreground.addMenuListener(ml);

		w_background = new ColorMenu("Background");
		w_background.setColor(textArea.getBackground());
		w_background.addActionListener((ActionListener) new lstbkcolors());
		ml2 = new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent e) { }

			@Override
			public void menuDeselected(MenuEvent e) { }

			@Override
			public void menuSelected(MenuEvent e) {
				int p = textArea.getCaretPosition();
				AttributeSet a = w_doc.getCharacterElement(p).getAttributes();
				Color c = StyleConstants.getBackground(a);
				w_background.setColor(c);
			}
		};
		w_background.addMenuListener(ml2);

		_findAction = new AbstractAction("Find...", findIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
				if (w_findDialog == null)
					w_findDialog = new FindDialog(JWordProcessor.this, 0);

				w_findDialog.setLocationRelativeTo(JWordProcessor.this);
				w_findDialog.setSelectedIndex(0);
			}
		};
		_replaceAction = new AbstractAction("Replace...", findReplaceIcon) {

			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
				if (w_findDialog == null)
					w_findDialog = new FindDialog(JWordProcessor.this, 1);

				w_findDialog.setLocationRelativeTo(JWordProcessor.this);
				w_findDialog.setSelectedIndex(1);
			}
		};
	}

	private void createPanel(boolean displayOnly) {

		textArea = new JTextPane();
		w_kit = new AdvancedRTFEditorKit();
		textArea.setEditorKit(w_kit);
		w_doc = (AdvancedRTFDocument) w_kit.createDefaultDocument();
		//w_doc = new AdvancedRTFDocument();
		textArea.setDocument(w_doc);
		add((Component) new JScrollPane(textArea), "Center");
		textArea.addCaretListener(lsts);
		textArea.addFocusListener(flst);

		if(displayOnly) {
			textArea.setEditable(false);
		}
		else {
			//textArea.setDragEnabled(true);
			textArea.addKeyListener(new KeyListener() {

	            @Override
	            public void keyPressed(KeyEvent e) {
	                if ((e.getKeyCode() == KeyEvent.VK_V)
	                		&& ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0)) {
	                	pasteImage();
	                }
	            }

	            @Override
	            public void keyTyped(KeyEvent e) { }

	            @Override
	            public void keyReleased(KeyEvent e) { }
	        });
		}
	}

	public void clearPanel() {

		w_doc = (AdvancedRTFDocument) w_kit.createDefaultDocument();
		textArea.setDocument(w_doc);
	}

	public void createMenu() {

		menubar = new JMenuBar();

		//	File
		JMenu file = new JMenu("<html><u>F</u>ile</html>");
		file.setMnemonic('f');

		addMenuItem(file, "Open file", new OpenFileChooser(), openFileIcon,
			KeyStroke.getKeyStroke(79, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		addMenuItem(file, "Save to file", new SaveFileChooser(), saveFileIcon,
			KeyStroke.getKeyStroke(83, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		addMenuItem(file, "Print", new PrintDoc(), printIcon,
			KeyStroke.getKeyStroke(80, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

		menubar.add(file);

		//	Edit
		JMenu wEdit = new JMenu("<html><u>E</u>dit</html>");
		wEdit.setMnemonic('e');

		JMenuItem copyItem = wEdit.add(_copyAction);
		JMenuItem cutItem = wEdit.add(_cutAction);
		JMenuItem pasteItem = wEdit.add(_pasteAction);
		copyItem.setAccelerator(KeyStroke.getKeyStroke(67, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		cutItem.setAccelerator(KeyStroke.getKeyStroke(88, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		pasteItem.setAccelerator(KeyStroke.getKeyStroke(86, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		wEdit.add(copyItem);
		wEdit.add(cutItem);
		wEdit.add(pasteItem);

		wEdit.addSeparator();

		JMenuItem undoitem = wEdit.add(_undoAction);
		undoitem.setAccelerator(KeyStroke.getKeyStroke(90, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		JMenuItem redoitem = wEdit.add(_redoAction);
		redoitem.setAccelerator(KeyStroke.getKeyStroke(89, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		wEdit.add(undoitem);
		wEdit.add(redoitem);

		wEdit.addSeparator();

		JMenuItem finditem = wEdit.add(_findAction);
		finditem.setAccelerator(KeyStroke.getKeyStroke(70, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		JMenuItem replaceitem = wEdit.add(_replaceAction);
		replaceitem.setAccelerator(KeyStroke.getKeyStroke(82, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

		menubar.add(wEdit);

		//	Format
		JMenu format = new JMenu("<html>F<u>o</u>rmat</html>");
		format.setMnemonic('o');
		format.add((JMenuItem) w_foreground);
		format.add((JMenuItem) w_background);

		addMenuItem(format, "Font...", new openfontDialog(), fontSelectIcon,
				KeyStroke.getKeyStroke(84, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		addMenuItem(format, "Paragraph...", new openparaghDialog(), paragraphIcon,
				KeyStroke.getKeyStroke(55, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		addMenuItem(format, "Insert image", new OpenImageChooser(), pictureIcon, null);

		JMenu wStyle = new JMenu("Style");
		wStyle.setPreferredSize(preferredSize);

		addMenuItem(wStyle, "Update", new lstupdatestyle(), null,
				KeyStroke.getKeyStroke(85, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		addMenuItem(wStyle, "Reapply", new lstreapply(), null,
				KeyStroke.getKeyStroke(57, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));

		format.add(wStyle);

		menubar.add(format);
	}

	private void addMenuItem(
			JMenu menu,
			String commandName,
			ActionListener alistener,
			Icon defaultIcon,
			KeyStroke accelerator) {

		JMenuItem item = new JMenuItem(commandName);
		item.addActionListener(alistener);
		item.setIcon(defaultIcon);
		item.setAccelerator(accelerator);
		menu.add(item);
	}

	public void updateUndo() {
		if (m_undo.canUndo()) {
			_undoAction.setEnabled(true);
			_undoAction.putValue("Name", m_undo.getUndoPresentationName());
		} else {
			_undoAction.setEnabled(false);
			_undoAction.putValue("Name", "Undo");
		}
		if (m_undo.canRedo()) {
			_redoAction.setEnabled(true);
			_redoAction.putValue("Name", m_undo.getRedoPresentationName());
		} else {
			_redoAction.setEnabled(false);
			_redoAction.putValue("Name", "Redo");
		}
	}

	public void createToolbar() {

		f_toolBar = new JToolBar();
		//	f_toolBar.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		f_toolBar.setOpaque(false);
		f_toolBar.setFloatable(false);
		f_toolBar.setBackground(Color.LIGHT_GRAY);
		f_toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);

		Dimension buttonDimension = new Dimension(28, 28);

		GuiUtils.addButton(f_toolBar, undoIcon, _undoAction, "Undo", buttonDimension);
		GuiUtils.addButton(f_toolBar, redoIcon, _redoAction, "Redo", buttonDimension);

		f_toolBar.addSeparator();

		GuiUtils.addButton(f_toolBar, copyIcon, _copyAction, "Copy", buttonDimension);
		GuiUtils.addButton(f_toolBar, copyIcon, _cutAction, "Cut", buttonDimension);
		GuiUtils.addButton(f_toolBar, pasteIcon, _pasteAction, "Paste", buttonDimension);

		f_toolBar.addSeparator();

		f_bBold = GuiUtils.addToggleButton(f_toolBar, boldIcon, new boldFont(), "Bold", buttonDimension);
		f_bItalic = GuiUtils.addToggleButton(f_toolBar, italicIcon, new italicFont(), "Italic", buttonDimension);
		f_underline = GuiUtils.addToggleButton(f_toolBar, underlineIcon, new underline(), "Underline", buttonDimension);
		f_strike = GuiUtils.addToggleButton(f_toolBar, strikethroughIcon, new strikefont(), "Strikethrough", buttonDimension);

		f_toolBar.addSeparator();
		ActionListener alignListener = new alignText();
		w_btLeft = GuiUtils.addToggleButton(f_toolBar, alignLeftIcon, alignListener, "Left", buttonDimension);
		w_btCenter = GuiUtils.addToggleButton(f_toolBar, alignCenterIcon, alignListener, "Center", buttonDimension);
		w_btRight = GuiUtils.addToggleButton(f_toolBar, alignRightIcon, alignListener, "Right", buttonDimension);
		w_btJustified = GuiUtils.addToggleButton(f_toolBar, justifyIcon, alignListener, "Justify", buttonDimension);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(w_btLeft);
		buttonGroup.add(w_btCenter);
		buttonGroup.add(w_btRight);
		buttonGroup.add(w_btJustified);
		f_toolBar.addSeparator();

		GuiUtils.addButton(f_toolBar, findIcon, _findAction, "Find", buttonDimension);
		GuiUtils.addButton(f_toolBar, findReplaceIcon, _replaceAction, "Replace", buttonDimension);

		f_toolBar.addSeparator();

		f_cbFonts = new JComboBox<String>(m_fontNames);
		f_cbFonts.setMaximumSize(f_cbFonts.getPreferredSize());
		f_cbFonts.setEditable(true);
		f_cbSizes = new JComboBox<String>(m_fontSizes);
		f_cbSizes.setMaximumSize(f_cbSizes.getPreferredSize());
		//	w_cbStyles = new JComboBox();

		f_toolBar.add(f_cbFonts);
		f_toolBar.add(f_cbSizes);
		f_toolBar.addSeparator();

		GuiUtils.addButton(f_toolBar, fontSelectIcon, new openfontDialog(), "Font...", buttonDimension);
		GuiUtils.addButton(f_toolBar, paragraphIcon, new openparaghDialog(), "Paragraph...", buttonDimension);
		GuiUtils.addButton(f_toolBar, pictureIcon, new OpenImageChooser(), "Insert image", buttonDimension);

		f_toolBar.addSeparator();

		GuiUtils.addButton(f_toolBar, spellingIcon, new openSpellcheckDialog(), "Check spelling", buttonDimension);
	}

	public void showAttributes(int p) {

		if(!textArea.isEditable())
			return;

		int size;
		f_skipUpdate = true;
		AttributeSet a = w_doc.getCharacterElement(p).getAttributes();
		if(a == null)
			return;

		String name = StyleConstants.getFontFamily(a);

		if (!f_fontName.equals(name)) {
			f_fontName = name;
			f_cbFonts.setSelectedItem(name);
		}
		if (f_fontSize != (size = StyleConstants.getFontSize(a))) {
			f_fontSize = size;
			f_cbSizes.setSelectedItem(Integer.toString(f_fontSize));
		}
		f_skipUpdate = false;
	}

	public void showstyleAttributes(int p) {
		Style style = w_doc.getLogicalStyle(p);
		String name = style.getName();
		w_cbStyles.setSelectedItem(name);
		f_skipUpdate = false;
	}

	public void showStyles() {
		f_skipUpdate = true;
		if (w_cbStyles.getItemCount() > 0) {
			w_cbStyles.removeAllItems();
		}
		Enumeration<?> en = w_doc.getStyleNames();
		while (en.hasMoreElements()) {
			String str = en.nextElement().toString();
			w_cbStyles.addItem(str);
		}
		f_skipUpdate = false;
	}

	public void setAttributeSet(AttributeSet attr) {
		setAttributeSet(attr, false);
	}

	public void setAttributeSet(AttributeSet attr, boolean setParagraphAttributes) {
		if (f_skipUpdate) {
			return;
		}
		int xStart = textArea.getSelectionStart();
		int xFinish = textArea.getSelectionEnd();
		if (!textArea.hasFocus()) {
			xStart = f_xStart;
			xFinish = f_xFinish;
		}
		if (setParagraphAttributes) {
			w_doc.setParagraphAttributes(xStart, xFinish - xStart, attr, false);
		} else if (xStart != xFinish) {
			w_doc.setCharacterAttributes(xStart, xFinish - xStart, attr, false);
		} else {
			MutableAttributeSet inputAttributes = w_kit.getInputAttributes();
			inputAttributes.addAttributes(attr);
		}
	}

	public AdvancedRTFDocument getDocument() {
		return w_doc;
	}

	public JTextPane getTextPane() {
		return textArea;
	}

	public void setSelection(int xStart, int xFinish, boolean moveUp) {
		if (moveUp) {
			textArea.setCaretPosition(xFinish);
			textArea.moveCaretPosition(xStart);
		} else {
			textArea.select(xStart, xFinish);
		}
		f_xStart = textArea.getSelectionStart();
		f_xFinish = textArea.getSelectionEnd();
	}

	public class ExitCnf implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (textArea.getText() != "") {
				int option = JOptionPane.showConfirmDialog(null, "Do you want to save the changes?");
				if (option == 0) {
					block7 : {
						try {
							f_chooser = new JFileChooser();
							f_chooser.setFileFilter((FileFilter) new RftFileFilter());
							int n = f_chooser.showSaveDialog(JWordProcessor.this);
							f_chooser.setCurrentDirectory(new File("."));
							repaint();
							if (n != 0)
								break block7;
							File f = f_chooser.getSelectedFile();
							try {
								FileOutputStream out = new FileOutputStream(f, false);
								w_kit.write(out, (Document) w_doc, 0,
										w_doc.getLength());
								out.close();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							setCursor(Cursor.getPredefinedCursor(0));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					System.exit(0);
				}
				if (option == 1) {
					System.exit(0);
				}
			}
		}
	}

	public class NewDoc implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			block8 : {
				try {
					if (textArea.getText() == "")
						break block8;
					int option = JOptionPane.showConfirmDialog(null, "Do you want to save the changes?");
					if (option == 0) {
						block9 : {
							try {
								f_chooser = new JFileChooser();
								f_chooser.setFileFilter((FileFilter) new RftFileFilter());
								int n = f_chooser.showSaveDialog(JWordProcessor.this);
								f_chooser.setCurrentDirectory(new File("."));
								repaint();
								if (n != 0)
									break block9;
								File f = f_chooser.getSelectedFile();
								try {
									FileOutputStream out = new FileOutputStream(f);
									w_kit.write(out, (Document) w_doc, 0,
											w_doc.getLength());
									out.close();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								setCursor(Cursor.getPredefinedCursor(0));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						textArea.setText("");
					}
					if (option == 1) {
						textArea.setText("");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class OpenFileChooser implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			block4 : {
				try {
					textArea.setText("");
					f_chooser = new JFileChooser();
					f_chooser.setCurrentDirectory(new File("."));
					f_chooser.setFileFilter((FileFilter) new RftFileFilter());
					int n = f_chooser.showOpenDialog(JWordProcessor.this);
					if (n != 0)
						break block4;
					File f = f_chooser.getSelectedFile();
					repaint();
					try {
						FileInputStream in = new FileInputStream(f);
						w_kit.read(in, (Document) w_doc, 0);
						textArea.setDocument(w_doc);
						in.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					setCursor(Cursor.getPredefinedCursor(0));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class OpenImageChooser implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			block5 : {
				try {
					//	textArea.setText("");
					f_chooser = new JFileChooser();
					f_chooser.setCurrentDirectory(new File("."));
					int n = f_chooser.showOpenDialog(JWordProcessor.this);
					if (n != 0)
						break block5;
					File f = f_chooser.getSelectedFile();
					repaint();
					try {
						ImageIcon icon = new ImageIcon(f.getPath());
/*						int w = icon.getIconWidth();
						int h = icon.getIconHeight();
						if (w <= 0 || h <= 0) {
							JOptionPane.showMessageDialog(JWordProcessor.this,
									"Error reading image file\n" + f.getPath(), "Warning", 2);
							return;
						}
						SimpleAttributeSet attr = new SimpleAttributeSet();
						StyleConstants.setIcon(attr, icon);
						int p = textArea.getCaretPosition();
						w_doc.insertString(p, " ", attr);*/
						w_doc.insertPicture(icon, textArea.getCaretPosition());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					setCursor(Cursor.getPredefinedCursor(0));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class PrintDoc implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				boolean done = textArea.print();
				if (done) {
					JOptionPane.showMessageDialog(JWordProcessor.this, "Printing is done");
				}
			} catch (PrinterException ex) {
				ex.printStackTrace();
			}
		}
	}

	public class SaveFileChooser implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			block5 : {
				try {
					int response;
					f_chooser = new JFileChooser();
					f_chooser.setFileFilter((FileFilter) new RftFileFilter());
					int n = f_chooser.showSaveDialog(JWordProcessor.this);
					f_chooser.setCurrentDirectory(new File("."));
					repaint();
					if (n != 0)
						break block5;
					File f = f_chooser.getSelectedFile();
					if (f.exists() && (response = JOptionPane.showConfirmDialog(null,
							"Do you want to replace the existing file?", "Confirm", 0, 3)) != 0) {
						return;
					}
					try {
						w_kit.write(f.getAbsolutePath(), w_doc);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					setCursor(Cursor.getPredefinedCursor(0));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public class Undoer implements UndoableEditListener {
		public Undoer() {
			m_undo.die();
			updateUndo();
		}

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			m_undo.addEdit(e.getEdit());
			updateUndo();
		}
	}

	public class boldFont implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setBold(attr, f_bBold.isSelected());
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class italicFont implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setItalic(attr, f_bItalic.isSelected());
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class lst implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			f_fontName = f_cbFonts.getSelectedItem().toString();
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setFontFamily(attr, f_fontName);
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class lstbkcolors implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setBackground(attr, w_background.getColor());
			setAttributeSet(attr);
		}
	}

	public class lstcbstyle implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (f_skipUpdate || w_cbStyles.getItemCount() == 0) {
				return;
			}
			String name = (String) w_cbStyles.getSelectedItem();
			int index = w_cbStyles.getSelectedIndex();
			int p = textArea.getCaretPosition();
			if (index == -1) {
				w_cbStyles.addItem(name);
				Style style = w_doc.addStyle(name, null);
				AttributeSet a = w_doc.getCharacterElement(p).getAttributes();
				style.addAttributes(a);
				return;
			}
			Style currStyle = w_doc.getLogicalStyle(p);
			if (!currStyle.getName().equals(name)) {
				Style style = w_doc.getStyle(name);
				setAttributeSet(style);
			}
		}
	}

	public class lstcolorsfg implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setForeground(attr, w_foreground.getColor());
			setAttributeSet(attr);
		}
	}

	public class lstreapply implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = (String) w_cbStyles.getSelectedItem();
			Style style = w_doc.getStyle(name);
			setAttributeSet(style);
		}
	}

	public class lstsizes implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int fontSize = 0;
			try {
				fontSize = Integer.parseInt(f_cbSizes.getSelectedItem().toString());
			} catch (Exception ex) {
				System.out.println(ex);
			}
			f_fontSize = fontSize;
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setFontSize(attr, fontSize);
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class lstupdatestyle implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			String name = (String) w_cbStyles.getSelectedItem();
			Style style = w_doc.getStyle(name);
			int p = textArea.getCaretPosition();
			AttributeSet a = w_doc.getCharacterElement(p).getAttributes();
			style.addAttributes(a);
			textArea.repaint();
		}
	}

	public class openSpellcheckDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			SpellChecker.showSpellCheckerDialog((JTextComponent) textArea, null);
		}
	}

	public class openfontDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			repaint();
			AttributeSet a = w_doc.getCharacterElement(textArea.getCaretPosition())
					.getAttributes();
			w_fontDialog.setAttributesadvf(a);
//			Dimension d1 = w_fontDialog.getSize();
//			Dimension d2 = getSize();
//			int x = Math.max((d2.width - d1.width) / 2, 0);
//			int y = Math.max((d2.height - d1.height) / 2, 0);
//			w_fontDialog.setBounds(x + getX(), y + getY(),
//					d1.width, d1.height);
			w_fontDialog.setLocationRelativeTo(JWordProcessor.this);
			w_fontDialog.setVisible(true);
			if (w_fontDialog.getOption() == 0) {
				setAttributeSet(w_fontDialog.getAttributesadvf());
				showAttributes(textArea.getCaretPosition());
			}
		}
	}

	public class openparaghDialog implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			repaint();
			AttributeSet a = w_doc.getCharacterElement(textArea.getCaretPosition())
					.getAttributes();
			m_paragraphDialog.setAttributesph(a);
//			Dimension d1 = m_paragraphDialog.getSize();
//			Dimension d2 = getSize();
//			int x = Math.max((d2.width - d1.width) / 2, 0);
//			int y = Math.max((d2.height - d1.height) / 2, 0);
//			m_paragraphDialog.setBounds(x + getX(), y + getY(),
//					d1.width, d1.height);
			m_paragraphDialog.setLocationRelativeTo(JWordProcessor.this);
			m_paragraphDialog.setVisible(true);
			if (m_paragraphDialog.getOption() == 0) {
				setAttributeSet(m_paragraphDialog.getAttributesph(), true);
				showAttributes(textArea.getCaretPosition());
			}
		}
	}

	public class strikefont implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setStrikeThrough(attr, f_strike.isSelected());
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class underline implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SimpleAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setUnderline(attr, f_underline.isSelected());
			setAttributeSet(attr);
			textArea.grabFocus();
		}
	}

	public class alignText implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			AttributeSet a =
					new SimpleAttributeSet(w_doc.
							getCharacterElement(textArea.getCaretPosition()).getAttributes());

			StyleConstants.setAlignment((SimpleAttributeSet)a, getAlignment());
			setAttributeSet(a, true);
			textArea.grabFocus();
		}
	}

	private int getAlignment() {
		if (w_btLeft.isSelected()) {
			return StyleConstants.ALIGN_LEFT;
		}
		if (w_btCenter.isSelected()) {
			return StyleConstants.ALIGN_CENTER;
		}
		if (w_btRight.isSelected()) {
			return StyleConstants.ALIGN_RIGHT;
		}
		return StyleConstants.ALIGN_JUSTIFIED;
	}

	/**
	 * @return the textArea
	 */
	public JTextPane getTextArea() {
		return textArea;
	}

	/**
	 * @return the w_doc
	 */
	public Document getStyledDocument() {
		return w_doc;
	}

	/**
	 * @return the w_kit
	 */
	public AdvancedRTFEditorKit getRTFEditorKit() {
		return w_kit;
	}

	/**
	 * @return the f_toolBar
	 */
	public JToolBar getF_toolBar() {
		return f_toolBar;
	}

	/**
	 * @return the menubar
	 */
	public JMenuBar getMenubar() {
		return menubar;
	}

	/**
	 * @return the w_fontDialog
	 */
	public FontDialog getW_fontDialog() {
		return w_fontDialog;
	}

	/**
	 * @return the m_paragraphDialog
	 */
	public ParagraphDialog getM_paragraphDialog() {
		return m_paragraphDialog;
	}

	/**
	 * @return the w_findDialog
	 */
	public FindDialog getW_findDialog() {
		return w_findDialog;
	}

	private void pasteImage() {

		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable == null)
			return;

		if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				Image im = (Image) transferable.getTransferData(DataFlavor.imageFlavor);
				w_doc.insertPicture(new ImageIcon(im), textArea.getCaretPosition());
			}
			catch (UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			setCursor(Cursor.getPredefinedCursor(0));
		}
	}

    private void createStyles(StyledDocument doc) {

        Style baseStyle = doc.addStyle("base", null);
        StyleConstants.setFontFamily(baseStyle, "Lucida Sans Unicode");
        StyleConstants.setFontSize(baseStyle, 18);
        StyleConstants.setLeftIndent(baseStyle, 10f);

        Style style = doc.addStyle("bold", baseStyle);
        StyleConstants.setBold(style, true);

        style = doc.addStyle("italic", baseStyle);
        StyleConstants.setItalic(style, true);

        style = doc.addStyle("blue", baseStyle);
        StyleConstants.setForeground(style, Color.blue);

        style = doc.addStyle("underline", baseStyle);
        StyleConstants.setUnderline(style, true);

        style = doc.addStyle("green", baseStyle);
        StyleConstants.setForeground(style, Color.green.darker());
        StyleConstants.setUnderline(style, true);

        style = doc.addStyle("highlight", baseStyle);
        StyleConstants.setForeground(style, Color.yellow);
        StyleConstants.setBackground(style, Color.black);
    }

    public void loadDocument(Document doc) {

    	if(doc == null)
    		doc = w_kit.createDefaultDocument();

    	w_doc = (AdvancedRTFDocument) doc;
    	textArea.setDocument(w_doc);
    }
}














