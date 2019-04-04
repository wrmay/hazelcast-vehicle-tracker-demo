package com.hazelcast.demo;

public class LoaderConfig {
	private String dataDir;
	private int parallelWriters;
	private int replaySpeed;
	
	public String getDataDir() {
		return dataDir;
	}
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}
	public int getParallelWriters() {
		return parallelWriters;
	}
	public void setParallelWriters(int parallelWriters) {
		this.parallelWriters = parallelWriters;
	}
	public int getReplaySpeed() {
		return replaySpeed;
	}
	public void setReplaySpeed(int replaySpeed) {
		this.replaySpeed = replaySpeed;
	}
	
	// set defaults here
	public LoaderConfig() {
		this.dataDir = "data";
		this.parallelWriters = 10;
		this.replaySpeed = 24;
	}
	
}
