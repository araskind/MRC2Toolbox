package edu.umich.med.mrc2.datoolbox.gui.rawdata.manint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.stream.IntStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ManualIntegrationSettingsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1619715915840696181L;
	private static final Icon dialogIcon = GuiUtils.getIcon("preferences", 32);
	private JFormattedTextField rtWindowExtensionTextFieldTextField;
	private JFormattedTextField massErrorTextField;
	private JComboBox filterWidthComboBox;
	private JComboBox massErrorTypeComboBox;
	private JSpinner plotHeightSpinner;
	JSpinner maxIsotopeIntensityErrorSpinner;

	public ManualIntegrationSettingsDialog(ActionListener listener) {
		super();
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(400, 250));
		setTitle("Manual integration preferences");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 84, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("RT window extension width");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		rtWindowExtensionTextFieldTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowExtensionTextFieldTextField.setMinimumSize(new Dimension(80, 20));
		rtWindowExtensionTextFieldTextField.setPreferredSize(new Dimension(80, 20));
		rtWindowExtensionTextFieldTextField.setColumns(6);
		GridBagConstraints gbc_rtWindowExtensionTextFieldTextField = new GridBagConstraints();
		gbc_rtWindowExtensionTextFieldTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtWindowExtensionTextFieldTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtWindowExtensionTextFieldTextField.gridx = 1;
		gbc_rtWindowExtensionTextFieldTextField.gridy = 0;
		panel.add(rtWindowExtensionTextFieldTextField, gbc_rtWindowExtensionTextFieldTextField);
		
		JLabel lblNewLabel_1 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Smoothing filter width");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 1;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		Integer[] fwList = IntStream.rangeClosed(3, 30).boxed().toArray(size -> new Integer[size]); 
		filterWidthComboBox = new JComboBox<Integer>(new DefaultComboBoxModel<Integer>(fwList));
		GridBagConstraints gbc_filterWidthComboBox = new GridBagConstraints();
		gbc_filterWidthComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_filterWidthComboBox.gridwidth = 2;
		gbc_filterWidthComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterWidthComboBox.gridx = 1;
		gbc_filterWidthComboBox.gridy = 1;
		panel.add(filterWidthComboBox, gbc_filterWidthComboBox);
		
		JLabel lblNewLabel_3 = new JLabel("Mass error");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setPreferredSize(new Dimension(80, 20));
		massErrorTextField.setMinimumSize(new Dimension(80, 20));
		massErrorTextField.setColumns(6);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 2;
		panel.add(massErrorTextField, gbc_formattedTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_massErrorTypeComboBox = new GridBagConstraints();
		gbc_massErrorTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_massErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTypeComboBox.gridx = 2;
		gbc_massErrorTypeComboBox.gridy = 2;
		panel.add(massErrorTypeComboBox, gbc_massErrorTypeComboBox);
		
		JLabel lblMaxIsotopeIntensity = new JLabel("Max isotope intensity error");
		GridBagConstraints gbc_lblMaxIsotopeIntensity = new GridBagConstraints();
		gbc_lblMaxIsotopeIntensity.anchor = GridBagConstraints.EAST;
		gbc_lblMaxIsotopeIntensity.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxIsotopeIntensity.gridx = 0;
		gbc_lblMaxIsotopeIntensity.gridy = 3;
		panel.add(lblMaxIsotopeIntensity, gbc_lblMaxIsotopeIntensity);
		
		maxIsotopeIntensityErrorSpinner = new JSpinner();
		maxIsotopeIntensityErrorSpinner.setModel(new SpinnerNumberModel(30, 0, 100, 1));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 3;
		panel.add(maxIsotopeIntensityErrorSpinner, gbc_spinner);
		
		JLabel label = new JLabel("%");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 2;
		gbc_label.gridy = 3;
		panel.add(label, gbc_label);
		
		JLabel lblMinPlotHeight = new JLabel("Min. plot height");
		GridBagConstraints gbc_lblMinPlotHeight = new GridBagConstraints();
		gbc_lblMinPlotHeight.anchor = GridBagConstraints.EAST;
		gbc_lblMinPlotHeight.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinPlotHeight.gridx = 0;
		gbc_lblMinPlotHeight.gridy = 4;
		panel.add(lblMinPlotHeight, gbc_lblMinPlotHeight);
		
		plotHeightSpinner = new JSpinner();
		plotHeightSpinner.setModel(new SpinnerNumberModel(300, 100, 600, 10));
		GridBagConstraints gbc_plotHeightSpinner = new GridBagConstraints();
		gbc_plotHeightSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_plotHeightSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_plotHeightSpinner.gridx = 1;
		gbc_plotHeightSpinner.gridy = 4;
		panel.add(plotHeightSpinner, gbc_plotHeightSpinner);
		
		JLabel lblPx = new JLabel("px");
		GridBagConstraints gbc_lblPx = new GridBagConstraints();
		gbc_lblPx.anchor = GridBagConstraints.WEST;
		gbc_lblPx.insets = new Insets(0, 0, 0, 5);
		gbc_lblPx.gridx = 2;
		gbc_lblPx.gridy = 4;
		panel.add(lblPx, gbc_lblPx);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		JButton exportButton = new JButton(MainActionCommands.SAVE_MANUAL_INTEGRATOR_SETTINGS.getName());
		buttonPanel.add(exportButton);
		exportButton.setActionCommand(MainActionCommands.SAVE_MANUAL_INTEGRATOR_SETTINGS.getName());
		exportButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(exportButton);
		rootPane.setDefaultButton(exportButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}
	
	public int getSmoothingFilterWidth() {		
		return (int)filterWidthComboBox.getSelectedItem();
	}
	
	public void setSmoothingFilterWidth(int width) {
		filterWidthComboBox.setSelectedItem(width);
	}
	
	public double getMzError() {
		
		if(!massErrorTextField.getText().trim().isEmpty())
			return Double.parseDouble(massErrorTextField.getText().trim());
		else
			return 0.0d;
	}
	
	public void setMzError(double msError) {
		massErrorTextField.setText(Double.toString(msError));
	}
	
	public MassErrorType getMzErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public void setMzErrorType(MassErrorType errorType) {
		massErrorTypeComboBox.setSelectedItem(errorType);
	}
	
	public double getRtWindowExtensionWidth() {
		
		if(!rtWindowExtensionTextFieldTextField.getText().trim().isEmpty())
			return Double.parseDouble(rtWindowExtensionTextFieldTextField.getText().trim());
		else
			return 0.0d;
	}
	
	public void setRtWindowExtensionWidth(double rtExtension) {		
		rtWindowExtensionTextFieldTextField.setText(Double.toString(rtExtension));
	}
	
	public int getmaxIsotopeIntensityErrorPercent() {
		return (int) maxIsotopeIntensityErrorSpinner.getValue();
	}
	
	public void setmaxIsotopeIntensityErrorPercent(int isotopeError) {
		maxIsotopeIntensityErrorSpinner.setValue(isotopeError);
	}
	
	public int getMinPlotHeight() {
		return (int)plotHeightSpinner.getValue();
	}
	
	public void setMinPlotHeight(int height) {
		plotHeightSpinner.setValue(height);
	}
}












