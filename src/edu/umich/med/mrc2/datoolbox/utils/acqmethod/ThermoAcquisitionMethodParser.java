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

package edu.umich.med.mrc2.datoolbox.utils.acqmethod;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradientStep;

public class ThermoAcquisitionMethodParser {
	
	public static final String FLOW_RATE_PREFIX = "PumpModule.Pump.Flow.Nominal:";
	public static final String FLOW_RATE_SUFFIX = "[ml/min]";
	public static final String PERCENT_B_PREFIX = "PumpModule.Pump.%B.Value:";
	public static final String PERCENT_B_SUFFIX = "[%]";
	
	public static ChromatographicGradient extractGradient(File methodFile){		
		
		ChromatographicGradient grad = new ChromatographicGradient();
		Set<ChromatographicGradientStep>gradSteps = 
				new TreeSet<ChromatographicGradientStep>();
		byte[] bytes = null;
		try {
			bytes = Files.readAllBytes(methodFile.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bytes == null)
			return null;		
		
		String contents = new String(bytes, StandardCharsets.UTF_8);		
		String contentsCleaned = StringUtils.remove(contents, '\u0000');
		int startOverview = contentsCleaned.indexOf("---- Overview");
		int endOverview = contentsCleaned.indexOf("Stop Run");
		String overview = contentsCleaned.substring(startOverview, endOverview) + "Stop Run";
		String[]lsSetupArray = overview.split("\\n");	
		
		Pattern timePattern = Pattern.compile("^(\\d+\\.\\d+) \\[min\\]\\s*(.*)");
		Matcher regexMatcher = null;
		
		for(int i=0; i<lsSetupArray.length; i++) {
			
			String line = lsSetupArray[i].trim().replaceAll("\\s+", " ");
			//	System.out.println(line);
			regexMatcher = timePattern.matcher(line);
			if(regexMatcher.find()) {
				
				String command = regexMatcher.group(2).trim();
				if(command != null) {
					
					if(command.equals("Run"))
						System.out.println("***");
					
					if(command.isEmpty() || command.equals("Run")) {
						
						double startTime = Double.parseDouble(regexMatcher.group(1));
						double flowRate = -1.0d;
						double mobilePhaseBpercent = -1.0d;
						
						for(int j=i+1; j<i+4; j++) {
							
							if(j == lsSetupArray.length)
								break;
							
							line = lsSetupArray[j].trim().replaceAll("\\s+", " ");
							if(line.startsWith(FLOW_RATE_PREFIX)) {
								line = line.replace(FLOW_RATE_PREFIX, "").
										replace(FLOW_RATE_SUFFIX, "").trim();
								flowRate = Double.parseDouble(line);
							}
							if(line.startsWith(PERCENT_B_PREFIX)) {
								
								line = line.replace(PERCENT_B_PREFIX, "").
										replace(PERCENT_B_SUFFIX, "").trim();					
								mobilePhaseBpercent = Double.parseDouble(line);
							}						
						}
						if(flowRate >= 0.0d && mobilePhaseBpercent >= 0.0d) {
							
							ChromatographicGradientStep step = new ChromatographicGradientStep(
									startTime, 
									mobilePhaseBpercent, 
									flowRate);
							grad.getGradientSteps().add(step);
						}
						i=i+3;
					}
					if(command.equals("Stop Run")) {
						
						double stopTime = Double.parseDouble(regexMatcher.group(1));
						grad.setStopTime(stopTime);
					}
				}				
			}
		}
		return grad;
	}
}
















