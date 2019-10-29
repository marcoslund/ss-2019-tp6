package ar.edu.itba.ss.tpe6;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Grid {
	
	private List<Particle> particles;
	private final double externalRadius;
	private final double internalRadius;
	
	public Grid(final List<Particle> particles) {
		this.particles = particles;
		this.externalRadius = Configuration.externalRadius;
		this.internalRadius = Configuration.INTERNAL_RADIUS;
	}
	
	public List<Particle> getParticles() {
		return Collections.unmodifiableList(particles);
	}
	
	public void setParticles(List<Particle> newParticles) {
		Objects.requireNonNull(newParticles);
		particles = newParticles;
	}

	public double getExternalRadius() {
		return externalRadius;
	}

	public double getInternalRadius() {
		return internalRadius;
	}
	
}
