from calculator import calculateDistance, squareList, averageLists, linearRegression, calculateDeltas, average, discreteRange, PDF, stdevLists, average
from functools import reduce #python 3
import numpy

def getBallDistancesFromOrigin(simulation):
  firstBall = simulation.steps[0].ball
  distances = [calculateDistance(firstBall.position(), step.ball.position()) for step in simulation.steps]
  return distances

# Not used yet
def getParticleDistancesFromOrigin(simulation, index = 5):
  firstParticle = simulation.steps[0].particles[index]
  distances = [calculateDistance(firstParticle.position(), step.particles[index].position()) for step in simulation.steps]
  return distances

def calculateCollisionFrequency(simulation):
  amountOfCollisions = len(simulation.steps)
  totalSimulationTime = simulation.steps[-1].time
  return amountOfCollisions/totalSimulationTime

def calculateCollisionTimesAverage(simulation):
  accumulatedTimes = [step.time for step in simulation.steps]
  deltaTimes = calculateDeltas(accumulatedTimes)
  return average(deltaTimes)

def calculateProbabilityCollisionTimesDistribution(simulation):
  accumulatedTimes = [step.time for step in simulation.steps]
  deltaTimes = calculateDeltas(accumulatedTimes)
  # return deltaTimes
  # next lines calculate PDF manually, but the chart library does this automatically
  hist, bin_edges = PDF(deltaTimes, 0.25)
  return deltaTimes, bin_edges

def calculateProbabilityVelocities(simulation):
  lastThirdSteps = simulation.getLastThird()
  listOfSpeedsTime0 = simulation.steps[0].getParticlesSpeed()
  listOfSpeeds = [step.getParticlesSpeed() for step in lastThirdSteps]
  speeds = reduce(lambda x,y: x+y,listOfSpeeds)
  return speeds, listOfSpeedsTime0
  # next lines calculate PDF manually, but the chart library does this automatically
  hist, bin_edges = PDF(speeds, 0.2)
  return hist * calculateDeltas(bin_edges)

def calculateKineticEnergy(simulation):
  particlesList = [step.particles for step in simulation.steps]
  getEnergy = lambda particle: (particle.mass * (particle.getVelocityLength() ** 2)) / 2.0
  kineticsList = [ map(getEnergy, particles) for particles in particlesList]
  kineticsSteps = [sum(kinetics) for kinetics in kineticsList]
  return kineticsSteps

def calculateExitsValues(qs):
  lastThird = qs[-len(qs)//3:]
  avg = averageLists(lastThird)
  err = stdevLists(lastThird)
  return avg, err

def calculateDiffusion(simulations, getDistanceFromOrigin = getBallDistancesFromOrigin):
  squaredDistances = [squareList(getDistanceFromOrigin(simulation)) for simulation in simulations]
  maxTimes = max([len(squaredDistance) for squaredDistance in squaredDistances])
  minTimes = min([len(squaredDistance) for squaredDistance in squaredDistances])
  
  # Since time limit differs between multiple simulations, we repeat the last element for those shorter.
  # normalizedLists = []
  # for squaredDistance in squaredDistances:
  #   if len(squaredDistance) <= maxTimes:
  #     normalizedLists.append(squaredDistance + [squaredDistance[-1]]*(maxTimes - len(squaredDistance)))

  # Since time limit differs between multiple simulations, we keep their shortest length
  print(minTimes)
  normalizedLists = []
  for squaredDistance in squaredDistances:
    normalizedLists.append(squaredDistance[:minTimes])

  # Since time limit differs between multiple simulations, we average those that exist for a specific step.
  # normalizedLists = []
  # for squaredDistance in squaredDistances:
  #   if len(squaredDistance) <= maxTimes:
  #     normalizedLists.append(squaredDistance + [numpy.nan]*(maxTimes - len(squaredDistance)))

  averageSquaredDistances = averageLists(normalizedLists)[(minTimes)//2:]
  deviations = stdevLists(normalizedLists)[(minTimes)//2:]

  diffusion, b = linearRegression(averageSquaredDistances)
  return diffusion,b, averageSquaredDistances, deviations