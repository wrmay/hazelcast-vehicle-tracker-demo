package com.hazelcast.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Not intended for use by multiple threads
 * 
 * The underlying file is opened and closed for every operation.
 * 
 * @author wrmay
 *
 */
public class DataFile  {
	public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static Logger log = LogManager.getLogger(DataFile.class);
	
	private DateFormat dateFormat = new SimpleDateFormat(DataFile.DATE_FORMAT);	
	
	private File file;
	private int lastLineRead;  //used only for reporting the line number in case of errors
	
	public DataFile(File f) throws IOException {
		file = f;
	}
	
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	/**
	 * 
	 * @return null when there are no more
	 */
	
	public Location getFirst() throws IOException {
		Location result = null;
		lastLineRead = 0;
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
			lastLineRead = 0;
			result = next(reader);
		}
		
		
		return result;
	}


	public List<Location> getTicks(long from, long to) throws IOException {
		LinkedList<Location> result = new LinkedList<Location>();
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
			lastLineRead = 0;
			Location location = next(reader);
			
			// scan past anything that is too early
			while(location != null && (location.getTimestamp() < from)) location = next(reader);
			
			while(location != null && (location.getTimestamp() < to)) {
				result.add(location);
				location = next(reader);
			}
		}
		
		
		return result;
	}
	
	// returns null if there is no next line or all of the remaining lines are invalid
	private Location next(BufferedReader reader) throws IOException {
		Location result = null;
		String line = reader.readLine();
		++lastLineRead;
		
		// if everything is well formatted, this while block will not read any more lines
		while (line != null) {
			String []words = line.split(",");
			if (words.length != 4) {
				log.warn("skipping invalid entry at line {} of {}", lastLineRead, file);
				line = reader.readLine();
				++lastLineRead;
			} else {
				try {
					int id = Integer.parseInt(words[0]);
					long timestamp = dateFormat.parse(words[1]).getTime();
					float longitude = Float.parseFloat(words[2]);
					float latitude = Float.parseFloat(words[3]);
					
					result = new Location();
					result.setVehicleId(id);
					result.setTimestamp(timestamp);
					result.setLatitude(latitude);
					result.setLongitude(longitude);
					break;  ///BREAK
				} catch(Exception x) {
					log.warn("skipping line {} of {} [{}] due to incorrect format",lastLineRead, file, line);
					reader.readLine();
					++lastLineRead;
				}
			}
		
		}
		return result;
	}
}
