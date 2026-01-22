/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class RecentFilesDialog extends JDialog implements ActionListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8104188743164691852L;
	private static final Icon dialogIcon = GuiUtils.getIcon("openCollection", 32);
	
	private Collection<File>recentFiles;
	private FilesTable filesTable;

	public RecentFilesDialog(
			String title, 
			Collection<File> recentFiles, 
			ActionListener actionListener,
			String selectCommand) {
		super();
		setTitle(title);
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 300));
		setPreferredSize(new Dimension(800, 300));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.recentFiles = recentFiles;
		
		filesTable = new FilesTable();
		filesTable.setTableModelFromFileCollection(recentFiles);	

		JScrollPane scrollPane = new JScrollPane(filesTable);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(selectCommand);
		btnSave.setActionCommand(selectCommand);
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		filesTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							btnSave.doClick();
						}
					}
				});		
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public File getSelectedFile() {
		return filesTable.getSelectedFile();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
}
