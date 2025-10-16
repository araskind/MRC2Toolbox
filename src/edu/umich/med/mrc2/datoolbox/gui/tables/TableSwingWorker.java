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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.util.List;

import javax.swing.SwingWorker;

public class TableSwingWorker extends SwingWorker<BasicTableModel, Object[]> {

    private final BasicTableModel tableModel;
    private final List<Object[]> modelData;

    public TableSwingWorker(BasicTableModel tableModel, List<Object[]> modelData) {
        this.tableModel = tableModel;
        this.modelData = modelData;
    }

    @Override
    protected BasicTableModel doInBackground() throws Exception {

        // This is a deliberate pause to allow the UI time to render
        Thread.sleep(1000);

        System.out.println("Start polulating");

        for (int index = 0; index < modelData.size(); index++) {

        	Object[] data = modelData.get(index);
            publish(data);
            Thread.yield();
        }
        return tableModel;
    }

    @Override 	
    protected void process(List<Object[]> chunks) {
        System.out.println("Adding " + chunks.size() + " rows");
        tableModel.addRows(chunks);
    }
    
    @Override
    protected void done() {
    	
    }
}
