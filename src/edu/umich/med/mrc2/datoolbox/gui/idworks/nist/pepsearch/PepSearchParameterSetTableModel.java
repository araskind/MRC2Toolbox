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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class PepSearchParameterSetTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6655746948950669281L;

	public static final String PS_OBJECT_COLUMN = "ID";
	public static final String HR_SEARCH_OPTION_COLUMN = "HighRes option";
	public static final String HR_SEARCH_TYPE_COLUMN = "HighRes type";	
	public static final String HR_SEARCH_THRESHOLD_COLUMN = "HighRes threshold";
	public static final String IS_REVERSE_COLUMN = "Reverse";
	public static final String IS_ALT_MATCHING_COLUMN = "Alt. match";
	public static final String IGNORE_AROUND_PRECURSOR_COLUMN = "Ignore around prec.";
	public static final String PRECURSOR_ERROR_COLUMN = "Prec. error";
	public static final String FRAG_ERROR_COLUMN = "Frag. error";
	public static final String MASS_RANGE_COLUMN = "Mass range";
	public static final String MATCH_POLARITY_COLUMN = "Match polarity";
	public static final String MATCH_CHARGE_COLUMN = "Match charge";
	public static final String SCORE_CUTOFF_COLUMN = "Score cutoff";
	public static final String NUM_HITS_COLUMN = "#Hits";

	public PepSearchParameterSetTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PS_OBJECT_COLUMN, "Parameter set ID", NISTPepSearchParameterObject.class, false),
			new ColumnContext(HR_SEARCH_OPTION_COLUMN, 
					"High resolution search option (regular / in-source / hybrid)", String.class, false),
			new ColumnContext(HR_SEARCH_TYPE_COLUMN, 
					"High resolution search type (peptide / generic / dot product)", String.class, false),
			new ColumnContext(HR_SEARCH_THRESHOLD_COLUMN, 
					"High resolution search threshold", String.class, false),
			new ColumnContext(IS_REVERSE_COLUMN, "Run reverse search", Boolean.class, false),
			new ColumnContext(IS_ALT_MATCHING_COLUMN, "Use alternative matching", Boolean.class, false),			
			new ColumnContext(IGNORE_AROUND_PRECURSOR_COLUMN, 
					"Ignore the peaks around precursor ion within specified window", String.class, false),
			new ColumnContext(PRECURSOR_ERROR_COLUMN, "Maximal mass error for precursor ion", String.class, false),
			new ColumnContext(FRAG_ERROR_COLUMN, "Maximal mass error for fragment ions", String.class, false),
			new ColumnContext(MASS_RANGE_COLUMN, "Limit search to mass range", String.class, false),			
			new ColumnContext(MATCH_POLARITY_COLUMN, MATCH_POLARITY_COLUMN, Boolean.class, false),
			new ColumnContext(MATCH_CHARGE_COLUMN, MATCH_CHARGE_COLUMN, Boolean.class, false),
			new ColumnContext(SCORE_CUTOFF_COLUMN, "Report obly hits with the score above cutoff", Integer.class, false),
			new ColumnContext(NUM_HITS_COLUMN, "Maximal number of hits to report", Long.class, false),
		};
	}

	public void setModelFromObjectCollection(
			Collection<NISTPepSearchParameterObject> psObjects) {

		setRowCount(0);
		if(psObjects == null || psObjects.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(NISTPepSearchParameterObject pso  : psObjects) {
			
			String ignoreAroundPrecursor = "";
			if(pso.isIgnorePeaksAroundPrecursor() 
					&& pso.getIgnorePeaksAroundPrecursorWindow() > 0.0d) {				
				ignoreAroundPrecursor = 
						MRC2ToolBoxConfiguration.getPpmFormat().format(
								pso.getIgnorePeaksAroundPrecursorWindow()) + 
						pso.getIgnorePeaksAroundPrecursorAccuracyUnits().name();
			}
			String precursorError = 
					MRC2ToolBoxConfiguration.getPpmFormat().format(
							pso.getPrecursorMzErrorValue()) + 
					pso.getPrecursorMzErrorType().name();
			String fragmentError = 
					MRC2ToolBoxConfiguration.getPpmFormat().format(
							pso.getFragmentMzErrorValue()) + 
					pso.getFragmentMzErrorType().name();
			Object[] obj = new Object[] {
					pso,
					pso.getHiResSearchOption().getDescription(),
					pso.getHiResSearchType().getDescription(),
					pso.getHiResSearchThreshold().getDescription(),
					pso.isEnableReverseSearch(),
					pso.isEnableAlternativePeakMatching(),
					ignoreAroundPrecursor,
					precursorError,
					fragmentError,
					pso.getMzRange().toString(),
					pso.isMatchPolarity(),
					pso.isMatchCharge(),
					pso.getMinMatchFactor(),
					null,
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setModelFromHitCountMap(
			Map<NISTPepSearchParameterObject, Long>paramCounts) {

		setRowCount(0);
		if(paramCounts == null || paramCounts.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(Entry<NISTPepSearchParameterObject, Long> psoEntry  : paramCounts.entrySet()) {
			
			NISTPepSearchParameterObject pso = psoEntry.getKey();
			String ignoreAroundPrecursor = "";
			if(pso.isIgnorePeaksAroundPrecursor() 
					&& pso.getIgnorePeaksAroundPrecursorWindow() > 0.0d) {				
				ignoreAroundPrecursor = 
						MRC2ToolBoxConfiguration.getPpmFormat().format(
								pso.getIgnorePeaksAroundPrecursorWindow()) + 
						pso.getIgnorePeaksAroundPrecursorAccuracyUnits().name();
			}
			String precursorError = 
					MRC2ToolBoxConfiguration.getPpmFormat().format(
							pso.getPrecursorMzErrorValue()) + 
					pso.getPrecursorMzErrorType().name();
			String fragmentError = 
					MRC2ToolBoxConfiguration.getPpmFormat().format(
							pso.getFragmentMzErrorValue()) + 
					pso.getFragmentMzErrorType().name();
			Object[] obj = new Object[] {
					pso,
					pso.getHiResSearchOption().getDescription(),
					pso.getHiResSearchType().getDescription(),
					pso.getHiResSearchThreshold().getDescription(),
					pso.isEnableReverseSearch(),
					pso.isEnableAlternativePeakMatching(),
					ignoreAroundPrecursor,
					precursorError,
					fragmentError,
					pso.getMzRange().toString(),
					pso.isMatchPolarity(),
					pso.isMatchCharge(),
					pso.getMinMatchFactor(),
					psoEntry.getValue(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}










