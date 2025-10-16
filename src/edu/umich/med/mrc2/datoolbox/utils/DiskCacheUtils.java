/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.utils;

import org.apache.commons.jcs3.access.exception.CacheException;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DiskCacheUtils {

    public static void putMSFeatureInfoBundleInCache( MSFeatureInfoBundle bundle ) {
        String key = bundle.getMSFeatureId();
        try {
        	MRC2ToolBoxCore.msFeatureCache.put(key, bundle );
        }
        catch ( CacheException e ) {
            System.err.println( String.format( "Problem putting feature info bundle %s in the cache, for key %s%n%s",
            		bundle.getMsFeature().getName(), key, e.getMessage() ) );
        }
    }

    public static MSFeatureInfoBundle retrieveMSFeatureInfoBundleFromCache( String msId ) {
        return (MSFeatureInfoBundle)MRC2ToolBoxCore.msFeatureCache.get( msId );
    }
    
    public static void putCompoundIdentityInCache( CompoundIdentity cid ) {
    	
    	if(MRC2ToolBoxCore.compoundIdCache == null || cid == null)
    		return;
    	
        String key = cid.getPrimaryDatabaseId();
        try {
        	MRC2ToolBoxCore.compoundIdCache.put(key, cid );
        }
        catch ( CacheException e ) {
            System.err.println( String.format( "Problem putting compound %s in the cache, for key %s%n%s",
            		cid.getName(), key, e.getMessage() ) );
        }
    }

    public static CompoundIdentity retrieveCompoundIdentityFromCache( String accession ) {
    	
    	if(MRC2ToolBoxCore.compoundIdCache == null || accession == null)
    		return null;
    	else
    		return (CompoundIdentity)MRC2ToolBoxCore.compoundIdCache.get( accession );
    }
    
    public static void putMsMsLibraryFeatureInCache( MsMsLibraryFeature msmsLibEntry ) {
    	
    	if(MRC2ToolBoxCore.msmsLibraryCache == null || msmsLibEntry == null)
    		return;
    				
        String key = msmsLibEntry.getUniqueId();
        try {
        	MRC2ToolBoxCore.msmsLibraryCache.put(key, msmsLibEntry );
        }
        catch ( CacheException e ) {
            System.err.println( String.format( "Problem putting MSMS library entry %s in the cache, for key %s%n%s",
            		msmsLibEntry.getCompoundIdentity().getName(), key, e.getMessage() ) );
        }
    }

    public static MsMsLibraryFeature retrieveMsMsLibraryFeatureFromCache( String mrcLibId ) {
    	
    	if(MRC2ToolBoxCore.msmsLibraryCache == null || mrcLibId == null)
    		return null;
    	else
    		return (MsMsLibraryFeature)MRC2ToolBoxCore.msmsLibraryCache.get( mrcLibId );
    }
}
