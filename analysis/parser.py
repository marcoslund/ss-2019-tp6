from models import Particle, Step, Simulation
import glob
import sys
import numpy as np
import os

def getDirectoryFromArgs():
  return sys.argv[1]

def parseDirectoryFromArgs():
  return parseDirectory(sys.argv[1])

def parseModeFromArgs():
  return int(sys.argv[2])

def parseTimesFile(filename):
  times = [float(line.rstrip('\n')) for line in open(filename)]
  return times

def parseFile(filename):
  lines = [line.rstrip('\n') for line in open(filename)]
  steps = []
  while len(lines) > 0:
    steps.append(parseStep(lines))
  return Simulation(steps, os.path.basename(filename))

def parseDirectory(directory, parse=parseFile):
  return [parse(f) for f in glob.glob(directory + '/*')]

def parseBiblio(directory):
  lines = [line.rstrip('\n') for line in open(directory)]
  lines = [np.array(line.split(',')).astype(float) for line in lines]
  name = os.path.basename(directory).split('.')[0]
  return name, lines

def parseStep(lines):
  nextLines = int(lines.pop(0))
  time = float(lines.pop(0).split("Time=").pop())
  particles = [ parseParticle(lines.pop(0)) for _ in range(nextLines)]
  return Step(time, particles)

def parseParticle(line):
  properties = line.split(" ")
  particle = Particle(*properties)
  return particle