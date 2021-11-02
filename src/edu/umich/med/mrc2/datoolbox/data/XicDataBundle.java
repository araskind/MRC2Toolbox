package edu.umich.med.mrc2.datoolbox.data;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class XicDataBundle {

	private Collection<MsPoint>rawXic;
	private Collection<MsPoint>smoothXic;
	private Range peakRtRange;
	private Range xicExtractionRtRange;
	
	Map<Integer,Double>scanRtMap;
	
	public XicDataBundle(
			Collection<MsPoint> rawXic, 
			Collection<MsPoint> smoothXic, 
			Range peakRtRange,
			Range xicExtractionRtRange) {
		super();
		this.rawXic = new TreeSet<MsPoint>(MsUtils.scanSorter);
		this.rawXic.addAll(rawXic);
		this.smoothXic = new TreeSet<MsPoint>(MsUtils.scanSorter);
		this.smoothXic.addAll(smoothXic);
		this.peakRtRange = peakRtRange;
		this.xicExtractionRtRange = xicExtractionRtRange;
		scanRtMap = new TreeMap<Integer,Double>();
	}

	public Collection<MsPoint> getRawXic() {
		return rawXic;
	}

	public Collection<MsPoint> getSmoothXic() {
		return smoothXic;
	}
	
	public Collection<MsPoint> getRawXicWithinRtRange(Range rtRange) {
		
		if(scanRtMap.isEmpty())
			return null;
		
		return rawXic.stream().
				filter(p -> rtRange.contains(scanRtMap.get(p.getScanNum()))).
				sorted(MsUtils.scanSorter).
				collect(Collectors.toList());
	}

	public Collection<MsPoint> getSmoothXicWithinRtRange(Range rtRange) {
		
		if(scanRtMap.isEmpty())
			return null;
		
		return smoothXic.stream().
				filter(p -> rtRange.contains(scanRtMap.get(p.getScanNum()))).
				sorted(MsUtils.scanSorter).
				collect(Collectors.toList());
	}

	public Range getPeakRtRange() {
		return peakRtRange;
	}
	
	public void setPeakRtRange(Range peakRtRange) {
		this.peakRtRange = peakRtRange;
	}

	public Range getXicExtractionRtRange() {
		return xicExtractionRtRange;
	}
	
	public int getTopScan() {
		
		MsPoint topPoint =  getTopPoint();	
		if(topPoint == null)
			return -1;
		else
			return topPoint.getScanNum();
	}
	
	public MsPoint getTopPoint() {
		return rawXic.stream().
				sorted(MsUtils.reverseIntensitySorter).
				findFirst().orElse(null);
	}
	
	public MsPoint getRawPointForScan(int scanNum) {
		return rawXic.stream().
				filter(p -> p.getScanNum() == scanNum).
				findFirst().orElse(null);
	}
	
	public int getTopSmoothScan() {
		
		MsPoint topPoint = getTopSmoothPoint();	
		if(topPoint == null)
			return -1;
		else
			return topPoint.getScanNum();
	}
	
	public MsPoint getTopSmoothPoint() {
		return smoothXic.stream().
				sorted(MsUtils.reverseIntensitySorter).
				findFirst().orElse(null);
	}
	
	public MsPoint getSmoothPointForScan(int scanNum) {
		return smoothXic.stream().
				filter(p -> p.getScanNum() == scanNum).
				findFirst().orElse(null);
	}
	
	public double getmaxIntensity() {
		
		MsPoint topPoint = rawXic.stream().
				sorted(MsUtils.reverseIntensitySorter).findFirst().orElse(null);	
		if(topPoint == null)
			return -1;
		else
			return topPoint.getIntensity();
	}
	
	public double getmaxSmoothIntensity() {
		
		MsPoint topPoint = smoothXic.stream().
				sorted(MsUtils.reverseIntensitySorter).findFirst().orElse(null);	
		if(topPoint == null)
			return -1;
		else
			return topPoint.getIntensity();
	}
	
	public Map<Integer,Double>getRawIntensityByScan(){
		
		Map<Integer,Double>rawIntensityByScan = new TreeMap<Integer,Double>();
		rawXic.stream().forEach(p -> rawIntensityByScan.put(p.getScanNum(), p.getIntensity()));		
		return rawIntensityByScan;
	}
	
	public Map<Integer,Double>getSmoothedIntensityByScan(){
		
		Map<Integer,Double>smoothedIntensityByScan = new TreeMap<Integer,Double>();
		smoothXic.stream().forEach(p -> smoothedIntensityByScan.put(p.getScanNum(), p.getIntensity()));		
		return smoothedIntensityByScan;
	}
	
	public Map<Integer,Double>getRawIntensityByScanWithinRtRange(Range rtRange){
		
		if(scanRtMap.isEmpty())
			return null;
		
		Map<Integer,Double>rawIntensityByScan = new TreeMap<Integer,Double>();
		rawXic.stream().
			filter(p -> rtRange.contains(scanRtMap.get(p.getScanNum()))).
			forEach(p -> rawIntensityByScan.put(p.getScanNum(), p.getIntensity()));		
		return rawIntensityByScan;
	}
	
	public double getRawAreaWithinRtRange(Range rtRange){
		
		if(scanRtMap.isEmpty())
			return 0.0d;
		
		return getRawIntensityByScanWithinRtRange(rtRange).entrySet().stream().
				mapToDouble(p -> p.getValue()).sum();
	}
	
	public Map<Integer,Double>getSmoothedIntensityByScanWithinRtRange(Range rtRange){
		
		if(scanRtMap.isEmpty())
			return null;
		
		Map<Integer,Double>smoothedIntensityByScan = new TreeMap<Integer,Double>();
		smoothXic.stream().
			filter(p -> rtRange.contains(scanRtMap.get(p.getScanNum()))).
			forEach(p -> smoothedIntensityByScan.put(p.getScanNum(), p.getIntensity()));		
		return smoothedIntensityByScan;
	}
	
	public double getSmoothAreaWithinRtRange(Range rtRange){
		
		if(scanRtMap.isEmpty())
			return 0.0d;
		
		return getSmoothedIntensityByScanWithinRtRange(rtRange).entrySet().stream().
				mapToDouble(p -> p.getValue()).sum();
	}

	public Map<Integer, Double> getScanRtMap() {
		return scanRtMap;
	}

	public void setScanRtMap(Map<Integer, Double> scanRtMap) {
		this.scanRtMap = scanRtMap;
	}
	
	public Set<Integer>getScans(){
		
		Set<Integer>scans = new TreeSet<Integer>();
		if(scanRtMap.isEmpty()) {
			rawXic.stream().forEach(p -> scans.add(p.getScanNum()));
			smoothXic.stream().forEach(p -> scans.add(p.getScanNum()));
		}
		else
			scans.addAll(scanRtMap.keySet());
		
		return scans;
	}
	
	public Set<Integer>getScansWithinRtRange(Range rtRange) {
		
		Set<Integer>scans = new TreeSet<Integer>();
		if(scanRtMap.isEmpty()) 
			return null;
		
		scanRtMap.entrySet().stream().
			filter(p -> rtRange.contains(p.getValue())).
			forEach(p -> scans.add(p.getKey()));

		return scans;
	}
}














