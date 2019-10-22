package ar.edu.itba.ss.tpe5;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
	
	public static void main(String[] args) {
		Configuration.requestParameters();
		long startTime = System.nanoTime();
		executeSingleRun();
		long endTime = System.nanoTime();
		System.out.println("Process done in " + TimeUnit.NANOSECONDS.toMillis(endTime - startTime) + " ms.");
	}
	
	private static void executeSingleRun() {
		List<Particle> particles = Configuration.generateRandomInputFilesAndParseConfiguration();
		Grid grid = new Grid(particles);
		GranularManager manager = new GranularManager(grid);
		manager.execute();
	}

}
