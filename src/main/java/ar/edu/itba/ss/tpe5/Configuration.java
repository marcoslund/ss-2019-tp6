package ar.edu.itba.ss.tpe5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Configuration {

	private static String INPUT_FILE_NAME = "config.txt";
	private static String OUTPUT_FILE_NAME = "ovito_output.xyz";
	private static String EXIT_FILE_NAME = "exit.txt";
	public static final double BOX_WIDTH = 0.4; // m
	public static final double BOX_HEIGHT = 1.5; // m
	public static final double HOLE_WIDTH = 0.25; // m
	public static final double MIN_PARTICLE_HEIGHT = BOX_HEIGHT / 10; // m
	public static final double MIN_PARTICLE_RADIUS = 0.01; // m
	private static final double MAX_PARTICLE_RADIUS = 0.015; // m
	public static final double K_NORM = 1e5;
	public static final double K_TANG = 2 * K_NORM;
	public static final double PARTICLE_MASS = 0.01; // kg
	private static final double INIT_VEL = 0.0; // m/s
	private static int particleCount;
	private static double timeStep = 0.1 * Math.sqrt(PARTICLE_MASS / K_NORM);
	private static double timeLimit;
	private static final int INVALID_POSITION_LIMIT = 500;
	public static final double GRAVITY = -9.8; // m/s^2
	private static String fileName = "";
	
	public static void requestParameters() {
		Scanner scanner = new Scanner(System.in);
	    
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
		EXIT_FILE_NAME = fileName + ".txt";
	    
	    scanner.close();
	}
	
	/* Parameters must have already been requested */
	public static List<Particle> generateRandomInputFilesAndParseConfiguration() {
		generateInputFile();
		List<Particle> particles = parseConfiguration();
		generateOvitoOutputFile();
		generateExitFile();
		return particles;
	}
	
	private static List<Particle> parseConfiguration() {
		List<Particle> particles = new ArrayList<>();
		try(BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
			String line = br.readLine(); /* Time (0) */
			for(int i = 0; i < particleCount; i++) {
				line = br.readLine();
				if(line == null)
					failWithMessage("Particles do not match particle count.");
				String[] attributes = line.split(" ");
				attributes = removeSpaces(attributes);
				setParticleProperties(particles, attributes);
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return particles;
	}
	
	private static String[] removeSpaces(String[] array) {
		List<String> list = new ArrayList<>(Arrays.asList(array));
		List<String> filteredList = list.stream().filter(s -> !s.equals("") && !s.equals(" ")).collect(Collectors.toList());
		String[] newArray = new String[filteredList.size()];
		return filteredList.toArray(newArray);
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
	
	private static void failWithMessage(String message) {
		System.err.println(message);
		System.exit(1);
	}
	
	private static void setParticleProperties(final List<Particle> particles, final String[] attributes) {
		final int propertyCount = 6;
		Integer id = null;
		Double radius = null;
		Double x = null;
		Double y = null;
		Double vx = null;
		Double vy = null;
		if(attributes.length != propertyCount || (id = stringToInt(attributes[0])) == null || (radius = stringToDouble(attributes[1])) == null
			|| (x = stringToDouble(attributes[2])) == null || (y = stringToDouble(attributes[3])) == null 
			|| (vx = stringToDouble(attributes[4])) == null || (vy = stringToDouble(attributes[5])) == null) {
				failWithMessage(attributes[0] + ", " + attributes[1] + ", " + attributes[2] + ", " + attributes[3] 
						+ ", " + attributes[4] + ", " + attributes[5] + " are invalid attributes.");
			}
		particles.add(new Particle(id, radius, PARTICLE_MASS, x, y, vx, vy));
	}
	
	/* Time (0) */
    private static void generateInputFile() {
        List<Particle> particles = new ArrayList<>();
        File inputFile = new File(INPUT_FILE_NAME);
        inputFile.delete();
        try(FileWriter fw = new FileWriter(inputFile)) {
            inputFile.createNewFile();
            fw.write("0\n");
            
            Random r = new Random();
            int invalidPositions = 0;
            while(invalidPositions < INVALID_POSITION_LIMIT) {
            	double radius = r.nextDouble() * (MAX_PARTICLE_RADIUS - MIN_PARTICLE_RADIUS) + MIN_PARTICLE_RADIUS;
                double randomPositionX = BOX_WIDTH * 0.1;
                double randomPositionY = BOX_HEIGHT * 0.9;
                boolean isValidPosition = false;

                while(!isValidPosition) {
                    randomPositionX = (BOX_WIDTH - 2 * radius) * r.nextDouble() + radius;
                    randomPositionY = (BOX_HEIGHT - 2 * radius) * r.nextDouble() + radius + MIN_PARTICLE_HEIGHT;
                    isValidPosition = validateParticlePosition(particles, randomPositionX, randomPositionY, radius);
					
                    invalidPositions += (isValidPosition) ? 0 : 1;
					if (invalidPositions > INVALID_POSITION_LIMIT) break;
				}
				if (invalidPositions > INVALID_POSITION_LIMIT) break;
				invalidPositions = 0;
                Particle p = new Particle(radius, PARTICLE_MASS, randomPositionX, randomPositionY, INIT_VEL, INIT_VEL);
                particles.add(p);
                fw.write(p.getId() + " " + radius + " " + randomPositionX + " " + randomPositionY + " " + INIT_VEL + " " + INIT_VEL + "\n");
            }
            particleCount = particles.size();
        } catch (IOException e) {
            System.err.println("Failed to create input file.");
            e.printStackTrace();
        }
    }
    
    public static boolean validateParticlePosition(final List<Particle> particles, final double randomPositionX,
    		final double randomPositionY, final double radius) {
        if(particles.isEmpty())
            return true;
        if(randomPositionX - radius < 0 || randomPositionX + radius > BOX_WIDTH
			|| randomPositionY - radius - MIN_PARTICLE_HEIGHT < 0 || randomPositionY + radius > BOX_HEIGHT + MIN_PARTICLE_HEIGHT)
        	return false;
        for(Particle p : particles) {
            if(Math.sqrt(Math.pow(p.getPosition().getX() - randomPositionX, 2) + Math.pow(p.getPosition().getY() - randomPositionY, 2))
                    < (p.getRadius() + radius))
                return false;
        }
        return true;
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

	private static void generateExitFile() {
		File exitFile = new File(EXIT_FILE_NAME);
		exitFile.delete();
		try {
			exitFile.createNewFile();
		} catch (IOException e) {
			System.err.println("Failed to create exit file.");
			e.printStackTrace();
		}
	}
	
	public static void writeOvitoOutputFile(double time, List<Particle> particles) {
		File outputFile = new File(OUTPUT_FILE_NAME);
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			fw.write(particleCount + "\n");
			fw.write("Lattice=\"" + BOX_WIDTH + " 0.0 0.0 0.0 " + BOX_HEIGHT 
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

	public static void writeExitFile(double time) {
		File outputFile = new File(EXIT_FILE_NAME);
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			fw.write(time + "\n");
		} catch (IOException e) {
			System.err.println("Failed to write exit file.");
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

	public static double getTimeStep() {
		return timeStep;
	}
}
