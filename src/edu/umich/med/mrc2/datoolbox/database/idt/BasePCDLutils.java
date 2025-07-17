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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.sql.Connection;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class BasePCDLutils {

	public static CompoundLibrary getPCDLbaseLibrary() {
		
		CompoundLibrary basePCDLlibrary = MRC2ToolBoxCore.getActiveMsLibraries().stream().
				filter(l -> l.getLibraryId().equals(MRC2ToolBoxConfiguration.BASE_PCDL_LIBRARY_ID)).
				findFirst().orElse(null);
		if(basePCDLlibrary != null)
			return basePCDLlibrary;
		
		try {
			basePCDLlibrary = MSRTLibraryUtils.getLibrary(MRC2ToolBoxConfiguration.BASE_PCDL_LIBRARY_ID);
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		if (basePCDLlibrary == null) 
			return null;

		try {
			Connection conn = ConnectionManager.getConnection();
			Collection<LibraryMsFeatureDbBundle>bundles =
					MSRTLibraryUtils.createFeatureBundlesForLibrary(basePCDLlibrary.getLibraryId(), conn);

			for(LibraryMsFeatureDbBundle fBundle : bundles) {

				if(fBundle.getConmpoundDatabaseAccession() != null) {

					LibraryMsFeature newTarget = fBundle.getFeature();
					MSRTLibraryUtils.attachIdentity(
							newTarget, fBundle.getConmpoundDatabaseAccession(), false, conn);

					if(newTarget.getPrimaryIdentity() != null) {

						newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
						basePCDLlibrary.addFeature(newTarget);
					}
				}
			}
			ConnectionManager.releaseConnection(conn);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
		if(basePCDLlibrary != null)
			MRC2ToolBoxCore.getActiveMsLibraries().add(basePCDLlibrary);
		
		return basePCDLlibrary;
	}
}
