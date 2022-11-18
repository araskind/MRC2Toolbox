/* This file is part of JnaFileChooser.
 *
 * JnaFileChooser is free software: you can redistribute it and/or modify it
 * under the terms of the new BSD license.
 *
 * JnaFileChooser is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 */
package edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.jna.Platform;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;

/**
 * JnaFileChooser is a wrapper around the native Windows file chooser
 * and folder browser that falls back to the Swing JFileChooser on platforms
 * other than Windows or if the user chooses a combination of features
 * that are not supported by the native dialogs (for example multiple
 * selection of directories).
 *
 * Example:
 * JnaFileChooser fc = new JnaFileChooser();
 * fc.setFilter("All Files", "*");
 * fc.setFilter("Pictures", "jpg", "jpeg", "gif", "png", "bmp");
 * fc.setMultiSelectionEnabled(true);
 * fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
 * if (fc.showOpenDialog(parent)) {
 *     Files[] selected = fc.getSelectedFiles();
 *     // do something with selected
 * }
 *
 * @see JFileChooser, WindowsFileChooser, WindowsFileBrowser
 */
public class JnaFileChooser
{
	private static enum Action { Open, Save }
	private static final Icon stopIcon = GuiUtils.getIcon("stopSign", 64);

	/**
	 * the availabe selection modes of the dialog
	 */
	public static enum Mode {
		Files(JFileChooser.FILES_ONLY),
		Directories(JFileChooser.DIRECTORIES_ONLY),
		FilesAndDirectories(JFileChooser.FILES_AND_DIRECTORIES);
		private int jFileChooserValue;
		private Mode(int jfcv) {
			this.jFileChooserValue = jfcv;
		}
		public int getJFileChooserValue() {
			return jFileChooserValue;
		}
	}

	protected File[] selectedFiles;
	protected File currentDirectory;
	protected ArrayList<String[]> filters;
	protected String[] selectedFilter;
	protected boolean multiSelectionEnabled;
	protected Mode mode;
	protected boolean allowOverwrite = false;

	protected String defaultFile;
    protected String dialogTitle;
    protected String openButtonText;
    protected String saveButtonText;

	/**
	 * creates a new file chooser with multiselection disabled and mode set
	 * to allow file selection only.
	 */
	public JnaFileChooser() {
		filters = new ArrayList<String[]>();
		multiSelectionEnabled = false;
		mode = Mode.Files;
		selectedFiles = new File[] { null };

		defaultFile = "";
        dialogTitle = "";
        openButtonText = "";
        saveButtonText = "";
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectory the initial directory
	 */
	public JnaFileChooser(File currentDirectory) {
		this();
        if (currentDirectory != null) {
			this.currentDirectory = currentDirectory.isDirectory() ?
				currentDirectory : currentDirectory.getParentFile();
		}
	}

	/**
	 * creates a new file chooser with the specified initial directory
	 *
	 * @param currentDirectory the initial directory
	 */
	public JnaFileChooser(String currentDirectoryPath) {
		this(currentDirectoryPath != null ?
			new File(currentDirectoryPath) : null);
	}

	/**
	 * shows a dialog for opening files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showOpenDialog(Window parent) {
		return showDialog(parent, Action.Open);
	}

	/**
	 * shows a dialog for saving files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showSaveDialog(Window parent) {
		return showDialog(parent, Action.Save);
	}
	
	/**
	 * shows a dialog for opening files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showOpenDialog(Component parent) {
		return showDialog(parent, Action.Open);
	}

	/**
	 * shows a dialog for saving files
	 *
	 * @param parent the parent window
	 *
	 * @return true if the user clicked OK
	 */
	public boolean showSaveDialog(Component parent) {
		return showDialog(parent, Action.Save);
	}

	private boolean showDialog(Object parent, Action action) {
		
		if(!Window.class.isAssignableFrom(parent.getClass()) 
				&& !Component.class.isAssignableFrom(parent.getClass()))
			return false;
		
		// Native windows filechooser doesn't support mixed selection mode
		if (Platform.isWindows() && mode == Mode.Files) {
			
//			// windows filechooser can only multiselect files, 
			//	windows folder browser doesn't support remembering last location
//			if (mode == Mode.Directories && !multiSelectionEnabled)
//				return showWindowsFolderBrowser(parent);
//			
//			if (mode == Mode.Files)
			
			return showWindowsFileChooser(parent, action);	
		}
		// fallback to Swing
		return showSwingFileChooser(parent, action);
	}
	
	private boolean showSwingFileChooser(Object parent, Action action) {
			
		final ImprovedFileChooser fc = new ImprovedFileChooser(currentDirectory);
		fc.setPreferredSize(new Dimension(800, 640));
		fc.setMultiSelectionEnabled(multiSelectionEnabled);
		fc.setFileSelectionMode(mode.getJFileChooserValue());

		// set select file
		if (!defaultFile.isEmpty() & action == Action.Save) {
			File fsel = new File(defaultFile);
			fc.setSelectedFile(fsel);
		}
		if (!dialogTitle.isEmpty()) {
			fc.setDialogTitle(dialogTitle);
		}
		if (action == Action.Open & !openButtonText.isEmpty()) {
			fc.setApproveButtonText(openButtonText);
		} else if (action == Action.Save & !saveButtonText.isEmpty()) {
			fc.setApproveButtonText(saveButtonText);
		}

		// build filters
		if (filters.size() > 0) {
			boolean useAcceptAllFilter = false;
			for (final String[] spec : filters) {
				// the "All Files" filter is handled specially by JFileChooser
				if (spec[1].equals("*")) {
					useAcceptAllFilter = true;
					continue;
				}
				fc.addChoosableFileFilter(new FileNameExtensionFilter(
					spec[0], Arrays.copyOfRange(spec, 1, spec.length)));
			}
			fc.setAcceptAllFileFilterUsed(useAcceptAllFilter);
		}
		int result = -1;
		
		if(Window.class.isAssignableFrom(parent.getClass())) {
			
			if (action == Action.Open) {
				result = fc.showOpenDialog((Window)parent);
			}
			else {
				if (saveButtonText.isEmpty()) {
					result = fc.showSaveDialog((Window)parent);
	            }
				else {
					result = fc.showDialog((Window)parent, null);
	            }
			}
		}
		if (result == JFileChooser.APPROVE_OPTION) {

			selectedFiles = multiSelectionEnabled ? fc.getSelectedFiles() : new File[] { fc.getSelectedFile() };
			currentDirectory = fc.getCurrentDirectory();		
			if(fc.getFileFilter() instanceof FileNameExtensionFilter) {
				
				FileNameExtensionFilter fFilter = (FileNameExtensionFilter) fc.getFileFilter();
				if(fFilter != null) {
					ArrayList<String> parts = new ArrayList<String>();
					parts.add(fFilter.getDescription());
					Collections.addAll(parts, fFilter.getExtensions());
					selectedFilter = parts.toArray(new String[parts.size()]);
				}
			}
			if (action == Action.Save && fc.getSelectedFile() != null 
					&& fc.getSelectedFile().exists() && !allowOverwrite ) {

				int answer = JOptionPane.showConfirmDialog(fc,
						"File " + fc.getSelectedFile().getName() + " already exists, overwrite?", "Overwrite warning",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, stopIcon);

				if (answer == JOptionPane.YES_OPTION) 
					return true;
				else
					return false;			
			}
			return true;
		}
		return false;
	}

	private boolean showWindowsFileChooser(Object parent, Action action) {
		
		final WindowsFileChooser fc = new WindowsFileChooser(currentDirectory);
		fc.setMultipleSelection(multiSelectionEnabled);
		fc.setFilters(filters);

		if (!defaultFile.isEmpty())
			fc.setDefaultFilename(defaultFile);

		if (!dialogTitle.isEmpty()) 
			fc.setTitle(dialogTitle);
		
		final boolean result = fc.showDialog(parent, action == Action.Open);
		if (result) {
			selectedFiles = fc.getSelectedFiles();
			currentDirectory = fc.getCurrentDirectory();
			
			if(filters.size() > 0 && fc.getFilterIndex() > 0)
				selectedFilter = filters.get(fc.getFilterIndex() - 1);
		}
		return result;
	}
	
	private boolean showWindowsFolderBrowser(Object parent) {
		final WindowsFolderBrowser fb = new WindowsFolderBrowser();
		if (!dialogTitle.isEmpty()) {
			fb.setTitle(dialogTitle);
		}		
		final File file = fb.showDialog(parent);
		if (file != null) {
			selectedFiles = new File[] { file };
			currentDirectory = file.getParentFile() != null ?
				file.getParentFile() : file;
			return true;
		}
		return false;
	}
	
	/**
	 * add a filter to the user-selectable list of file filters
	 *
     * @param name   name of the filter
     * @param filter you must pass at least 1 argument, the arguments are the file
     *               extensions.
	 */
	public void addFilter(String name, String... filter) {
		if (filter.length < 1) {
			throw new IllegalArgumentException();
		}
		ArrayList<String> parts = new ArrayList<String>();
		parts.add(name);
		Collections.addAll(parts, filter);
		filters.add(parts.toArray(new String[parts.size()]));
	}

	/**
	 * sets the selection mode
	 *
	 * @param mode the selection mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public Mode getMode() {
		return mode;
	}

	public void setCurrentDirectory(String currentDirectoryPath) {
		this.currentDirectory = (currentDirectoryPath != null ? new File(currentDirectoryPath) : null);
	}

	/**
	 * sets whether to enable multiselection
	 *
	 * @param enabled true to enable multiselection, false to disable it
	 */
	public void setMultiSelectionEnabled(boolean enabled) {
		this.multiSelectionEnabled = enabled;
	}

	public boolean isMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	public void setDefaultFileName(String dfile) {
		this.defaultFile = dfile;
	}

	/**
	 * set a title name
	 *
	 * @param Title of dialog
	 * 
	 */
	public void setTitle(String title) {
		this.dialogTitle = title;
	}

	/**
	 * set a open button name
	 *
	 * @param open button text
	 * 
	 */
	public void setOpenButtonText(String buttonText) {
		this.openButtonText = buttonText;
	}

	/**
	 * set a save button name
	 *
	 * @param save button text
	 * 
	 */
	public void setSaveButtonText(String buttonText) {
		this.saveButtonText = buttonText;
	}

	public File[] getSelectedFiles() {
		return selectedFiles;
	}

	public File getSelectedFile() {
		return selectedFiles[0];
	}

	public File getCurrentDirectory() {
		return currentDirectory;
	}

	public void setAllowOverwrite(boolean allowOverwrite) {
		this.allowOverwrite = allowOverwrite;
	}

	public ArrayList<String[]> getFilters() {
		return filters;
	}

	public String[] getSelectedFilter() {
		return selectedFilter;
	}
}
