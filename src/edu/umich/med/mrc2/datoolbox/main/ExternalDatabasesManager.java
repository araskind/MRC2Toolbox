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

package edu.umich.med.mrc2.datoolbox.main;

import java.util.Collection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.ExternalDatabase;
import edu.umich.med.mrc2.datoolbox.database.idt.ExternalDatabaseUtils;

public class ExternalDatabasesManager {

	private static final Logger logger = LogManager.getLogger(ExternalDatabasesManager.class);
	
	private static Collection<ExternalDatabase> externalDatabases;

	private ExternalDatabasesManager() {

	}

	/**
	 * @return the externalDatabases
	 */
	public static Collection<ExternalDatabase> getExternalDatabases() {
		
		if(externalDatabases == null || externalDatabases.isEmpty()) {
			try {
				externalDatabases = ExternalDatabaseUtils.getExternalDatabaseList();
			} catch (Exception e) {
				logger.error("Failed to read external databases list", e);
			}
		}
		return externalDatabases;
	}

	public static ExternalDatabase getDatabaseByName(String name) {

		return getExternalDatabases().stream().
				filter(d -> d.getName().equals(name)).findFirst().orElse(null);
	}

	public static ExternalDatabase getDatabaseById(String id) {

		return getExternalDatabases().stream().
				filter(d -> d.getId().equals(id)).findFirst().orElse(null);
	}
}
