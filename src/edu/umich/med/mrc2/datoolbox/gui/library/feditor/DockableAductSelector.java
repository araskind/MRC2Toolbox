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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableAductSelector extends DefaultSingleCDockable{

	private AdductSelectionTable adductsTable;
	private JComboBox<Polarity>polarityComboBox;

	private static final Icon componentIcon = GuiUtils.getIcon("chemModList", 16);

	public DockableAductSelector(TableModelListener adductSelectonListener, ItemListener polarityListener) {

		super("DockableAductSelector", componentIcon, "Adduct selector", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		adductsTable = new AdductSelectionTable();
		adductsTable.getModel().addTableModelListener(adductSelectonListener);
		add(new JScrollPane(adductsTable), BorderLayout.CENTER);

		JPanel selectorPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) selectorPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		selectorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(selectorPanel, BorderLayout.NORTH);

		JLabel lblPolarity = new JLabel("Polarity");
		lblPolarity.setFont(new Font("Tahoma", Font.BOLD, 12));
		selectorPanel.add(lblPolarity);

		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(new Polarity[] {
						Polarity.Positive,
						Polarity.Neutral,
						Polarity.Negative}));

		polarityComboBox.setSelectedItem(Polarity.Neutral);

		selectorPanel.add(polarityComboBox);
		polarityComboBox.addItemListener(polarityListener);
	}

	public void setPolarity(Polarity polarity) {
		polarityComboBox.setSelectedItem(polarity);
	}

	public void loadFeatureData(LibraryMsFeature activeFeature, Polarity polarity) {
		adductsTable.loadFeatureData(activeFeature, polarity);
	}

	public synchronized void clearPanel() {
		adductsTable.clearTable();
	}

	public Collection<Adduct> getSelectedAdducts() {
		return adductsTable.getSelectedAdducts();
	}

	public Polarity getPolarity() {
		return (Polarity) polarityComboBox.getSelectedItem();
	}
}














