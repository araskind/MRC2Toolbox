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

package edu.umich.med.mrc2.datoolbox.gui.main;

public enum MainActionCommands {

	// Project
	NEW_METABOLOMICS_EXPERIMENT_COMMAND("New metabolomics experiment"),
	OPEN_METABOLOMICS_EXPERIMENT_COMMAND("Open metabolomics experiment"),
	SAVE_EXPERIMENT_COMMAND("Save experiment"),
	SAVE_EXPERIMENT_COPY_COMMAND("Save experiment as ..."),
	
	NEW_IDTRACKER_PROJECT_DIALOG_COMMAND("New MetIDTracker project"),
	NEW_IDTRACKER_PROJECT_COMMAND("Create new MetIDTracker project"),
	EDIT_IDTRACKER_PROJECT_DIALOG_COMMAND("Edit MetIDTracker project"),
	SAVE_IDTRACKER_PROJECT_COMMAND("Save MetIDTracker project"),
	DELETE_IDTRACKER_PROJECT_COMMAND("Delete MetIDTracker project"),
		
	NEW_IDTRACKER_EXPERIMENT_DIALOG_COMMAND("New MetIDTracker experiment"),
	NEW_IDTRACKER_EXPERIMENT_COMMAND("Crearte new MetIDTracker experiment"),
	EDIT_IDTRACKER_EXPERIMENT_DIALOG_COMMAND("Edit MetIDTracker experiment"),
	SAVE_IDTRACKER_EXPERIMENT_COMMAND("Save MetIDTracker experiment"),	
	DELETE_IDTRACKER_EXPERIMENT_COMMAND("Delete MetIDTracker experiment"),	

	SHOW_IDTRACKER_LOGIN_COMMAND("Show MetIDTracker login"),		
	IDTRACKER_LOGIN_COMMAND("Login to MetIDTracker"),
	VERIFY_IDTRACKER_PASSWORD_COMMAND("Verify MetIDTracker password"),
	CHANGE_IDTRACKER_PASSWORD_DIALOG_COMMAND("MetIDTracker password change dialog"),
	CHANGE_IDTRACKER_PASSWORD_COMMAND("Change MetIDTracker password"),
	COPY_IDTRACKER_PASSWORD_COMMAND("Copy MetIDTracker password"),
	IDTRACKER_LOGOUT_COMMAND("Log out from MetIDTracker"),
	
	SHOW_OPEN_IDTRACKER_EXPERIMENT_DIALOG_COMMAND("Show \"Open MetIDTracker experiment\" dialog"),
	OPEN_IDTRACKER_EXPERIMENT_COMMAND("Open MetIDTracker experiment"),
	CLOSE_EXPERIMENT_COMMAND("Close experiment"),
	SELECT_EXPERIMENT_LOCATION_COMMAND("Select experiment location"),
	
	// Raw data project
	NEW_RAW_DATA_EXPERIMENT_SETUP_COMMAND("New raw data analysis experiment"),
	NEW_RAW_DATA_EXPERIMENT_COMMAND("Create new raw data analysis experiment"),
	OPEN_RAW_DATA_EXPERIMENT_COMMAND("Open raw data analysis experiment"),
	OPEN_RAW_DATA_EXPERIMENT_FROM_DATABASE_COMMAND("Open raw data analysis experiment from database"),
	CLOSE_RAW_DATA_EXPERIMENT_COMMAND("Close raw data analysis experiment"),
	SAVE_RAW_DATA_EXPERIMENT_COMMAND("Save raw data analysis experiment"),
	EDIT_RAW_DATA_EXPERIMENT_SETUP_COMMAND("Show raw data analysis experiment editor"),
	EDIT_RAW_DATA_EXPERIMENT_COMMAND("Edit raw data analysis experiment"),
	
	//	Raw data analysis
	MSMS_FEATURE_EXTRACTION_SETUP_COMMAND("Set up MSMS feature extraction"),
	MSMS_FEATURE_EXTRACTION_COMMAND("Extract MSMS features"),
	SEND_MSMS_FEATURES_TO_IDTRACKER_WORKBENCH("Send MSMS features to MetIDTracker workbench"),
	SHOW_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_LIST_COMMAND("Show MSMS feature extraction method list"),
	LOAD_SAVED_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND("Load MSMS feature extraction method from database"),
	SHOW_SAVE_MSMS_FEATURE_EXTRACTION_METHOD_DIALOG_COMMAND("Save current MSMS feature extraction method"),
	SAVE_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND("Save MSMS feature extraction method to database"),
	DELETE_MSMS_FEATURE_EXTRACTION_METHOD_COMMAND("Delete selected MSMS feature extraction method"),
	
	//	User manager
	SHOW_USER_MANAGER_COMMAND("Show User Manager"),
	ADD_USER_DIALOG_COMMAND("Add User dialog"),
	ADD_USER_COMMAND("Add User"),
	EDIT_USER_DIALOG_COMMAND("Edit User dialog"),
	EDIT_USER_COMMAND("Edit User"),
	DELETE_USER_COMMAND("Delete User"),
	
	//	Organization manager
	SHOW_ORGANIZATION_MANAGER_COMMAND("Show Organization Manager"),
	ADD_ORGANIZATION_DIALOG_COMMAND("Add Organization dialog"),
	ADD_ORGANIZATION_COMMAND("Add Organization"),
	EDIT_ORGANIZATION_DIALOG_COMMAND("Edit Organization dialog"),
	EDIT_ORGANIZATION_COMMAND("Edit Organization"),
	DELETE_ORGANIZATION_COMMAND("Delete Organization"),
	SELECT_PI_COMMAND("Select principal investigator"),
	SELECT_CONTACT_PERSON_COMMAND("Select contact person"),
		
	//	Raw file tools
	SHOW_RAW_DATA_FILE_TOOLS_COMMAND("Show raw data file tools"),
	SELECT_RAW_DATA_FOLDER_FOR_CLEANUP("Select raw data folder for cleanup"),
	CLEANUP_RAW_DATA("Cleanup raw data"),
	SELECT_RAW_DATA_FOLDER_FOR_COMPRESSION("Select raw data folder for compression"),
	COMPRESS_RAW_DATA("Compress raw data"),
	SELECT_RAW_DATA_FOLDER_FOR_WORKLIST("Select raw data folder to extract worklist"),
	EXTRACT_WORKLIST_FROM_RAW_DATA_FOLDER("Extract worklist"),
	SAVE_EXTRACTED_WORKLIST_TO_FILE("Save worklist to file"),	
	ADD_EXPERIMENT_METADATA_COMMAND("Add experiment metadata"),
	SAVE_EXPERIMENT_METADATA_COMMAND("Save experiment metadata"),
	SET_EXPERIMENT_DATA_UPLOAD_PARAMETERS_COMMAND("Set experiment database upload parameters"),
	SEND_EXPERIMENT_DATA_TO_DATABASE_COMMAND("Send experiment data to database"),
	CLEAR_EXPERIMENT_METADATA_COMMAND("Clear experiment metadata"),

	ACTIVATE_DATA_PIPELINE_COMMAND("Activate data pipeline"),
	DELETE_DATA_PIPELINE_COMMAND("Delete data pipeline"),
	EDIT_DATA_PIPELINE_COMMAND("Edit data pipeline"),
	CONFIRM_ASSAY_CHANGE_COMMAND("Confirm assay change"),

	EDIT_EXPERIMENT_NAME_COMMAND("Edit experiment name"),
	EDIT_EXPERIMENT_DESCRIPTION_COMMAND("Edit experiment description"),
	SAVE_NEW_EXPERIMENT_NAME_COMMAND("Save new experiment name"),
	SAVE_NEW_EXPERIMENT_DESCRIPTION_COMMAND("Save new experiment description"),

	//	Preferences
	SAVE_PREFERENCES_COMMAND("Save program preferences"),
	DEFAULT_DIR_BROWSE_COMMAND("Browse for default experiment directory"),
	DEFAULT_DATA_DIR_BROWSE_COMMAND("Browse for default data directory"),
	RAW_DATA_REPOSITORY_DIR_BROWSE_COMMAND("Browse for raw data repository"),
	QUAL_AUTOMATION_BROWSE_COMMAND("Browse for QualAutomation binary"),
	MS_CONVERT_BROWSE_COMMAND("Browse for msconvert binary"),
	SIRIUS_BROWSE_COMMAND("Browse for Sirius CLI binary"),
	LIB2NIST_BROWSE_COMMAND("Browse for lib2nist binary"),
	PERCOLATOR_BROWSE_COMMAND("Browse for Percolator binary"),
	XIC_TEMPLATE_BROWSE_COMMAND("Browse for XIC template method"),
	METHOD_DIR_BROWSE_COMMAND("Browse for methods dir"),
	R_BINARY_BROWSE_COMMAND("Browse for R binary"),
	R_JAVA_BROWSE_COMMAND("Browse for RJava package directory"),
	NIST_MS_BROWSE_COMMAND("Browse for NIST MSSEARCH directory"),
	NIST_PEPSEARCH_BROWSE_COMMAND("Browse for NIST PepSearch binary"),
	TEST_DATABASE_CONNECTION("Test database connection"),
	SAVE_DATABASE_CONNECTION("Save database connection"),
	
	//	Database definition commands
	CONTINUE_PROGRAM_STARTUP_COMMAND("Continue program startup"),

	//	Task control commands
	SET_HIGH_PRIORITY_COMMAND("Set high priority"),
	SET_NORMAL_PRIORITY_COMMAND("Set normal priority"),
	RESTART_SELECTED_TASK_COMMAND("Restart selected task"),
	CANCEL_SELECTED_TASK_COMMAND("Cancel selected task"),
	CANCEL_ALL_TASKS_COMMAND("Cancel all tasks"),

	// Load data
	LOAD_DATA_COMMAND("Load quantitative data"),
	LOAD_DATA_FROM_MULTIFILES_COMMAND("Load quantitative data from multiple files"),
	LOAD_DATA_FROM_PROFINDER_PFA_COMMAND("Load quantitative data from ProFinder PFA file"),
	ADD_DATA_FROM_MULTIFILES_COMMAND("Add quantitative data to active assay from multiple files"),
	LOAD_MS1_DATA_FROM_MULTIFILES_COMMAND("Load MS1 features from multiple files"),
	LOAD_AVG_MS1_DATA_FROM_FILE_COMMAND("Load averaged MS1 features from file"),
	LOAD_MSMS_DATA_FROM_MULTIFILES_COMMAND("Load MSMS data from multiple files"),
	LOAD_DATA_FROM_EXCEL_FILE_COMMAND("Load quantitative data from Excel file"),
	IMPORT_DATA_FROM_EXCEL_WORKSHEET_COMMAND("Import quantitative data from Excel worksheet"),
	LOAD_LIBRARY_COMMAND("Load compound library"),
	LOAD_WORKLIST_COMMAND("Load assay worklist"),
	ADD_WORKLIST_COMMAND("Add assay worklist data"),
	SAVE_WORKLIST_COMMAND("Save assay worklist to file"),
	SAVE_ASSAY_MANIFEST_COMMAND("Save assay manifest to file"),
	COPY_WORKLIST_COMMAND("Copy assay worklist to clipboard as text"),
	EXTRACT_WORKLIST_COMMAND("Extract assay worklist to text file from raw data folder"),
	SCAN_DIR_SAMPLE_INFO_COMMAND("Scan directory for sample information"),
	SCAN_DIR_ADD_SAMPLE_INFO_COMMAND("Add sample information from directory"),
	LOAD_MGF_COMMAND("Load MGF data file"),
	SEND_WORKLIST_TO_DATABASE("Send worklist to database"),
	REFRESH_WORKLIST("Refresh worklist"),
	CHECK_WORKLIST_FOR_MISSING_DATA("Check worklist for missing data"),
	LOOKUP_WORKLIST_DATA_IN_DATABASE("Add worklist information from database"),

	//	Multi-file import
	SHOW_DATA_PIPELINE_DEFINITION_DIALOG_COMMAND("Show data pipeline definition dialog"),
	DEFINE_DATA_PIPELINE_COMMAND("Define data pipeline"),
	SAVE_DATA_PIPELINE_COMMAND("Save data pipeline definition"),
	SELECT_INPUT_LIBRARY_COMMAND("Select input library"),
	ADD_DATA_FILES_COMMAND("Add data files"),
	REMOVE_DATA_FILES_COMMAND("Remove data files"),
	CLEAR_DATA_COMMAND("Clear all input data"),
	IMPORT_DATA_COMMAND("Import data"),
	ASSIGN_SAMPLESS_COMMAND ("Assign sample to data files"),
	SHOW_DATA_ACQUISITION_SELECTOR_COMMAND("Show data acquisition method selector"),
	SELECT_DATA_ACQUISITION_METHOD_COMMAND("Select data acquisition method"),
	SHOW_DATA_EXTRACTION_SELECTOR_COMMAND("Show data analysis method selector"),
	SELECT_DATA_EXTRACTION_METHOD_COMMAND("Select data analysis method"),
	

	//	Design
	SHOW_DESIGN_TABLE_EDITOR_COMMAND("Show design editor"),
	LINK_SAMPLES_TO_FILES_COMMAND("Link files to samples"),
	DELETE_DATA_FILES_COMMAND("Delete data file(s)"),
	ENABLE_SELECTED_SAMPLES_COMMAND("Enable selected samples"),
	DISABLE_SELECTED_SAMPLES_COMMAND("Disable selected samples"),
	ENABLE_ALL_SAMPLES_COMMAND("Enable all shown samples"),
	DISABLE_ALL_SAMPLES_COMMAND("Disable all shown samples"),
	INVERT_ENABLED_SAMPLES_COMMAND("Invert enabled samples"),
	CLEAR_SAMPLES_FILTER_COMMAND("Clear samples filter"),
	LOAD_DESIGN_COMMAND("Load experiment design"),
	LOAD_DATA_FILE_SAMPLE_MAP_COMMAND("Load data file / sample map"),
	APPEND_DESIGN_COMMAND("Append design"),
	CLEAR_DESIGN_COMMAND("Clear design"),
	EXPORT_DESIGN_COMMAND("Export design to file"),
	ADD_FACTOR_COMMAND("Add experimental factor"),
	DELETE_FACTOR_COMMAND("Delete experimental factor"),
	EDIT_FACTOR_COMMAND("Edit experimental factor"),
	ADD_SAMPLE_COMMAND("Add sample"),
	DELETE_SAMPLE_COMMAND("Delete sample"),
	ADD_SAMPLE_DIALOG_COMMAND("Show \"Add sample\" dialog"),
	EDIT_SAMPLE_DIALOG_COMMAND("Show \"Edit sample\" dialog"),
	EDIT_SAMPLE_COMMAND("Edit sample"),
	SAVE_NEW_FACTOR_COMMAND("Save new factor"),
	SAVE_EDITED_FACTOR_COMMAND("Save edited factor"),
	SAVE_DESIGN_TO_LIMS_COMMAND("Save design to LIMS"),
	SEND_DESIGN_TO_EXPERIMENT_COMMAND("Send design to experiment"),
	MATCH_IMPORTED_TO_DESIGN_COMMAND("Match imported data to samples"),
	ACCEPT_EXCEL_SAMPLE_MATCH_COMMAND("Accept sample assignment"),
	SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND("Edit reference sample(s) dialog"),
	EDIT_REFERENCE_SAMPLES_COMMAND("Edit reference sample(s)"),
	ADD_REFERENCE_SAMPLES("Add reference samples"),
	REMOVE_REFERENCE_SAMPLES("Remove reference samples"),	
	ENABLE_ALL_LEVELS_COMMAND("Enable all experimental groups"),
	DISABLE_ALL_LEVELS_COMMAND("Disable all experimental groups"),
	EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND("Edit design for selected samples"),
	CHOOSE_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND("Choose acquisition method for selected data files"),
	ASSIGN_ACQ_METHOD_FOR_SELECTED_DATA_FILES_COMMAND("Assign acquisition method for selected data files"),
	SPECIFY_INJ_VOLUME_FOR_SELECTED_DATA_FILES_COMMAND("Specify injection volume for selected data files"),
	ASSIGN_INJ_VOLUME_FOR_SELECTED_DATA_FILES_COMMAND("Assign injection volume for selected data files"),
	SPECIFY_INJ_TIME_FOR_SELECTED_DATA_FILES_COMMAND("Specify injection time for selected data files"),
	ASSIGN_INJ_TIME_FOR_SELECTED_DATA_FILES_COMMAND("Assign injection time for selected data files"),
	ASSIGN_BATCH_FOR_SELECTED_DATA_FILES_COMMAND("Assign batch for selected data files"),
	LOAD_EXCEL_DATA_FOR_PREVIEW_COMMAND("Load data from Excel file into preview"),
	CLEAR_EXCEL_IMPORT_WIZARD_COMMAND("Clear Excel import wizard"),

	//	Plot
	SAVE_AS_PNG("Save as PNG"),
    SAVE_AS_SVG("Save as SVG"),
    SAVE_AS_PDF("Save as PDF"),
    COPY_AS_IMAGE("Copy image to clipboard"),

    //	Table
    SHOW_ALL_TABLE_COLUMNS_COMMAND("Show all columns"),
    ADJUST_ALL_TABLE_COLUMNS_COMMAND("Adjust all columns"),
    RESET_COLUMN_FILTERS_COMMAND("Reset column filters"),
    SHOW_TABLE_PREFERENCES_COMMAND("Show table preferences"),
    APPLY_TABLE_PREFERENCES_COMMAND("Set table preferences"),
    ADD_COLUMN_TO_SORTER_COMMAND("Add column to sorter"),
    REMOVE_COLUMN_FROM_SORTER_COMMAND("Remove column from sorter"),
    INCREASE_SORTER_PRIORITY_COMMAND("Increase sorter priority"),
    DECREASE_SORTER_PRIORITY_COMMAND("Decrease sorter priority"),
    
    //	3D plot
    RESET_ZOOM("Reset zoom"),
	ZOOM_IN("Zoom in"),
    ZOOM_OUT("Zoom out"),
    ROTATE_LEFT("Rotate left"),
    ROTATE_RIGHT("Rotate right"),
    ROTATE_FORWARD("Rotate forward"),
    ROTATE_BACKWARD("Rotate backward"),
    ROLL_LEFT("Roll left"),
    ROLL_RIGHT("Roll right"),

    //MS-calculator
    CALCULATE_ISOTOPE_DISTRIBUTION("Calculate isotope distribution"),
    TOGGLE_CAPSLOCK("Toggle CAPSLOCK"),
    GENERATE_FORMULA_COMMAND("Generate formulas"),
    EDIT_ELEMENT_LIST_COMMAND("Edit element list"),
    RESET_ELEMENTS_COMMAND("Reset element limits"),
    CLEAR_ISOTOPE_DISTRIBUTION_PANEL("Clear isotope distribution panel"),
    SEARCH_FORMULA_AGAINST_DATABASE_COMMAND("Search formula against database"),
    SEARCH_FORMULA_AGAINST_LIBRARY_COMMAND("Search formula against active library"),
    COPY_FORMULA_COMMAND("Copy formula to clipboard"),
    COPY_LINE_COMMAND("Copy line to clipboard"),

	// Remove data
	CLEAR_LIBRARY_COMMAND("Clear compound library"),
	CLEAR_WORKLIST_COMMAND("Clear assay worklist"),

	// Export data
	EXPORT_RESULTS_COMMAND("Export results"),
	SELECT_DATA_EXPORT_FILE_COMMAND("Set data export file"),
	EXPORT_RESULTS_4R_COMMAND("Export results for R"),
	EXPORT_RESULTS_4MPP_COMMAND("Export results for MPP"),
	EXPORT_RESULTS_4METSCAPE_COMMAND("Export results for MetScape"),
	EXPORT_RESULTS_4BINNER_COMMAND("Export results for Binner"),
	EXPORT_RESULTS_TO_MWTAB_COMMAND("Export experiment report in MWTab format"),
	EXPORT_MZRT_STATISTICS_COMMAND("Export M/Z & RT data for individual samples"),
	SHOW_RAWA_DATA_UPLOAD_PREP_DIALOG("Prepare raw data for upload"),

	//	Copy/paste/cut etc...
	COPY_SELECTED_ROWS_COMMAND(	"Copy selected rows"),
	COPY_SELECTED_ROWS_WITH_HEADER_COMMAND("Copy selected rows with header"),

	//
	COPY_SELECTED_MS1_ROWS_COMMAND("Copy selected MS1 features (TAB separated)"),
	COPY_SELECTED_MS1_ROWS_WITH_HEADER_COMMAND(	"Copy selected MS1 features (TAB separated) with header"),
	COPY_SELECTED_MS2_ROWS_COMMAND("Copy selected MS2 features (TAB separated)"),
	COPY_SELECTED_MS2_ROWS_WITH_HEADER_COMMAND("Copy selected MS2 features (TAB separated) with header"),
	COPY_FEATURE_SPECTRUM_AS_MSP_COMMAND("Copy selected MS2 feature spectrum as MSP"),
	COPY_FEATURE_SPECTRUM_AS_ARRAY_COMMAND("Copy selected MS2 feature spectrum as array"),

	// Correlation panel commands
	SHOW_REFERENCE_DATA_COMMAND("Show reference data"),
	SHOW_MS_TOOLBOX_COMMAND("Show MS toolbox"),
	SHOW_HEATMAP_COMMAND("Show heatmap"),
	SHOW_DENDROGRAMM_COMMAND("Show dendrogram"),
	SHOW_CORRELATION_TABLE_COMMAND("Show correlation table"),
	SHOW_CLUSTER_FILTER_COMMAND("Show feature cluster filter"),
	HIDE_CLUSTER_FILTER_COMMAND("Hide tree filter"),
	FILTER_CLUSTERS_COMMAND("Filter clusters"),
	RESET_FILTER_CLUSTERS_COMMAND("Reset feature cluster filter"),
	EXPAND_CLUSTERS_COMMAND("Expand all clusters"),
	COLLAPSE_CLUSTERS_COMMAND("Collapse all clusters"),
	DELETE_CLUSTER_COMMAND("Delete cluster"),
	DISSOLVE_CLUSTER_COMMAND("Dissolve cluster"),
	EDIT_CLUSTER_COMMAND("Edit cluster"),
	CREATE_XIC_METHOD_COMMAND("Create XIC method for selected cluster"),
	CREATE_XIC_METHOD_SET_COMMAND("Create Qual XIC methods for all clusters"),

	// Correlations
	SHOW_CORRELATIONS_ANALYSIS_SETUP_COMMAND("Correlation analysis setup"),
	FIND_FEATURE_CORRELATIONS_COMMAND("Find feature correlations"),
	RERUN_BINNING_COMMAND("Re-analyze correlations"),
	SHOW_BINNER_REPORT_IMPORT_DIALOG("Binner analysis results import setup"),
	IMPORT_BINNER_DATA_COMMAND("Import Binner analysis results"),
	EXPLORE_MASS_DIFFS_IN_BINNEER_DATA_COMMAND("Explore mass differences in Binner data"),
	RACALCULATE_CLUSTER_CORR_MATRIX_COMMAND("Re-calculate correlations within clusters"),
	ANNOTATE_CLUSTER_COMMAND("Annotate cluster features"),
	SHOW_BATCH_ANNOTATE_PREFERENCES_COMMAND("Batch annotation setup"),
	BATCH_ANNOTATE_CLUSTERS_COMMAND("Annotate all clusters"),
	ANNOTATE_MASS_DIFFERENCES_COMMAND("Annotate mass differences"),
	EXTRACT_MASS_DIFFERENCES_COMMAND("Extract mass differences"),

	// Feature list and cluster commands
	ASSIGN_FEATURE_ANNOTATION_COMMAND("Assign annotation"),
	NEW_CLUSTER_FROM_SELECTED_COMMAND("New cluster from selected features"),
	REMOVE_SELECTED_FROM_CLUSTER_COMMAND("Remove selected from cluster"),
	REMOVE_SELECTED_FROM_CLUSTER_AND_LIST_COMMAND("Remove selected from cluster AND list"),
	REMOVE_SELECTED_FROM_LIST_COMMAND("Remove selected from list"),
	SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND("Match selected feature to library"),
	SEARCH_FEATURE_AGAINST_DATABASE_COMMAND("Search selected feature against database"),
	ADD_MANUAL_IDENTIFICATION_COMMAND("Add manual compound identification"),
	ADD_MANUAL_MSMS_IDENTIFICATION_COMMAND("Add manual MSMS compound identification"),
	CLEAR_SELECTED_FEATURE_IDENTIFICATION_COMMAND("Clear selected feature identification"),
	CLEAR_SELECTED_MSMS_FEATURE_IDENTIFICATION_COMMAND("Clear selected MSMS feature identification"),
	EDIT_FEATURE_METADATA_COMMAND("Edit feature information"),
	REMOVE_UNEXPLAINED_FEATURES_COMMAND("Remove all unexplained features from clusters"),
	REJECT_UNEXPLAINED_FEATURES_COMMAND(	"Reject all unexplained features"),
	REJECT_ALL_BUT_MOLION_COMMAND(	"Reject all features except molecular ion"),
	RESTORE_ALL_REJECTED_FEATURES_COMMAND(	"Restore all rejected features"),
	ADD_ACTIVE_FEATURES_TO_SUBSET_COMMAND(	"Add active features to selected subset"),
	TOGGLE_CLUSTER_LOCK_COMMAND("Toggle cluster lock"),
	SHOW_ALL_CLUSTER_SPECTRA_COMMAND("Show spectra for all features"),
	RECALCULATE_CORRRELATIONS_4CLUSTER_COMMAND("Recalculate correlations for cluster"),
	MERGE_SELECTED_FEATURES_COMMAND("Merge selected features"),	
	SHOW_FEATURE_FILTER_COMMAND("Show feature filter"),
	SHOW_CLUSTERED_FEATURE_FILTER_COMMAND("Show clustered feature filter"),
	FILTER_FEATURES_COMMAND("Filter features"),
	SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND("Search by RT/identity setup"),
	SEARCH_FEATURES_BY_RT_ID_COMMAND("Search features by RT/identity"),
	SHOW_MSMS_DATA_SET_STATISTICS_COMMAND("Show MSMS data set statistics"),
	SHOW_CLUSTERED_MSMS_DATA_SET_STATISTICS_COMMAND("Show clustered MSMS data set statistics"),

	// Duplicates
	SHOW_FIND_DUPLICATES_DIALOG_COMMAND("Show find duplicates options"),
	FIND_DUPLICATES_COMMAND("Find duplicate featuress"),
	SHOW_DUPLICATES_MERGE_DIALOG_COMMAND("Show duplicates merge options"),
	MERGE_DUPLICATES_COMMAND("Merge duplicate entries"),
	EXPORT_DUPLICATES_COMMAND("Export duplicate entries"),
	EXPORT_RESULTS_TO_EXCEL_COMMAND("Export experiment report to Excel"),
	EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND("Export results for metabolomics workbench"),
	CHECK_FOR_DUPLICATE_NAMES_COMMAND("Check for duplicate names"),	
	SHOW_ONLY_PROBLEM_CLUSTERS_COMMAND("Show only problem clusters"),	
	SHOW_ALL_CLUSTERS_COMMAND("Show all clusters"),
	
	//	MSMS clusters
	SHOW_MSMS_CLUSTER_FILTER_COMMAND("Show MSMS cluster filter"),
	FILTER_MSMS_CLUSTERS_COMMAND("Filter MSMS clusters"),
	RELOAD_ACTIVE_MSMS_CLUSTERS_SET_COMMAND("Reload active MSMS clusters data set"),
	SHOW_MSMS_CLUSTERS_SUMMARY_COMMAND("Show MSMS clusters summary"),
	COPY_LOOKUP_FEATURES_TO_CLIPBOARD_COMMAND("Copy lookup features toclipboard"),

	// Data integration
	DATA_INTEGRATION_DIALOG_COMMAND("Show data integration setup dialog"),
	COLLECT_IDENTIFIED_CPD_COMMAND("Collect data for identified compounds"),
	CLEAR_IDENTIFIED_CPD_COMMAND("Clear data for identified compounds"),
	DELETE_INTEGRATION_SET_COMMAND("Delete data integration set"),
	ACCEPT_CLEAN_ID_LIST_COMMAND("Accept integrated list of identified compounds"),

	// Sorting commands
	SORT_BY_AREA_COMMAND("Sort by area (high to low)"),
	SORT_BY_MZ_COMMAND(	"Sort by base peak m/z"),
	SORT_BY_FNUM_COMMAND(	"Sort by feature number in cluster (high to low)"),
	SORT_BY_CLUSTER_NAME_COMMAND(	"Sort by cluster name"),
	SORT_BY_RT_COMMAND("Sort by retention"),
	SORT_BY_RANK_COMMAND("Sort by rank"),

	// Feature statistics panel commands
	CALC_FEATURES_STATS_COMMAND("Calculate features stats"),
	CLEAN_EMPTY_FEATURES_COMMAND("Remove features with no data"),
	SHOW_KNOWN_FEATURES_COMMAND(	"Show known features only"),
	SHOW_UNKNOWN_FEATURES_COMMAND(	"Show unknown features only"),
	SHOW_QC_FEATURES_COMMAND(	"Show quality control features only"),
	SHOW_FEATURES_ASSIGNED_TO_CLUSTERS_COMMAND(	"Show features assigned to clusters"),
	SHOW_FEATURES_NOT_ASSIGNED_TO_CLUSTERS_COMMAND(	"Show features NOT assigned to clusters or assigned to single-feature clusters"),
	SHOW_IMPUTE_DIALOG_COMMAND(	"Show data imputation dialog"),
	SHOW_FEATURES_AGAINST_LIBRARIES_DIALOG_COMMAND("Library search setup"),
	SHOW_FEATURES_AGAINST_DATABASES_DIALOG_COMMAND("Databse search setup"),
	SEARCH_FEATURES_AGAINST_LIBRARIES_COMMAND("Search features against libraries"),
	SEARCH_FEATURES_AGAINST_DATABASES_COMMAND("Search features against databses"),
	SHOW_MISSING_IDENTIFICATIONS_COMMAND("Show missing identifications"),
	CLEAR_IDENTIFICATIONS_COMMAND("Clear feature identifications"),
	IMPUTE_DATA_COMMAND("Impute missing data"),
	FIND_REATURES_BY_ADDUCT_MASS("Find feaures with adduct masses"),
	SHOW_FEATURE_MZ_RT_BUBBLE_PLOT("Show M/Z vs RT plot for features"),

	//	LIMS commands
	REFRESH_LIMS_DATA_COMMAND("Load/refresh data from LIMS"),
	SHOW_DATA_UPLOAD_WIZARD_COMMAND("Show data upload wizard"),
	SYNCHRONIZE_MRC2LIMS_TO_METLIMS_COMMAND("Synchronize MRC2LIMS to METLIMS"),
	RESYNCHRONIZE_MRC2LIMS_EXPERIMENT_TO_METLIMS_COMMAND("Reload experiment from METLIMS to MRC2LIMS"),
	CREATE_EXPERIMENT_DIRECTORY_STRUCTURE_COMMAND("Create directory structure for selected experiment"),
	DELETE_EXPERIMENT_FROM_MRC2LIMS_COMMAND("Delete experiment from MRC2LIMS"),
	
	//	MoTrPAC panel commands
	SHOW_MOTRPAC_METADATA_REFERENCE_COMMAND("Show MoTrpPAC metadata reference"),
	SHOW_MOTRPAC_REPORT_UPLOAD_DIALOG_COMMAND("Show MoTrpPAC report upload dialog"),
	UPLOAD_MOTRPAC_REPORT_COMMAND("Upload MoTrpPAC report"),
	EDIT_MOTRPAC_REPORT_METADATA_COMMAND("Edit MoTrpPAC report metadata"),
	SAVE_MOTRPAC_REPORT_METADATA_COMMAND("Save MoTrpPAC report metadata"),
	CONFIRM_DELETE_MOTRPAC_REPORT_COMMAND("Authorize deleting MoTrpPAC report"),
	DELETE_MOTRPAC_REPORT_COMMAND("Delete MoTrpPAC report"),

	CREATE_MOTRPAC_REPORT_FILES_COMMAND("Create MoTrpPAC report files"),
	
	CREATE_DIRECTORY_STRUCTURE_FOR_BIC_UPLOAD("Create directory structure for BIC upload"),
	SELECT_FILE_LIST_FOR_COMPRESSION_COMMAND("Select file list for compression"),
	SELECT_RAW_DATA_DIRECTORIES_COMMAND("Select raw data directories"),
	SELECT_DESTINATION_FOR_COMPRESSED_FILES_COMMAND("Select destination for compressed files"),
	SET_UP_AGILENT_DOTD_FILES_COMPRESSION_COMMAND("Set up Agilent raw data compression"),
	COMPRESS_AGILENT_DOTD_FILES_FOR_UPLOAD_COMMAND("Compress Agilent raw data for upload"),	
	
	SET_UP_UPLOAD_MANIFEST_GENERATION_COMMAND("Set up BIC upload manifest generation"),
	SELECT_MOTRPAC_UPLOAD_BATCH_FOR_MANIFEST_FILE_COMMAND("Select MoTrPAC BATCH directory to process"),
	CREATE_MANIFEST_FOR_BIC_UPLOAD_COMMAND("Create BIC upload manifest"),
		
	//	MoTrPAC study dialog
	ADD_MOTRPAC_STUDY_DIALOG_COMMAND("Add MoTrPAC study dialog"),
	ADD_MOTRPAC_STUDY_COMMAND("Add MoTrPAC study"),
	EDIT_MOTRPAC_STUDY_DIALOG_COMMAND("Edit MoTrPAC study dialog"),
	EDIT_MOTRPAC_STUDY_COMMAND("Edit MoTrPAC study"),
	DELETE_MOTRPAC_STUDY_COMMAND("Delete MoTrPAC study"),
	SAVE_MOTRPAC_STUDY_COMMAND("Save MoTrPAC study definition"),
	
	//	MoTrPAC assay manager
	ADD_MOTRPAC_ASSAY_DIALOG_COMMAND("Add MoTrPAC assay dialog"),
	ADD_MOTRPAC_ASSAY_COMMAND("Add MoTrPAC assay"),
	EDIT_MOTRPAC_ASSAY_DIALOG_COMMAND("Edit MoTrPAC assay dialog"),
	EDIT_MOTRPAC_ASSAY_COMMAND("Edit MoTrPAC assay"),
	DELETE_MOTRPAC_ASSAY_COMMAND("Delete MoTrPAC assay"),
	
	//	Tisue codes manager
	ADD_TISSUE_CODE_DIALOG_COMMAND("Add tissue code dialog"),
	ADD_TISSUE_CODE_COMMAND("Add tissue code"),
	EDIT_TISSUE_CODE_DIALOG_COMMAND("Edit tissue code dialog"),
	EDIT_TISSUE_CODE_COMMAND("Edit tissue code"),
	DELETE_TISSUE_CODE_COMMAND("Delete tissue code"),
	
	//	Study assay editor
	EDIT_MOTRPAC_STUDY_ASSAYS_COMMAND("Edit MoTrPAC study assay list"),
	SAVE_MOTRPAC_STUDY_ASSAYS_COMMAND("Save MoTrPAC study assay list"),
	ADD_MOTRPAC_ASSAYS_TO_STUDY_COMMAND("Add MoTrPAC assays to study"),
	REMOVE_MOTRPAC_ASSAYS_FROM_STUDY_COMMAND("Remove MoTrPAC assays from study"),	
	
	//	Study experiment editor
	ADD_EXPERIMENTS_TO_MOTRPAC_STUDY_COMMAND("Add experiments to MoTrPAC study"),
	REMOVE_EXPERIMENTS_FROM_MOTRPAC_STUDY_COMMAND("Remove experiments from MoTrPAC study"),
	EDIT_MOTRPAC_STUDY_EXPERIMENTS_COMMAND("Edit MoTrPAC study experiment list"),
	SAVE_MOTRPAC_STUDY_EXPERIMENTS_COMMAND("Save MoTrPAC study experiment list"),

	//	Study experiment tissue editor
	EDIT_MOTRPAC_STUDY_TISSUES_COMMAND("Edit MoTrPAC experiment tissue list"),
	SAVE_MOTRPAC_STUDY_TISSUES_COMMAND("Save MoTrPAC experiment tissue list"),
	ADD_MOTRPAC_STUDY_TISSUES_COMMAND("Add tissues to MoTrPAC experiment"),
	REMOVE_MOTRPAC_STUDY_TISSUES_COMMAND("Remove tissues from MoTrPAC experiment"),

	// Feature subset commands
	NEW_FEATURE_SUBSET_COMMAND("Create new feature subset"),
	ADD_SELECTED_FEATURES_TO_SUBSET_COMMAND(	"Add selected features to active subset"),
	ADD_FILTERED_FEATURES_TO_SUBSET_COMMAND(	"Add filtered features to active subset"),
	EDIT_FEATURE_SUBSET_COMMAND(	"Edit selected feature subset"),
	DELETE_FEATURE_SUBSET_COMMAND(	"Delete selected feature subset"),
	REMOVE_SELECTED_FEATURES_FROM_SUBSET_COMMAND(	"Remove selected features from subset"),
	EXPORT_FEATURE_SUBSET_COMMAND(	"Export selected feature subset as library"),
	SHOW_FEATURE_EXPORT_DIALOG_COMMAND(	"Feature subset export dialog"),
	FINISH_FEATURE_SUBSET_EDIT_COMMAND(	"Save changes to feature subset"),
	LINK_FEATURE_SUBSET_COMMAND(	"Link feature subset"),
	UNLINK_FEATURE_SUBSET_COMMAND(	"Unlink feature subset"),
	LOCK_FEATURE_SUBSET_COMMAND("Lock selected feature subset"),
	UNLOCK_FEATURE_SUBSET_COMMAND("Unlock selected feature subset"),

	// Design subset commands
	SHOW_DESIGN_SUBSET_DIALOG_COMMAND("Show design subset dialog"),
	VIEW_DESIGN_SUBSET_COMMAND("View design subset"),
	NEW_DESIGN_SUBSET_COMMAND("Create new design subset"),
	COPY_DESIGN_SUBSET_COMMAND("Copy selected design subset"),
	DELETE_DESIGN_SUBSET_COMMAND("Delete selected design subset"),
	LINK_DESIGN_SUBSET_COMMAND(	"Link selected design subset"),
	UNLINK_DESIGN_SUBSET_COMMAND("Unlink selected design subset"),
	SAVE_DESIGN_SUBSET_EDIT_COMMAND("Save new/edited design subset"),
	LOCK_DESIGN_SUBSET_COMMAND("Lock selected design subset"),
	UNLOCK_DESIGN_SUBSET_COMMAND("Unlock selected design subset"),

	// Adducts/modifications
	SHOW_CHEM_MOD_EDITOR_COMMAND("Show chemical modifications editor"),
	SHOW_ADDUCT_EXCHANGE_EDITOR_COMMAND(	"Show adduct exchange editor"),
	NEW_MODIFICATION_COMMAND("Add new modification"),
	NEW_COMPOSITE_MODIFICATION_COMMAND("Add new composite modification"),
	EDIT_MODIFICATION_COMMAND("Edit selected modification"),
	DELETE_MODIFICATION_COMMAND("Delete selected modification"),
	IMPORT_MODIFICATIONS_COMMAND("Import modifications list from file"),
	EXPORT_MODIFICATIONS_COMMAND("Export modifications list to file"),
	SAVE_SIMPLE_MODIFICATION_DATA_COMMAND("Save modification"),
	SAVE_COMPOSITE_MODIFICATION_DATA_COMMAND("Save composite adduct"),
	NEW_EXCHANGE_COMMAND("Add new exchage"),
	EDIT_EXCHANGE_COMMAND("Edit selected exchange"),
	DELETE_EXCHANGE_COMMAND("Delete selected exchsnge"),
	IMPORT_EXCHANGE_LIST_COMMAND("Import exchage list from file"),
	EXPORT_EXCHANGE_LIST_COMMAND("Save exchange list to file"),
	SAVE_EXCHANGE_DATA_COMMAND("Save exchange"),
	VERIFY_MODIFICATION_SMILES_COMMAND("Verify modification structure from SMILES"),
	
	//	Composite modification editor
	SELECT_CHARGE_CARRIER_DIALOG_COMMAND("Show \"Select charge carrier\" dialog"),
	SELECT_CHARGE_CARRIER_COMMAND("Select charge carrier"),
	ADD_NEUTRAL_LOSS_DIALOG_COMMAND("Show \"Add neutral loss\" dialog"),
	ADD_NEUTRAL_LOSS_COMMAND("Add neutral loss"),
	ADD_NEUTRAL_ADDUCT_DIALOG_COMMAND("Show \"Add neutral adduct\" dialog"),
	ADD_NEUTRAL_ADDUCT_COMMAND("Add neutral adduct"),
	REMOVE_SELECTED_ADDUCT_COMMAND("Remove selected adduct"),
	
	//	Binner adduct editor
	REFRESH_BINNER_ADDUCT_LIST_COMMAND("Refresh Binner annotation list"),
	NEW_BINNER_ADDUCT_COMMAND("Create new Binner annotation from adduct"),
	NEW_BINNER_ADDUCT_FROM_EXCHANGE_COMMAND("Create new Binner annotation from adduct exchange"),
	NEW_BINNER_ADDUCT_FROM_MASS_DIFF_COMMAND("Create new Binner annotation from repeats"),
	EDIT_BINNER_ADDUCT_COMMAND("Edit Binner annotation"),
	SAVE_BINNER_ADDUCT_COMMAND("Save Binner annotation"),
	DELETE_BINNER_ADDUCT_COMMAND("Delete Binner annotation"),
	EXPORT_BINNER_ADDUCTS_COMMAND("Export Binner annotations to file"),
	SHOW_BINNER_MASS_DIFFERENCE_MANAGER_COMMAND("Edit Binner mass differences"),
	
	//	Binner massDiff editor
	NEW_BINNER_MASS_DIFFERENCE_COMMAND("Create new Binner mass difference combination"),
	EDIT_BINNER_MASS_DIFFERENCE_COMMAND("Edit selected Binner mass difference combination"),
	DELETE_BINNER_MASS_DIFFERENCE_COMMAND("Delete selected Binner mass difference combination"),
	SAVE_BINNER_MASS_DIFFERENCE_COMMAND("Save Binner mass difference combination"),
	NEW_COMPOSITE_MODIFICATION_FROM_MASS_DIFF_COMMAND("Create new composite modification from Binner mass difference"),
	
	//	Database parser tool
	SHOW_DATABASE_PARSER_COMMAND("Show database parser"),

	// QC panel
	CALC_DATASET_STATS_COMMAND("Calculate global data set statistics"),
	CALC_DATASET_PCA_COMMAND(	"PCA on data set samples"),

	//	Annotations
	SHOW_OBJECT_ANNOTATION_DIALOG_COMMAND("Show annotation editor dialog"),
	ADD_OBJECT_ANNOTATION_COMMAND("Add new annotation"),
	ADD_STRUCTURAL_ANNOTATION_COMMAND("Add new structural annotation"),
	EDIT_OBJECT_ANNOTATION_COMMAND("Edit annotation"),
	DELETE_OBJECT_ANNOTATION_COMMAND("Delete annotation"),
	SAVE_OBJECT_ANNOTATION_COMMAND("Save annotation"),
	SAVE_OBJECT_DOCUMENT_ANNOTATION_COMMAND("Save annotation document"),
	SAVE_OBJECT_STRUCTURAL_ANNOTATION_COMMAND("Save structural annotation document"),
	PREVIEW_ANNOTATION_COMMAND("View annotation"),
	DOWNLOAD_ANNOTATION_COMMAND("Download annotation as document"),
	
	//	Annotation editor
	TEXT_ANNOTATION_FORMAT_COMMAND("Switch to plain text mode"),
	RTF_ANNOTATION_FORMAT_COMMAND("Switch to rich text mode"),
	ATTACH_DOCUMENT_ANNOTATION_COMMAND("Attach document annotation"),

	//	Document upload
	ADD_DOCUMENT_DIALOG_COMMAND("Add document dialog"),
	ADD_DOCUMENT_COMMAND("Add document"),
	DELETE_DOCUMENT_COMMAND("Delete document"),

	// Feature identification
	DISABLE_PRIMARY_IDENTIFICATION_COMMAND("Disable primary identification"),
	DELETE_IDENTIFICATION_COMMAND("Delete selected identification"),
	DELETE_ALL_IDENTIFICATIONS_COMMAND("Delete all identifications"),
	GO_TO_LIBRARY_FEATURE_COMMAND("Show library entry"),
	GO_TO_COMPOUND_IN_DATABASE_COMMAND("Show compound database entry"),
	GO_TO_PRIMARY_COMPOUND_IN_DATABASE_COMMAND("Show compound database entry for feature"),
	SHOW_ID_LEVEL_ASSIGNMENT_DIALOG_COMMAND("Select ID level"),
	ASSIGN_ID_LEVEL_COMMAND("Assign ID level"),
	COPY_LIBRARY_SPECTRUM_AS_MSP_COMMAND("Copy library spectrum to MSP"),
	COPY_LIBRARY_SPECTRUM_AS_ARRAY_COMMAND("Copy library spectrum to array"),
	SET_AS_PRIMARY_ID_FOR_CLUSTER("Set as primary ID for feature cluster"),

	//	Reordering toolbar commands
	MOVE_TO_TOP_COMMAND("Move to the top"),
	MOVE_UP_COMMAND("Move up"),
	MOVE_DOWN_COMMAND("Move down"),
	MOVE_TO_BOTTOM_COMMAND("Move to the bottom"),

	//	Level editor commands
	ADD_LEVEL_COMMAND("Add level"),
	DELETE_LEVEL_COMMAND("Delete level"),

	//	Renaming commands
	RENAME_GENERIC_COMMAND("Rename"),
	RENAME_FACTOR_COMMAND("Rename selected factor"),
	RENAME_LEVEL_COMMAND("Rename selected level"),
	RENAME_DESIGN_SUBSET_COMMAND("Rename selected experiment design subset"),
	RENAME_FEATURE_SET_COMMAND("Rename selected feature subset"),

	//	Library manager
	SHOW_LIBRARY_MANAGER_COMMAND("Show library manager"),
	SHOW_LIBRARY_LIST_COMMAND("Open library from database"),
	CREATE_NEW_LIBRARY_COMMAND("Create new library"),
	EDIT_MS_LIBRARY_INFO_COMMAND("Edit library information"),
	DELETE_LIBRARY_COMMAND("Delete library"),
	DUPLICATE_LIBRARY_COMMAND("Duplicate library"),
	OPEN_LIBRARY_COMMAND("Open library"),
	CLOSE_LIBRARY_COMMAND("Close library"),
	IMPORT_COMPOUND_LIBRARY_COMMAND("Import compound library"),
	EXPORT_COMPOUND_LIBRARY_COMMAND("Export compound library"),
	EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND("Export filtered compound library"),
	NEW_LIBRARY_DIALOG_COMMAND("Show \"New Library\" dialog"),
	EDIT_LIBRARY_DIALOG_COMMAND("Show \"Edit Library information\" dialog"),
	DUPLICATE_LIBRARY_DIALOG_COMMAND("Show \"Duplicate library\" dialog"),
	MERGE_LIBRARY_WITH_UNKNOWNS_DIALOG_COMMAND("Show \"Merge database and file library\" dialog"),
	MERGE_LIBRARIES_COMMAND("Merge selected libraries"),
	SEARCH_FEATURE_ACCESSION_IN_DATABASE_COMMAND("Lookup database entry"),
	SEARCH_FEATURE_FORMULA_IN_DATABASE_COMMAND("Lookup in database by formula"),
	SEARCH_FEATURE_MASS_IN_DATABASE_COMMAND("Lookup in database by exact mass"),
	CONVERT_LIBRARY_FOR_RECURSION_DIALOG_COMMAND("Show \"Convert library for recursion\" dialog"),	
	SELECT_INPUT_LIBRARY_FOR_CONVERSION_COMMAND("Select CEF library for conversion"),
	SELECT_OUTPUT_FOLDER_FOR_CONVERTED_LIBRARY_COMMAND("Select output folder for converted library"),
	CONVERT_LIBRARY_FOR_RECURSION_COMMAND("Convert library for recursion"),
	LOAD_SELECTED_LIBRARY_COMMAND("Load selected library"),

	//	MSMS
	SHOW_MSMS_IMPORT_DIALOG_COMMAND("Show MSMS file import dialog"),
	IMPORT_MSMS_FROM_XML_COMMAND("Import MSMS from XML file"),
	IMPORT_MSMS_FROM_MSP_COMMAND("Import MSMS from MSP file"),
	SHOW_MSMS_DATA_FILTER_COMMAND("Show MSMS data filter"),
	FILTER_MSMS_DATA_COMMAND("Filter MSMS data"),
	IMPORT_FILTERED_MSMS_DATA_COMMAND("Import filtered MSMS data"),
	RESET_MSMS_FILTER_COMMAND("Reset MSMS filter"),
	ACCEPT_MSMS_COMMAND("Accept new/edited MSMS"),
	DELETE_MSMS_COMMAND("Delete selected MSMS"),
	SELECT_MSMS_FILE_TO_IMPORT_COMMAND("Select MSMSfile to import"),

	NEW_LIBRARY_FEATURE_DIAOG_COMMAND("New library feature dialog"),
	EDIT_LIBRARY_FEATURE_DIALOG_COMMAND("Show library feature editor"),
	NEW_LIBRARY_FEATURE_COMMAND("Create new library feature"),
	EDIT_LIBRARY_FEATURE_COMMAND("Edit library feature"),
	UNDO_LIBRARY_FEATURE_EDIT_COMMAND("Undo library feature edit"),
	DELETE_LIBRARY_FEATURE_COMMAND("Delete library feature"),
	DUPLICATE_LIBRARY_FEATURE_COMMAND("Duplicate library feature"),
	IMPORT_LIBRARY_FEATURE_RT_DIALOG_COMMAND("Show library RT import dialog"),
	IMPORT_LIBRARY_FEATURE_RT_COMMAND("Import library feature's RT from file"),

	//	Compound database
	SHOW_DATABASE_SEARCH_COMMAND("Show database search dialog"),
	SHOW_DATABASE_BATCH_SEARCH_COMMAND("Show \"Batch compound database search\" dialog"),
	BATCH_SEARCH_COMPOUND_DATABASE_COMMAND("Batch search compound database"),
	SEARCH_DATABASE_COMMAND("Search compound database"),
	SEARCH_DATABASE_BY_MZ_ADDUCT_COMMAND("Search compound database using MZ/Adduct"),
	CLEAR_DATABASE_SEARCH_COMMAND("Clear compound database search"),
	SETUP_BATCH_COMPOUND_IMPORT_TO_DATABASE_COMMAND("Setup batch compound import into the database"),
	IMPORT_COMPOUNDS_TO_DATABASE_COMMAND("Import compounds into the database"),
	EXPORT_COMPOUNDS_FROM_DATABASE_COMMAND("Export selected compounds from the database"),
	CREATE_LIBRARY_FROM_DATABASE_DIALOG_COMMAND("New library from selected compounds dialog"),
	CREATE_LIBRARY_FROM_DATABASE_COMMAND("Create new library from selected compounds"),
	ADD_TO_LIBRARY_FROM_DATABASE_DIALOG_COMMAND("Add selected compounds to library dialog"),
	ADD_TO_LIBRARY_FROM_DATABASE_COMMAND("Add selected compounds to active library"),
	ADD_COMPOUND_LIST_TO_LIBRARY_DIALOG_COMMAND("Add compound list to library dialog"),
	ADD_COMPOUND_LIST_TO_LIBRARY_COMMAND("Add compound list to active library"),
	EDIT_DATABASE_ENTRY_DIALOG_COMMAND("Show database entry editor"),
	EDIT_DATABASE_ENTRY_COMMAND("Edit database entry"),
	DELETE_DATABASE_ENTRY_COMMAND("Delete database entry"),
	COPY_COMPOUND_ACCESSION_COMMAND("Copy accession number"),
	COPY_COMPOUND_IDENTITY_COMMAND("Copy compound identity"),
	COPY_COMPOUND_FORMULA_COMMAND("Copy compound formula"),
	COPY_COMPOUND_NAME_COMMAND("Copy compound name"),
	
	// Manual feature ID
	SET_MANUAL_FEATURE_ID_COMMAND("Manually set feature identity"),

	//	Table copy
	COPY_TABLE_DATA_COMMAND("Copy table data as TAB-separated"),
	COPY_TABLE_SELECTED_ROWS_DATA_COMMAND("Copy selected table rows as TAB-separated"),
	
	//	Library MS table
	COPY_SELECTED_MASSES_AS_CSV_COMMAND("Copy selected masses as CSV"),
	COPY_MASS_LIST_AS_CSV_COMMAND("Copy complete mass list (CSV)"),
	COPY_SELECTED_ADUCT_MASS_SUBLIST_2_AS_CSV_COMMAND("Copy top 2 masses from visible adducts (CSV)"),
	COPY_SELECTED_ADUCT_MASS_SUBLIST_3_AS_CSV_COMMAND("Copy top 3 masses from visible adducts (CSV)"),	
	COPY_SPECTRUM_AS_TSV_COMMAND("Copy spectrum as TAB-separated text"),
	COPY_NORMALIZED_SPECTRUM_AS_TSV_COMMAND("Copy normalized spectrum as TAB-separated text"),
	COPY_FEATURE_WITH_METADATA_COMMAND("Copy feature with metadata"),
	COPY_SCAN_WITH_METADATA_COMMAND("Copy scan with metadata"),

	//	MGF panel
	IMPORT_MGF_COMMAND("Import data from MGF"),
	EXPORT_MSMS_COMMAND("Export MS/MS data"),

	//	Automator commands
	RUN_AUTOMATOR_COMMAND("Run batch raw data analysis"),
	RERUN_FAILED_ASSAY_COMMAND("Rerun failed raw data analysis"),
	STOP_AUTOMATOR_COMMAND("Stop batch raw data analysis"),

	//	Assay method manager
	SHOW_ASSAY_METHOD_MANAGER_COMMAND("Show assay method manager"),
	ADD_ASSAY_METHOD_DIALOG_COMMAND("Add assay method dialog"),
	ADD_ASSAY_METHOD_COMMAND("Add assay method"),
	EDIT_ASSAY_METHOD_DIALOG_COMMAND("Edit assay method dialog"),
	EDIT_ASSAY_METHOD_COMMAND("Edit assay method"),
	DELETE_ASSAY_METHOD_COMMAND("Delete assay method"),
	SELECT_ASSAY_COMMAND("Select assay"),

	//	Reference sample editor
	SHOW_REFERENCE_SAMPLE_MANAGER_COMMAND("Show reference sample manager"),
	ADD_REFERENCE_SAMPLE_DIALOG_COMMAND("Add reference sample dialog"),
	ADD_REFERENCE_SAMPLE_COMMAND("Add reference sample"),
	EDIT_REFERENCE_SAMPLE_DIALOG_COMMAND("Edit reference sample dialog"),
	EDIT_REFERENCE_SAMPLE_COMMAND("Edit reference sample"),
	DELETE_REFERENCE_SAMPLE_COMMAND("Delete reference sample"),

	//	ID sample editor
	SHOW_STOCK_SAMPLE_SELECTOR_COMMAND("Show stock sample selector"),
	SELECT_STOCK_SAMPLE_COMMAND("Select stock sample"),
	
	//	LIMS experiment selector
	SHOW_LIMS_EXPERIMENT_SELECTOR_COMMAND("Show LIMS experiment selector"),
	SELECT_LIMS_EXPERIMENT_COMMAND("Select LIMS experiment"),
		
	//	Software list editor
	ADD_SOFTWARE_COMMAND("Add software"),
	EDIT_SOFTWARE_COMMAND("Edit software"),
	SAVE_SOFTWARE_DETAILS_COMMAND("Save software details"),
	DELETE_SOFTWARE_COMMAND("Delete software"),
	SHOW_SOFTWARE_VENDOR_SELECTOR_COMMAND("Show software vendor selector"),
	SELECT_SOFTWARE_VENDOR_COMMAND("Select software vendor"),
	
	//	Vendor/Manufacturer list editor
	ADD_VENDOR_COMMAND("Add vendor"),
	EDIT_VENDOR_COMMAND("Edit vendor"),
	SAVE_VENDOR_DETAILS_COMMAND("Save vendor details"),
	DELETE_VENDOR_COMMAND("Delete vendor"),
	
	//	Acquisition method editor
	ADD_ACQUISITION_METHOD_DIALOG_COMMAND("Add acquisition method dialog"),
	ADD_ACQUISITION_METHOD_COMMAND("Add acquisition method"),
	EDIT_ACQUISITION_METHOD_DIALOG_COMMAND("Edit acquisition method dialog"),
	EDIT_ACQUISITION_METHOD_COMMAND("Edit acquisition method"),
	DELETE_ACQUISITION_METHOD_COMMAND("Delete acquisition method"),
	DOWNLOAD_ACQUISITION_METHOD_COMMAND("Download acquisition method"),
	LINK_ACQUISITION_METHOD_TO_EXPERIMENT_COMMAND("Add data acquisition method to experiment"),
	
	//	Mobile phase editor
	ADD_MOBILE_PHASE_DIALOG_COMMAND("Add mobile phase dialog"),
	ADD_MOBILE_PHASE_COMMAND("Add mobile phase"),
	EDIT_MOBILE_PHASE_DIALOG_COMMAND("Edit mobile phase dialog"),
	EDIT_MOBILE_PHASE_COMMAND("Edit mobile phase"),
	DELETE_MOBILE_PHASE_COMMAND("Delete mobile phase"),
	
	//	Gradient step editor
	ADD_GRADIENT_STEP_COMMAND("Add gradient step"),
	DELETE_GRADIENT_STEP_COMMAND("Delete gradient step"),

	//	Instrument editor
	ADD_INSTRUMENT_DIALOG_COMMAND("Add instrument dialog"),
	ADD_INSTRUMENT_COMMAND("Add instrument"),
	EDIT_INSTRUMENT_DIALOG_COMMAND("Edit instrument dialog"),
	EDIT_INSTRUMENT_COMMAND("Edit instrument"),
	DELETE_INSTRUMENT_COMMAND("Delete instrument"),
	SELECT_INSTRUMENT_DIALOG_COMMAND("Show instrument selector"),
	SELECT_INSTRUMENT_COMMAND("Select instrument"),

	//	Data extraction method editor
	ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND("Add data extraction method dialog"),
	ADD_DATA_EXTRACTION_METHOD_FROM_DATABASE_DIALOG_COMMAND("Add data extraction method from database dialog"),
	ADD_DATA_EXTRACTION_METHOD_FROM_DATABASE_COMMAND("Add data extraction method from database"),
	ADD_DATA_EXTRACTION_METHOD_COMMAND("Add data extraction method"),
	EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND("Edit data extraction method dialog"),
	EDIT_DATA_EXTRACTION_METHOD_COMMAND("Edit data extraction method"),
	DELETE_DATA_EXTRACTION_METHOD_COMMAND("Delete data extraction method"),
	DOWNLOAD_DATA_EXTRACTION_METHOD_COMMAND("Download data extraction method"),
	LINK_DATA_EXTRACTION_METHOD_TO_ACQUISITION_COMMAND("Add data extraction method to acquisition method"),
	SHOW_SOFTWARE_SELECTOR_COMMAND("Show software selector"),
	SELECT_SOFTWARE_COMMAND("Select software"),

	//	Chromatographic column manager
	ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND("Add chromatographic column dialog"),
	ADD_CHROMATOGRAPHIC_COLUMN_COMMAND("Add chromatographic column"),
	EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND("Edit chromatographic column dialog"),
	EDIT_CHROMATOGRAPHIC_COLUMN_COMMAND("Edit chromatographic column"),
	DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND("Delete chromatographic column"),

	//	Sample prep manager
	ADD_SAMPLE_PREP_DIALOG_COMMAND("Add sample preparation dialog"),
	ADD_SAMPLE_PREP_COMMAND("Add sample preparation"),
	EDIT_SAMPLE_PREP_DIALOG_COMMAND("Edit sample preparation dialog"),
	EDIT_SAMPLE_PREP_COMMAND("Edit sample preparation"),
	DELETE_SAMPLE_PREP_COMMAND("Delete sample preparation"),
	SELECT_SAMPLE_PREP_FROM_DATABASE_COMMAND("Select existing sample prep"),
	LOAD_SAMPLE_PREP_FROM_DATABASE_COMMAND("Load selected sample prep data"),
	CLEAR_SAMPLE_PREP_DEFINITION_COMMAND("Clear sample prep definition"),
	EDIT_SAMPLE_PREP_NAME_COMMAND("Edit sample preparation name"),
	SAVE_SAMPLE_PREP_NAME_COMMAND("Save sample preparation name"),
	
	LOAD_SAMPLES_WITH_PREP_FROM_DATABASE_COMMAND("Load samples with sample prep data"),
	
		
	//	Add processing results
	ADD_DATA_PROCESSING_RESULTS_DIALOG_COMMAND("Show \"Add data processing results\" dialog"),
	ADD_DATA_PROCESSING_RESULTS_COMMAND("Add data processing results"),

	//	SOP protocol manager
	ADD_SOP_PROTOCOL_DIALOG_COMMAND("Add SOP protocol dialog"),
	ADD_SOP_PROTOCOL_COMMAND("Add SOP protocol"),
	EDIT_SOP_PROTOCOL_DIALOG_COMMAND("Edit SOP protocol dialog"),
	EDIT_SOP_PROTOCOL_COMMAND("Edit SOP protocol"),
	DELETE_SOP_PROTOCOL_COMMAND("Delete SOP protocol"),
	DOWNLOAD_SOP_PROTOCOL_COMMAND("Download SOP protocol"),
	
	//	Date selector
	SHOW_SELECT_DATE_DIALOG_COMMAND("Select date dialog"),
	SELECT_DATE_COMMAND("Select date"),

	//	ID status manager
	SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND("Show ID level manager"),
	ADD_ID_LEVEL_DIALOG_COMMAND("Add ID level dialog"),
	ADD_ID_LEVEL_COMMAND("Add ID level"),
	EDIT_ID_LEVEL_DIALOG_COMMAND("Edit ID level dialog"),
	EDIT_ID_LEVEL_COMMAND("Edit ID level"),
	DELETE_ID_LEVEL_COMMAND("Delete ID level"),
	
	//	Reference MSMS library export/decoy
	EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND("Export selected reference MSMS library"),
	CREATE_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND("Create decoy library from selected"),
	IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND("Import decoy library"),
	
	//	Color picker
	SHOW_COLOR_PICKER_COMMAND("Show color picker"),
	SELECT_COLOR_COMMAND("Select color"),
	
	//	ID follow-up step manager
	SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND("Show ID follow-up step manager"),
	ADD_ID_FOLLOWUP_STEP_DIALOG_COMMAND("Add ID follow-up step dialog"),
	ADD_ID_FOLLOWUP_STEP_COMMAND("Add ID follow-up step"),
	EDIT_ID_FOLLOWUP_STEP_DIALOG_COMMAND("Edit ID follow-up step dialog"),
	EDIT_ID_FOLLOWUP_STEP_COMMAND("Edit ID follow-up step"),
	DELETE_ID_FOLLOWUP_STEP_COMMAND("Delete ID follow-up step"),
	ASSIGN_ID_FOLLOWUP_STEPS_TO_FEATURE_DIALOG_COMMAND("Assign follow-up steps to feature"),
	SAVE_ID_FOLLOWUP_STEP_ASSIGNMENT_COMMAND("Save ID follow-up step assignment"),
	
	//	Standard feature annotation manager
	SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND("Show standard feature annotation manager"),
	ADD_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND("Add standard feature annotation dialog"),
	ADD_STANDARD_FEATURE_ANNOTATION_COMMAND("Add standard feature annotation"),
	EDIT_STANDARD_FEATURE_ANNOTATION_DIALOG_COMMAND("Edit standard feature annotation dialog"),
	EDIT_STANDARD_FEATURE_ANNOTATION_COMMAND("Edit standard feature annotation"),
	DELETE_STANDARD_FEATURE_ANNOTATION_COMMAND("Delete standard feature annotation"),
	ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND("Assign standard feature annotations to feature"),
	SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND("Save standard feature annotation assignment"),
	
	//	MetIDTracker data explorer
	SHOW_IDTRACKER_DATA_EXPLORER_PLOT("Show MetIDtracker data explorer"),
	
	//	Linking raw data 
	INDEX_RAW_DATA_REPOSITORY_COMMAND("Index raw data repository"),
	LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND("Load raw data for current MSMS feature set"),
	
	//	MSCONVERT
	SETUP_RAW_DATA_CONVERSION_COMMAND("Set up raw data conversion"),
	CONVERT_RAW_DATA_COMMAND("Convert raw data"),
	SELECT_OUPUT_DIRECTORY_COMMAND("Select output directory"),
	
	//	XIC
	XIC_FOR_FEATURE_DIALOG_COMMAND("Extract XIC setup"),
	XIC_FOR_FEATURE_COMMAND("Extract XIC for feature"),
	
	//	User selector
	SELECT_USER_DIALOG_COMMAND("Show uselr selector dialog"),
	SELECT_USER_COMMAND("Select user"),

	//	Compound identification review commands
	ACCEPT_COMPOUND_IDENTIFICATION("Accept compound identification results"),
	DISCARD_COMPOUND_IDENTIFICATION("Discard compound identification results"),

	EDIT_PREFERENCES_COMMAND("Edit preferences"),
	SHOW_HELP_COMMAND("Help"),
	SHOW_WEB_HELP_COMMAND("Online help"),
	ABOUT_BOX_COMMAND("About ..."),
	EXIT_COMMAND("Exit"),

	//	Feature tree commands
	DELETE_FEATURE_COMMAND("Delete MS feature"),
	FIND_IN_CLUSTERS_COMMAND("Find feature in clusters"),
	SHOW_STATS_COMMAND("Show feature statistics"),

	//	Automator
	BROWSE_FOR_POSITIVE_MODE_METHOD("Browse for POS mode method"),
	BROWSE_FOR_NEGATIVE_MODE_METHOD("Browse for NEG mode method"),
	SELECT_RECENT_POSITIVE_MODE_METHOD("Select recent POS mode method"),
	SELECT_RECENT_NEGATIVE_MODE_METHOD("Select recent NEG mode method"),
	SET_POSITIVE_MODE_METHOD("Set POS mode method"),
	SET_NEGATIVE_MODE_METHOD("Set NEG mode method"),
	BROWSE_FOR_POSITIVE_MODE_RAW_DATA_FOLDER("Browse for POS mode raw data folder"),
	BROWSE_FOR_NEGATIVE_MODE_RAW_DATA_FOLDER("Browse for NEG mode raw data folder"),
	SELECT_RECENT_POSITIVE_MODE_RAW_DATA_FOLDER("Select recent POS mode raw data folder"),
	SELECT_RECENT_NEGATIVE_MODE_RAW_DATA_FOLDER("Select recent NEG mode raw data folder"),
	SET_POSITIVE_MODE_RAW_DATA_FOLDER("Set POS mode raw data folder"),
	SET_NEGATIVE_MODE_RAW_DATA_FOLDER("Set NEG mode raw data folder"),
	
	//	LIMS
	SAVE_EXPERIMENT_SUMMARY_COMMAND("Save LIMS experiment summary"),

	//	Binner data import
	BROWSE_FOR_BINNER_REPORT_COMMAND("Browse for Binner report"),
	BROWSE_FOR_BINNER_POSTPROCESSOR_REPORT_COMMAND("Browse for Binner Postprocessor report"),

	//
	ASSIGN_SAMPLE_TO_DATA_FILES_COMMAND("Assign sample to data files"),
	ASSIGN_DA_METHOD_TO_DATA_FILES_DIALOG_COMMAND("Select data analysis method for selected files"),
	ASSIGN_DA_METHOD_TO_DATA_FILES_COMMAND("Assign data analysis method to data files"),

	SHOW_PUBCHEM_DATA_LOADER("Show PubChem data loader"),
	FETCH_PUBCHEM_DATA("Fetch PubChem data"),
	SHOW_CUSTOM_COMPOUND_LOADER("Show custom compound loader"),
	VALIDATE_CUSTOM_COMPOUND_DATA("Validate custom compound data"),
	SAVE_CUSTOM_COMPOUND_DATA("Save custom compound data"),
	
	//	Compound curator
	SHOW_COMPOUND_DATABASE_CURATOR("Show compound database curator"),
	SHOW_MS_READY_COMPOUND_CURATOR("Show MS-ready compound curator"),
	FETCH_COMPOUND_DATA_FOR_CURATION("Fetch compound data for curation"),
	VALIDATE_MS_READY_STRUCTURE("Validate MS-ready structure"),
	SAVE_MS_READY_STRUCTURE("Save MS-ready structure"),

	//	Synonym editor
	ADD_SYNONYM_DIALOG_COMMAND("Add synonym dialog"),
	ADD_SYNONYM_COMMAND("Add synonym"),
	EDIT_SYNONYM_DIALOG_COMMAND("Edit synonym dialog"),
	EDIT_SYNONYM_COMMAND("Edit synonym"),
	DELETE_SYNONYM_COMMAND("Delete synonym"),

	//	DA method selector / reference MS1 import dialog
	SELECT_REF_MS1_DATA_FILE_COMMAND("Select reference MS1 input file"),
	SELECT_DA_METHOD_DIALOG_COMMAND("Show DA method selector"),
	SELECT_DA_METHOD_COMMAND("Select data analysis method"),

	//	Raw data upload preparation
	BROWSE_FOR_RAW_DATA_DIR("Select raw data directory"),
	BROWSE_FOR_ZIP_DIR("Select ZIP destination directory"),
	CLEAN_AND_ZIP_COMMAND("Prepare raw data for upload"),

	// ID workbench panel commands
	SHOW_IDTRACKER_MANAGER_COMMAND("Show MetIDTracker manager"),
	LOAD_FEATURES_FOR_ID_COMMAND("Load features for identification"),
	ID_SETUP_DIALOG_COMMAND("Identification setup"),
	IDDA_SETUP_DIALOG_COMMAND("IDDA experiment import setup"),
	IDDA_IMPORT_COMMAND("Import IDDA experiment data"),
	IDTRACKER_FEATURE_SEARCH_COMMAND("Search MetIDTracker features"),
	IDTRACKER_RESET_FORM_COMMAND("Reset MetIDTracker search form"),
	IDTRACKER_REFRESH_FORM_OPTIONS_COMMAND("Refresh MetIDTracker search form options"),
	IDTRACKER_EXPERIMENT_FEATURE_SEARCH_COMMAND("Search MetIDTracker features for experiment"),
	IDTRACKER_EXPERIMENT_RESET_FORM_COMMAND("Reset MetIDTracker experiment search form"),
	IDTRACKER_EXPERIMENT_REFRESH_FORM_OPTIONS_COMMAND("Refresh MetIDTracker experiment search form options"),
	
	RELOAD_COMPLETE_DATA_SET_COMMAND("Reload complete data set"),
	RELOAD_ACTIVE_MSMS_FEATURES("Reload active MSMS feature collection"),
	RELOAD_ACTIVE_MSMS_CLUSTER_SET_FEATURES("Reload features for active MSMS cluster set"),
	RELOAD_ACTIVE_MS_ONE_FEATURES("Reload complete active MS1 feature set"),
	CLEAR_IDTRACKER_WORKBENCH_PANEL("Clear workbench"),
	
	SETUP_MAJOR_CLUSTER_FEATURE_EXTRACTION_COMMAND("Set up major cluster feature extraction"),
	EXTRACT_MAJOR_CLUSTER_FEATURES_COMMAND("Extract major cluster features"),
	
	
	//	NIST-MS / PEP-search / Sirius commands
	NIST_MS_PEPSEARCH_SETUP_COMMAND("Set up NIST MSMS pepsearch"),
	NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND("Set up offline NIST MSMS pepsearch"),
	NIST_MS_PEPSEARCH_RUN_COMMAND("Search NIST MSMS libraries with pepsearch"),
	NIST_MS_OFFLINE_PEPSEARCH_RUN_COMMAND("Search NIST MSMS libraries with pepsearch offline"),
	ADD_PEPSEARCH_LIBRARY_COMMAND("Add NIST-format library to list"),
	REMOVE_PEPSEARCH_LIBRARY_COMMAND("Remove NIST-format library from list"),
	SELECT_PEPSEARCH_INPUT_FILE_COMMAND("Select input file for NIST pepsearch"),
	EXPORT_FEATURES_TO_MSP_COMMAND("Export selected features to NIST MSP file"),
	SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND("Sirius MS export setup"),
	EXPORT_FEATURES_TO_SIRIUS_MS_COMMAND("Export selected features to Sirius MS file"),
	GENERATE_PEPSEARCH_CLI_COMMAND("Generate PepSearch CLI command"),
	COPY_PEPSEARCH_CLI_COMMAND("Copy PepSearch CLI command to clipboard"),
	NIST_MS_SEARCH_SETUP_COMMAND("Set up NIST MS search"),
	NIST_MS_SEARCH_RUN_COMMAND("Search NIST MSMS libraries"),
	
	//	PepSearch data verifier
	SELECT_PEPSEARCH_OUTPUT_FILE_COMMAND("Select NIST pepsearch output file"),
	VALIDATE_PEPSEARCH_RESULTS_COMMAND("Validate NIST pepsearch results"),
	VALIDATE_PEPSEARCH_RESULTS_AND_WRITE_FILE_WITH_SPECTRA_COMMAND("Validate results and write new file with spectra"),
	UPLOAD_PEPSEARCH_RESULTS_COMMAND("Upload NIST pepsearch results"),
	
	//	FDR estimation
	SHOW_PEPSEARCH_PARAMETER_SET_SELECTOR("Show PepSearch parameter set selector"),
	SELECT_PEPSEARCH_PARAMETER_SET("Select PepSearch parameter set"),
	SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES("Set up FDR estimation for MSMS library identifications"),
	CALCULATE_FDR_FOR_LIBRARY_MATCHES("Calculate FDR for MSMS library identifications"),
	
	//	Recalculate MSMS top hit
	SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT("Set up default MSMS library match re-assignment"),
	REASSIGN_DEFAULT_MSMS_LIBRARY_MATCHES("Reassign default MSMS library matches"),
	
	//	Spectrum entropy
	SETUP_SPECTRUM_ENTROPY_SCORING("Set parameters for MSMS entropy-based scoring"),
	RECALCULATE_SPECTRUM_ENTROPY_SCORES("Recalculate entropy-based MSMS scores"),
	
	//	MetIDTracker export
	SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND("MetIDTracker data export setup"),
	EXPORT_IDTRACKER_DATA_COMMAND("Export MetIDTracker data"),
	RESET_IDTRACKER_DATA_EXPORT_FIELDS_COMMAND("Reset MetIDTracker data export fields"),
	SHOW_MSMS_CLUSTER_DATA_EXPORT_DIALOG_COMMAND("MSMS clusters data export setup"),
	EXPORT_MSMS_CLUSTER_DATA_COMMAND("Export MSMS clusters data"),
	EXPORT_MSMS_CLUSTER_DATA_FOR_SIRIUS_COMMAND("Export MSMS clusters data for SIRIUS"),
	
	//	Tracker search dialog
	SHOW_IDTRACKER_SEARCH_DIALOG_COMMAND("MetIDTracker database search setup"),
	SHOW_IDTRACKER_SAVED_QUERIES_LIST_COMMAND("Show MetIDTracker saved queries list"),
	LOAD_IDTRACKER_SAVED_QUERY_COMMAND("Load saved MetIDTracker search query"),
	SHOW_IDTRACKER_SAVE_QUERY_DIALOG_COMMAND("Save current MetIDTracker search query"),
	IDTRACKER_SAVE_QUERY_COMMAND("Save MetIDTracker search query to database"),
	IDTRACKER_DELETE_QUERY_COMMAND("Delete selected MetIDTracker search query"),
	
	//	Tracker by experiment search dialog
	SHOW_IDTRACKER_BY_EXPERIMENT_MZ_RT_SEARCH_DIALOG_COMMAND("MetIDTracker database search by MZ/RT setup"),
	SEARCH_IDTRACKER_BY_EXPERIMENT_MZ_RT_COMMAND("Get selected MZ/RT data from MetIDTracker"),
	SHOW_ACTIVE_DATA_SET_MZ_RT_SEARCH_DIALOG_COMMAND("Active data set search by MZ/RT setup"),
	SEARCH_ACTIVE_DATA_SET_BY_MZ_RT_COMMAND("Get selected MZ/RT data from active data set"),
	
	//	Summary
	SHOW_ACTIVE_DATA_SET_SUMMARY_COMMAND("Show active data set summary"),
	
	//	MSMS search dialog
	SHOW_SAVED_MSMS_QUERY_LIST_COMMAND("Show MSMS query list"),
	LOAD_SAVED_MSMS_QUERY_COMMAND("Load MSMS search query from database"),
	SHOW_SAVE_MSMS_QUERY_DIALOG_COMMAND("Save current MSMS search query"),
	SAVE_MSMS_QUERY_COMMAND("Save MSMS search query to database"),
	DELETE_MSMS_QUERY_COMMAND("Delete selected MSMS search query"),

	//	Scan CEFs for MSMS
	CEF_MSMS_SCAN_SETUP_COMMAND("Set up prescan of CEF files with MSMS search results"),
	CEF_MSMS_SCAN_RUN_COMMAND("Scan CEF files with MSMS search results for compound ID data"),

	//	LabNotebook
	SHOW_MS_INSTRUMENT_SELECTOR_COMMAND("Show MS instrument selector"),
	SET_MS_INSTRUMENT_COMMAND("Select MS instrument"),
	CREATE_NEW_LAB_NOTE_COMMAND("Create new LabNotebook entry"),
	EDIT_LAB_NOTE_COMMAND("Edit LabNotebook entry"),
	DELETE_LAB_NOTE_COMMAND("Delete LabNotebook entry"),
	SAVE_LAB_NOTE_COMMAND("Save LabNotebook entry"),

	//	RTF editor commands
	RTF_COPY_COMMAND("Copy to clipboard"),
	RTF_CUT_COMMAND("Cut selected text"),
	RTF_PASTE_COMMAND("Paste from clipboard"),
	ALIGN_LEFT_COMMAND("Align left"),
	ALIGN_RIGHT_COMMAND("Align right"),
	ALIGN_CENTER_COMMAND("Center"),
	JUSTIFY_COMMAND("Justify"),
	INCREASE_INDENT_COMMAND("Increase indent"),
	DECREASE_INDENT_COMMAND("Decrease indent"),
	BOLD_FONT_COMMAND("Bold"),
	ITALIC_FONT_COMMAND("Italic"),
	STRIKETHROUGH_FONT_COMMAND("Strikethrough"),
	UNDERLINE_FONT_COMMAND("Underline"),
	COLORPICKER_COMMAND("ColorPicker"),
	FONT_SELECTOR_COMMAND("Show font selector"),
	PARAGRAPH_EDITOR_COMMAND("Show paragraph style editor"),

	//	Taxonomy select dialog
	SHOW_SELECT_SPECIES_DIALOG_COMMAND("Show \"Select species\" dialog"),
	SELECT_SPECIES_COMMAND("Select species"),
	SEARCH_SPECIES_COMMAND("Search taxonomy database"),

	//	Sample type select dialog
	SHOW_SELECT_SAMPLE_TYPE_DIALOG_COMMAND("Show \"Select sample type\" dialog"),
	SELECT_SAMPLE_TYPE_COMMAND("Select sample type"),
	SEARCH_SAMPLE_TYPES_COMMAND("Search sample types database"),
	
	//	MetIDTracker data upload wizard
	COMPLETE_EXPERIMENT_DEFINITION_COMMAND("Complete experiment definition"),
	COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND("Complete sample list definition"),
	COMPLETE_SAMPLE_PREP_DEFINITION_COMMAND("Complete sample preparation definition"),
	COMPLETE_ANALYSIS_METHODS_DEFINITION_COMMAND("Complete analysis methods definition"),
	COMPLETE_ANALYSIS_WORKLIST_VERIFICATION_COMMAND("Complete analysis worklist verification"),
	COMPLETE_DATA_VERIFICATION_COMMAND("Complete data verification"),
	FINALIZE_DATA_UPLOAD_COMMAND("Finalize data for upload"),
	UPLOAD_DATA_TO_IDTRACKER_COMMAND("Upload data to IDTracker"),
	
    //	Raw data panel
    GROUP_TREE_BY_FILE("Group tree by file name"),
    GROUP_TREE_BY_TYPE("Group tree by data type"),
    GROUP_TREE_BY_COMPOUND("Group tree by compound"),
    EXPAND_TREE("Expand tree"),
    COLLAPSE_TREE("Collapse tree"),
    CREATE_XIC("Extract chromatogram"),
    CLEAR_XIC("Clear chromatogram panel"),
    AVERAGE_MS("Extract average MS"),
    CLEAR_AVERAGE_MS("Clear average MS panel"),
    OPEN_RAW_DATA_FILE_COMMAND("Open raw data file(s)"),
    CLOSE_RAW_DATA_FILE_COMMAND("Close raw data file(s)"),
    FINALIZE_CLOSE_RAW_DATA_FILE_COMMAND("Complete raw data file(s) closing"),
    
    IMPORT_MS1_DATA_FROM_CEF_COMMAND("Import MS1 data from CEF file"),
       
	//	Data extract
	EXTRACT_CHROMATOGRAM("Extract chromatogram"),
	EXTRACT_SPECTRUM("Extract spectrum"),
	AUTORANGE_RT("Autorange RT"),
	EXTRACT_AVERAGE_MS("Extract average MS"),
	FIND_MSMS_BY_FRAGMENTS("Find MSMS by fragments"),
	FIND_MSMS_BY_PARENT_ION("Find MSMS by parent ion"),	
	GET_RT_RANGE_FROM_CHROMATOGRAM_SELECTION("Get RT range from chromatogram selection"),
	FIND_MZ_IN_MS_ONE("Find MS1 scans containing M/Z"),	
	ADD_MASSES_TO_EXTRACT_XIC_COMMAND("Add masses to extract"),
	
	//	Raw data tree
	CHOOSE_FILE_COLOR("Select data file color"), 
	SET_FILE_COLOR("Set file color"),
	REMOVE_DATA_FILE("Remove data file"),
	REMOVE_CHROMATOGRAM("Remove chromatogram"),
	REMOVE_SPECTRUM("Remove spectrum"),
	
	//	Identification table 
	SHOW_ONLY_BEST_LIB_HIT_FOR_COMPOUND("Show only best library hit per compound"),
	SHOW_ALL_LIB_HITS_FOR_COMPOUND("Show all library hits for each compound"),
	
	//	Mass-spectrum plot
	ZOOM_TO_MSMS_PRECURSOR_COMMAND("Zoom to MSMS precursor"),
	SHOW_FULL_MS_RANGE_COMMAND("Show full spectrum range"),
	
	//	Mass-defect plot
	RECALCULATE_MASS_DEFECTS_FOR_RT_RANGE("Recalculate mass defects for RT range"),
	
	//	MS-feature plot /MS-feature subsets for projects
	CREATE_NEW_MS_FEATURE_SUBSET_FROM_SELECTED("Create new subset from selected features"),
	ADD_SELECCTED_TO_EXISTING_MS_FEATURE_SUBSET("Add selected features to existing subset"),
	FILTER_SELECTED_MS_FEATURES_IN_TABLE("Show only selected in feature table"),
	
	
	//	MetIDTracker MSMS-feature plot
	REFRESH_MSMS_FEATURE_PLOT("Refresh MSMS feature plot"),
	CREATE_NEW_MSMS_FEATURE_COLLECTION_FROM_SELECTED("Create new collection from selected features"),
	ADD_SELECTED_TO_EXISTING_MSMS_FEATURE_COLLECTION("Add selected features to existing collection"),
	FILTER_SELECTED_MSMS_FEATURES_IN_TABLE("Filter selected features in MSMS feature table"),
	REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION("Remove selected features from active feature collection"),
	
	//	Compound database curator
	SHOW_SIMPLE_REDUNDANT_COMPOUND_DATA_PULL_DIALOG("Simple redundant compouds pull dialog"),
	PULL_REDUNDANT_COMPOUNDS_FROM_DATABASE("Pull redundant compounds from the database"),
	
	//	Feature collections
	SHOW_FEATURE_COLLECTION_MANAGER_DIALOG_COMMAND("Show feature collection/MSMS cluster data set manager"),
	ADD_FEATURE_COLLECTION_DIALOG_COMMAND("\"Create new feature collection\" dialog"),
	ADD_FEATURE_COLLECTION_COMMAND("Create new empty feature collection"),
	ADD_FEATURE_COLLECTION_WITH_FEATURES_COMMAND("Create new feature collection with selected features"),
	ADD_FEATURES_TO_SELECTED_COLLECTION_COMMAND("Add features to selected collection"),
	EDIT_FEATURE_COLLECTION_DIALOG_COMMAND("\"Edit selected feature collection\" dialog"),
	EDIT_FEATURE_COLLECTION_COMMAND("Edit selected feature collection"),
	DELETE_FEATURE_COLLECTION_COMMAND("Delete selected feature collection"),
	LOAD_FEATURE_COLLECTION_COMMAND("Load selected feature collection for analysis"),
	
	//	Feature lookup data sets
	ADD_FEATURE_LOOKUP_DATA_SET_DIALOG_COMMAND("\"Create new feature lookup data set\" dialog"),
	ADD_FEATURE_LOOKUP_DATA_SET_COMMAND("Create new feature lookup data set"),
	EDIT_FEATURE_LOOKUP_DATA_SET_DIALOG_COMMAND("\"Edit selected feature lookup data set\" dialog"),
	EDIT_FEATURE_LOOKUP_DATA_SET_COMMAND("Edit selected feature lookup data set"),
	DELETE_FEATURE_LOOKUP_DATA_SET_COMMAND("Delete selected feature lookup data set"),
	
	//	MSMS cluster data sets
	SHOW_MSMS_CLUSTER_DATASET_MANAGER_DIALOG_COMMAND("Show MSMS cluster data set manager"),
	ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND("\"Save new MSMS cluster data set to database\" dialog"),
	ADD_MSMS_CLUSTER_DATASET_COMMAND("Save new MSMS cluster data set to database"),
	//	ADD_MSMS_CLUSTER_DATASET_WITH_CLUSTERS_COMMAND("Create new MSMS cluster data set with selected clusters"),
	ADD_MSMS_CLUSTERS_TO_SELECTED_DATASET_COMMAND("Add clusters to selected MSMS cluster data set"),
	EDIT_MSMS_CLUSTER_DATASET_DIALOG_COMMAND("\"Edit selected MSMS cluster data set\" dialog"),
	EDIT_MSMS_CLUSTER_DATASET_COMMAND("Edit selected MSMS cluster data set"),
	DELETE_MSMS_CLUSTER_DATASET_COMMAND("Delete selected MSMS cluster data set"),
	LOAD_MSMS_CLUSTER_DATASET_COMMAND("Load selected MSMS cluster data set for analysis"),
	REFRESH_FEATURE_AND_CLUSTER_COLLECTIONS_COMMAND("Refresh feature and MSMS cluster collection data"),
	SHOW_LOOKUP_FEATURE_LIST_FOR_CLUSTER_DATA_SET_COMMAND("Show lookup feature list for MSMS cluster data set"),
	
	//	Chromatogram smoothing 
	SMOOTH_CHROMATOGRAM_COMMAND("Smooth chromatogram"),
	SHOW_RAW_CHROMATOGRAM_COMMAND("Show raw chromatogram"),
	SHOW_SMOOTHING_PREFERENCES_COMMAND("Show smoothing preferences"),
	SAVE_SMOOTHING_PREFERENCES_COMMAND("Save smoothing preferences"),
	
	//	Feature filter
	IMPORT_LOOKUP_FEATURE_LIST_FROM_FILE_COMMAND("Import lookup feature list from file"),
	SELECT_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND("Select lookup features list from database"),
	LOAD_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND("Load lookup features list from database"),
	
	//	Manual peak integration
	SHOW_MANUAL_INTEGRATOR_SETTINGS("Show manual integrator settings"),
	SAVE_MANUAL_INTEGRATOR_SETTINGS("Save manual integrator settings"),
	INTEGRATE_HIGHLIGHTED_RANGES("Integrate peaks over highlighted ranges"),
	ACCEPT_INTEGRATION_RESULTS("Accept integration results"),
	CLEAR_HIGHLIGHTED_RANGES("Clear highlighted integration ranges"),
	RELOAD_ORIGINAL_CHROMATOGRAMS("Reload original chromatograms"),
	
	COPY_VISIBLE_TABLE_ROWS_COMMAND("Copy visible rows to clipboard"),
	COPY_SELECTED_TABLE_ROWS_COMMAND("Copy selected rows to clipboard"),
	COPY_SELECTED_VALUE_COMMAND("Copy selected value"),
	
	SELECT_COMPOUND_COLLECTION_COMMAND("Select compound collection"),
	LOAD_COMPOUND_COLLECTION_COMMAND("Load compound collection"),
	LOAD_COMPOUND_MULTIPLEXES_COMMAND("Load compound multiplex data"),
	
	SETUP_MULTIPLEXES_EXPORT_COMMAND("Set up compound multiplex data export"),
	EXPORT_SELECTED_MULTIPLEXES_COMMAND("Export selected compound multiplex data"),
	
	EXPORT_SELECTED_MULTIPLEX_FOR_FBF_COMMAND("Export selected compound multiplex for \"Find by Formula\""),
	EXPORT_SELECTED_MULTIPLEX_FOR_PCDL_IMPORT_COMMAND("Export selected compound multiplex for import into PCDL library"),
	
	SETUP_COMPOUND_PROPERTIES_SEARCH_COMMAND("Show \"Search compounds by properties\" dialog"),
	SEARCH_COMPOUND_PROPERTIES_COMMAND("Search compounds by properties"),
	
	EDIT_COMPOUND_MS_READY_STRUCTURE_COMMAND("Edit compound MS-ready structure"),
	SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND("Save compound MS-ready structure"),
	
	EDIT_SELECTED_FIELD_COMMAND("Edit selected field"),
	SAVE_CHANGES_COMMAND("Save changes"),
	;

	private final String name;

	MainActionCommands(String command) {
		this.name = command;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public static MainActionCommands getCommandByName(String commandName) {
		
		for(MainActionCommands command : MainActionCommands.values()) {
			
			if(command.name().equals(commandName))
				return command;
		}	
		return null;
	}
}















