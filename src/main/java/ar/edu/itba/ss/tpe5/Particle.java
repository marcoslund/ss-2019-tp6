package ar.edu.itba.ss.tpe5;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Particle implements Cloneable {
	
	private static int count = 0;
	
	private int id;
	private double radius;
	private double mass;
	private double pressure;
	private Point2D.Double position;
	private Point2D.Double velocity;
	private Set<Particle> neighbors;
	
	public Particle(final double radius, final double mass, final double x, final double y, final double vx, final double vy) {
		this.id = count++;
		this.radius = radius;
		this.position = new Point2D.Double(x, y);
		this.velocity = new Point2D.Double(vx, vy);
		this.neighbors = new HashSet<>();
	}
	
	public Particle(int id, double radius, double mass, double x, double y, double vx, double vy) {
		this.id = id;
		this.radius = radius;
		this.mass = mass;
		this.position = new Point2D.Double(x, y);
		this.velocity = new Point2D.Double(vx, vy);
		this.neighbors = new HashSet<>();
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o)
			return true;
		if(!(o instanceof Particle))
			return false;
		Particle other = (Particle) o;
		return this.id == other.getId();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
	
	@Override
	public String toString() {
		return "id: " + id + "; radius: " + radius + " ; mass: " + mass + " ; x: " + position.x
				+ " ; y: " + position.y + " ; vx: " + velocity.x + " ; vy: " + velocity.y;
	}
	
	@Override
	public Particle clone() {
		Particle p = new Particle(id, radius, mass, position.getX(), position.getY(), velocity.getX(), velocity.getY());
		p.setPressure(p.getPressure());
		return p;
	}

	public int getId() {
		return id;
	}

	public double getRadius() {
		return radius;
	}

	public double getMass() {
		return mass;
	}

	public double getPressure() {
		return Math.abs(pressure);
	}

	public Point2D.Double getPosition() {
		return position;
	}

	public void setPosition(double x, double y) {
		position.x = x;
		position.y = y;
	}

	public void calculatePressure(double forces) {
		double perimeter =  Math.PI * 2.0 * radius;
		this.pressure = forces / perimeter;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public Point2D.Double getVelocity() {
		return velocity;
	}

	public void setVelocity(double vx, double vy) {
		velocity.x = vx;
		velocity.y = vy;
	}
	
	public double getVelocityAngle() {
		return Math.atan2(velocity.y, velocity.x);
	}
	
	public Set<Particle> getNeighbors() {
		return Collections.unmodifiableSet(neighbors);
	}
	
	public void clearNeighbors() {
		neighbors.clear();
	}
	
	public void addNeighbor(Particle p) {
		if(p == null)
			return;
		neighbors.add(p);
	}
	
	public boolean isNeighbor(Particle other) {
		return neighbors.contains(other);
	}
	
	public double getBorderToBorderDistance(final Particle other) {
		double horizontalDistance = Math.abs(getPosition().x - other.getPosition().x);
		double verticalDistance = Math.abs(getPosition().y - other.getPosition().y);
		return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2)) - getRadius() - other.getRadius();
	}
	
	public double getCenterToCenterDistance(final Particle other) {
		double horizontalDistance = Math.abs(getPosition().x - other.getPosition().x);
		double verticalDistance = Math.abs(getPosition().y - other.getPosition().y);
		return Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2));
	}
	
	public Point2D.Double getRelativeVelocity(final Particle other) {
		return new Point2D.Double(velocity.getX() - other.getVelocity().getX(), velocity.getY() - other.getVelocity().getY());
	}

}
