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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundNameScope;
import edu.umich.med.mrc2.datoolbox.data.enums.StringMatchFidelity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class SearchCompoundDatabaseTask extends AbstractTask {

	private String cpdName;
	private String molFormula;
	private String cpdId;
	private String inchi;
	private Range massRange;
	private boolean getExactMatch;
	private boolean searchSynonyms;
	private boolean allowSpellingErrors;
	private CompoundNameScope nameScope;
	private StringMatchFidelity nameMatchFidelity;

	private Collection<CompoundIdentity> cpdList;

	public SearchCompoundDatabaseTask(
			String cpdName,
			String molFormula,
			String cpdId,
			String inchi,
			Range massRange,
			boolean getExactMatch,
			boolean searchSynonyms,
			boolean allowSpellingErrors) {

		super();
		this.cpdName = cpdName;
		this.molFormula = molFormula;
		this.cpdId = cpdId;
		this.inchi = inchi;
		this.massRange = massRange;
		this.getExactMatch = getExactMatch;
		this.searchSynonyms = searchSynonyms;
		this.allowSpellingErrors = allowSpellingErrors;

		taskDescription = "Searching compound database";
	}

	public SearchCompoundDatabaseTask(
			String cpdName2,
			String molFormula2,
			String cpdId2,
			String inchi2,
			Range massRange2,
			CompoundNameScope nameScope2,
			StringMatchFidelity nameMatchFidelity2) {

		super();
		this.cpdName = cpdName2;
		this.molFormula = molFormula2;
		this.cpdId = cpdId2;
		this.inchi = inchi2;
		this.massRange = massRange2;
		this.nameScope = nameScope2;
		this.nameMatchFidelity = nameMatchFidelity2;

		taskDescription = "Searching compound database";
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		total = 100;
		processed = 30;
		cpdList = new ArrayList<CompoundIdentity>();
		try {
//			cpdList.addAll(RemoteCompoundDatabaseUtils.findCompounds(
//					cpdName,
//					getExactMatch,
//					searchSynonyms,
//					allowSpellingErrors,
//					molFormula,
//					cpdId,
//					inchi,
//					massRange));

			Collection<String> idList = CompoundDatabaseUtils.findCompoundAccessions(
					cpdName,
					nameScope,
					nameMatchFidelity,
					molFormula,
					cpdId,
					inchi,
					massRange);
			if(!idList.isEmpty()) {
				fetchCompoundsById(idList);
			}
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void fetchCompoundsById(Collection<String> idList) throws Exception {

		taskDescription = "Fetching compound data ...";
		total = idList.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		for(String id : idList) {

			CompoundIdentity identity = CompoundDatabaseUtils.getCompoundById(id, conn);
			if(identity != null)
				cpdList.add(identity);
			processed++;
		}
		ConnectionManager.releaseConnection(conn);
	}

	@Override
	public Task cloneTask() {

		return new SearchCompoundDatabaseTask(
				cpdName,  molFormula,  cpdId,  inchi,  massRange,
				getExactMatch,  searchSynonyms, allowSpellingErrors);
	}

	/**
	 * @return the cpdList
	 */
	public Collection<CompoundIdentity> getCompoundList() {

		return cpdList;
	}
}
