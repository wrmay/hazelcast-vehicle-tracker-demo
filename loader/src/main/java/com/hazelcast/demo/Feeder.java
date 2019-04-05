package com.hazelcast.demo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hazelcast.core.IMap;

public class Feeder implements Runnable {

	private static Logger log = LogManager.getLogger(Feeder.class);
	
	// config
	private IMap<Integer,Location> locationMap;
	private LinkedList<DataFile> dataFiles;
	private long startTime;
	private long stepSize;
	
	// state
	LinkedList<Location> ticks;
	private long currStartTime;
	private long totalTicksWritten;
	
	public Feeder() {
		this.dataFiles = new LinkedList<DataFile>();
		this.ticks = new LinkedList<Location>();
		this.totalTicksWritten = 0;
	}

	public void setTarget(IMap<Integer,Location> map) {
		locationMap = map;
	}
	
	public void setStartTime(long t) {
		startTime = t;
		this.currStartTime = startTime;
	}
	
	public void setStepSize(long s) {
		stepSize = s;
	}
	
	public void addDataFile(DataFile f) {
		dataFiles.add(f);
	}
	
	@Override
	public void run() {
		// each time we run, we'll move forward in time "stepSize" milliseconds 
		// we want everything greater than or equal to currStartTime and (strictly) less than currStartTime + stepSize
		//
		
		ticks.clear();
		
		for(DataFile f: dataFiles) {
			try {
				ticks.addAll(f.getTicks(currStartTime, currStartTime + stepSize));
			} catch(IOException x) {
				log.warn("An error occurred while processing {}.  Other files will be processed.",f.getPath(),x);
			}
		}
		ticks.sort(new LocationComparator());

		
		// note that in some cases it would make sense to use putAll here and only send one tick per vehicle per time step 
		// but in this case we are purposely trying to send all the ticks
		for (Location l: ticks) locationMap.put(l.getVehicleId(), l);
		
		currStartTime += stepSize;   // next time, we will start at this point in time
		totalTicksWritten += ticks.size();
		log.info("Simulated time={}.  Ticks written by this feeder so far={}", new SimpleDateFormat(DataFile.DATE_FORMAT).format(currStartTime), totalTicksWritten);
		
	}
	
	private static class LocationComparator implements Comparator<Location> {

		@Override
		public int compare(Location left, Location right) {
			return Long.valueOf(left.getTimestamp()).compareTo(Long.valueOf(right.getTimestamp()));
		}
		
	}
	
}
