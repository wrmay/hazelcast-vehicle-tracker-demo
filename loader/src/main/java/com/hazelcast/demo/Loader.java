package com.hazelcast.demo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import org.springframework.core.env.Environment;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;

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
    HazelcastInstance hz;
    
    @Autowired
    Environment environ;
    
    @Autowired
    LoaderConfig config;
    
	@Override
	public void run(String... args) throws Exception {
		try {
			File dataDir = new File(config.getDataDir());
			if (!dataDir.isDirectory())
				throw new RuntimeException(String.format("\"%s\" does not exist or is not a directory.",config.getDataDir()));
			
			ArrayList<File> files = new ArrayList<File>(10000);
			long firstTimestamp = Long.MAX_VALUE;
			int count = 0;
			
			for(File f: dataDir.listFiles()) {
				if (f.isFile() && f.getName().endsWith(".txt")) {
					DataFile df = new DataFile(f);
					try {
						Location loc = df.next();
						if (loc != null) {
							count++;
							if (loc.getTimestamp() < firstTimestamp) firstTimestamp = loc.getTimestamp();
						}
					} finally {
						df.close();
					}
				}
			}
			
			log.info("There are {} data files and the earliest location datum is at {}", count, new SimpleDateFormat(DataFile.DATE_FORMAT).format(firstTimestamp)); 
			
		} finally {
			hz.shutdown();
		}
	}
    
	@PreDestroy
	public void shutdown() {
		log.info("BYE");
	}
    
}
