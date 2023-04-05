/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.search;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataFieldCategory;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class PropertySearchDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2183793780141152240L;
	private static final Icon searchIcon = GuiUtils.getIcon("searchCompounds", 32);
	
	private Map<CpdMetadataField,String>queryMap;
	private Map<CpdMetadataField,PropertySearchPanel>panelMap;
	
	public PropertySearchDialog(ActionListener actionListener) {
		super();
		setTitle("Search compounds by properties");
		setIconImage(((ImageIcon) searchIcon).getImage());
		setPreferredSize(new Dimension(800, 800));
		setSize(new Dimension(800, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		queryMap = new HashMap<CpdMetadataField,String>();
		panelMap = new HashMap<CpdMetadataField,PropertySearchPanel>();
		
		JTabbedPane tabbedSearchPanel = new JTabbedPane();
		tabbedSearchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(tabbedSearchPanel, BorderLayout.CENTER);

		Collection<CpdMetadataField>fields = new HashSet<CpdMetadataField>();
		try {
			fields = CompoundMultiplexUtils.getCpdMetadataFields();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<CpdMetadataFieldCategory>categories =
				fields.stream().
				map(f -> f.getCategory()).distinct().
				sorted().collect(Collectors.toList());
		for(CpdMetadataFieldCategory category : categories) {
			
			Collection<CpdMetadataField>categoryFields = 
					fields.stream().filter(c -> c.getCategory().equals(category)).
					sorted().collect(Collectors.toList());
			JPanel categoryPanel = createCategorySearchPanel(categoryFields);
			
			tabbedSearchPanel.addTab(category.getName(), new JScrollPane(categoryPanel));
		}		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.SEARCH_COMPOUND_PROPERTIES_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SEARCH_COMPOUND_PROPERTIES_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}

	private JPanel createCategorySearchPanel(Collection<CpdMetadataField> categoryFields) {
		
		JPanel categoryPanel = new JPanel();
		categoryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		int numRows = categoryFields.size() + 2;
		int rh[] = new int[numRows];
		Arrays.fill(rh, 0);		
		double rw[] = new double[numRows];
		Arrays.fill(rw, 0.0);
		rw[rw.length-2] = 1.0;
		rw[rw.length-1] = Double.MIN_VALUE;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowHeights = rh;		
		gridBagLayout.rowWeights = rw;
		categoryPanel.setLayout(gridBagLayout);
		
		int rowCount = 0;
		for(CpdMetadataField field : categoryFields) {
						
			PropertySearchPanel panel = new PropertySearchPanel(field);
			panelMap.put(field, panel);
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.anchor = GridBagConstraints.NORTH;
			gbc_panel.insets = new Insets(0, 0, 5, 0);			
			gbc_panel.fill = GridBagConstraints.HORIZONTAL;
			gbc_panel.gridx = 0;
			gbc_panel.gridy = rowCount;
			categoryPanel.add(panel, gbc_panel);
			
			rowCount++;
		}
		JLabel lblNewLabel = new JLabel(" ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = rowCount;
		categoryPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		return categoryPanel;
	}

}

































