package ar.edu.itba.ss.tpe5;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GranularManager {
	
    private final Grid grid;
		private final double timeStep;
		private double accumulatedTime = 0.0;
    
    public GranularManager(final Grid grid) {
    	this.grid = grid;
    	this.timeStep = Configuration.getTimeStep();
    }
    
    public void execute() {
		List<Particle> previousParticles = initPreviousParticles(grid.getParticles());
		double accumulatedPrintingTime = 0.0;
		double printingTimeLimit = 0.005; //s
		
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
			grid.calculateAllParticlesNeighbors();
			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
				accumulatedPrintingTime = 0;
			}
			accumulatedTime += timeStep;
			accumulatedPrintingTime += timeStep;
			List<Particle> updatedParticles = new ArrayList<>(previousParticles.size());
			verletUpdate(previousParticles, updatedParticles);

			grid.setParticles(updatedParticles);
			grid.updateGridSections();
		}
	}
    
    public void verletUpdate(List<Particle> previousParticles, List<Particle> updatedParticles) {
        List<Particle> currentParticles = grid.getParticles();
        List<Particle[]> repositionParticles = new ArrayList<>();

        for(int i = 0; i < currentParticles.size(); i++) {
            Particle currParticle = currentParticles.get(i);
            Particle prevParticle = previousParticles.get(i);
            Particle updatedParticle = currParticle.clone();
            
            Point2D.Double acceleration = getAcceleration(currParticle);
            
            double newPositionX = 2 * currParticle.getPosition().getX() - prevParticle.getPosition().getX()
                    + Math.pow(timeStep, 2) * acceleration.getX();
            double newVelocityX = (newPositionX - prevParticle.getPosition().getX()) / (2 * timeStep);
            
            double newPositionY = 2 * currParticle.getPosition().getY() - prevParticle.getPosition().getY()
                    + Math.pow(timeStep, 2) * acceleration.getY();
            double newVelocityY = (newPositionY - prevParticle.getPosition().getY()) / (2 * timeStep);
            
            if(newPositionY < 0) {
            	repositionParticles.add(new Particle[] {prevParticle, updatedParticle});
            } else {
            	prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
                prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getVelocity().getY());
                updatedParticle.setPosition(newPositionX, newPositionY);
				updatedParticle.setVelocity(newVelocityX, newVelocityY);
			}
			updatedParticle.setPressure(currParticle.getPressure());
            updatedParticles.add(updatedParticle);
        }
        for(Particle[] repParticles : repositionParticles) {
        	repositionParticles(repParticles, updatedParticles);
        }
    }

    private void repositionParticles(final Particle[] repParticles, final List<Particle> updatedParticles) {
			// Write to exit file
			Configuration.writeExitFile(accumulatedTime);

			double randomPositionX = 0;
			double randomPositionY = Configuration.BOX_HEIGHT + Configuration.MIN_PARTICLE_HEIGHT - repParticles[0].getRadius();
			Random r = new Random();
			boolean isValidPosition = false;
			double limit = 100;
			double accumLimit = 0;
			while(!isValidPosition) {
				if (accumLimit == limit) {
					randomPositionY -= repParticles[0].getRadius();
					accumLimit = 0;
				}
				randomPositionX = (Configuration.BOX_WIDTH - 2 * repParticles[0].getRadius()) * r.nextDouble() + repParticles[0].getRadius();
				isValidPosition = Configuration.validateParticlePosition(updatedParticles, randomPositionX, randomPositionY, repParticles[0].getRadius());
				accumLimit++;
			}
			repParticles[0].setPosition(randomPositionX, randomPositionY);
			repParticles[0].setVelocity(0, 0);
			repParticles[1].setPosition(randomPositionX, randomPositionY);
			repParticles[1].setVelocity(0, 0);
		}

	private Point2D.Double getAcceleration(final Particle p) {
    	Point2D.Double force = getParticleForce(p);
        return new Point2D.Double(force.getX() / p.getMass(), force.getY() / p.getMass());
    }

    private Point2D.Double getParticleForce(final Particle p) {
    	double resultantForceX = 0;
			double resultantForceY = 0;
			double resultantForceN = 0;
			List<Particle> neighbors = new ArrayList<>(p.getNeighbors());
			// Add as neighbors two particles for the corners
			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2 + Configuration.HOLE_WIDTH, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
        for(Particle n : neighbors) {
        	double normalUnitVectorX = (n.getPosition().getX() - p.getPosition().getX()) / Math.abs(n.getRadius() - p.getRadius());
        	double normalUnitVectorY = (n.getPosition().getY() - p.getPosition().getY()) / Math.abs(n.getRadius() - p.getRadius());
        	double norm = Math.sqrt(Math.pow(normalUnitVectorX, 2) + Math.pow(normalUnitVectorY, 2));
        	normalUnitVectorX /= norm;
        	normalUnitVectorY /= norm;
        	Point2D.Double normalUnitVector = new Point2D.Double(normalUnitVectorX, normalUnitVectorY);
        	Point2D.Double tangentUnitVector = new Point2D.Double(- normalUnitVectorY, normalUnitVectorX);
        	
        	double overlap = p.getRadius() + n.getRadius() - p.getCenterToCenterDistance(n);
        	if(overlap < 0)
				overlap = 0;
        	Point2D.Double relativeVelocity = p.getRelativeVelocity(n);
        	
			double normalForce = - Configuration.K_NORM * overlap;
        	double tangentForce = - Configuration.K_TANG * overlap * (relativeVelocity.getX() * tangentUnitVector.getX()
					+ relativeVelocity.getY() * tangentUnitVector.getY());
        	
			resultantForceN += normalForce;
			
        	resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * (- normalUnitVector.getY());
			resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * normalUnitVector.getX();
        }
				
        // Check for horizontal border overlaps
        double horizBorderOverlap = 0;
        double boxHoleStartingX = (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2;
		double boxHoleEndingX = boxHoleStartingX + Configuration.HOLE_WIDTH;
		boolean isWithinHole = p.getPosition().getX() > boxHoleStartingX && p.getPosition().getX() < boxHoleEndingX;
		
        if (!isWithinHole && Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT) < p.getRadius()) {
        	horizBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT));
		}

        resultantForceY += Configuration.K_NORM * horizBorderOverlap;
        resultantForceX += - Configuration.K_TANG * horizBorderOverlap * p.getVelocity().getX();
        
        // Check for vertical border overlaps
        double vertBorderOverlap = 0;
        if(p.getPosition().getX() - p.getRadius() < 0) {
        	vertBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getX()));
        	resultantForceX += Configuration.K_NORM * vertBorderOverlap;
        } else if(p.getPosition().getX() + p.getRadius() > Configuration.BOX_WIDTH) {
        	vertBorderOverlap = p.getRadius() - Math.abs(p.getPosition().getX() - Configuration.BOX_WIDTH);
        	resultantForceX += - Configuration.K_NORM * vertBorderOverlap;
        }
        resultantForceY += - Configuration.K_TANG * vertBorderOverlap * p.getVelocity().getY();
        
		resultantForceY += p.getMass() * Configuration.GRAVITY;
		p.calculatePressure(resultantForceN);
        return new Point2D.Double(resultantForceX, resultantForceY);
    }
    
    // Euler Algorithm evaluated in (- timeStep)
    private List<Particle> initPreviousParticles(final List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>();
		for(Particle p : currentParticles) {
			Particle prevParticle = p.clone();
			Point2D.Double force = getParticleForce(p);
			
			double prevPositionX = p.getPosition().getX() - timeStep * p.getVelocity().getX()
					+ Math.pow(timeStep, 2) * force.getX() / (2 * p.getMass());
			double prevPositionY = p.getPosition().getY() - timeStep * p.getVelocity().getY()
					+ Math.pow(timeStep, 2) * force.getY() / (2 * p.getMass());
			
			double prevVelocityX = p.getVelocity().getX() - (timeStep / p.getMass()) * force.getX();
			double prevVelocityY = p.getVelocity().getY() - (timeStep / p.getMass()) * force.getY();
			
			prevParticle.setPosition(prevPositionX, prevPositionY);
			prevParticle.setVelocity(prevVelocityX, prevVelocityY);
			previousParticles.add(prevParticle);
		}
		
		return previousParticles;
	}
    
}
