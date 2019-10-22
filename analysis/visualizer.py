import numpy as np
import matplotlib.pyplot as plt
from analyzer import calculateCollisionFrequency, calculateCollisionTimesAverage, calculateProbabilityCollisionTimesDistribution, calculateProbabilityVelocities, calculateDiffusion, calculateKineticEnergy, calculateExitsValues
from parser import parseDirectoryFromArgs, parseModeFromArgs, parseTimesFile, parseDirectory
from calculator import errorFn, discreteRange, PDF, mb, averageLists, stdevLists
import os
import pickle

OUTPUT_FOLDER = 'output'

def saveFig(fig, name):
  if not os.path.exists(OUTPUT_FOLDER):
    os.makedirs(OUTPUT_FOLDER)
  fig.savefig(f'{OUTPUT_FOLDER}/{name}.png')
  
def ex3_1(simulations):
  for simulation in simulations:
    print(f'Simulacion: {simulation.name}')
    print(f'Frecuencia de colisiones (#/s):  {calculateCollisionFrequency(simulation)}')
    print(f'Promedio de tiempos de colision:  {calculateCollisionTimesAverage(simulation)}')
    times,edges = calculateProbabilityCollisionTimesDistribution(simulation)

    fig, ax = plt.subplots()
    ax.hist(times, bins=20, weights=np.ones_like(times) / len(times)) 
    ax.set_xlabel('Tiempos de colisión (s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)})') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_1')

def ex3_2(simulations):
  for simulation in simulations:
    print(f'Simulacion: {simulation.name}')
    speeds, listOfSpeedsTime0 = calculateProbabilityVelocities(simulation)

    # grafica el ultimo tercio
    fig, ax = plt.subplots()
    ax.hist(speeds, weights=np.ones_like(speeds) / len(speeds), bins=20) 
    ax.set_xlabel('Modulo de las velocidades (m/s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)}) - Ultimo tercio de tiempo') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_2')
    
    # grafica en t=0
    fig, ax = plt.subplots()
    ax.hist(listOfSpeedsTime0, weights=np.ones_like(listOfSpeedsTime0) / len(listOfSpeedsTime0), bins=20) 
    ax.set_xlabel('Modulo de las velocidades (m/s)')
    ax.set_ylabel('Distribución de probabilidad')
    ax.set_title(f'Movimiento Browniano (N={len(simulation.steps[0].particles)}) - t=0') 
    fig.tight_layout()

    saveFig(fig, f'{simulation.name}--3_2--initial')

def ex3_4(simulations):
  diffusionSlope, diffusionB, averageSquaredDistances, deviations = calculateDiffusion(simulations)
  print(f'Coeficiente de difusion aproximado: {diffusionSlope}')

  fig, ax = plt.subplots()
  x_axis = [ x + len(averageSquaredDistances) for x in range(len(averageSquaredDistances)) ]
  markers, caps, bars = ax.errorbar(x_axis, averageSquaredDistances, yerr=deviations, capsize=5, capthick=2, fmt="o", zorder=1, markersize=2) 
  ax.set_xlabel('Step')
  ax.set_ylabel('DCM = <z^2>')
  ax.set_title(f'Movimiento Browniano (N={len(simulations[0].steps[0].particles)}) - Ultima mitad del tiempo') 
  fig.tight_layout()
  
  # loop through bars and caps and set the alpha value
  [bar.set_alpha(0.5) for bar in bars]
  # [cap.set_alpha(0.5) for cap in caps]


  # Create linear regresion
  x = np.linspace(min(x_axis),max(x_axis),1000)
  y = diffusionSlope*(x - len(averageSquaredDistances))+diffusionB
  ax.plot(x,y, '--', zorder=2,linewidth=2)
  ax.legend(loc='upper left')

  saveFig(fig, '3_4')

def error(simulations):
  diffusionSlope, diffusionB, averageSquaredDistances, deviations = calculateDiffusion(simulations)
  print(f'Coeficiente de difusion aproximado: {diffusionSlope}')
  
  fig, ax = plt.subplots()
  y_axis, x_axis = errorFn(range(len(averageSquaredDistances)),averageSquaredDistances)
  ax.plot([x * 10 ** 5 for x in x_axis], y_axis) 
  ax.set_xlabel('C (10^-5)')
  ax.set_ylabel('Error')
  ax.set_title(f'Error del ajuste por función lineal') 
  fig.tight_layout()
  saveFig(fig, 'error')


def ex2_2(simulations):
  print(f'simulations: {len(simulations)}')
  for simulation in simulations:
    print(f'steps #: {len(simulation.steps)}')
    print("Starting to calculate percentages\n")

    percentages = [step.firstChamberPercentage() for step in simulation.steps]
    print(percentages[:10])


    print("Starting to calculate indexes\n")
    xs = discreteRange(0,len(simulation.steps)*0.1, 0.1)
    print("Plotting\n")
    fig, ax = plt.subplots()
    ax.plot(xs, percentages, '.',markersize=2)
    ax.plot(xs, [0.5] * len(xs), '--')
    ax.set_xlabel('Tiempo (s)')
    ax.set_ylabel('Fracción en el recinto izquierdo')
    fig.tight_layout()

    plt.show()
    saveFig(fig, '2_2')

def ex2_4(simulations):
  print(f'simulations: {len(simulations)}')
  for simulation in simulations:
    velocities = [step.speeds() for step in simulation.getSecondHalf()]
    velocities = [item for sublist in velocities for item in sublist] #flatten
    bin_size = 200
    print("Plotting\n")
    fig, ax = plt.subplots()
    values = np.histogram(velocities, density=True, bins=bin_size)
    bin_centres = (values[1][:-1] + values[1][1:])/2.
    bin_centres_x = (values[0][:-1] + values[0][1:])/2.
    ax.plot(bin_centres, values[0], 'o', markersize=2)

    adjustment = [mb(x) for x in bin_centres]
    ax.plot(bin_centres, adjustment, '-')

    ax.set_xlabel('Velocidad (m/s)')
    ax.set_ylabel('PDF')
    fig.tight_layout()

    saveFig(fig, '2_4')

    # plot error
    results, rang = errorFn(bin_centres, values[0])
    fig, ax = plt.subplots()
    ax.plot(rang, results, 'o', markersize=2) 
    ax.set_ylabel('Error')
    ax.set_xlabel('Parámetro Libre (a)')
    fig.tight_layout()

    saveFig(fig, '2_4_error')

def tp5_e1a():
  timesList = parseDirectory('analysis/exits', parseTimesFile)
  totalTime = 5 # seconds
  windowSize = 1 # 1s
  offset = 0.1 # 100ms

  exitsList = []
  for times in timesList:
    print(f'Amount of particles gone: {len(times)}')
    print(f'Last time: {times[-1]}')

    exits = []
    for iteration in range(int(totalTime / windowSize) * int(windowSize / offset) - 10): # TODO: Ese 8 esta hardcodeado para que no se vaya del indice
      currentIdx = next(idx for idx, time in enumerate(times) if time >= offset * iteration )
      initialTime = times[currentIdx]
      accumulated = 0
      while (times[currentIdx] < initialTime + windowSize):
        accumulated += 1
        currentIdx += 1
      exits.append(accumulated)
    exitsList.append(exits)

  # average last third of exits
  avg, err = calculateExitsValues(exitsList)
  print(f'Caudal promedio: {avg}')
  print(f'Error: {err}')
  fig, ax = plt.subplots()
  ax.set_ylabel('Caudal [p/s]')
  ax.set_xlabel('Tiempo [s]')
  markers, caps, bars = ax.errorbar([x * offset for x in range(len(avg))], avg, yerr=err,capsize=5, capthick=2, fmt='o-', markersize=4)
  [bar.set_alpha(0.5) for bar in bars]
  fig.tight_layout()
  plt.show()
  saveFig(fig, '1_1a')

def tp5_e1b(simulations):
    kineticEnergies = [calculateKineticEnergy(simulation) for simulation in simulations]
    kineticEnergy = averageLists(kineticEnergies)
    kineticErrs = stdevLists(kineticEnergies)
    dt = 0.005 # seconds
    fig, ax = plt.subplots()
    # ax.set_yscale('log')
    ax.set_ylabel('Energía cinética [J]')
    ax.set_xlabel('Tiempo [s]')
    fig.tight_layout()
    ax.plot([x * dt for x in range(len(kineticEnergy))], kineticEnergy, 'o-', markersize=3)
    saveFig(fig, '1_1b')

def tp5_e1c(simulations):
  for simulation in simulations:
    kineticEnergy = calculateKineticEnergy(simulation)
    dt = 0.005 # seconds
    fig, ax = plt.subplots()
    # ax.set_yscale('log')
    ax.set_ylabel('Energía cinética [J]')
    ax.set_xlabel('Tiempo [s]')
    fig.tight_layout()
    ax.plot([x * dt for x in range(len(kineticEnergy))], kineticEnergy, 'o-', markersize=4)
    saveFig(fig, '1_1b')

def bev():
  xs = [0.15, 0.17, 0.22, 0.25] # Acá van los valores usados de D 
  ys = [1, 2, 3, 4] # Acá van los valores de caudales para cada D
  err = [0.1, 0.1, 0.1, 0.1] #Acá van los errores del caudal para cada D
  results, rang = errorFn(xs, ys)
  fig, ax = plt.subplots()
  ax.set_ylabel('Caudal promedio [p/s]')
  ax.set_xlabel('Ancho de apertura [m]')
  fig.tight_layout()
  ax.plot(xs, ys, 'o-', markersize=4)
  ax.plot(rang, results, 'o', markersize=2) 
  saveFig(fig, '1_1bev')



def run():
  print("python analysis/visualizer.py . 6")
  print("python analysis/visualizer.py analysis/results 7")
  print("python analysis/visualizer.py analysis/results 8")
  print("Las imágenes se guardan en la carpeta output de la raiz del proyecto.")
  print("Parse simulations\n")
  # if os.path.exists('22a.tmp'):
  #   print("File exists!\n")
  #   file = open('22a.tmp', 'rb')
  #   simulations = pickle.load(file)
  #   file.close()
  # else:
    # file = open('22a.tmp', 'wb')
    # print("Saving file\n")
    # pickle.dump(simulations, file)
    # file.close()
  print("Parse mode\n")
  mode = parseModeFromArgs()
  if (mode != 6):
    simulations = parseDirectoryFromArgs()
  if mode == 1:
    ex3_4(simulations)
  elif mode == 2:
    ex3_1(simulations)
    ex3_2(simulations)
  elif mode == 3:
    error(simulations)
  elif mode == 4:
    ex2_2(simulations)
  elif mode == 5:
    ex2_4(simulations)
  elif mode == 6:
    tp5_e1a()
  elif mode == 7:
    tp5_e1b(simulations)
  elif mode == 8:
    bev()

run()