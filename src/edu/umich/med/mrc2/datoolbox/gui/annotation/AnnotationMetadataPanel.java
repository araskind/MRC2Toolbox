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

package edu.umich.med.mrc2.datoolbox.gui.annotation;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;

public class AnnotationMetadataPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6971804850625454514L;
	private JLabel createdByUserLabel;
	private JLabel dateCreatedLabel;
	private JLabel editedByUserLabel;
	private JLabel dateEditedLabel;
	private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	private LinkMouseListener linkMouseListener;

	public AnnotationMetadataPanel() {
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblCreatedBy = new JLabel("Created by: ");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.EAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 0;
		add(lblCreatedBy, gbc_lblCreatedBy);
		
		createdByUserLabel = new JLabel("");
		GridBagConstraints gbc_ctreatedByUserLabel = new GridBagConstraints();
		gbc_ctreatedByUserLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_ctreatedByUserLabel.insets = new Insets(0, 0, 5, 5);
		gbc_ctreatedByUserLabel.anchor = GridBagConstraints.BELOW_BASELINE;
		gbc_ctreatedByUserLabel.gridx = 1;
		gbc_ctreatedByUserLabel.gridy = 0;
		add(createdByUserLabel, gbc_ctreatedByUserLabel);
		
		JLabel lblCreatedOn = new JLabel("Created on: ");
		GridBagConstraints gbc_lblCreatedOn = new GridBagConstraints();
		gbc_lblCreatedOn.anchor = GridBagConstraints.EAST;
		gbc_lblCreatedOn.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedOn.gridx = 2;
		gbc_lblCreatedOn.gridy = 0;
		add(lblCreatedOn, gbc_lblCreatedOn);
		
		dateCreatedLabel = new JLabel("");
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dateCreatedLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateCreatedLabel.anchor = GridBagConstraints.SOUTH;
		gbc_dateCreatedLabel.gridx = 3;
		gbc_dateCreatedLabel.gridy = 0;
		add(dateCreatedLabel, gbc_dateCreatedLabel);
		
		JLabel lblEditedBy = new JLabel("Edited by: ");
		GridBagConstraints gbc_lblEditedBy = new GridBagConstraints();
		gbc_lblEditedBy.anchor = GridBagConstraints.EAST;
		gbc_lblEditedBy.insets = new Insets(0, 0, 0, 5);
		gbc_lblEditedBy.gridx = 0;
		gbc_lblEditedBy.gridy = 1;
		add(lblEditedBy, gbc_lblEditedBy);
		
		editedByUserLabel = new JLabel("");
		GridBagConstraints gbc_editedByUserLabel = new GridBagConstraints();
		gbc_editedByUserLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_editedByUserLabel.insets = new Insets(0, 0, 0, 5);
		gbc_editedByUserLabel.gridx = 1;
		gbc_editedByUserLabel.gridy = 1;
		add(editedByUserLabel, gbc_editedByUserLabel);
		
		JLabel lblEditedOn = new JLabel("Edited on: ");
		GridBagConstraints gbc_lblEditedOn = new GridBagConstraints();
		gbc_lblEditedOn.anchor = GridBagConstraints.EAST;
		gbc_lblEditedOn.insets = new Insets(0, 0, 0, 5);
		gbc_lblEditedOn.gridx = 2;
		gbc_lblEditedOn.gridy = 1;
		add(lblEditedOn, gbc_lblEditedOn);
		
		dateEditedLabel = new JLabel("");
		GridBagConstraints gbc_dateEditedLabel = new GridBagConstraints();
		gbc_dateEditedLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateEditedLabel.gridx = 3;
		gbc_dateEditedLabel.gridy = 1;
		add(dateEditedLabel, gbc_dateEditedLabel);
		
		linkMouseListener = new LinkMouseListener();
	}
	
	public void loadAnnotation(ObjectAnnotation annotation) {
		
		clearPanel();
		createdByUserLabel.setText(getUserLabel(annotation.getCreateBy()));
		createdByUserLabel.setName(annotation.getCreateBy().getId());
		createdByUserLabel.addMouseListener(linkMouseListener);
		createdByUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		dateCreatedLabel.setText(dateFormat.format(annotation.getDateCreated()));
		if(annotation.getLastModifiedBy() != null) {
			
			editedByUserLabel.setText(getUserLabel(annotation.getLastModifiedBy()));
			editedByUserLabel.setName(annotation.getLastModifiedBy().getId());
			editedByUserLabel.addMouseListener(linkMouseListener);
			editedByUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			dateEditedLabel.setText(dateFormat.format(annotation.getLastModified()));
		}
	}
	
	public void  clearPanel() {
		
		createdByUserLabel.setText("");
		createdByUserLabel.setName(null);
		dateCreatedLabel.setText("");
		editedByUserLabel.setText("");
		editedByUserLabel.setName(null);
		dateEditedLabel.setText("");
		
		createdByUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		createdByUserLabel.removeMouseListener(linkMouseListener);
		editedByUserLabel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		editedByUserLabel.removeMouseListener(linkMouseListener);
	}
	
	private String getUserLabel(LIMSUser user) {
		
		if(user.getEmail() == null)
			return user.getFullName();
		
		if(user.getEmail().isEmpty())
			return user.getFullName();
		
		return "<html><u><font color='blue'><A HREF=\"mailto:" + 
		user.getEmail() + "\">" + user.getFullName() + "</A>";
	}
	
	private static class LinkMouseListener extends MouseAdapter {

	    @Override
	    public void mouseClicked(java.awt.event.MouseEvent evt) {
	    	
	        JLabel l = (JLabel) evt.getSource();
	        if(l.getName() == null)
	        	return;
	        
			LIMSUser user = IDTDataCache.getUserById(l.getName());
			if(user == null)
				return;
			
			if(user.getEmail() == null)
				return;

			if(user.getEmail().isEmpty())
				return;

	        try {
	            Desktop.getDesktop().mail(new URI("mailto:" + user.getEmail()));
	        } catch (URISyntaxException | IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	}
}
