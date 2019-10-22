import numpy
import math

class Particle:
  def __init__(self, id, radius, x, y, vx, vy, pressure):
    self.id = int(id)
    self.radius = float(radius)
    self.mass = 0.01
    self.x = float(x)
    self.y = float(y)
    self.vx = float(vx)
    self.vy = float(vy)
    self.pressure = float(pressure)
  def getVelocityLength(self):
    return math.sqrt(self.vx ** 2  + self.vy ** 2)
  def position(self):
    return (self.x, self.y)
  def __str__(self):
    return f'Id: {self.id}\nRadius: {self.radius}\nMass: {self.mass}\nPosition X: {self.x}\nPosition Y: {self.y}\nVelocity X: {self.vx}\nVelocity Y: {self.vy}\n'

class Step:
  def __init__(self, time, particles):
    self.time = time
    self.particles = particles
  def getParticlesSpeed(self):
    return [particle.getVelocityLength() for particle in self.particles]
  def firstChamberPercentage(self):
    return len([ x for x in filter(lambda p: p.x < 1, self.particles)]) / (len(self.particles) * 1.0)
  def speeds(self):
    return [particle.speed for particle in self.particles]
    

class Simulation:
  def __init__(self, steps, name):
    self.steps = steps
    self.name = name
  def getSecondHalf(self):
    return self.steps[len(self.steps)//2:]
  def getLastThird(self):
    return self.steps[-len(self.steps)//3:]