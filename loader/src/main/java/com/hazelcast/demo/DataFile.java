package com.hazelcast.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Not intended for use by multiple threads
 * 
 * @author wrmay
 *
 */
public class DataFile implements AutoCloseable {
	public static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static Logger log = LogManager.getLogger(DataFile.class);
	
	private DateFormat dateFormat = new SimpleDateFormat(DataFile.DATE_FORMAT);	
	
	private BufferedReader reader;
	private File file;
	private int lastLineRead;
	
	public DataFile(File f) throws IOException {
		file = f;
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		lastLineRead = 0;  // line numbers start at 1
	}
	
	/**
	 * 
	 * @return null when there are no more
	 */
	public Location next() throws IOException {
		boolean validLine = false;
		Location result = null;
		String line = reader.readLine();
		
		// if everything is well formatted, this while block will not read any more lines
		while (line != null) {
			++lastLineRead;
			String []words = line.split(",");
			if (words.length != 4) {
				log.warn("skipping invalid entry at line {} of {}", lastLineRead, file);
				line = reader.readLine();
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
				}
			}
		}
		
		return result;
	}

	@Override
	public void close() throws Exception {
		try {
			reader.close();
		} catch(IOException x) {
			log.warn("An exception occurred while trying to close {}",file);
		}
	}
}
