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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class ProjectDesignEditorPanel extends JPanel implements ActionListener, ListSelectionListener {

	private FactorEditorTable factorEditorTable;
	private LevelEditorTable levelEditorTable;
	private ReorderingToolbar factorOrderToolbar, levelOrderToolbar;
	private JSplitPane splitPane;
	private ExperimentDesignSubset activeDesignSubset;

	public enum ObjectToMove{
		FACTOR,
		LEVEL;
	}

	public ProjectDesignEditorPanel() {

		activeDesignSubset = null;
		setLayout(new BorderLayout(0, 0));

		splitPane = new JSplitPane();
		splitPane.setDividerSize(0);
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane, BorderLayout.CENTER);

		//	Factors
		JPanel factorPanel = new JPanel();
		factorPanel.setBorder(new TitledBorder(null, "Factors", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(factorPanel);
		factorPanel.setLayout(new BorderLayout(0, 0));

		factorOrderToolbar = new ReorderingToolbar(this, ReorderingToolbar.DataEditMode.RENAME,
				MainActionCommands.RENAME_FACTOR_COMMAND.getName());

		factorPanel.add(factorOrderToolbar, BorderLayout.EAST);

		factorEditorTable = new FactorEditorTable();
		factorEditorTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane factorScrollPane = new JScrollPane(factorEditorTable);
		factorScrollPane.setViewportView(factorEditorTable);
		factorScrollPane.setPreferredSize(factorEditorTable.getPreferredScrollableViewportSize());
		factorPanel.add(factorScrollPane, BorderLayout.CENTER);

		//	Levels
		JPanel levelPanel = new JPanel();
		levelPanel.setBorder(new TitledBorder(null, "Levels", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setRightComponent(levelPanel);
		levelPanel.setLayout(new BorderLayout(0, 0));

		levelOrderToolbar = new ReorderingToolbar(this, ReorderingToolbar.DataEditMode.RENAME,
				MainActionCommands.RENAME_LEVEL_COMMAND.getName());

		levelPanel.add(levelOrderToolbar, BorderLayout.EAST);

		levelEditorTable = new LevelEditorTable();
		JScrollPane levelScrollPane = new JScrollPane(levelEditorTable);
		levelScrollPane.setViewportView(levelEditorTable);
		levelScrollPane.setPreferredSize(levelEditorTable.getPreferredScrollableViewportSize());
		levelPanel.add(levelScrollPane, BorderLayout.CENTER);
	}

	public void loadDesignSubset(ExperimentDesignSubset designSubset) {

		activeDesignSubset = designSubset;
		factorEditorTable.setTableModelFromDesignSubset(designSubset);
		if(factorEditorTable.getRowCount() > 0)
			factorEditorTable.setRowSelectionInterval(0, 0);
		setEditingAllowed(!designSubset.isLocked());
	}

	public void setEditingAllowed(boolean allowEdit) {

		factorOrderToolbar.setEditingAllowed(allowEdit);
		levelOrderToolbar.setEditingAllowed(allowEdit);
		factorEditorTable.setEditingAllowed(allowEdit);
		levelEditorTable.setEditingAllowed(allowEdit);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		ObjectToMove objectToMove = ObjectToMove.FACTOR;

		if(event.getSource() instanceof JButton) {

			if(((JButton)event.getSource()).getParent().equals(levelOrderToolbar))
				objectToMove = ObjectToMove.LEVEL;
		}
		if (command.equals(MainActionCommands.MOVE_TO_TOP_COMMAND.getName())
				|| command.equals(MainActionCommands.MOVE_UP_COMMAND.getName())
				|| command.equals(MainActionCommands.MOVE_DOWN_COMMAND.getName())
				|| command.equals(MainActionCommands.MOVE_TO_BOTTOM_COMMAND.getName())){

			if(objectToMove.equals(ObjectToMove.FACTOR) && factorEditorTable.getSelectedRow() > -1)
				moveFactor(command);

			if(objectToMove.equals(ObjectToMove.LEVEL) && levelEditorTable.getSelectedRow() > -1)
				moveLevel(command);
		}
	}

	private void moveLevel(String moveCommand) {

		ExperimentDesignLevel levelToMove = levelEditorTable.getSelectedLevel();
		int selectedLevelRow = levelEditorTable.getSelectedRow();
		ExperimentDesignFactor activeFactor = factorEditorTable.getSelectedFactor();
		ArrayList<ExperimentDesignLevel>activeLevels = new ArrayList<ExperimentDesignLevel>();
		activeLevels.addAll(Arrays.asList(activeDesignSubset.getOrderedDesign().get(activeFactor)));
		int lastActiveRow = activeLevels.size() - 1;

//		if(levelEditorTable.isSelectedLevelActive()) {

			if (moveCommand.equals(MainActionCommands.MOVE_TO_TOP_COMMAND.getName())) {

				if(selectedLevelRow == 0)
					return;
				else {
					ExperimentDesignLevel toMove = activeLevels.remove(selectedLevelRow);
					activeLevels.add(0, toMove);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_UP_COMMAND.getName())) {

				if(selectedLevelRow == 0)
					return;
				else {
					Collections.swap(activeLevels, selectedLevelRow, selectedLevelRow - 1);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_DOWN_COMMAND.getName())) {

				if(selectedLevelRow == levelEditorTable.getRowCount() - 1)
					return;
				else {
					Collections.swap(activeLevels, selectedLevelRow, selectedLevelRow + 1);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_TO_BOTTOM_COMMAND.getName())) {

				if(selectedLevelRow == levelEditorTable.getRowCount() - 1)
					return;
				else {
					ExperimentDesignLevel toMove = activeLevels.remove(selectedLevelRow);
					activeLevels.add(levelEditorTable.getRowCount() - 1, toMove);
				}
			}
			//	Rearrange levels in subset
			activeDesignSubset.reorderLevels(activeLevels.toArray(new ExperimentDesignLevel[activeLevels.size()]));
			levelEditorTable.setTableModelFromDesignSubsetFactor(activeDesignSubset, activeFactor);
			levelEditorTable.highlightLevel(levelToMove);
//		}
//		else {
//			MessageDialogue.showErrorMsg("Can not move inactive level.");
//		}
	}

	private void moveFactor(String moveCommand) {

		ArrayList<ExperimentDesignFactor>activeFactors = new ArrayList<ExperimentDesignFactor>();
		activeFactors.addAll(activeDesignSubset.getOrderedDesign().keySet());

		ExperimentDesignFactor factorToMove = factorEditorTable.getSelectedFactor();
		int selectedFactorRow = factorEditorTable.getSelectedRow();
		int lastActiveRow = activeFactors.size() - 1;

//		if(factorEditorTable.isSelectedFactorActive()) {

			if (moveCommand.equals(MainActionCommands.MOVE_TO_TOP_COMMAND.getName())) {

				if(selectedFactorRow == 0)
					return;
				else {
					ExperimentDesignFactor toMove = activeFactors.remove(selectedFactorRow);
					activeFactors.add(0, toMove);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_UP_COMMAND.getName())) {

				if(selectedFactorRow == 0)
					return;
				else {
					Collections.swap(activeFactors, selectedFactorRow, selectedFactorRow - 1);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_DOWN_COMMAND.getName())) {

				if(selectedFactorRow == factorEditorTable.getRowCount() - 1)
					return;
				else {
					Collections.swap(activeFactors, selectedFactorRow, selectedFactorRow + 1);
				}
			}
			if (moveCommand.equals(MainActionCommands.MOVE_TO_BOTTOM_COMMAND.getName())) {

				if(selectedFactorRow == factorEditorTable.getRowCount() - 1)
					return;
				else {
					ExperimentDesignFactor toMove = activeFactors.remove(selectedFactorRow);
					activeFactors.add(factorEditorTable.getRowCount() - 1, toMove);
				}
			}
			//	Rearrange factors in subset
			activeDesignSubset.reorderFactors(activeFactors.toArray(new ExperimentDesignFactor[activeFactors.size()]));
			factorEditorTable.setTableModelFromDesignSubset(activeDesignSubset);
			factorEditorTable.highlightFactor(factorToMove);
//		}
//		else {
//			MessageDialogue.showErrorMsg("Can not move inactive factor.");
//		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			ExperimentDesignFactor factor = factorEditorTable.getSelectedFactor();
			ExperimentDesignSubset dSubset = factorEditorTable.getActiveDesignSubset();

			if(factor != null && dSubset != null)
				levelEditorTable.setTableModelFromDesignSubsetFactor(dSubset, factor);
		}

	}

	public synchronized void clearPanel() {

		factorEditorTable.clearTable();
		levelEditorTable.clearTable();
		activeDesignSubset = null;
	}

	public void reloadDesign() {
		// TODO Auto-generated method stub

	}
}


































