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

package edu.umich.med.mrc2.datoolbox.utils.filefilter;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFileFilterIE  implements FileFilter {

    protected final String[] dirNames;
    protected final boolean exclude;

    public DirectoryFileFilterIE(String dirName, boolean exclude) {

        this.dirNames = new String[] { dirName.toLowerCase() };
        this.exclude = exclude;
    }
    
    public DirectoryFileFilterIE(String[] dirNames, boolean exclude) {

        this.dirNames = dirNames;
        this.exclude = exclude;
    }

    public boolean accept(File file) {
    	
        if (!file.isDirectory()) 
        	return true;
        
        boolean acceptDir = true;
        if(exclude) {
        	
        	for(String check : dirNames) {  
        		
        		 if(file.getName().equalsIgnoreCase(check)) {
        			 acceptDir = false;
        			 break;
        		 }
        	}       	
        }
        else {
        	acceptDir = false;
        	for(String check : dirNames) { 
        		
	       		 if(file.getName().equalsIgnoreCase(check)) {
        			 acceptDir = true;
        			 break;
        		 }
        	}
        }       
        return acceptDir;
    }
}
