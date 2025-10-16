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

package edu.umich.med.mrc2.datoolbox.data;

import java.awt.Image;
import java.util.Collection;

public class MsPlotDataObject {

	private Collection<MsPoint>spectrum;
	private MsPoint parent;
	private String label;
	private String metaData;
	private Image image;
	
	public MsPlotDataObject(
			Collection<MsPoint> spectrum, 
			MsPoint parent, 
			String label) {
		super();
		this.spectrum = spectrum;
		this.parent = parent;
		this.label = label;
	}

	public Collection<MsPoint> getSpectrum() {
		return spectrum;
	}

	public MsPoint getParent() {
		return parent;
	}

	public String getLabel() {
		return label;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
}
