import numpy
import math
# Calculate euclidean distance between two points
def calculateDistance(a, b):
  a = numpy.array(a)
  b = numpy.array(b)
  return numpy.linalg.norm(a-b)

# Takes a list and returns a new list with the first list's elements squared
def squareList(lst):
  return [x ** 2 for x in lst]

# Takes a list and returns a list of the differences between the elements
# e.g. [1,2,4,5] = [1,2,1]
def calculateDeltas(lst):
  return [ abs(element) for element in numpy.diff(lst)]

# Takes multiple lists, and returns a single list where each element is the average of the elements
# of the passed list in a specific index
def averageLists(lists):
  return numpy.nanmean(lists, axis=0 )

# Takes multiple lists, and returns a single list where each element is the std_dev of the elements
# of the passed list in a specific index
def stdevLists(lists):
  return numpy.nanstd(lists, axis=0 )

# Returns the average of a list
def average(lst):
  return numpy.mean(lst)

# Like range, but with decimal steps
def discreteRange(*args):
  return numpy.arange(*args)

# Gets a probability histogram from a list
def PDF(lst, maxValue):
  return numpy.histogram(lst, bins=10, density=True) if maxValue == None else numpy.histogram(lst, bins=10, range=(0, maxValue), density=True)

# Returns a linear regresion (slope) from a list of points
def linearRegression(data):
  m,b = numpy.polyfit(range(len(data)), data, 1)
  return m,b


def linearFn(x,c):
  return c*x

def mb(x):
  return mb_pdf(x, 10.9285)

def mb_pdf(x,c):
  return math.sqrt(2 / math.pi) * (( (x ** 2) * math.exp(- (x ** 2) / (2.0 * (c ** 2))) ) / (c ** 3))

def beverloo(x,c):
  gravity = 9.8
  hole_size = 1.5
  mean_radius = 0.025 # average of particles radius
  n = 886 / (1 * 0.1) # particles per unit of area
  return n * math.sqrt(gravity) * ((hole_size - c * mean_radius) ** 1.5)

def errorCalculator(xs, ys, approxFn, c):
  results = []
  for i in range(len(ys)):
    results.append((ys[i] - approxFn(xs[i], c)) ** 2)
  return sum(results)

def errorFn(xs, ys, approxFn = beverloo):
  results = []
  rang = discreteRange(10,12, 0.0001)
  for c in rang:
    results.append(errorCalculator(xs, ys, approxFn, c))
  print(f'min: {rang[results.index(min(results))]}\n')
  return results, rang
