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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;

public abstract class LongTableUpdateTask extends SwingWorker<BasicTableModel, Object[]> {

	private JDialog progressDialog;

	@Override
    public BasicTableModel doInBackground() throws Exception {
		return null;
    }

    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
    	progressDialog.dispose();
    }

    public void setProgressDialog(JDialog progressDialog) {
    	this.progressDialog = progressDialog;
    }
  }