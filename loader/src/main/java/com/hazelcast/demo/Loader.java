package com.hazelcast.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * 
 * @author wrmay
 *
 */

@ComponentScan
@Configuration
@ImportResource("file:loader.xml")
public class Loader implements CommandLineRunner
{
	private static Logger log = LogManager.getLogger(Loader.class);
	
    public static void main( String[] args )
    {
		SpringApplication.run(Loader.class, args);
    }

    
    @Bean
    HazelcastInstance hazelcast() {
    	return HazelcastClient.newHazelcastClient();
    }
    
    @Autowired
    private HazelcastInstance hz;
    
    @Autowired
    private LoaderConfig config;
    
    private ScheduledThreadPoolExecutor executor;
    private Feeder []feeders;
    
    private long firstTimestamp;
    private int fileCount;
    
    
	@Override
	public void run(String... args) throws Exception {
		try {
			IMap<Integer,Location> vehicleMap = hz.getMap("locations");
			
			// create all of the feeders
			feeders = new Feeder[config.getParallelWriters()];
			for (int i=0;i<config.getParallelWriters();++i) feeders[i] = new Feeder();
			
			initialScan();
			log.info("There are {} data files and the earliest location datum is at {}", fileCount, new SimpleDateFormat(DataFile.DATE_FORMAT).format(firstTimestamp)); 
			
			// we don't know the start time until after the initial scan
			// now that we know it, finish setting up the feeders
			for(Feeder feeder: feeders) {
				feeder.setTarget(vehicleMap);
				feeder.setStartTime(firstTimestamp);
				feeder.setStepSize(1000 * config.getReplaySpeed());
			}
			
			
			executor = new ScheduledThreadPoolExecutor(config.getParallelWriters());
			for(Feeder feeder:feeders) executor.scheduleAtFixedRate(feeder, 0, 1, TimeUnit.SECONDS);
			
			
			waitForEnter();
			executor.shutdown();
			
			
		} finally {
			hz.shutdown();
		}
	}
	
	
    
	@PreDestroy
	public void shutdown() {
		log.info("BYE");
	}
    
	
	private void initialScan() throws IOException {
		File dataDir = new File(config.getDataDir());
		if (!dataDir.isDirectory())
			throw new RuntimeException(String.format("\"%s\" does not exist or is not a directory.",config.getDataDir()));
		
		firstTimestamp = Long.MAX_VALUE;
		fileCount = 0;
		
		
		for(File f: dataDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".txt")) {
				DataFile df = new DataFile(f);
				Location loc = df.getFirst();
				if (loc != null) {
					fileCount++;
					if (loc.getTimestamp() < firstTimestamp) firstTimestamp = loc.getTimestamp();
				}
				
				// assign the file to a stripe
				int stripe = Math.abs(f.getName().hashCode()) % config.getParallelWriters();
				feeders[stripe].addDataFile(df);
			}
		}
		
	}
	
	private void waitForEnter() {
		System.out.println("Press enter to stop.");
		try (BufferedReader reader= new BufferedReader(new InputStreamReader(System.in))){
			reader.readLine();
		} catch(IOException x) {
			// not a problem
		}
	}
}
