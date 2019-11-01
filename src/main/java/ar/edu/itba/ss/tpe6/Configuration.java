package ar.edu.itba.ss.tpe6;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Configuration {

	private static String INPUT_FILE_NAME = "config.txt";
	private static String OUTPUT_FILE_NAME = "ovito_output.xyz";
	public static final double INTERNAL_RADIUS = 2; // m
	public static final double A_CONSTANT = 2000; // N
	public static final double B_CONSTANT = 0.08; // m
	public static final double MIN_PARTICLE_RADIUS = 0.25; // m
	public static final double MAX_PARTICLE_RADIUS = 0.35; // m
	public static final double K_NORM = 1.2e5; // kg/s^2
	public static final double K_TANG = 2 * K_NORM; // kg/(m s)
	public static final double PARTICLE_MASS = 80; // kg
	private static final double INIT_VEL = 0.0; // m/s
	public static final double TAU = 0.1; // s
	public static final double DESIRED_VEL = 0.8; // m/s
	private static int particleCount = 0;
	//private static double timeStep = 0.1 * Math.sqrt(PARTICLE_MASS / K_NORM);
	private static double timeLimit;
	public static double externalRadius;
	private static final int INVALID_POSITION_LIMIT = 500;
	//public static final double GRAVITY = -9.8; // m/s^2
	private static String fileName = "";
	
	public static void requestParameters() {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter Particle count:");
		Integer pc = null;
		while(pc == null || pc <= 0) {
			pc = stringToInt(scanner.nextLine());
		}
		particleCount = pc;
	    
	    System.out.println("Enter Time Limit:");
    	Double selectedTimeLimit = null;
	    while(selectedTimeLimit == null || selectedTimeLimit <= 0) {
	    	selectedTimeLimit = stringToDouble(scanner.nextLine());
	    }
	    timeLimit = selectedTimeLimit;
	    System.out.println("Enter Filename:");
	    while(fileName == "") {
	    	fileName = scanner.nextLine();
		}
		INPUT_FILE_NAME = fileName + "-input.txt";
		OUTPUT_FILE_NAME = fileName + ".xyz";
	    
	    scanner.close();
	}
	
	/* Parameters must have already been requested */
	public static List<Particle> generateRandomInputFilesAndParseConfiguration() {
		List<Particle> particles = generateParticles();
		externalRadius = calculateExternalRadius(particles);
		setInitialPositions(particles);
		generateOvitoOutputFile();
		return particles;
	}

	private static void setInitialPositions(List<Particle> particles) {
		int invalidPositions = 0;
		File inputFile = new File(INPUT_FILE_NAME);
		inputFile.delete();
		Random r = new Random();

		try(FileWriter fw = new FileWriter(inputFile)) {
			inputFile.createNewFile();
			fw.write("0\n");

			int i;
			for(i = 0; i < particles.size(); i++) {
				Particle p = particles.get(i);
				double randomPositionX = 0;
				double randomPositionY = 0;
				boolean isValidPosition = false;

				while (!isValidPosition) {
					randomPositionX = (externalRadius * 2 - 2 * p.getRadius()) * r.nextDouble() + p.getRadius();
					randomPositionY = (externalRadius * 2 - 2 * p.getRadius()) * r.nextDouble() + p.getRadius();
					isValidPosition = validateParticlePosition(particles, randomPositionX, randomPositionY, p.getRadius());

					invalidPositions += (isValidPosition) ? 0 : 1;
				}
				if (invalidPositions > INVALID_POSITION_LIMIT) break;
				invalidPositions = 0;
				p.setPosition(randomPositionX, randomPositionY);

				fw.write(p.getId() + " " + p.getRadius() + " " + randomPositionX + " " + randomPositionY + " " + INIT_VEL + " " + INIT_VEL + "\n");
			}
			particles.removeAll(particles.subList(i, particles.size()));
			particleCount = particles.size();
		} catch (IOException e) {
			System.err.println("Failed to create input file.");
			e.printStackTrace();
		}
	}
	
	private static Integer stringToInt(String s) {
		Integer i = null;
		try {
			i = Integer.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return i;
	}
	
	private static Double stringToDouble(String s) {
		Double d = null;
		try {
			d = Double.valueOf(s);
		} catch(NumberFormatException e) {
			return null;
		}
		return d;
	}

	private static double calculateExternalRadius(final List<Particle> particles) {
		return 5 * particles.stream()
				.mapToDouble(p -> p.getRadius() * 2)
				.average()
				.orElse(0) + INTERNAL_RADIUS;
	}
	
	/* Time (0) */
    private static List<Particle> generateParticles() {
        List<Particle> particles = new ArrayList<>();

		Random r = new Random();

		for(int i = 0; i < particleCount; i++) {
			double radius = r.nextDouble() * (MAX_PARTICLE_RADIUS - MIN_PARTICLE_RADIUS) + MIN_PARTICLE_RADIUS;
			Particle p = new Particle(radius, PARTICLE_MASS, 0, 0, INIT_VEL, INIT_VEL);
			particles.add(p);
		}

		return particles;
    }
    
    public static boolean validateParticlePosition(final List<Particle> particles,
												   final double randomPositionX,
												   final double randomPositionY,
												   final double radius) {
        if(!isWithinCircle(randomPositionX, randomPositionY, radius, externalRadius, externalRadius)
				|| isWithinCircle(randomPositionX, randomPositionY, radius, externalRadius, INTERNAL_RADIUS)) {
        	return false;
		}
    	for(Particle p : particles) {
    		if(Double.compare(p.getPosition().getX(), 0) == 0
					&& Double.compare(p.getPosition().getY(), 0) == 0) {
    			continue;
			}
            if(Math.sqrt(Math.pow(p.getPosition().getX() - randomPositionX, 2) + Math.pow(p.getPosition().getY() - randomPositionY, 2))
                    < (p.getRadius() + radius))
                return false;
        }
        return true;
    }

	private static boolean isWithinCircle(final double randomPositionX,
										  final double randomPositionY,
										  final double particleRadius,
										  final double circlePosition,
										  final double circleRadius) {
		double centerToCenterDistance = Math.sqrt(
				Math.pow(Math.abs(randomPositionX - circlePosition), 2)
						+ Math.pow(Math.abs(randomPositionY - circlePosition), 2)
		);
		return centerToCenterDistance - particleRadius <= circleRadius;
    }

	private static void generateOvitoOutputFile() {
		File outputFile = new File(OUTPUT_FILE_NAME);
		outputFile.delete();
		try {
			outputFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create Ovito output file.");
			e.printStackTrace();
		}
	}
	
	public static void writeOvitoOutputFile(double time, List<Particle> particles) {
		File outputFile = new File(OUTPUT_FILE_NAME);
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			fw.write(particleCount + "\n");
			fw.write("Lattice=\"" + externalRadius * 2 + " 0.0 0.0 0.0 " + externalRadius * 2
				+ " 0.0 0.0 0.0 1.0"
				+ "\" Properties=id:I:1:radius:R:1:pos:R:2:velo:R:2:Pressure:R:1 Time=" + String.format(Locale.US, "%.2g", time) + "\n");
			for(Particle p : particles) {
				writeOvitoParticle(fw, p);
			}
		} catch (IOException e) {
			System.err.println("Failed to write Ovito output file.");
			e.printStackTrace();
		}
	}
	
	private static void writeOvitoParticle(FileWriter fw, Particle particle) throws IOException {
		fw.write(particle.getId() + " " + particle.getRadius() + " " + particle.getPosition().x + " "
				+ particle.getPosition().y + " " + particle.getVelocity().x + " " + particle.getVelocity().y + " " + particle.getPressure());
		fw.write('\n');
	}

	public static int getParticleCount() {
		return particleCount;
	}

	public static void setParticleCount(int particleCount) {
		Configuration.particleCount = particleCount;
	}
	
	public static double getTimeLimit() {
		return timeLimit;
	}

//	public static double getTimeStep() {
//		return timeStep;
//	}
}
