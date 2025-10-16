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

package edu.umich.med.mrc2.datoolbox.msmsfdr;

public enum PercolatorIputFields {
	SpecId,
	Label,
	ScanNr,
	ExpMass,
	RetentionTime,
	rank,
	score,
	delta_next_best_score,
	dot_product,
	reverse_dot_product,
	probability,
	abs_delta_mz,
	msms_entropy_score,
	abs_delta_rt,
	Peptide,
	Proteins,
}
