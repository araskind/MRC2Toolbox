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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.pref.TableColumnState;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class TableLlayoutManager {

	private static final Map<String,TableColumnState[]>tableLayouts = new TreeMap<String,TableColumnState[]>();
	private static final File tableLayoutFile = Paths.get(MRC2ToolBoxCore.configDir + "tableLayouts.xml").toFile();
	
	public static void setTableLayout(BasicTable table) {
		tableLayouts.put(table.getClass().getName(), table.getTableLayout());
	}
	
	public static TableColumnState[] getTableLayout(BasicTable table) {
		return tableLayouts.get(table.getClass().getName());
	}
	
	public static void saveLayouts() {
		
		try {
			XStream projectXstream = initXstream();
			RandomAccessFile raf = 
					new RandomAccessFile(tableLayoutFile.getAbsolutePath(), "rw");
			FileOutputStream fout = 
					new FileOutputStream(raf.getFD());	        
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			projectXstream.toXML(tableLayouts, bout);
			bout.close();
			fout.close();
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void loadLayouts() {
		
		if(tableLayoutFile.exists() && tableLayoutFile.canRead()) {
			
			try {			
				XStream projectImport = new XStream(new StaxDriver());
				projectImport.setMode(XStream.XPATH_RELATIVE_REFERENCES);
				projectImport.addPermission(NoTypePermission.NONE);
				projectImport.addPermission(NullPermission.NULL);
				projectImport.addPermission(PrimitiveTypePermission.PRIMITIVES);
				projectImport.allowTypesByRegExp(new String[] { ".*" });
				projectImport.ignoreUnknownElements();
				
				InputStream input = new FileInputStream(tableLayoutFile);				
				BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));	
				
				@SuppressWarnings("unchecked")
				Map<String,TableColumnState[]>savedTableLayouts = (TreeMap<String,TableColumnState[]>) projectImport.fromXML(br);
				tableLayouts.clear();
				tableLayouts.putAll(savedTableLayouts);
				br.close();
				input.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static XStream initXstream() {

		XStream xstream = new XStream(new StaxDriver());

		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		xstream.addPermission(NoTypePermission.NONE);
		xstream.addPermission(NullPermission.NULL);
		xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);

		return xstream;
	}
}
