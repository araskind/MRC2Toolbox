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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.DatabseDialect;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DatabasePreferencesPanel extends JPanel 
	implements BackedByPreferences, ActionListener, ItemListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6402454920107583369L;
	private Preferences prefs;
	private JTextField hostNameTextField;
	private JTextField sidTextField;
	private JPasswordField passwordField;
	private JTextField userTextField;
	private JComboBox databaseTypeComboBox;
	private JFormattedTextField portTextField;
	private JButton testConnectionButton;
	private JButton saveConnectionButton;
	private JLabel lblNewLabel_6;
	private JTextField dbSchemaTextField;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public DatabasePreferencesPanel() {

		super(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Database type");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		databaseTypeComboBox = new JComboBox(
				new DefaultComboBoxModel<DatabseDialect>(new DatabseDialect[] {
						DatabseDialect.Oracle,
						DatabseDialect.PostgreSQL,
				}));
		GridBagConstraints gbc_databaseTypeComboBox = new GridBagConstraints();
		gbc_databaseTypeComboBox.gridwidth = 2;
		gbc_databaseTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_databaseTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_databaseTypeComboBox.gridx = 1;
		gbc_databaseTypeComboBox.gridy = 0;
		panel.add(databaseTypeComboBox, gbc_databaseTypeComboBox);
		
		JLabel lblNewLabel_1 = new JLabel("Host name");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		hostNameTextField = new JTextField();
		GridBagConstraints gbc_hostNameTextField = new GridBagConstraints();
		gbc_hostNameTextField.gridwidth = 2;
		gbc_hostNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_hostNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_hostNameTextField.gridx = 1;
		gbc_hostNameTextField.gridy = 1;
		panel.add(hostNameTextField, gbc_hostNameTextField);
		hostNameTextField.setColumns(10);
		
		JLabel lblNewLabel_2 = new JLabel("Port");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		portTextField = new JFormattedTextField(new DecimalFormat("###"));
		portTextField.setColumns(10);
		GridBagConstraints gbc_portTextField = new GridBagConstraints();
		gbc_portTextField.gridwidth = 2;
		gbc_portTextField.insets = new Insets(0, 0, 5, 0);
		gbc_portTextField.anchor = GridBagConstraints.WEST;
		gbc_portTextField.gridx = 1;
		gbc_portTextField.gridy = 2;
		panel.add(portTextField, gbc_portTextField);
		
		JLabel lblNewLabel_3 = new JLabel("SID/database");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 3;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		sidTextField = new JTextField();
		GridBagConstraints gbc_sidTextField = new GridBagConstraints();
		gbc_sidTextField.gridwidth = 2;
		gbc_sidTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sidTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sidTextField.gridx = 1;
		gbc_sidTextField.gridy = 3;
		panel.add(sidTextField, gbc_sidTextField);
		sidTextField.setColumns(10);
		
		lblNewLabel_6 = new JLabel("Schema");
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 4;
		panel.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		dbSchemaTextField = new JTextField();
		GridBagConstraints gbc_dbSchemaTextField = new GridBagConstraints();
		gbc_dbSchemaTextField.gridwidth = 2;
		gbc_dbSchemaTextField.insets = new Insets(0, 0, 5, 5);
		gbc_dbSchemaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dbSchemaTextField.gridx = 1;
		gbc_dbSchemaTextField.gridy = 4;
		panel.add(dbSchemaTextField, gbc_dbSchemaTextField);
		dbSchemaTextField.setColumns(10);
		
		JLabel lblNewLabel_4 = new JLabel("User");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 5;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		userTextField = new JTextField();
		GridBagConstraints gbc_userTextField = new GridBagConstraints();
		gbc_userTextField.gridwidth = 2;
		gbc_userTextField.insets = new Insets(0, 0, 5, 0);
		gbc_userTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_userTextField.gridx = 1;
		gbc_userTextField.gridy = 5;
		panel.add(userTextField, gbc_userTextField);
		userTextField.setColumns(10);
		
		JLabel lblNewLabel_5 = new JLabel("Password");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 6;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(30);
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.insets = new Insets(0, 0, 5, 0);
		gbc_passwordField.gridwidth = 2;
		gbc_passwordField.anchor = GridBagConstraints.WEST;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 6;
		panel.add(passwordField, gbc_passwordField);
		
		testConnectionButton = new JButton("Test connection");
		testConnectionButton.setActionCommand(MainActionCommands.TEST_DATABASE_CONNECTION.getName());
		testConnectionButton.addActionListener(this);
		GridBagConstraints gbc_testConnectionButton = new GridBagConstraints();
		gbc_testConnectionButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_testConnectionButton.insets = new Insets(0, 0, 0, 5);
		gbc_testConnectionButton.gridx = 1;
		gbc_testConnectionButton.gridy = 7;
		panel.add(testConnectionButton, gbc_testConnectionButton);
		
		saveConnectionButton = new JButton("Save connection");
		saveConnectionButton.setActionCommand(MainActionCommands.SAVE_DATABASE_CONNECTION.getName());
		saveConnectionButton.addActionListener(this);
		GridBagConstraints gbc_saveConnectionButton = new GridBagConstraints();
		gbc_saveConnectionButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_saveConnectionButton.gridx = 2;
		gbc_saveConnectionButton.gridy = 7;
		panel.add(saveConnectionButton, gbc_saveConnectionButton);
		
		loadPreferences();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.TEST_DATABASE_CONNECTION.getName()))
				testConnection(false);
		
		if(e.getActionCommand().equals(MainActionCommands.SAVE_DATABASE_CONNECTION.getName()))
				saveConnection();
	}
	
	public void saveConnection() {
		
		if(testConnection(true)) {
			
			savePreferences();
			MessageDialog.showWarningMsg(
				"Database connection information saved..", this);
		}
 	}

	public boolean testConnection(boolean silentOnSuccess) {

		Collection<String>errors = verifyConnectionParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}
        Connection conn = null;
        String url = createJdbcConnectionString();
        try {
            conn = DriverManager.getConnection(url, getUserName(), getPassword());
            if(!silentOnSuccess)
            	MessageDialog.showInfoMsg("Connection successful", this);  
            
            if(conn != null)
            	conn.close();
            
            return true;
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        	MessageDialog.showErrorMsg("Connection failled!\n\n" + e.getMessage(), this);
        	return false;
        }
       
	}

	private Collection<String>verifyConnectionParameters(){
		
		ArrayList<String>errors = new ArrayList<String>();
		
		if(hostNameTextField.getText().trim().isEmpty())
			errors.add("Missing host name.");
		
		if(portTextField.getText().trim().isEmpty())
			errors.add("Missing port number.");
		
		if(sidTextField.getText().trim().isEmpty()) {
			
			if(getDatabseDialect().equals(DatabseDialect.Oracle))
				errors.add("Missing SID/service name.");
			
			if(getDatabseDialect().equals(DatabseDialect.PostgreSQL))
				errors.add("Missing database name.");
		}
		if(getDatabseDialect().equals(DatabseDialect.PostgreSQL) 
				&& dbSchemaTextField.getText().trim().isEmpty())
			errors.add("Missing database schema name.");

		if(userTextField.getText().trim().isEmpty())
			errors.add("Missing user name.");
		
		if(getPassword().isEmpty())
			errors.add("Missing password.");
		
		return errors;
	}
	
	private DatabseDialect getDatabseDialect() {
		return (DatabseDialect)databaseTypeComboBox.getSelectedItem();
	}
	
	private String createJdbcConnectionString() {

		String connectionString = null;
		DatabseDialect dct = getDatabseDialect();
		if(dct.equals(DatabseDialect.Oracle)) {

			connectionString = 
					 dct.getJdbcString() + 
					 hostNameTextField.getText().trim() + ":" + 
					 portTextField.getText().trim() + ":" + 
					 sidTextField.getText().trim();
		}
		if(dct.equals(DatabseDialect.PostgreSQL)) {
	
			connectionString = 
					 dct.getJdbcString() + 
					 hostNameTextField.getText().trim() + ":" + 
					 portTextField.getText().trim() + "/" + 
					 sidTextField.getText().trim() + 
					 "?currentSchema=" + dbSchemaTextField.getText().trim() +
					 "&ssl=true" +
					 "&sslmode=require" +
					 "&sslfactory=org.postgresql.ssl.NonValidatingFactory" +
					 "&loggerLevel=OFF";
		}
		return connectionString;
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		prefs = preferences;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		
		databaseTypeComboBox.setSelectedItem(MRC2ToolBoxConfiguration.getDatabaseType());
		hostNameTextField.setText(MRC2ToolBoxConfiguration.getDatabaseHost());
		portTextField.setText(MRC2ToolBoxConfiguration.getDatabasePort());
		sidTextField.setText(MRC2ToolBoxConfiguration.getDatabaseNameSid());
		dbSchemaTextField.setText(MRC2ToolBoxConfiguration.getDatabaseSchema());
		userTextField.setText(MRC2ToolBoxConfiguration.getDatabaseUserName());
		passwordField.setText(MRC2ToolBoxConfiguration.getDatabasePassword());
	}

	@Override
	public void savePreferences() {

		MRC2ToolBoxConfiguration.setDatabaseType((DatabseDialect) databaseTypeComboBox.getSelectedItem());
		MRC2ToolBoxConfiguration.setDatabaseHost(hostNameTextField.getText().trim());
		MRC2ToolBoxConfiguration.setDatabasePort(portTextField.getText().trim());
		MRC2ToolBoxConfiguration.setDatabaseNameSid(sidTextField.getText().trim());
		MRC2ToolBoxConfiguration.setDatabaseSchema(dbSchemaTextField.getText().trim());
		MRC2ToolBoxConfiguration.setDatabaseUserName(getUserName());
		MRC2ToolBoxConfiguration.setDatabasePassword(getPassword());
		String url = createJdbcConnectionString();
		MRC2ToolBoxConfiguration.setDatabaseConnectionString(url);
	}
	
	public String getUserName() {
		return userTextField.getText().trim();
	}

	public String getPassword() {
		return new String(passwordField.getPassword());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		// TODO change defaults based on the database type selection
		if (e.getStateChange() == ItemEvent.SELECTED) {
			
		}
	}
}

















