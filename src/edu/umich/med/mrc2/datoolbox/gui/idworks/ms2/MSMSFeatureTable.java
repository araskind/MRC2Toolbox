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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.AdductComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSExperimentComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ReferenceMsMsLibraryMatchTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleTypeComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureIdentificationState;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.format.AdductFormat;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.FeatureIdentificationStateFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSExperimentFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MsFeatureInfoBundleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ReferenceMsMsLibraryMatchTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.format.SampleTypeFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ColorCircleFlagRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FeatureIdentificationStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IdentificationFollowupStateRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSExperimentRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MSFeatureIdentificationLevelColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureInfoBundleRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PercentValueRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SampleTypeRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongTableUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.TableUpdateProgressDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class MSMSFeatureTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8853294038580302429L;
	private MSMSFeatureTableModel model;
	private MouseMotionAdapter mma;
	private TableUpdateProgressDialog idp;
	private FormattedDecimalRenderer scoreRenderer;

	public MSMSFeatureTable() {
		super();
		model = new MSMSFeatureTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		rowSorter = new TableRowSorter<MSMSFeatureTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN),
				new MsFeatureInfoBundleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.DATABASE_LINK_COLUMN),
				new MsFeatureIdentityComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.ACQ_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.DEX_METHOD_ID_COLUMN),
				new AnalysisMethodComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.EXPERIMENT_COLUMN),
				new LIMSExperimentComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.ID));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.SAMPLE_TYPE_COLUMN),
				new SampleTypeComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.ADDUCT_COLUMN),
				new AdductComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(MSMSFeatureTableModel.MSMS_MATCH_TYPE_COLUMN),
				new ReferenceMsMsLibraryMatchTypeComparator());	
		
		//	Renderers
		setDefaultRenderer(MSFeatureInfoBundle.class, new MsFeatureInfoBundleRenderer(SortProperty.Name));
		setDefaultRenderer(DataAcquisitionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(DataExtractionMethod.class, new AnalysisMethodRenderer());
		setDefaultRenderer(LIMSExperiment.class, new LIMSExperimentRenderer());
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRenderer(SortProperty.ID));
		setDefaultRenderer(LIMSSampleType.class, new SampleTypeRenderer(SortProperty.Name));
		
		columnModel.getColumnById(MSMSFeatureTableModel.COMPOUND_NAME_COLUMN).
			setCellRenderer(new WordWrapCellRenderer());	

		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		columnModel.getColumnById(MSMSFeatureTableModel.DATABASE_LINK_COLUMN)
			.setCellRenderer(msfIdRenderer);	// Database ID column
		columnModel.getColumnById(MSMSFeatureTableModel.RETENTION_COLUMN)
			.setCellRenderer(rtRenderer); // Retention time 
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass			
		columnModel.getColumnById(MSMSFeatureTableModel.NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass
		columnModel.getColumnById(MSMSFeatureTableModel.LIBRARY_PRECURSOR_DELTA_MZ_COLUMN)
			.setCellRenderer(mzRenderer); // Parent mass		
		columnModel.getColumnById(MSMSFeatureTableModel.AMBIGUITY_COLUMN)
			.setCellRenderer(new FeatureIdentificationStateRenderer());		
		columnModel.getColumnById(MSMSFeatureTableModel.ANNOTATIONS_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.FOLLOWUP_COLUMN)
			.setCellRenderer(new IdentificationFollowupStateRenderer()); 
		columnModel.getColumnById(MSMSFeatureTableModel.ID_LEVEL_COLUMN)
			.setCellRenderer(new MSFeatureIdentificationLevelColorRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_ION_PURITY_COLUMN)
			.setCellRenderer(new PercentValueRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.PARENT_ION_IS_MINOR_ISOTOPE_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_ENTROPY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(new DecimalFormat("##.###")));
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_TOTAL_INTENSITY_COLUMN)
			.setCellRenderer(areaRenderer);
		scoreRenderer = new FormattedDecimalRenderer(new DecimalFormat("###.##"), true);
		columnModel.getColumnById(MSMSFeatureTableModel.SPECTRUM_ENTROPY_COLUMN)
			.setCellRenderer(scoreRenderer);
		columnModel.getColumnById(MSMSFeatureTableModel.MSMS_MATCH_TYPE_COLUMN)
			.setCellRenderer(new ColorCircleFlagRenderer(16)); 

		//	Database link adapter
		mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(columnModel.getColumnById(MSMSFeatureTableModel.DATABASE_LINK_COLUMN))) {

					if (columnAtPoint(p) == columnModel.getColumnIndex(MSMSFeatureTableModel.DATABASE_LINK_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(msfIdRenderer);
		addMouseMotionListener(msfIdRenderer);
		
		columnModel.getColumnById(MSMSFeatureTableModel.AMBIGUITY_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.ID_LEVEL_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.ANNOTATIONS_COLUMN).setMaxWidth(50);
		columnModel.getColumnById(MSMSFeatureTableModel.FOLLOWUP_COLUMN).setMaxWidth(50);	
		columnModel.getColumnById(MSMSFeatureTableModel.COMPOUND_NAME_COLUMN).setMinWidth(200);

		//	Table header filter
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(MSFeatureInfoBundle.class, new MsFeatureInfoBundleFormat(SortProperty.Name));
		thf.getParserModel().setComparator(MSFeatureInfoBundle.class, new MsFeatureInfoBundleComparator(SortProperty.Name));
		thf.getParserModel().setFormat(MsFeatureIdentity.class, new MsFeatureIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(MsFeatureIdentity.class, new MsFeatureIdentityComparator(SortProperty.ID));
		thf.getParserModel().setComparator(DataAcquisitionMethod.class, new AnalysisMethodComparator(SortProperty.ID));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class, new AnalysisMethodFormat(SortProperty.ID));
		thf.getParserModel().setFormat(DataExtractionMethod.class, new AnalysisMethodFormat(SortProperty.ID));
		thf.getParserModel().setComparator(DataExtractionMethod.class, new AnalysisMethodComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSExperiment.class, new LIMSExperimentFormat());
		thf.getParserModel().setComparator(LIMSExperiment.class, new LIMSExperimentComparator(SortProperty.ID));
		thf.getParserModel().setFormat(ExperimentalSample.class, new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSSampleType.class, new SampleTypeFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSSampleType.class, new SampleTypeComparator(SortProperty.Name));	
		thf.getParserModel().setFormat(FeatureIdentificationState.class, new FeatureIdentificationStateFormat());
		thf.getParserModel().setFormat(Adduct.class, new AdductFormat(SortProperty.Name));
		thf.getParserModel().setComparator(Adduct.class, new AdductComparator(SortProperty.Name));
		thf.getParserModel().setFormat(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeFormat());
		thf.getParserModel().setComparator(ReferenceMsMsLibraryMatch.class, new ReferenceMsMsLibraryMatchTypeComparator());
		
		finalizeLayout();
	}

	public void setTableModelFromFeatureListOld(Collection<MSFeatureInfoBundle> featureList) {

		thf.setTable(null);
		model.setTableModelFromFeatureList(featureList);
		thf.setTable(this);
		adjustVariableColumns();
	}
	
	private void adjustVariableColumns() {
		
		fixedWidthColumns.clear();
		fixedWidthColumns.add(getColumnIndex(MSMSFeatureTableModel.AMBIGUITY_COLUMN));
		fixedWidthColumns.add(getColumnIndex(MSMSFeatureTableModel.ID_LEVEL_COLUMN));
		fixedWidthColumns.add(getColumnIndex(MSMSFeatureTableModel.ANNOTATIONS_COLUMN));
		fixedWidthColumns.add(getColumnIndex(MSMSFeatureTableModel.FOLLOWUP_COLUMN));
				
		tca.adjustColumnsExcluding(fixedWidthColumns);
		columnModel.getColumnById(MSMSFeatureTableModel.COMPOUND_NAME_COLUMN).setMinWidth(200);
	}
	
	public void setTableModelFromFeatureList(Collection<MSFeatureInfoBundle> featureList) {

		thf.setTable(null);
		List<Object[]> modelData = createModelData(featureList);
		model.setRowCount(0);
//		TableSwingWorker worker = new TableSwingWorker(model, modelData, this);
//		worker.execute();
		
		TableUpdateTask task = new TableUpdateTask(model, modelData);
		String message = "Loading data for " + 
				Integer.toString(featureList.size()) + " features, please wait ...";
		idp = new TableUpdateProgressDialog(message, this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	private List<Object[]> createModelData(Collection<MSFeatureInfoBundle> featureList) {
		
		List<Object[]> modelData = new ArrayList<Object[]>();
		for (MSFeatureInfoBundle bundle : featureList) {

			MsFeature cf = bundle.getMsFeature();
			TandemMassSpectrum instrumentMsMs = 
					cf.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);

			if(instrumentMsMs == null)
				continue;

			String compoundName = "";
			LIMSSampleType limsSampleType = null;
			if(bundle.getStockSample() != null) 
				limsSampleType = bundle.getStockSample().getLimsSampleType();
				
			FeatureIdentificationState idState = cf.getIdentificationState();
			boolean hasAnnotations = (!cf.getAnnotations().isEmpty() 
					|| !bundle.getStandadAnnotations().isEmpty());
			boolean hasFollowup = !bundle.getIdFollowupSteps().isEmpty();
			MSFeatureIdentificationLevel idLevel = null;
			Double libraryPrecursorDeltaMz = null;
			Double neutralMassDeltaMz = null;
			MsFeatureIdentity primaryId = cf.getPrimaryIdentity();
			Adduct adduct = null;
			Double entropyMsMsScore = null;
			ReferenceMsMsLibraryMatch refMatch = null;
			if(primaryId != null) {

				if(primaryId.getCompoundIdentity() == null) {
					System.out.println(cf.getPrimaryIdentity().
							getReferenceMsMsLibraryMatch().getMatchedLibraryFeature().getUniqueId() + " has no compound ID");
				}
				else {
					compoundName = primaryId.getName();
					double neutralMass = primaryId.getCompoundIdentity().getExactMass();
					neutralMassDeltaMz = instrumentMsMs.getParent().getMz() - neutralMass;
					refMatch = primaryId.getReferenceMsMsLibraryMatch();
					if(refMatch != null) {
						
						MsPoint libPrecursor = refMatch.getMatchedLibraryFeature().getParent();
						if(libPrecursor != null) 
							libraryPrecursorDeltaMz = instrumentMsMs.getParent().getMz() - libPrecursor.getMz();					
					
						entropyMsMsScore = refMatch.getEntropyBasedScore();
					}
				}
				idLevel = cf.getPrimaryIdentity().getIdentificationLevel();
				adduct = primaryId.getPrimaryAdduct();
			}
			if(adduct == null) {
				
				if(cf.getSpectrum() != null) 	
					adduct = cf.getSpectrum().getPrimaryAdduct();
				else
					adduct = AdductManager.getDefaultAdductForCharge(cf.getCharge());
			}
			Object[] obj = {
				bundle,			//	MS_FEATURE_COLUMN	MsFeature
				compoundName,	//	COMPOUND_NAME_COLUMN	String
				cf.getPrimaryIdentity(),	//	DATABSE_LINK_COLUMN	MsFeatureIdentity
				idState, //	AMBIGUITY_COLUMN, Boolean
				idLevel,
				adduct,
				hasAnnotations,	//	ANNOTATIONS_COLUMN, Boolean
				hasFollowup,
				cf.getRetentionTime(),		//	RETENTION_COLUMN	Double
				instrumentMsMs.getParent().getMz(),	//	PARENT_MZ_COLUMN	Double
				neutralMassDeltaMz,		//	NEUTRAL_MASS_PRECURSOR_DELTA_MZ_COLUMN	Double
				libraryPrecursorDeltaMz,	//	LIBRARY_PRECURSOR_DELTA_MZ_COLUMN	Double				
				instrumentMsMs.getCidLevel(),	//	COLLISION_ENERGY_COLUMN	Double
				entropyMsMsScore,
				refMatch,
//				score,	//	LIB_SCORE_COLUMN	Double
//				lib,	//	MSMS_LIB_COLUMN	CompoundLibrary
				limsSampleType,	//	SAMPLE_TYPE_COLUMN	LIMSSampleType
				bundle.getSample(),		//	SAMPLE_COLUMN	IDTExperimentalSample
				bundle.getExperiment(),	//	EXPERIMENT_COLUMN	LIMSExperiment
				bundle.getAcquisitionMethod(),	//	ACQ_METHOD_ID_COLUMN	LIMSAcquisitionMethod
				bundle.getDataExtractionMethod(),	//	DEX_METHOD_ID_COLUMN	LIMSDataExtractionMethod
				instrumentMsMs.getParentIonPurity(),	//	PARENT_ION_PURITY_COLUMN	Double				
				instrumentMsMs.isParentIonMinorIsotope(),	//	PARENT_ION_IS_MINOR_ISOTOPE_COLUMN	Boolean
				instrumentMsMs.getEntropy(), //	SPECTRUM_ENTROPY_COLUMN		Double
				instrumentMsMs.getTotalIntensity(),	//	SPECTRUM_TOTAL_INTENSITY_COLUMN	Double
			};
			modelData.add(obj);
		}	
		return modelData;
	}
	
	class TableUpdateTask extends LongTableUpdateTask {

	    private final BasicTableModel tableModel;
	    private final List<Object[]> modelData;

	    public TableUpdateTask(BasicTableModel tableModel, List<Object[]> modelData) {
	        this.tableModel = tableModel;
	        this.modelData = modelData;
	    }

		@Override
		public BasicTableModel doInBackground() throws Exception {

	        Thread.sleep(200);
	        for (int index = 0; index < modelData.size(); index++) {

	        	Object[] data = modelData.get(index);
	            publish(data);
	            Thread.yield();
	        }
	        return tableModel;
	    }
		
	    @Override 	
	    protected void process(List<Object[]> chunks) {
	        try {
				tableModel.addRows(chunks);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    @Override
	    public void done() {    	
			try {
				thf.setTable(MSMSFeatureTable.this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adjustVariableColumns();
			idp.dispose();
	    }
	}

	public MSFeatureInfoBundle getSelectedBundle() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN));
	}

	public Collection<MSFeatureInfoBundle> getMultipleSelectedBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		if(getSelectedRowCount() == 0)
			return bundles;

		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row : getSelectedRows())
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}

	public Collection<MSFeatureInfoBundle> getFilteredBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(row), colIdx));

		return bundles;
	}
	
	public Collection<MSFeatureInfoBundle> getAllBundles() {

		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		for(int row=0; row<model.getRowCount(); row++)
			 bundles.add((MSFeatureInfoBundle)model.getValueAt(row, colIdx));

		return bundles;
	}

	public void updateFeatureData(MSFeatureInfoBundle bundle) {
		model.updateFeatureData(bundle);
	}

	public void selectBundle(MSFeatureInfoBundle toSelect) {

		int colIdx = model.getColumnIndex(MSMSFeatureTableModel.MS_FEATURE_COLUMN);
		
		for(int i=0; i<getRowCount(); i++) {

			if(((MSFeatureInfoBundle)model.getValueAt(convertRowIndexToModel(i), colIdx)).equals(toSelect)) {
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
	}
}




















