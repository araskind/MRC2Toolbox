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

package edu.umich.med.mrc2.datoolbox.gui.jcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;
import org.openscience.jchempaint.controller.undoredo.IUndoRedoable;
import org.openscience.jchempaint.renderer.selection.LogicalSelection;

import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class StructuralAnnotationEditor extends JDialog implements ActionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1988262306408008692L;

	private static final Icon editStructuralAnnotationIcon = GuiUtils.getIcon("editLibraryFeature", 32);

	private JButton saveButton, cancelButton;
	private ObjectAnnotation currentAnnotation;
	private JChemPaintPanel jcpPanel;
	private JPanel panel_1;
	private JTextArea textArea;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StructuralAnnotationEditor(ActionListener listener) {

		super();
		setTitle("Edit structural annotation");
		setIconImage(((ImageIcon) editStructuralAnnotationIcon).getImage());
		setPreferredSize(new Dimension(850, 600));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);

		IChemModel chemModel = JChemPaint.emptyModel();
		chemModel.setID("structural annotation");
		List<String>ignoreItems = Arrays.asList("new", "close", "exit");
		jcpPanel = new JChemPaintPanel(chemModel, JChemPaint.GUI_APPLICATION, false, null, ignoreItems);
		getContentPane().add(jcpPanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(panel_1, BorderLayout.SOUTH);
				
		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);
		cancelButton.addActionListener(al);

		saveButton = new JButton("Save annotation");
		panel_1.add(saveButton);
		saveButton.addActionListener(listener);
		saveButton.setActionCommand(MainActionCommands.SAVE_OBJECT_STRUCTURAL_ANNOTATION_COMMAND.getName());
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);

		textArea = new JTextArea();
		textArea.setBorder(
				new CompoundBorder(
						new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Structure notes",
								TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
						new EmptyBorder(10, 10, 10, 10)));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setRows(2);
		panel.add(textArea, BorderLayout.CENTER);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();

	}

	public synchronized void clearPanel() {
		//	TODO
	}

	public ObjectAnnotation getAnnotation() {
		
		currentAnnotation.setChemModel(jcpPanel.getChemModel());
		return currentAnnotation;
	}
	
	public boolean isJCPModelEmpty() {
		
		IChemModel model = jcpPanel.getChemModel();		
		if(model.getReactionSet() != null && !model.getReactionSet().isEmpty())
			return false;
		
		if(model.getRingSet() != null && !model.getRingSet().isEmpty())
			return false;
		
		if(model.getCrystal() != null && !model.getCrystal().isEmpty())
			return false;
		
		if(model.getMoleculeSet() != null && model.getMoleculeSet().getAtomContainerCount() > 0) {
			
			if(model.getMoleculeSet().getAtomContainer(0).getAtomCount() == 0)
				return true;
			else
				return false;
		}		
		return true;
	}

	public void loadAnnotation(ObjectAnnotation annotation) {

		clearPanel();
		currentAnnotation = annotation;
		if(currentAnnotation == null)
			return;
		
		if(currentAnnotation.getChemModel() == null) {
			currentAnnotation.setChemModel(jcpPanel.getChemModel());
		}
		else{
	        try {
	        	jcpPanel.get2DHub().unsetRGroupHandler();
	        	IChemModel chemModel = currentAnnotation.getChemModel();
	            if (jcpPanel.get2DHub().getUndoRedoFactory() != null
	                    && jcpPanel.get2DHub().getUndoRedoHandler() != null) {
	                IUndoRedoable undoredo = jcpPanel.get2DHub()
	                        .getUndoRedoFactory().getLoadNewModelEdit(
	                                jcpPanel.getChemModel(),
									null,
	                                jcpPanel.getChemModel()
	                                        .getMoleculeSet(),
	                                jcpPanel.getChemModel()
	                                        .getReactionSet(),
	                                chemModel.getMoleculeSet(),
	                                chemModel.getReactionSet(),
	                                "Structural annotation");
	                jcpPanel.get2DHub().getUndoRedoHandler().postEdit(
	                        undoredo);
	            }
	            jcpPanel.getChemModel().setMoleculeSet(
	                    chemModel.getMoleculeSet());
	            jcpPanel.getChemModel().setReactionSet(chemModel.getReactionSet());
	            jcpPanel.getRenderPanel().getRenderer()
	                    .getRenderer2DModel().setSelection(
	                            new LogicalSelection(
	                                    LogicalSelection.Type.NONE));

	            // the newly opened file should nicely fit the screen
	            jcpPanel.getRenderPanel().setFitToScreen(true);

	            jcpPanel.getRenderPanel().update(
	                    jcpPanel.getRenderPanel().getGraphics());

	            // enable zooming by removing constraint
	            jcpPanel.getRenderPanel().setFitToScreen(false);
	            
	            jcpPanel.setIsAlreadyAFile(null);
	        
	            //in case this is an application, we set the file name as title
	            if (jcpPanel.getGuistring().equals(
	                    JChemPaint.GUI_APPLICATION))
	                ((JChemPaintPanel)jcpPanel).setTitle("Structural annotation for " + 
	                    currentAnnotation.getAnnotatedObjectType().getName());
	        
	        } catch (Exception e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	            jcpPanel.announceError(e1);
	        }
	        textArea.setText(annotation.getChemModelNotes());
		}
	}
	
	public String getAnnotationNotes() {
		return textArea.getText().trim();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent event) {
		// TODO Auto-generated method stub
		if (event.getStateChange() == ItemEvent.SELECTED) {

		}
	}

	/**
	 * @return the jcPanel
	 */
	public JChemPaintPanel getJcPanel() {
		return jcpPanel;
	}
}
