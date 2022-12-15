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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats;

import java.util.ArrayList;
import java.util.List;

import org.renjin.primitives.matrix.DeferredMatrixProduct;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.renjin.RenjinUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataSetUtils;

public class PCATask extends AbstractTask {

	private DataAnalysisProject currentProject;
	private DataPipeline aciveDataPipeline;
	private ExperimentDesignSubset activeDesign;
	private MsFeatureSet activeFeatures;
	private int componentNumber;

	private Matrix projection;
	private Matrix dataSubset;
	private RenjinScriptEngine rScriptEngine;
	private Matrix importanceMatrix;

	public PCATask(
			DataAnalysisProject currentProject,
			DataPipeline aciveAssay,
			ExperimentDesignSubset activeDesign,
			MsFeatureSet activeFeatures,
			int componentNumber) {

		super();
		taskDescription = "Running PCA on " + activeDesign.getName() + 
				" using " + activeFeatures.getName() + " feature subset";

		this.currentProject = currentProject;
		this.aciveDataPipeline = aciveAssay;
		this.activeDesign = activeDesign;
		this.activeFeatures = activeFeatures;
		this.componentNumber = componentNumber;

		rScriptEngine = MRC2ToolBoxCore.getrScriptEngine();
	}

	@Override
	public void run() {

		total = 100;
		processed = 20;

		setStatus(TaskStatus.PROCESSING);
		try {
			processed = 20;
			dataSubset = DataSetUtils.subsetDataMatrix(
					currentProject, aciveDataPipeline, activeDesign, activeFeatures);
			List<Double> target = new ArrayList<Double>();
			dataSubset.replace(Ret.LINK, Double.NaN, 1.0d).replace(Ret.NEW, 0.0d, 1.0d).
				transpose().allValues().forEach(o -> target.add((Double) o));
			double[] converted = target.stream().mapToDouble(Double::doubleValue).toArray();
			AttributeMap map = 
					AttributeMap.builder().setDim((int)dataSubset.getRowCount(), (int)dataSubset.getColumnCount()).build();
			DoubleArrayVector testMatrix = new DoubleArrayVector(converted, map);
			rScriptEngine.put("z", testMatrix);
			rScriptEngine.eval("pcarr <- prcomp(z[ , which(apply(z, 2, var) != 0)], scale. = TRUE)");

			ListVector summary = (ListVector)rScriptEngine.eval("summary(pcarr)");
			DoubleArrayVector importance = (DoubleArrayVector)summary.getElementAsSEXP(5);
			importanceMatrix = RenjinUtils.convertVectorToDoubleMatrix(importance);

			DeferredMatrixProduct scores = (DeferredMatrixProduct)summary.getElementAsSEXP(4);
			projection = RenjinUtils.convertVectorToDoubleMatrix(scores);
			projection.setMetaDataDimensionMatrix(1, dataSubset.getMetaDataDimensionMatrix(1));
			componentNumber = (int) projection.getColumnCount();
			processed = 100;
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {

		return new PCATask(
				currentProject, 
				aciveDataPipeline, 
				activeDesign, 
				activeFeatures, 
				componentNumber);
	}

	/**
	 * @return the aciveAssay
	 */
	public DataPipeline getAciveDataPipeline() {
		return aciveDataPipeline;
	}

	/**
	 * @return the activeDesign
	 */
	public ExperimentDesignSubset getActiveDesign() {
		return activeDesign;
	}

	/**
	 * @return the activeFeatures
	 */
	public MsFeatureSet getActiveFeatures() {
		return activeFeatures;
	}

	/**
	 * @return the componentNumber
	 */
	public int getComponentNumber() {
		return componentNumber;
	}

	/**
	 * @return the projection
	 */
	public Matrix getProjection() {
		return projection;
	}

	/**
	 * @return the dataSubset
	 */
	public Matrix getDataSubset() {
		return dataSubset;
	}

	/**
	 * @return the importanceMatrix
	 */
	public Matrix getImportanceMatrix() {
		return importanceMatrix;
	}
}
