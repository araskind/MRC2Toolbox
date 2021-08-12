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

package edu.umich.med.mrc2.datoolbox.renjin;

import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.ListVector;
import org.ujmp.core.Matrix;

public class TryRenjin {

	public static void main(String[] args) throws Exception {

		RenjinScriptEngineFactory factory = new RenjinScriptEngineFactory();
		RenjinScriptEngine engine = factory.getScriptEngine();

//		int[][] tm = TestUtils.getSequentialMatrix(10, 20);
//		Matrix dataMatrix = Matrix.Factory.linkToArray(tm);
//		List<Integer> target = new ArrayList<Integer>();
//		dataMatrix.allValues().forEach(o -> target.add((Integer) o));
//		int[] converted = target.stream().mapToInt(Integer::intValue).toArray();
//		IntArrayVector testMatrix = new IntArrayVector(converted, AttributeMap.builder().setDim(10, 20).build());
//		engine.put("z", testMatrix);
//		engine.eval("print(z)");

		engine.eval("pcarr <- prcomp(USArrests, scale = TRUE)");
		ListVector model = (ListVector)engine.eval("summary(pcarr)");
		DoubleArrayVector scores = (DoubleArrayVector)model.getElementAsSEXP(5);
		Matrix scoreMatrix = RenjinUtils.convertVectorToDoubleMatrix(scores);

		long sdevRow = scoreMatrix.getRowForLabel("Proportion of Variance");

		System.out.println("**************************" );

//		Vector coefficients = model.getElementAsVector("coefficients");
//		System.out.println("intercept = " + coefficients.getElementAsDouble(0));
//		System.out.println("slope = " + coefficients.getElementAsDouble(1));
//

		// ... put your Java code here ...
		// engine.eval("df <- data.frame(x=1:10, y=(1:10)+rnorm(n=10))");
		// engine.eval("print(df)");
		// engine.eval("print(lm(y ~ x, df))");

		// engine.eval(new java.io.FileReader("script.R"));

		// engine.eval("library(e1071)");
		// engine.eval("data(iris)");
		// engine.eval("svmfit <- svm(Species~., data=iris)");
		// engine.eval("print(svmfit)");
		//
		// Vector gammaVector = (Vector)engine.eval("svmfit$gamma");
		// double gamma = gammaVector.getElementAsDouble(0);
		//
		// Vector nclassesVector = (Vector)engine.eval("svmfit$nclasses");
		// int nclasses = nclasses = nclassesVector.getElementAsInt(0);
		//
		// StringVector levelsVector = (StringVector)engine.eval("svmfit$levels");
		// String[] levelsArray = levelsVector.toArray();
		// double[][] testMatrix = TestUtils.getRandomMatrix(10,20,1,200);

		// Vector res = (Vector)engine.eval("matrix(seq(9), nrow = 3)");
		// try {
		// Matrix m = new Matrix(res);
		// System.out.println("Result is a " + m.getNumRows() + "x" + m.getNumCols() + "
		// matrix.");
		// }
		// catch(IllegalArgumentException e) {
		// System.out.println("Result is not a matrix: " + e);
		// }

	}
}
