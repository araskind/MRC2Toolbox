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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Color;

public enum ColorGradient {
	
	BLUES("Blues", new Color(247,251,255), new Color(8,48,107)),
	BLUE_GREEN("Blue-Green", new Color(247,252,253), new Color(0,68,27)),
	BLUE_PURPLE("Blue-Purple", new Color(247,252,253), new Color(77,0,75)),
	BLUE_RED("Blue-Red",Color.BLUE, Color.RED),
	GREEN_BLUE("Green-Blue", new Color(247,52,240), new Color(8,64,129)),
	GRAYS("Grayscale", Color.WHITE, Color.BLACK),
	GREEN_RED("Green-Red",Color.GREEN, Color.RED),
	GREENS("Green-scale", new Color(247,252,245), new Color(0,68,27)),
	ORANGE_RED("Orange-Red", new Color(255,247,236), new Color(127,0,0)),
	ORANGES("Orange-scale", new Color(255,245,235), new Color(127,39,4)),
	PAIRED("Paired", new Color(166,206,227), new Color(177,89,40)),
	PURPLE_BLUE("Purple-Blue", new Color(255,247,251), new Color(2,56,88)),
	PURPLE_GREEN("Purple-Green", new Color(255,247,251), new Color(1,70,54)),
	PURPLE_RED("Purple-Red", new Color(247,244,249), new Color(103,0,31)),
	PURPLES("Purple-scale", new Color(252,251,253), new Color(63,0,125)),	
	RED_PURPLE("Red-Purple", new Color(255,247,243), new Color(73,0,106)),
	REDS("Red-scale", new Color(255,245,240), new Color(103,0,13)),	
	YELLOW_BLUE("Yellow-Blue", new Color(255,255,217), new Color(8,29,88)),
	YELLOW_BROWN("Yellow-Brown",Color.YELLOW, new Color(139,69,19)),
	YELLOW_GREEN("Yellow-Green", new Color(255,255,229), new Color(0,69,41)),
	YELLOW_RED("Yellow-Red", new Color(255,255,204), new Color(128,0,38));

	private final String name;
	private final Color lowValueColor;
	private final Color highValueColor;

	ColorGradient(String name, Color lowValueColor, Color highValueColor) {

		this.name = name;
		this.lowValueColor = lowValueColor;
		this.highValueColor = highValueColor;
	}

	public Color getLowValueColor() {		
		return lowValueColor;
	}
	
	public Color getHighValueColor() {		
		return highValueColor;
	}
	
	public String getName() {		
		return name;
	}
	
	@Override
	public String toString() {		
		return name;
	}
	
	public static ColorGradient getOptionByName(String optionName) {

		for(ColorGradient o : ColorGradient.values()) {

			if(o.name().equals(optionName))
				return o;
		}
		return null;
	}
}
