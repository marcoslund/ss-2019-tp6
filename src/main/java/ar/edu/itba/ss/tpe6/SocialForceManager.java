package ar.edu.itba.ss.tpe6;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class SocialForceManager {
	
    private final Grid grid;
    private final double timeStep;
	private double accumulatedTime = 0.0;
    
    public SocialForceManager(final Grid grid) {
    	this.grid = grid;
    	timeStep = Configuration.TIME_STEP;
    }
    
    public void execute() {
//		double accumulatedPrintingTime = 0.0;
//		double printingTimeLimit = 0.005; //s
    	
    	List<Particle> prevParticles = initPrevParticles(grid.getParticles());
    	List<Particle> predictedParticles = new ArrayList<>(prevParticles.size());
    	prevParticles.forEach(p -> predictedParticles.add(p.clone()));
    	
		while(Double.compare(accumulatedTime, Configuration.getTimeLimit()) <= 0) {
//			if (accumulatedPrintingTime >= printingTimeLimit) {
				Configuration.writeOvitoOutputFile(accumulatedTime, grid.getParticles());
//				accumulatedPrintingTime = 0;
//			}
			accumulatedTime += timeStep;
//			accumulatedPrintingTime += timeStep;
			
			grid.setParticles(updateParticles(prevParticles, predictedParticles));
		}
	}
    
    private List<Particle> updateParticles(final List<Particle> prevParticles, final List<Particle> predictedParticles) {
    	List<Particle> currentParticles = grid.getParticles();
    	predictParticles(predictedParticles, currentParticles, prevParticles);
    	List<Particle> updatedParticles = new ArrayList<>(currentParticles.size());
    	
		for(int i = 0; i < currentParticles.size(); i++) {
			Particle currParticle = currentParticles.get(i);
			Particle prevParticle = prevParticles.get(i);
			Particle predParticle = predictedParticles.get(i);
			Particle updatedParticle = currParticle.clone();
			
			Point2D.Double currAcceleration = getAcceleration(currParticle, currentParticles);
			Point2D.Double prevAcceleration = getAcceleration(prevParticle, prevParticles);
			Point2D.Double predAcceleration = getAcceleration(predParticle, predictedParticles);
			
			double correctedVelocityX = currParticle.getVelocity().getX()
					+ (1 / 3.0) * predAcceleration.getX() * timeStep
					+ (5 / 6.0) * currAcceleration.getX() * timeStep
					- (1 / 6.0) * prevAcceleration.getX() * timeStep;
			
			double correctedVelocityY = currParticle.getVelocity().getY()
					+ (1 / 3.0) * predAcceleration.getY() * timeStep
					+ (5 / 6.0) * currAcceleration.getY() * timeStep
					- (1 / 6.0) * prevAcceleration.getY() * timeStep;
			
			prevParticle.setPosition(currParticle.getPosition().getX(), currParticle.getPosition().getY());
			prevParticle.setVelocity(currParticle.getVelocity().getX(), currParticle.getVelocity().getY());
			updatedParticle.setPosition(predParticle.getPosition().getX(), predParticle.getPosition().getY());
			updatedParticle.setVelocity(correctedVelocityX, correctedVelocityY);
			updatedParticles.add(updatedParticle);
		}
		
		return updatedParticles;
	}
    
	private void predictParticles(final List<Particle> predictedParticles,
			final List<Particle> currentParticles, final List<Particle> prevParticles) {
		for(int i = 0; i < currentParticles.size(); i++) {
			Particle currParticle = currentParticles.get(i);
			Particle prevParticle = prevParticles.get(i);
			
			Point2D.Double currAcceleration = getAcceleration(currParticle, currentParticles);
			Point2D.Double prevAcceleration = getAcceleration(prevParticle, prevParticles);
			
			double newPositionX = currParticle.getPosition().getX() + currParticle.getVelocity().getX() * timeStep
					+ (2 / 3.0) * currAcceleration.getX() * Math.pow(timeStep, 2)
					- (1 / 6.0) * prevAcceleration.getX() * Math.pow(timeStep, 2);
			
			double predictedVelocityX = currParticle.getVelocity().getX() 
					+ (3 / 2.0) * currAcceleration.getX() * timeStep
					- (1 / 2.0) * prevAcceleration.getX() * timeStep;
			
			double newPositionY = currParticle.getPosition().getY() + currParticle.getVelocity().getY() * timeStep
					+ (2 / 3.0) * currAcceleration.getY() * Math.pow(timeStep, 2)
					- (1 / 6.0) * prevAcceleration.getY() * Math.pow(timeStep, 2);
			
			double predictedVelocityY = currParticle.getVelocity().getY() 
					+ (3 / 2.0) * currAcceleration.getY() * timeStep
					- (1 / 2.0) * prevAcceleration.getY() * timeStep;
			
			Particle predictedParticle = predictedParticles.get(i);
			predictedParticle.setPosition(newPositionX, newPositionY);
			predictedParticle.setVelocity(predictedVelocityX, predictedVelocityY);
			predictedParticles.add(predictedParticle);
		}
	}

	private Point2D.Double getAcceleration(final Particle particle, final List<Particle> particles) {
    	Point2D.Double granularForce = getGranularForce(particle);
    	Point2D.Double socialForce = getSocialForce(particle);
    	Point2D.Double drivingForce = getDrivingForce(particle);
    	
        return new Point2D.Double(
        		(granularForce.getX() + socialForce.getX() + drivingForce.getX()) / particle.getMass(),
        		(granularForce.getY() + socialForce.getY() + drivingForce.getY()) / particle.getMass());
    }
	
	private Point2D.Double getDrivingForce(final Particle particle) {
		double desiredDirectionUnitVectorX = (grid.getExternalRadius() - particle.getPosition().getY());
		double desiredDirectionUnitVectorY = - (grid.getExternalRadius() - particle.getPosition().getX());
		double norm = Math.sqrt(Math.pow(desiredDirectionUnitVectorX, 2) + Math.pow(desiredDirectionUnitVectorY, 2));
		desiredDirectionUnitVectorX /= norm;
		desiredDirectionUnitVectorY /= norm;
		
		double drivingForceX = particle.getMass() 
				* (Configuration.DESIRED_VEL * desiredDirectionUnitVectorX - particle.getVelocity().getX()) / Configuration.TAU;
		double drivingForceY = particle.getMass() 
				* (Configuration.DESIRED_VEL * desiredDirectionUnitVectorY - particle.getVelocity().getY()) / Configuration.TAU;
		
		return new Point2D.Double(drivingForceX, drivingForceY);
	}

	private Point2D.Double getSocialForce(final Particle particle) {
		// TODO Auto-generated method stub
		return new Point2D.Double(0, 0);
	}

	private Point2D.Double getGranularForce(final Particle particle) {
		Point2D.Double wallForce = getWallForce(particle);
		// TODO Auto-generated method stub
		return wallForce;
	}

	private Point2D.Double getWallForce(final Particle particle) {
		double normalForce = 0;
		double tangentForce = 0;
		double resultantForceX = 0;
		double resultantForceY = 0;
		
		double tangentUnitVectorX = (grid.getExternalRadius() - particle.getPosition().getY());
		double tangentUnitVectorY = - (grid.getExternalRadius() - particle.getPosition().getX());
		double norm = Math.sqrt(Math.pow(tangentUnitVectorX, 2) + Math.pow(tangentUnitVectorY, 2));
		tangentUnitVectorX /= norm;
		tangentUnitVectorY /= norm;
		Point2D.Double normalUnitVector = new Point2D.Double(tangentUnitVectorY, - tangentUnitVectorX);
    	Point2D.Double tangentUnitVector = new Point2D.Double(tangentUnitVectorX, tangentUnitVectorY);
		
		double distanceToCenter = Point2D.distance(particle.getPosition().getX(), particle.getPosition().getY(),
				grid.getExternalRadius(), grid.getExternalRadius());
		double tangentVelocity = particle.getVelocity().getX() * tangentUnitVectorX + particle.getVelocity().getY() * tangentUnitVectorY;
		
		if(distanceToCenter < particle.getRadius() + grid.getInternalRadius()) {
			// Inner wall collision
			double innerOverlap = (particle.getRadius() + grid.getInternalRadius()) - distanceToCenter;
			normalForce += innerOverlap * Configuration.K_NORM;
			tangentForce -= innerOverlap * Configuration.K_TANG * tangentVelocity;
			
		}
		if(distanceToCenter + particle.getRadius() > grid.getExternalRadius()) {
			// Outer wall collision
			double outerOverlap = distanceToCenter + particle.getRadius() - grid.getExternalRadius();
			normalForce -= outerOverlap * Configuration.K_NORM;
			tangentForce -= outerOverlap * Configuration.K_TANG * tangentVelocity;
		}
		
		resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * tangentUnitVector.getX();
		resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * tangentUnitVector.getY();
		
		return new Point2D.Double(resultantForceX, resultantForceY);
	}
	
	// Euler Algorithm evaluated in (- timeStep)
    private List<Particle> initPrevParticles(List<Particle> currentParticles) {
    	List<Particle> previousParticles = new ArrayList<>();
		for(Particle p : currentParticles) {
			Particle prevParticle = p.clone();
			
			Point2D.Double acceleration = getAcceleration(p, currentParticles);
			
			double prevPositionX = p.getPosition().getX() - timeStep * p.getVelocity().getX()
					+ Math.pow(timeStep, 2) * acceleration.getX() / 2;
			double prevPositionY = p.getPosition().getY() - timeStep * p.getVelocity().getY()
					+ Math.pow(timeStep, 2) * acceleration.getY() / 2;
			
			double prevVelocityX = p.getVelocity().getX() - timeStep * acceleration.getX();
			double prevVelocityY = p.getVelocity().getY() - timeStep * acceleration.getY();
			
			prevParticle.setPosition(prevPositionX, prevPositionY);
			prevParticle.setVelocity(prevVelocityX, prevVelocityY);
			previousParticles.add(prevParticle);
		}
		
		return previousParticles;
	}

//    private Point2D.Double getParticleForce(final Particle p) {
//    	double resultantForceX = 0;
//			double resultantForceY = 0;
//			double resultantForceN = 0;
//			List<Particle> neighbors = new ArrayList<>(p.getNeighbors());
//			// Add as neighbors two particles for the corners
//			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
//			neighbors.add(new Particle(Configuration.MIN_PARTICLE_RADIUS * 0.1, Configuration.PARTICLE_MASS, (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2 + Configuration.HOLE_WIDTH, Configuration.MIN_PARTICLE_HEIGHT, 0, 0));
//        for(Particle n : neighbors) {
//        	double normalUnitVectorX = (n.getPosition().getX() - p.getPosition().getX()) / Math.abs(n.getRadius() - p.getRadius());
//        	double normalUnitVectorY = (n.getPosition().getY() - p.getPosition().getY()) / Math.abs(n.getRadius() - p.getRadius());
//        	double norm = Math.sqrt(Math.pow(normalUnitVectorX, 2) + Math.pow(normalUnitVectorY, 2));
//        	normalUnitVectorX /= norm;
//        	normalUnitVectorY /= norm;
//        	Point2D.Double normalUnitVector = new Point2D.Double(normalUnitVectorX, normalUnitVectorY);
//        	Point2D.Double tangentUnitVector = new Point2D.Double(- normalUnitVectorY, normalUnitVectorX);
//
//        	double overlap = p.getRadius() + n.getRadius() - p.getCenterToCenterDistance(n);
//        	if(overlap < 0)
//				overlap = 0;
//        	Point2D.Double relativeVelocity = p.getRelativeVelocity(n);
//
//			double normalForce = - Configuration.K_NORM * overlap;
//        	double tangentForce = - Configuration.K_TANG * overlap * (relativeVelocity.getX() * tangentUnitVector.getX()
//					+ relativeVelocity.getY() * tangentUnitVector.getY());
//
//			resultantForceN += normalForce;
//
//        	resultantForceX += normalForce * normalUnitVector.getX() + tangentForce * (- normalUnitVector.getY());
//			resultantForceY += normalForce * normalUnitVector.getY() + tangentForce * normalUnitVector.getX();
//        }
//
//        // Check for horizontal border overlaps
//        double horizBorderOverlap = 0;
//        double boxHoleStartingX = (Configuration.BOX_WIDTH - Configuration.HOLE_WIDTH) / 2;
//		double boxHoleEndingX = boxHoleStartingX + Configuration.HOLE_WIDTH;
//		boolean isWithinHole = p.getPosition().getX() > boxHoleStartingX && p.getPosition().getX() < boxHoleEndingX;
//
//        if (!isWithinHole && Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT) < p.getRadius()) {
//        	horizBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getY() - Configuration.MIN_PARTICLE_HEIGHT));
//		}
//
//        resultantForceY += Configuration.K_NORM * horizBorderOverlap;
//        resultantForceX += - Configuration.K_TANG * horizBorderOverlap * p.getVelocity().getX();
//
//        // Check for vertical border overlaps
//        double vertBorderOverlap = 0;
//        if(p.getPosition().getX() - p.getRadius() < 0) {
//        	vertBorderOverlap = (p.getRadius() - Math.abs(p.getPosition().getX()));
//        	resultantForceX += Configuration.K_NORM * vertBorderOverlap;
//        } else if(p.getPosition().getX() + p.getRadius() > Configuration.BOX_WIDTH) {
//        	vertBorderOverlap = p.getRadius() - Math.abs(p.getPosition().getX() - Configuration.BOX_WIDTH);
//        	resultantForceX += - Configuration.K_NORM * vertBorderOverlap;
//        }
//        resultantForceY += - Configuration.K_TANG * vertBorderOverlap * p.getVelocity().getY();
//
//		resultantForceY += p.getMass() * Configuration.GRAVITY;
//		p.calculatePressure(resultantForceN);
//        return new Point2D.Double(resultantForceX, resultantForceY);
//    }
    
}
