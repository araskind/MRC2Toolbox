/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.main.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Preferences implementation that stores to a user-defined file. See
 * FilePreferencesFactory.
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id: FilePreferences.java 283 2009-06-18 17:06:58Z david $
 */
public class FilePreferences extends AbstractPreferences {
	
	private static final Logger logger = LogManager.getLogger(FilePreferences.class);

	private Map<String, String> root;
	private Map<String, FilePreferences> children;
	private boolean isRemoved = false;
	public static final String errorFormat	= "%s %s";

	public FilePreferences(AbstractPreferences parent, String name) {
		
		super(parent, name);
		logger.trace(String.format(errorFormat, "Instantiating node", name));
		root = new TreeMap<String, String>();
		children = new TreeMap<String, FilePreferences>();

		try {
			sync();
		} catch (BackingStoreException e) {
			logger.fatal(String.format(errorFormat, "Unable to sync on creation of node", name), e);
		}
	}

	protected void putSpi(String key, String value) {
		root.put(key, value);
		try {
			flush();
		} catch (BackingStoreException e) {
			logger.fatal(String.format(errorFormat, "Unable to flush after putting", key), e);
		}
	}

	protected String getSpi(String key) {
		return root.get(key);
	}

	protected void removeSpi(String key) {
		root.remove(key);
		try {
			flush();
		} catch (BackingStoreException e) {
			logger.fatal(String.format(errorFormat, "Unable to flush after removing", key), e);
		}
	}

	protected void removeNodeSpi() throws BackingStoreException {
		isRemoved = true;
		flush();
	}

	protected String[] keysSpi() throws BackingStoreException {
		return root.keySet().toArray(new String[root.keySet().size()]);
	}

	protected String[] childrenNamesSpi() throws BackingStoreException {
		return children.keySet().toArray(new String[children.keySet().size()]);
	}

	protected FilePreferences childSpi(String name) {
		FilePreferences child = children.get(name);
		if (child == null || child.isRemoved()) {
			child = new FilePreferences(this, name);
			children.put(name, child);
		}
		return child;
	}

	protected void syncSpi() throws BackingStoreException {
		if (isRemoved())
			return;

		final File file = FilePreferencesFactory.getPreferencesFile();

		if (!file.exists())
			return;

		synchronized (file) {
			Properties p = new Properties();
			try (FileInputStream fs = new FileInputStream(file)){
				p.load(fs);

				StringBuilder sb = new StringBuilder();
				getPath(sb);
				String path = sb.toString();

				final Enumeration<?> pnen = p.propertyNames();
				while (pnen.hasMoreElements()) {
					String propKey = (String) pnen.nextElement();
					if (propKey.startsWith(path)) {
						String subKey = propKey.substring(path.length());
						// Only load immediate descendants
						if (subKey.indexOf('.') == -1) {
							root.put(subKey, p.getProperty(propKey));
						}
					}
				}
			} catch (IOException e) {
				throw new BackingStoreException(e);
			}
		}
	}

	private void getPath(StringBuilder sb) {
		final FilePreferences parent = (FilePreferences) parent();
		if (parent == null)
			return;

		parent.getPath(sb);
		sb.append(name()).append('.');
	}

	protected void flushSpi() throws BackingStoreException {
		
		final File file = FilePreferencesFactory.getPreferencesFile();
		synchronized (file) {
			Properties p = new Properties();
			try {
				StringBuilder sb = new StringBuilder();
				getPath(sb);
				String path = sb.toString();
				if (file.exists()) {
					
					try(FileInputStream fs = new FileInputStream(file)){						
						p.load(fs);	
						List<String> toRemove = new ArrayList<String>();
	
						// Make a list of all direct children of this node to be removed
						final Enumeration<?> pnen = p.propertyNames();						
						while (pnen.hasMoreElements()) {
							
							String propKey = (String) pnen.nextElement();							
							if (propKey.startsWith(path)) {								
								String subKey = propKey.substring(path.length());
								
								// Only do immediate descendants
								if (subKey.indexOf('.') == -1) 
									toRemove.add(propKey);							
							}
						}
						// Remove them now that the enumeration is done with
						for (String propKey : toRemove) 
							p.remove(propKey);					
					}					
				}
				// If this node hasn't been removed, add back in any values
				if (!isRemoved) {
					
					for (Entry<String, String> rootEntry : root.entrySet())
						p.setProperty(path + rootEntry.getKey(), rootEntry.getValue());
				}
				try(FileOutputStream fos = new FileOutputStream(file)){
					p.store(fos, "FilePreferences");
				}				
			} catch (IOException e) {
				throw new BackingStoreException(e);
			}
		}
	}
}


























