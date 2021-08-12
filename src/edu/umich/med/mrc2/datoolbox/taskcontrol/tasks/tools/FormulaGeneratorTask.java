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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.tools;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.formula.MolecularFormulaGenerator;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.formula.rules.ElementRule;
import org.openscience.cdk.formula.rules.ToleranceRangeRule;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormulaSet;

import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class FormulaGeneratorTask extends AbstractTask {

	private ElementRule elementLimits;
	private ToleranceRangeRule toleranceRule;
	private double mass;
	private IMolecularFormulaSet formulas;
	private IChemObjectBuilder builder;
	
	private MolecularFormulaGenerator mfg;
	private MolecularFormulaRange mfRange;
	private double tolerance;

	public FormulaGeneratorTask(ElementRule elementLimits, ToleranceRangeRule toleranceRule, double mass) {
		
		super();

		this.mass = mass;
		formulas = null;
		builder = DefaultChemObjectBuilder.getInstance();
		
		mfRange  = (MolecularFormulaRange) ((Object[]) elementLimits.getParameters())[0];
		tolerance = (Double) ((Object[]) toleranceRule.getParameters())[1];
		
		mfg = new MolecularFormulaGenerator(builder, mass - tolerance, mass + tolerance, mfRange);
	}

	@Override
	public void run() {

		if (!isCanceled()) {

			setStatus(TaskStatus.PROCESSING);

			try {
				formulas = mfg.getAllFormulas();
				setStatus(TaskStatus.FINISHED);

			} catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
		}
	}

	public IMolecularFormulaSet getFormulas() {

		return formulas;
	}

	public double getMass() {
		return mass;
	}

	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}
}
